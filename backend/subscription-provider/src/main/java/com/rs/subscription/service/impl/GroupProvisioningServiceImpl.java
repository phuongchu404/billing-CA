package com.rs.subscription.service.impl;

import com.rs.subscription.service.*;

import com.rs.subscription.dto.request.AddGroupPlanRequest;
import com.rs.subscription.dto.request.CreateGroupPlanAssignmentRequest;
import com.rs.subscription.dto.request.CreatePlanTemplateRequest;
import com.rs.subscription.dto.request.PlanPricingRuleRequest;
import com.rs.subscription.dto.request.CreateUserRequest;
import com.rs.subscription.dto.request.GrantPartnerAccessRequest;
import com.rs.subscription.dto.request.PartnerAccountRequest;
import com.rs.subscription.dto.request.ProvisionGroupRequest;
import com.rs.subscription.dto.request.UpsertGroupRequest;
import com.rs.subscription.dto.response.GroupDetailResponse;
import com.rs.subscription.dto.response.GroupPlanAssignmentResponse;
import com.rs.subscription.dto.response.PlanTemplateResponse;
import com.rs.subscription.dto.response.ProvisionGroupResponse;
import com.rs.subscription.dto.response.UserResponse;
import com.rs.subscription.entity.Group;
import com.rs.subscription.entity.Role;
import com.rs.subscription.exception.ErrorCodes;
import com.rs.subscription.exception.SmsException;
import com.rs.subscription.repository.GroupRepository;
import com.rs.subscription.repository.RoleRepository;
import com.rs.subscription.enums.CommercialEnums;
import com.rs.subscription.security.SecurityUtil;
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
    private final UserService userService;
    private final PartnerGroupAccessService partnerGroupAccessService;
    private final RoleRepository roleRepository;

    @Transactional
    public ProvisionGroupResponse provision(ProvisionGroupRequest req) {
        String actor = SecurityUtil.getCurrentUsername()
            .orElse(req.getRequestedBy() != null ? req.getRequestedBy() : "system");
        Long actorUserId = SecurityUtil.getCurrentUserId().orElse(null);

        // 1. Tạo group + lưu contacts (PIC, CONTRACT)
        UpsertGroupRequest groupReq = new UpsertGroupRequest();
        groupReq.setGroupName(req.getGroupName());
        groupReq.setPicEmails(req.getPicEmails());
        groupReq.setContactEmails(req.getContactEmails());
        groupReq.setRefContractNo(req.getRefContractNo());
        groupReq.setCreatedBy(actor);
        groupReq.setOwnerUserId(actorUserId);
        GroupDetailResponse group = groupService.create(groupReq);

        // 2. Tạo plan template, planCode sinh server-side từ groupCode
        String planCode = "PLN_" + group.getGroupCode() + "_" + System.currentTimeMillis();
        CreatePlanTemplateRequest planReq = new CreatePlanTemplateRequest();
        planReq.setPlanCode(planCode);
        planReq.setPlanName(req.getPlanName());
        planReq.setCustomerSegment(CommercialEnums.CustomerSegment.GROUP.name());
        planReq.setTemplateScope(CommercialEnums.TemplateScope.PARTNER_PRIVATE.name());
        planReq.setStatus(CommercialEnums.TemplateStatus.AVAILABLE.name());
        planReq.setEffectiveFrom(req.getEffectiveFrom());
        planReq.setEffectiveTo(req.getEffectiveTo());
        planReq.setIsVisible(true);
        planReq.setAllowBulkSigning(false);
        planReq.setAllowApiAccess(false);
        planReq.setCreatedBy(actor);
        planReq.setPricingRules(req.getPricingRules());
        PlanTemplateResponse planTemplate = planTemplateService.create(planReq);

        // 3. Gán gói cước cho đại lý
        CreateGroupPlanAssignmentRequest assignReq = new CreateGroupPlanAssignmentRequest();
        assignReq.setGroupId(group.getGroupId());
        assignReq.setPlanTemplateId(planTemplate.getPlanTemplateId());
        assignReq.setAssignmentStatus(CommercialEnums.AssignmentStatus.AVAILABLE.name());
        assignReq.setRequestedBy(actor);
        assignReq.setApplyFrom(req.getEffectiveFrom());
        assignReq.setApplyTo(req.getEffectiveTo());
        GroupPlanAssignmentResponse assignment = groupPlanAssignmentService.create(assignReq);

        ProvisionGroupResponse response = new ProvisionGroupResponse();
        response.setGroup(group);
        response.setPlanTemplate(planTemplate);
        response.setAssignment(assignment);

        // 4. Tùy chọn: tạo tài khoản đối tác và cấp quyền xem group vừa tạo
        if (req.getPartnerAccount() != null) {
            UserResponse partnerUser = createPartnerUser(req.getPartnerAccount(), group.getGroupName(), actor);
            GrantPartnerAccessRequest grantReq = new GrantPartnerAccessRequest();
            grantReq.setPartnerUserId(partnerUser.getUserId());
            grantReq.setGroupId(group.getGroupId());
            partnerGroupAccessService.grant(grantReq, actor);
            response.setPartnerUser(partnerUser);
        }

        return response;
    }

    private UserResponse createPartnerUser(PartnerAccountRequest pa, String groupName, String actor) {
        Role partnerRole = roleRepository.findByRoleName("ROLE_PARTNER")
            .orElseThrow(() -> new SmsException(ErrorCodes.VALIDATION_FAILED,
                "ROLE_PARTNER not found. Please seed the roles table.", 500));

        CreateUserRequest req = new CreateUserRequest();
        req.setUsername(pa.getUsername());
        req.setEmail(pa.getEmail());
        req.setFullName(pa.getFullName() != null && !pa.getFullName().isBlank()
            ? pa.getFullName() : groupName);
        req.setPassword(pa.getPassword());
        req.setConfirmPassword(pa.getConfirmPassword());
        req.setRoleIds(java.util.List.of(partnerRole.getRoleId()));
        return userService.createUser(req, actor);
    }

    @Transactional
    public GroupPlanAssignmentResponse addPlanToGroup(Long groupId, AddGroupPlanRequest req) {
        String actor = SecurityUtil.getCurrentUsername()
            .orElse(req.getRequestedBy() != null ? req.getRequestedBy() : "system");

        if (req.getPricingRules() != null) {
            for (PlanPricingRuleRequest rr : req.getPricingRules()) {
                if (rr.getRangeMax() != null && rr.getRangeMax() < rr.getRangeMin()) {
                    throw new SmsException(ErrorCodes.VALIDATION_FAILED,
                            "Giá trị max phải lớn hơn hoặc bằng giá trị min trong bảng cấu hình giá", 400);
                }
            }
        }

        Group group = groupRepository.findById(groupId)
            .orElseThrow(() -> new SmsException(ErrorCodes.GROUP_NOT_FOUND, "Group not found: " + groupId, 404));

        String planCode = "PLN_" + group.getGroupCode() + "_" + System.currentTimeMillis();

        CreatePlanTemplateRequest planReq = new CreatePlanTemplateRequest();
        planReq.setPlanCode(planCode);
        planReq.setPlanName(req.getPlanName());
        planReq.setCustomerSegment(CommercialEnums.CustomerSegment.GROUP.name());
        planReq.setTemplateScope(CommercialEnums.TemplateScope.PARTNER_PRIVATE.name());
        planReq.setStatus(CommercialEnums.TemplateStatus.AVAILABLE.name());
        planReq.setEffectiveFrom(req.getApplyFrom());
        planReq.setEffectiveTo(req.getApplyTo());
        planReq.setIsVisible(true);
        planReq.setAllowBulkSigning(false);
        planReq.setAllowApiAccess(false);
        planReq.setCreatedBy(actor);
        planReq.setPricingRules(req.getPricingRules());
        PlanTemplateResponse planTemplate = planTemplateService.create(planReq);

        CreateGroupPlanAssignmentRequest assignReq = new CreateGroupPlanAssignmentRequest();
        assignReq.setGroupId(groupId);
        assignReq.setPlanTemplateId(planTemplate.getPlanTemplateId());
        assignReq.setAssignmentStatus(CommercialEnums.AssignmentStatus.AVAILABLE.name());
        assignReq.setRequestedBy(actor);
        assignReq.setApplyFrom(req.getApplyFrom());
        assignReq.setApplyTo(req.getApplyTo());
        return groupPlanAssignmentService.create(assignReq);
    }
}
