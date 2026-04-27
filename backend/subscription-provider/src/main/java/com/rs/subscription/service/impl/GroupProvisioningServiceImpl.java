package com.rs.subscription.service.impl;

import com.rs.subscription.service.*;

import com.rs.subscription.dto.request.AddGroupPlanRequest;
import com.rs.subscription.dto.request.CreateGroupPlanAssignmentRequest;
import com.rs.subscription.dto.request.CreatePlanTemplateRequest;
import com.rs.subscription.dto.request.ProvisionGroupRequest;
import com.rs.subscription.dto.request.UpsertGroupRequest;
import com.rs.subscription.dto.response.GroupDetailResponse;
import com.rs.subscription.dto.response.GroupPlanAssignmentResponse;
import com.rs.subscription.dto.response.PlanTemplateResponse;
import com.rs.subscription.dto.response.ProvisionGroupResponse;
import com.rs.subscription.entity.Group;
import com.rs.subscription.exception.ErrorCodes;
import com.rs.subscription.exception.SmsException;
import com.rs.subscription.repository.GroupRepository;
import com.rs.subscription.enums.CommercialEnums;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GroupProvisioningServiceImpl implements GroupProvisioningService {

    private final GroupService groupService;
    private final GroupRepository groupRepository;
    private final PlanTemplateService planTemplateService;
    private final GroupPlanAssignmentService groupPlanAssignmentService;

    @Transactional
    public ProvisionGroupResponse provision(ProvisionGroupRequest req) {
        // 1. Tạo group + lưu contacts (PIC, CONTRACT)
        UpsertGroupRequest groupReq = new UpsertGroupRequest();
        groupReq.setGroupName(req.getGroupName());
        groupReq.setPicEmails(req.getPicEmails());
        groupReq.setContactEmails(req.getContactEmails());
        groupReq.setRefContractNo(req.getRefContractNo());
        groupReq.setCreatedBy(req.getRequestedBy());
        GroupDetailResponse group = groupService.create(groupReq);

        // 2. Tạo plan template, planCode sinh server-side từ groupCode
        boolean hasDates = req.getEffectiveFrom() != null && req.getEffectiveTo() != null;
        String planCode = "PLN_" + group.getGroupCode() + "_" + System.currentTimeMillis();
        CreatePlanTemplateRequest planReq = new CreatePlanTemplateRequest();
        planReq.setPlanCode(planCode);
        planReq.setPlanName(req.getPlanName());
        planReq.setCustomerSegment(CommercialEnums.CustomerSegment.GROUP.name());
        planReq.setTemplateScope(CommercialEnums.TemplateScope.PARTNER_PRIVATE.name());
        planReq.setStatus(hasDates
                ? CommercialEnums.TemplateStatus.DRAFT.name()
                : CommercialEnums.TemplateStatus.AVAILABLE.name());
        planReq.setEffectiveFrom(req.getEffectiveFrom());
        planReq.setEffectiveTo(req.getEffectiveTo());
        planReq.setIsVisible(true);
        planReq.setAllowBulkSigning(false);
        planReq.setAllowApiAccess(false);
        planReq.setCreatedBy(req.getRequestedBy());
        planReq.setPricingRules(req.getPricingRules());
        PlanTemplateResponse planTemplate = planTemplateService.create(planReq);

        // 3. Gán gói cước cho đại lý
        CreateGroupPlanAssignmentRequest assignReq = new CreateGroupPlanAssignmentRequest();
        assignReq.setGroupId(group.getGroupId());
        assignReq.setPlanTemplateId(planTemplate.getPlanTemplateId());
        assignReq.setAssignmentStatus(CommercialEnums.AssignmentStatus.REQUESTED.name());
        assignReq.setRequestedBy(req.getRequestedBy());
        assignReq.setApplyFrom(req.getEffectiveFrom());
        assignReq.setApplyTo(req.getEffectiveTo());
        GroupPlanAssignmentResponse assignment = groupPlanAssignmentService.create(assignReq);

        ProvisionGroupResponse response = new ProvisionGroupResponse();
        response.setGroup(group);
        response.setPlanTemplate(planTemplate);
        response.setAssignment(assignment);
        return response;
    }

    @Transactional
    public GroupPlanAssignmentResponse addPlanToGroup(Long groupId, AddGroupPlanRequest req) {
        Group group = groupRepository.findById(groupId)
            .orElseThrow(() -> new SmsException(ErrorCodes.GROUP_NOT_FOUND, "Group not found: " + groupId, 404));

        boolean hasDates = req.getApplyFrom() != null && req.getApplyTo() != null;
        String planCode = "PLN_" + group.getGroupCode() + "_" + System.currentTimeMillis();

        CreatePlanTemplateRequest planReq = new CreatePlanTemplateRequest();
        planReq.setPlanCode(planCode);
        planReq.setPlanName(req.getPlanName());
        planReq.setCustomerSegment(CommercialEnums.CustomerSegment.GROUP.name());
        planReq.setTemplateScope(CommercialEnums.TemplateScope.PARTNER_PRIVATE.name());
        planReq.setStatus(hasDates
            ? CommercialEnums.TemplateStatus.DRAFT.name()
            : CommercialEnums.TemplateStatus.AVAILABLE.name());
        planReq.setEffectiveFrom(req.getApplyFrom());
        planReq.setEffectiveTo(req.getApplyTo());
        planReq.setIsVisible(true);
        planReq.setAllowBulkSigning(false);
        planReq.setAllowApiAccess(false);
        planReq.setCreatedBy(req.getRequestedBy());
        planReq.setPricingRules(req.getPricingRules());
        PlanTemplateResponse planTemplate = planTemplateService.create(planReq);

        CreateGroupPlanAssignmentRequest assignReq = new CreateGroupPlanAssignmentRequest();
        assignReq.setGroupId(groupId);
        assignReq.setPlanTemplateId(planTemplate.getPlanTemplateId());
        assignReq.setAssignmentStatus(CommercialEnums.AssignmentStatus.REQUESTED.name());
        assignReq.setRequestedBy(req.getRequestedBy());
        assignReq.setApplyFrom(req.getApplyFrom());
        assignReq.setApplyTo(req.getApplyTo());
        return groupPlanAssignmentService.create(assignReq);
    }
}
