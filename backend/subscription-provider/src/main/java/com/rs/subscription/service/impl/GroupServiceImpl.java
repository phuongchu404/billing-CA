package com.rs.subscription.service.impl;

import com.rs.subscription.service.*;

import com.rs.subscription.dto.request.UpsertGroupRequest;
import com.rs.subscription.dto.response.GroupDetailResponse;
import com.rs.subscription.dto.response.GroupListItemResponse;
import com.rs.subscription.dto.response.GroupListResponse;
import com.rs.subscription.dto.response.PlanHistoryResponse;
import com.rs.subscription.entity.Group;
import com.rs.subscription.entity.GroupContact;
import com.rs.subscription.entity.GroupPlanAssignment;
import com.rs.subscription.entity.PlanPricingRule;
import com.rs.subscription.entity.UserAccount;
import com.rs.subscription.enums.CommercialEnums;
import com.rs.subscription.exception.ErrorCodes;
import com.rs.subscription.exception.SmsException;
import com.rs.subscription.repository.GroupContactRepository;
import com.rs.subscription.repository.GroupPlanAssignmentRepository;
import com.rs.subscription.repository.GroupRepository;
import com.rs.subscription.repository.UserAccountRepository;
import com.rs.subscription.repository.UsageAggregateRepository;
import com.rs.subscription.aop.Auditable;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Auditable(entityType = CommercialEnums.ENTITY_GROUP)
@Service
@RequiredArgsConstructor
public class GroupServiceImpl implements GroupService {

    @PersistenceContext
    private EntityManager em;

    private final GroupRepository groupRepository;
    private final GroupContactRepository groupContactRepository;
    private final GroupPlanAssignmentRepository assignmentRepository;
    private final UsageAggregateRepository usageAggregateRepository;
    private final UserAccountRepository userAccountRepository;
    private final DataScopeService dataScopeService;

    // Subquery: one ACTIVE assignment per group (latest by activated_at)
    private static final String GROUP_BASE_FROM = """
            FROM `groups` g
            LEFT JOIN user_accounts u ON u.user_id = g.owner_user_id
            LEFT JOIN (
                SELECT group_id, group_plan_assignment_id, plan_template_id, apply_to, activated_at,
                       ROW_NUMBER() OVER (PARTITION BY group_id ORDER BY COALESCE(activated_at, '1970-01-01 00:00:00') DESC) AS rn
                FROM group_plan_assignments
                WHERE assignment_status = 'ACTIVE'
            ) gpa ON gpa.group_id = g.group_id AND gpa.rn = 1
            LEFT JOIN plan_templates pt ON pt.plan_template_id = gpa.plan_template_id
            LEFT JOIN (
                SELECT plan_template_id,
                       SUM(CASE WHEN is_active = 1 AND quota_total IS NOT NULL THEN quota_total ELSE 0 END) AS cert_quota,
                       SUM(CASE WHEN is_active = 1 AND pricing_metric = 'SIGNING_COUNT' AND range_max IS NOT NULL THEN range_max ELSE 0 END) AS sign_quota
                FROM plan_pricing_rules
                GROUP BY plan_template_id
            ) ppr ON ppr.plan_template_id = gpa.plan_template_id
            """;

