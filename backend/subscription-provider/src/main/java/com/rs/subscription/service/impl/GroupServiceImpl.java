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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Auditable(entityType = CommercialEnums.ENTITY_GROUP)
@Service
@RequiredArgsConstructor
public class GroupServiceImpl implements GroupService {

    private final GroupRepository groupRepository;
    private final GroupContactRepository groupContactRepository;
    private final GroupPlanAssignmentRepository assignmentRepository;
    private final UsageAggregateRepository usageAggregateRepository;
    private final UserAccountRepository userAccountRepository;
    private final DataScopeService dataScopeService;

    // ----------------------------------------------------------------
    // LIST ALL — lọc theo scope của user hiện tại, sau đó filter theo params
    // ----------------------------------------------------------------
    public GroupListResponse listAll(String status, String applyUntil, String updatedAt,
                                     int page, int size, String sortBy, String sortDir) {
        // Query 1: resolve visible group IDs
        List<Long> visibleIds = dataScopeService.resolveVisibleGroupIds();
        List<Group> groups;
        if (visibleIds == null) {
            groups = groupRepository.findAll();                    // Query 2a: admin — all groups
        } else if (visibleIds.isEmpty()) {
            return new GroupListResponse(List.of(), 0L, 0L, 0, page, size);
        } else {
            groups = groupRepository.findAllById(visibleIds);      // Query 2b: scoped groups
        }
        if (groups.isEmpty()) return new GroupListResponse(List.of(), 0L, 0L, 0, page, size);

        List<Long> groupIds = groups.stream().map(Group::getGroupId).toList();

        // Query 3: batch load ALL active assignments + PlanTemplate (JOIN FETCH, tránh N+1)
        List<GroupPlanAssignment> activeAssignments = assignmentRepository
                .findByGroupIdsAndStatusWithPlan(groupIds, CommercialEnums.AssignmentStatus.ACTIVE.name());

        // Index assignment by groupId (lấy 1 assignment mới nhất mỗi group)
        java.util.Map<Long, GroupPlanAssignment> assignmentByGroup = new java.util.LinkedHashMap<>();
        for (GroupPlanAssignment a : activeAssignments) {
            assignmentByGroup.merge(a.getGroup().getGroupId(), a, (existing, newer) ->
                    newer.getActivatedAt() != null && (existing.getActivatedAt() == null
                            || newer.getActivatedAt().isAfter(existing.getActivatedAt())) ? newer : existing);
        }

        // Query 4: batch load usage cho tất cả active assignments cùng lúc
        List<Long> assignmentIds = activeAssignments.stream()
                .map(GroupPlanAssignment::getGroupPlanAssignmentId).toList();
        java.util.Map<Long, long[]> usageByAssignment = new java.util.HashMap<>();
        if (!assignmentIds.isEmpty()) {
            usageAggregateRepository.sumUsagePerAssignment(assignmentIds).forEach(row -> {
                long assignmentId = ((Number) row[0]).longValue();
                long cts          = ((Number) row[1]).longValue();
                long signing      = ((Number) row[2]).longValue();
                usageByAssignment.put(assignmentId, new long[]{cts, signing});
            });
        }

        // Map to response items (no additional DB calls)
        List<GroupListItemResponse> allItems = groups.stream()
                .sorted(java.util.Comparator.comparingLong(Group::getGroupId))
                .map(g -> toListItemBatch(g, assignmentByGroup.get(g.getGroupId()), usageByAssignment))
                .toList();

        long activeCount = allItems.stream()
                .filter(i -> "ACTIVE".equals(i.getStatus()))
                .count();

        List<GroupListItemResponse> filtered = allItems.stream()
                .filter(item -> matchesGroupFilter(item, status, applyUntil, updatedAt))
                .sorted(buildComparator(sortBy, sortDir))
                .toList();

        long totalElements = filtered.size();
        int totalPages = size > 0 ? (int) Math.ceil((double) totalElements / size) : 1;
        List<GroupListItemResponse> paged = filtered.stream()
                .skip((long) page * size)
                .limit(size)
                .toList();

        return new GroupListResponse(paged, activeCount, totalElements, totalPages, page, size);
    }

