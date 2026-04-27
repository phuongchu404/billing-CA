package com.rs.subscription.service.impl;

import com.rs.subscription.service.*;

import com.rs.subscription.dto.request.UpsertGroupRequest;
import com.rs.subscription.dto.response.GroupDetailResponse;
import com.rs.subscription.dto.response.GroupListItemResponse;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Auditable(entityType = "GROUP")
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
    // LIST ALL — lọc theo scope của user hiện tại
    // ----------------------------------------------------------------
    public List<GroupListItemResponse> listAll() {
        List<Long> visibleIds = dataScopeService.resolveVisibleGroupIds();
        List<Group> groups;
        if (visibleIds == null) {
            groups = groupRepository.findAll();
        } else if (visibleIds.isEmpty()) {
            return List.of();
        } else {
            groups = groupRepository.findAllById(visibleIds);
        }
        return groups.stream()
                .sorted(java.util.Comparator.comparingLong(Group::getGroupId))
                .map(this::toListItem)
                .toList();
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
        if (request.getOwnerUserId() != null && !request.getOwnerUserId().isBlank()) {
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
            if (request.getOwnerUserId().isBlank()) {
                group.setOwner(null);
            } else {
                UserAccount owner = userAccountRepository.findById(request.getOwnerUserId())
                    .orElseThrow(() -> new SmsException(ErrorCodes.VALIDATION_FAILED,
                        "Owner user not found: " + request.getOwnerUserId(), 400));
                group.setOwner(owner);
            }
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
    public GroupDetailResponse assignOwner(Long groupId, String ownerUserId) {
        Group group = findEntity(groupId);
        if (ownerUserId == null || ownerUserId.isBlank()) {
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
        if ("INACTIVE".equals(group.getStatus())) {
            throw new SmsException(ErrorCodes.VALIDATION_FAILED, "Group is already inactive", 400);
        }
        group.setStatus("INACTIVE");
        groupRepository.save(group);
    }

    @Transactional
    public void activate(Long id) {
        assertCanAccess(id);
        Group group = findEntity(id);
        if ("ACTIVE".equals(group.getStatus())) {
            throw new SmsException(ErrorCodes.VALIDATION_FAILED, "Group is already active", 400);
        }
        group.setStatus("ACTIVE");
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

    private GroupListItemResponse toListItem(Group group) {
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

        Optional<GroupPlanAssignment> activeOpt = assignmentRepository
            .findFirstByGroupGroupIdAndAssignmentStatusOrderByActivatedAtDesc(group.getGroupId(), "ACTIVE");

        if (activeOpt.isPresent()) {
            GroupPlanAssignment active = activeOpt.get();
            res.setCurrentPlan(active.getPlanTemplate().getPlanName());
            res.setApplyUntil(active.getApplyTo());

            List<Long> ids = List.of(active.getGroupPlanAssignmentId());
            Object[] usage = usageAggregateRepository.sumUsageByAssignmentIds(ids);
            if (usage != null && usage.length >= 2) {
                int cts = toInt(usage[0]);
                int signing = toInt(usage[1]);
                res.setCtsCreated(cts);
                res.setSigningUsed(signing);
                Integer certQuota = getCertQuota(active);
                res.setCtsCreatedPct(certQuota != null && certQuota > 0
                    ? (cts * 100 / certQuota) + "%" : "Không giới hạn");
                Integer signQuota = getSigningQuota(active);
                res.setSigningUsedPct(signQuota != null && signQuota > 0
                    ? (signing * 100 / signQuota) + "%" : "Không giới hạn");
            }
        }
        return res;
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
                && "SIGNING_COUNT".equals(r.getPricingMetric())
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