    // ----------------------------------------------------------------
    // LIST ALL — DB-level filter + sort + paginate (handles 100k+ groups)
    // ----------------------------------------------------------------
    public GroupListResponse listAll(String status, String applyUntil, String updatedAt,
                                     int page, int size, String sortBy, String sortDir) {
        List<Long> visibleIds = dataScopeService.resolveVisibleGroupIds();
        if (visibleIds != null && visibleIds.isEmpty()) {
            return new GroupListResponse(List.of(), 0L, 0L, 0, page, size);
        }

        // Scope condition built from system Long IDs — no SQL injection risk
        String scopeCond = visibleIds == null ? "" :
                "AND g.group_id IN (" +
                visibleIds.stream().map(String::valueOf).collect(Collectors.joining(",")) +
                ")\n";

        // User-supplied filter params (use named params to prevent injection)
        StringBuilder filterCond = new StringBuilder();
        Map<String, Object> params = new LinkedHashMap<>();
        if (status != null && !status.isBlank()) {
            filterCond.append("AND g.status = :status\n");
            params.put("status", status);
        }
        if (applyUntil != null && !applyUntil.isBlank()) {
            filterCond.append("AND gpa.apply_to = :applyUntil\n");
            params.put("applyUntil", LocalDate.parse(applyUntil));
        }
        if (updatedAt != null && !updatedAt.isBlank()) {
            LocalDate d = LocalDate.parse(updatedAt);
            filterCond.append("AND g.updated_at >= :updatedAtStart AND g.updated_at < :updatedAtEnd\n");
            params.put("updatedAtStart", d.atStartOfDay());
            params.put("updatedAtEnd", d.plusDays(1).atStartOfDay());
        }

        String whereAll = "WHERE 1=1\n" + scopeCond + filterCond;
        String orderBy  = buildGroupOrderBy(sortBy, sortDir);

        String dataSql = "SELECT g.group_id, g.group_code, g.group_name, g.status, g.updated_at, "
                + "u.full_name, u.user_id, pt.plan_name, gpa.apply_to, gpa.group_plan_assignment_id, "
                + "COALESCE(ppr.cert_quota, 0), COALESCE(ppr.sign_quota, 0) "
                + GROUP_BASE_FROM + whereAll + orderBy;
        String countSql       = "SELECT COUNT(*) " + GROUP_BASE_FROM + whereAll;
        String activeCountSql = "SELECT COUNT(*) FROM `groups` g\nWHERE g.status = 'ACTIVE'\n" + scopeCond;

        jakarta.persistence.Query dataQuery    = em.createNativeQuery(dataSql);
        jakarta.persistence.Query countQuery   = em.createNativeQuery(countSql);
        jakarta.persistence.Query activeCountQ = em.createNativeQuery(activeCountSql);
        params.forEach((k, v) -> { dataQuery.setParameter(k, v); countQuery.setParameter(k, v); });
        dataQuery.setFirstResult(page * size);
        dataQuery.setMaxResults(size);

        @SuppressWarnings("unchecked")
        List<Object[]> rows    = dataQuery.getResultList();
        long totalElements     = ((Number) countQuery.getSingleResult()).longValue();
        long activeCount       = ((Number) activeCountQ.getSingleResult()).longValue();
        int  totalPages        = size > 0 ? (int) Math.ceil((double) totalElements / size) : 0;

        // Batch load usage for this page only (≤ size assignment IDs — constant cost)
        List<Long> assignmentIds = rows.stream()
                .filter(r -> r[9] != null)
                .map(r -> ((Number) r[9]).longValue())
                .distinct().toList();
        Map<Long, long[]> usageByAssignment = new HashMap<>();
        if (!assignmentIds.isEmpty()) {
            usageAggregateRepository.sumUsagePerAssignment(assignmentIds).forEach(row -> {
                long aId     = ((Number) row[0]).longValue();
                long cts     = ((Number) row[1]).longValue();
                long signing = ((Number) row[2]).longValue();
                usageByAssignment.put(aId, new long[]{cts, signing});
            });
        }

        List<GroupListItemResponse> items = rows.stream()
                .map(r -> fromRow(r, usageByAssignment))
                .toList();

        return new GroupListResponse(items, activeCount, totalElements, totalPages, page, size);
    }

    private static String buildGroupOrderBy(String sortBy, String sortDir) {
        String dir = "desc".equalsIgnoreCase(sortDir) ? "DESC" : "ASC";
        return switch (sortBy == null ? "" : sortBy) {
            case "groupCode"   -> "ORDER BY g.group_code "  + dir + "\n";
            case "groupName"   -> "ORDER BY g.group_name "  + dir + "\n";
            case "status"      -> "ORDER BY g.status "      + dir + "\n";
            case "ownerName"   -> "ORDER BY CASE WHEN u.full_name IS NULL THEN 1 ELSE 0 END ASC, u.full_name " + dir + "\n";
            case "currentPlan" -> "ORDER BY CASE WHEN pt.plan_name IS NULL THEN 1 ELSE 0 END ASC, pt.plan_name " + dir + "\n";
            case "applyUntil"  -> "ORDER BY CASE WHEN gpa.apply_to IS NULL THEN 1 ELSE 0 END ASC, gpa.apply_to " + dir + "\n";
            default            -> "ORDER BY g.updated_at DESC\n";
        };
    }

