package com.rs.subscription.service;

import com.rs.subscription.dto.request.UpsertGroupRequest;
import com.rs.subscription.dto.response.GroupDetailResponse;
import com.rs.subscription.dto.response.GroupListItemResponse;
import com.rs.subscription.dto.response.PlanHistoryResponse;
import com.rs.subscription.entity.Group;
import com.rs.subscription.entity.GroupContact;
import com.rs.subscription.entity.GroupPlanAssignment;
import com.rs.subscription.entity.PlanPricingRule;
import com.rs.subscription.exception.ErrorCodes;
import com.rs.subscription.exception.SmsException;
import com.rs.subscription.repository.GroupContactRepository;
import com.rs.subscription.repository.GroupPlanAssignmentRepository;
import com.rs.subscription.repository.GroupRepository;
import com.rs.subscription.repository.UsageAggregateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GroupService {

    private final GroupRepository groupRepository;
    private final GroupContactRepository groupContactRepository;
    private final GroupPlanAssignmentRepository assignmentRepository;
    private final UsageAggregateRepository usageAggregateRepository;

    // ----------------------------------------------------------------
    // LIST ALL
    // ----------------------------------------------------------------
    public List<GroupListItemResponse> listAll() {
        return groupRepository.findAll().stream().map(this::toListItem).toList();
    }

    // ----------------------------------------------------------------
    // GET BY ID
    // ----------------------------------------------------------------
    public GroupDetailResponse getById(Long id) {
        return toDetail(findEntity(id));
    }

    // ----------------------------------------------------------------
    // CREATE
    // ----------------------------------------------------------------
    @Transactional
    public GroupDetailResponse create(UpsertGroupRequest request) {
        // Tạo group với code tự sinh
        String groupCode = generateGroupCode();
        Group group = Group.builder()
            .groupCode(groupCode)
            .groupName(request.getGroupName())
            .username("grp_" + groupCode.toLowerCase().replace("-", "_"))
            .password("pending_reset") // sẽ đặt lại qua reset password flow
            .refContractNo(request.getRefContractNo())
            .createdBy("system")
            .status("ACTIVE")
            .build();
        Group saved = groupRepository.save(group);
        saveContacts(saved, request);
        // Reload với contacts
        return toDetail(findEntity(saved.getGroupId()));
    }

    // ----------------------------------------------------------------
    // UPDATE
    // ----------------------------------------------------------------
    @Transactional
    public GroupDetailResponse update(Long id, UpsertGroupRequest request) {
        Group group = findEntity(id);
        group.setGroupName(request.getGroupName());
        group.setRefContractNo(request.getRefContractNo());
        groupRepository.save(group);
        // Replace contacts
        groupContactRepository.deleteByGroupGroupId(id);
        saveContacts(group, request);
        return toDetail(findEntity(id));
    }

    // ----------------------------------------------------------------
    // SUSPEND / ACTIVATE
    // ----------------------------------------------------------------
    @Transactional
    public void suspend(Long id) {
        Group group = findEntity(id);
        if ("INACTIVE".equals(group.getStatus())) {
            throw new SmsException(ErrorCodes.VALIDATION_FAILED, "Group is already inactive", 400);
        }
        group.setStatus("INACTIVE");
        groupRepository.save(group);
    }

    @Transactional
    public void activate(Long id) {
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
    public List<PlanHistoryResponse> getPlanHistory(Long groupId) {
        findEntity(groupId); // ensure group exists
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

    private String generateGroupCode() {
        long count = groupRepository.count() + 1;
        return String.format("GRP-%06d", count);
    }

    private GroupListItemResponse toListItem(Group group) {
        GroupListItemResponse res = new GroupListItemResponse();
        res.setGroupId(group.getGroupId());
        res.setGroupCode(group.getGroupCode());
        res.setGroupName(group.getGroupName());
        res.setStatus(group.getStatus());
        res.setUpdatedAt(group.getUpdatedAt());

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

    /** Tổng quota chứng thư từ pricing rules (quotaTotal field) */
    private Integer getCertQuota(GroupPlanAssignment a) {
        int total = a.getPlanTemplate().getPricingRules().stream()
            .filter(r -> Boolean.TRUE.equals(r.getIsActive()) && r.getQuotaTotal() != null)
            .mapToInt(PlanPricingRule::getQuotaTotal).sum();
        return total > 0 ? total : null;
    }

    /** Quota lượt ký: dùng rangeMax của rule SIGNING_COUNT */
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