    private GroupListItemResponse toListItemBatch(Group group, GroupPlanAssignment active,
                                                   java.util.Map<Long, long[]> usageByAssignment) {
        GroupListItemResponse res = new GroupListItemResponse();
        res.setGroupId(group.getGroupId());
        res.setGroupCode(group.getGroupCode());
        res.setGroupName(group.getGroupName());
        res.setStatus(group.getStatus());
        res.setUpdatedAt(group.getUpdatedAt());
        if (group.getOwner() != null) {
            res.setOwnerUserId(group.getOwner().getUserId());
            res.setOwnerName(group.getOwner().getFullName());
        }
        if (active != null) {
            res.setCurrentPlan(active.getPlanTemplate().getPlanName());
            res.setApplyUntil(active.getApplyTo());
            long[] usage = usageByAssignment.getOrDefault(active.getGroupPlanAssignmentId(), new long[]{0, 0});
            int cts     = (int) usage[0];
            int signing = (int) usage[1];
            res.setCtsCreated(cts);
            res.setSigningUsed(signing);
            Integer certQuota = getCertQuota(active);
            res.setCtsCreatedPct(certQuota != null && certQuota > 0
                    ? (cts * 100 / certQuota) + "%" : "Không giới hạn");
            Integer signQuota = getSigningQuota(active);
            res.setSigningUsedPct(signQuota != null && signQuota > 0
                    ? (signing * 100 / signQuota) + "%" : "Không giới hạn");
        }
        return res;
    }

    private boolean matchesGroupFilter(GroupListItemResponse item,
                                       String status, String applyUntil, String updatedAt) {
        if (status != null && !status.isBlank() && !status.equals(item.getStatus())) return false;
        if (applyUntil != null && !applyUntil.isBlank()) {
            if (item.getApplyUntil() == null) return false;
            if (!item.getApplyUntil().equals(LocalDate.parse(applyUntil))) return false;
        }
        if (updatedAt != null && !updatedAt.isBlank()) {
            if (item.getUpdatedAt() == null) return false;
            if (!item.getUpdatedAt().toLocalDate().equals(LocalDate.parse(updatedAt))) return false;
        }
        return true;
    }

    private java.util.Comparator<GroupListItemResponse> buildComparator(String sortBy, String sortDir) {
        boolean desc = "desc".equalsIgnoreCase(sortDir);
        java.util.Comparator<GroupListItemResponse> cmp = switch (sortBy != null ? sortBy : "updatedAt") {
            case "groupCode"  -> java.util.Comparator.comparing(GroupListItemResponse::getGroupCode,
                                     java.util.Comparator.nullsLast(java.util.Comparator.naturalOrder()));
            case "groupName"  -> java.util.Comparator.comparing(GroupListItemResponse::getGroupName,
                                     java.util.Comparator.nullsLast(java.util.Comparator.naturalOrder()));
            case "status"     -> java.util.Comparator.comparing(GroupListItemResponse::getStatus,
                                     java.util.Comparator.nullsLast(java.util.Comparator.naturalOrder()));
            case "ownerName"  -> java.util.Comparator.comparing(GroupListItemResponse::getOwnerName,
                                     java.util.Comparator.nullsLast(java.util.Comparator.naturalOrder()));
            case "currentPlan"-> java.util.Comparator.comparing(GroupListItemResponse::getCurrentPlan,
                                     java.util.Comparator.nullsLast(java.util.Comparator.naturalOrder()));
            case "applyUntil" -> java.util.Comparator.comparing(GroupListItemResponse::getApplyUntil,
                                     java.util.Comparator.nullsLast(java.util.Comparator.naturalOrder()));
            default           -> java.util.Comparator.comparing(GroupListItemResponse::getUpdatedAt,
                                     java.util.Comparator.nullsLast(java.util.Comparator.naturalOrder()));
        };
        return desc ? cmp.reversed() : cmp;
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
        res.setCtsCreatedPct(certQuota != null && certQuota > 0 ? (cts * 100 / certQuota) + "%" : "Không giới hạn");
        Integer signQuota = getSigningQuota(a);
        res.setSigningUsedPct(signQuota != null && signQuota > 0 ? (signing * 100 / signQuota) + "%" : "Không giới hạn");
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
}