    private GroupListItemResponse fromRow(Object[] r, Map<Long, long[]> usageByAssignment) {
        GroupListItemResponse res = new GroupListItemResponse();
        res.setGroupId(toLong(r[0]));
        res.setGroupCode((String) r[1]);
        res.setGroupName((String) r[2]);
        res.setStatus((String) r[3]);
        if (r[4] != null) {
            res.setUpdatedAt(r[4] instanceof java.sql.Timestamp ts
                    ? ts.toLocalDateTime() : (LocalDateTime) r[4]);
        }
        res.setOwnerName((String) r[5]);
        if (r[6] != null) res.setOwnerUserId(toLong(r[6]));
        res.setCurrentPlan((String) r[7]);
        if (r[8] != null) {
            res.setApplyUntil(r[8] instanceof java.sql.Date d
                    ? d.toLocalDate() : (LocalDate) r[8]);
        }
        if (r[9] != null) {
            long assignmentId = toLong(r[9]);
            long[] usage  = usageByAssignment.getOrDefault(assignmentId, new long[]{0, 0});
            int cts     = (int) usage[0];
            int signing = (int) usage[1];
            res.setCtsCreated(cts);
            res.setSigningUsed(signing);
            long certQuota = toLong(r[10]);
            long signQuota = toLong(r[11]);
            res.setCtsCreatedPct(certQuota > 0 ? (cts * 100 / certQuota) + "%" : "—");
            res.setSigningUsedPct(signQuota > 0 ? (signing * 100 / signQuota) + "%" : "—");
        }
        return res;
    }

    // ----------------------------------------------------------------
    // GET BY ID
    // ----------------------------------------------------------------
    public GroupDetailResponse getById(Long id) {
        Group group = findEntity(id);
        assertCanAccess(id);
        return toDetail(group);
    }

    // ----------------------------------------------------------------
    // CREATE
    // ----------------------------------------------------------------
    @Transactional
    public GroupDetailResponse create(UpsertGroupRequest request) {
        assertEmailsNotDuplicate(request.getPicEmails(), request.getContactEmails(), null);

        String groupCode = generateGroupCode();

        UserAccount owner = null;
        if (request.getOwnerUserId() != null) {
            owner = userAccountRepository.findById(request.getOwnerUserId())
                .orElseThrow(() -> new SmsException(ErrorCodes.VALIDATION_FAILED,
                    "Owner user not found: " + request.getOwnerUserId(), 400));
        }

        Group group = Group.builder()
            .groupCode(groupCode)
            .groupName(request.getGroupName())
            .username("grp_" + groupCode.toLowerCase())
            .password("pending_reset")
            .refContractNo(request.getRefContractNo())
            .createdBy(request.getCreatedBy() != null ? request.getCreatedBy() : "system")
            .owner(owner)
            .status(CommercialEnums.GroupStatus.ACTIVE.name())
            .build();
        Group saved = groupRepository.save(group);
        saveContacts(saved, request);
        return toDetail(findEntity(saved.getGroupId()));
    }

    // ----------------------------------------------------------------
    // UPDATE
    // ----------------------------------------------------------------
    @Transactional
    public GroupDetailResponse update(Long id, UpsertGroupRequest request) {
        assertCanAccess(id);
        assertEmailsNotDuplicate(request.getPicEmails(), request.getContactEmails(), id);

        Group group = findEntity(id);
        group.setGroupName(request.getGroupName());
        group.setRefContractNo(request.getRefContractNo());

        if (request.getOwnerUserId() != null) {
            UserAccount owner = userAccountRepository.findById(request.getOwnerUserId())
                .orElseThrow(() -> new SmsException(ErrorCodes.VALIDATION_FAILED,
                    "Owner user not found: " + request.getOwnerUserId(), 400));
            group.setOwner(owner);
        }

        groupRepository.save(group);
        groupContactRepository.deleteByGroupGroupId(id);
        saveContacts(group, request);
        return toDetail(findEntity(id));
    }

    private void assertEmailsNotDuplicate(List<String> picEmails, List<String> contactEmails, Long excludeGroupId) {
        List<String> all = new ArrayList<>();
        if (picEmails != null)     picEmails.stream().filter(e -> e != null && !e.isBlank()).forEach(all::add);
        if (contactEmails != null) contactEmails.stream().filter(e -> e != null && !e.isBlank()).forEach(all::add);
        if (all.isEmpty()) return;

        List<String> duplicates = excludeGroupId != null
            ? groupContactRepository.findExistingEmailsExcludingGroup(all, excludeGroupId)
            : groupContactRepository.findExistingEmails(all);

        if (!duplicates.isEmpty()) {
            throw new SmsException(ErrorCodes.VALIDATION_FAILED,
                "Email đã được sử dụng bởi đại lý khác: " + String.join(", ", duplicates), 400);
        }
    }

    // ----------------------------------------------------------------
    // ASSIGN OWNER (admin gán nhân viên phụ trách)
    // ----------------------------------------------------------------
    @Transactional
    public GroupDetailResponse assignOwner(Long groupId, Long ownerUserId) {
        Group group = findEntity(groupId);
        if (ownerUserId == null) {
            group.setOwner(null);
        } else {
            UserAccount owner = userAccountRepository.findById(ownerUserId)
                .orElseThrow(() -> new SmsException(ErrorCodes.VALIDATION_FAILED,
                    "Owner user not found: " + ownerUserId, 400));
            group.setOwner(owner);
        }
        groupRepository.save(group);
        return toDetail(findEntity(groupId));
    }

    // ----------------------------------------------------------------
    // SUSPEND / ACTIVATE
    // ----------------------------------------------------------------
    @Transactional
    public void suspend(Long id) {
        assertCanAccess(id);
        Group group = findEntity(id);
        if (CommercialEnums.GroupStatus.INACTIVE.name().equals(group.getStatus())) {
            throw new SmsException(ErrorCodes.VALIDATION_FAILED, "Group is already inactive", 400);
        }
        group.setStatus(CommercialEnums.GroupStatus.INACTIVE.name());
        groupRepository.save(group);
    }

    @Transactional
    public void activate(Long id) {
        assertCanAccess(id);
        Group group = findEntity(id);
        if (CommercialEnums.GroupStatus.ACTIVE.name().equals(group.getStatus())) {
            throw new SmsException(ErrorCodes.VALIDATION_FAILED, "Group is already active", 400);
        }
        group.setStatus(CommercialEnums.GroupStatus.ACTIVE.name());
        groupRepository.save(group);
    }

    // ----------------------------------------------------------------
    // PLAN HISTORY
    // ----------------------------------------------------------------
    @Transactional(readOnly = true)
    public List<PlanHistoryResponse> getPlanHistory(Long groupId) {
        findEntity(groupId);
        assertCanAccess(groupId);
        return assignmentRepository.findHistoryByGroupId(groupId)
            .stream().map(this::toPlanHistory).toList();
    }

    // ----------------------------------------------------------------
    // Helpers
    // ----------------------------------------------------------------
    public Group findEntity(Long id) {
        return groupRepository.findById(id)
            .orElseThrow(() -> new SmsException(ErrorCodes.GROUP_NOT_FOUND, "Group not found: " + id, 404));
    }

    private void assertCanAccess(Long groupId) {
        if (!dataScopeService.canAccessGroup(groupId)) {
            throw new SmsException(ErrorCodes.GROUP_ACCESS_DENIED,
                "Access denied to group: " + groupId, 403);
        }
    }

    private static final String CODE_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int CODE_LENGTH = 8;
    private static final int CODE_RETRY_LIMIT = 5;
    private static final java.security.SecureRandom SECURE_RANDOM = new java.security.SecureRandom();

    private String generateGroupCode() {
        for (int attempt = 0; attempt < CODE_RETRY_LIMIT; attempt++) {
            StringBuilder sb = new StringBuilder(CODE_LENGTH);
            for (int i = 0; i < CODE_LENGTH; i++) {
                sb.append(CODE_CHARS.charAt(SECURE_RANDOM.nextInt(CODE_CHARS.length())));
            }
            String code = "GRP-" + sb;
            if (!groupRepository.existsByGroupCode(code)) {
                return code;
            }
        }
        throw new SmsException(ErrorCodes.VALIDATION_FAILED, "Unable to generate unique group code, please retry", 500);
    }

    private void saveContacts(Group group, UpsertGroupRequest request) {
        List<GroupContact> contacts = new ArrayList<>();
        if (request.getPicEmails() != null) {
            for (String email : request.getPicEmails()) {
                if (email != null && !email.isBlank()) {
                    contacts.add(GroupContact.builder()
                        .group(group).contactType("PIC").email(email.trim())
                        .isPrimary(contacts.stream().noneMatch(c -> "PIC".equals(c.getContactType())))
                        .receiveUsageAlert(true).isActive(true).build());
                }
            }
        }
        if (request.getContactEmails() != null) {
            for (String email : request.getContactEmails()) {
                if (email != null && !email.isBlank()) {
                    contacts.add(GroupContact.builder()
                        .group(group).contactType("CONTRACT").email(email.trim())
                        .isPrimary(contacts.stream().noneMatch(c -> "CONTRACT".equals(c.getContactType())))
                        .receiveUsageAlert(true).isActive(true).build());
                }
            }
        }
        groupContactRepository.saveAll(contacts);
    }

    private GroupDetailResponse toDetail(Group group) {
        GroupDetailResponse res = new GroupDetailResponse();
        res.setGroupId(group.getGroupId());
        res.setGroupCode(group.getGroupCode());
        res.setGroupName(group.getGroupName());
        res.setStatus(group.getStatus());
        res.setRefContractNo(group.getRefContractNo());
        res.setCreatedBy(group.getCreatedBy());
        res.setCreatedAt(group.getCreatedAt());
        res.setUpdatedAt(group.getUpdatedAt());
        if (group.getOwner() != null) {
            res.setOwnerUserId(group.getOwner().getUserId());
            res.setOwnerName(group.getOwner().getFullName());
        }

        List<GroupContact> contacts = groupContactRepository
            .findByGroupGroupIdOrderByIsPrimaryDescGroupContactIdAsc(group.getGroupId());

        res.setPicEmails(contacts.stream()
            .filter(c -> "PIC".equals(c.getContactType()) && Boolean.TRUE.equals(c.getIsActive()))
            .map(GroupContact::getEmail).toList());

        res.setContactEmails(contacts.stream()
            .filter(c -> ("CONTRACT".equals(c.getContactType()) || "BILLING".equals(c.getContactType()))
                && Boolean.TRUE.equals(c.getIsActive()))
            .map(GroupContact::getEmail).toList());

        return res;
    }

    private PlanHistoryResponse toPlanHistory(GroupPlanAssignment a) {
        PlanHistoryResponse res = new PlanHistoryResponse();
        res.setApplyFrom(a.getApplyFrom());
        res.setApplyTo(a.getApplyTo());
        res.setPlanName(a.getPlanTemplate().getPlanName());
        res.setAssignmentStatus(a.getAssignmentStatus());

        List<Long> ids = List.of(a.getGroupPlanAssignmentId());
        Object[] usage = usageAggregateRepository.sumUsageByAssignmentIds(ids);
        int cts = 0, signing = 0;
        if (usage != null && usage.length >= 2) {
            cts = toInt(usage[0]);
            signing = toInt(usage[1]);
        }
        res.setCtsCreated(cts);
        res.setSigningUsed(signing);
        Integer certQuota = getCertQuota(a);
        res.setCtsCreatedPct(certQuota != null && certQuota > 0 ? (cts * 100 / certQuota) + "%" : "—");
        Integer signQuota = getSigningQuota(a);
        res.setSigningUsedPct(signQuota != null && signQuota > 0 ? (signing * 100 / signQuota) + "%" : "—");
        return res;
    }

    private Integer getCertQuota(GroupPlanAssignment a) {
        int total = a.getPlanTemplate().getPricingRules().stream()
            .filter(r -> Boolean.TRUE.equals(r.getIsActive()) && r.getQuotaTotal() != null)
            .mapToInt(PlanPricingRule::getQuotaTotal).sum();
        return total > 0 ? total : null;
    }

    private Integer getSigningQuota(GroupPlanAssignment a) {
        int total = a.getPlanTemplate().getPricingRules().stream()
            .filter(r -> Boolean.TRUE.equals(r.getIsActive())
                && CommercialEnums.PricingMetric.SIGNING_COUNT.name().equals(r.getPricingMetric())
                && r.getRangeMax() != null)
            .mapToInt(PlanPricingRule::getRangeMax).sum();
        return total > 0 ? total : null;
    }

    private int toInt(Object val) {
        if (val == null) return 0;
        if (val instanceof Number) return ((Number) val).intValue();
        return 0;
    }

    private long toLong(Object val) {
        if (val == null) return 0L;
        if (val instanceof Number) return ((Number) val).longValue();
        return 0L;
    }
}


