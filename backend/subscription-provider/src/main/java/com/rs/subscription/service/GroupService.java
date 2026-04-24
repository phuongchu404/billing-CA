package com.rs.subscription.service;

import com.rs.subscription.dto.request.AddGroupMemberRequest;
import com.rs.subscription.dto.request.CreateGroupRequest;
import com.rs.subscription.dto.response.GroupMemberResponse;
import com.rs.subscription.dto.response.GroupResponse;
import com.rs.subscription.dto.response.SubscriptionResponse;
import com.rs.subscription.entity.Group;
import com.rs.subscription.entity.GroupMember;
import com.rs.subscription.entity.Plan;
import com.rs.subscription.entity.Subscription;
import com.rs.subscription.exception.ErrorCodes;
import com.rs.subscription.exception.SmsException;
import com.rs.subscription.repository.GroupMemberRepository;
import com.rs.subscription.repository.GroupRepository;
import com.rs.subscription.repository.SubscriptionRepository;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GroupService {

    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionService subscriptionService;
    private final PasswordEncoder passwordEncoder;

    public List<GroupResponse> listGroups() {
        return groupRepository.findAll().stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    @Transactional
    public GroupResponse createGroup(CreateGroupRequest req, String createdBy) {
        if (groupRepository.existsByGroupCode(req.getGroupCode())) {
            throw new SmsException(ErrorCodes.VALIDATION_FAILED, "Partner code already exists: " + req.getGroupCode(), 400);
        }
        if (groupRepository.existsByUsername(req.getUsername())) {
            throw new SmsException(ErrorCodes.VALIDATION_FAILED, "Username already taken: " + req.getUsername(), 400);
        }
        if (req.getPassword() == null || req.getPassword().isBlank()) {
            throw new SmsException(ErrorCodes.VALIDATION_FAILED, "Password is required when creating a partner", 400);
        }
        Group group = Group.builder()
            .groupCode(req.getGroupCode())
            .groupName(req.getGroupName())
            .username(req.getUsername())
            .password(passwordEncoder.encode(req.getPassword()))
            .contactEmail(req.getContactEmail())
            .contactPhone(req.getContactPhone())
            .refContractNo(req.getRefContractNo())
            .picEmails(req.getPicEmails())
            .status(Group.GroupStatus.ACTIVE)
            .createdBy(createdBy)
            .build();
        return toResponse(groupRepository.save(group));
    }

    public GroupResponse getGroupById(Long groupId) {
        return toResponse(findById(groupId));
    }

    @Transactional
    public GroupResponse updateGroup(Long groupId, CreateGroupRequest req) {
        Group group = findById(groupId);
        if (groupRepository.existsByUsernameAndGroupIdNot(req.getUsername(), groupId)) {
            throw new SmsException(ErrorCodes.VALIDATION_FAILED, "Username already taken: " + req.getUsername(), 400);
        }
        group.setGroupName(req.getGroupName());
        group.setUsername(req.getUsername());
        group.setContactEmail(req.getContactEmail());
        group.setContactPhone(req.getContactPhone());
        group.setRefContractNo(req.getRefContractNo());
        group.setPicEmails(req.getPicEmails());
        if (req.getPassword() != null && !req.getPassword().isBlank()) {
            group.setPassword(passwordEncoder.encode(req.getPassword()));
        }
        return toResponse(groupRepository.save(group));
    }

    @Transactional
    public void deactivateGroup(Long groupId) {
        Group group = findById(groupId);
        group.setStatus(Group.GroupStatus.INACTIVE);
        groupRepository.save(group);
    }

    @Transactional
    public void activateGroup(Long groupId) {
        Group group = findById(groupId);
        group.setStatus(Group.GroupStatus.ACTIVE);
        groupRepository.save(group);
    }

    public List<GroupMemberResponse> getMembers(Long groupId) {
        findById(groupId);
        return groupMemberRepository.findByGroupGroupId(groupId).stream()
            .map(this::toMemberResponse)
            .collect(Collectors.toList());
    }

    @Transactional
    public GroupMemberResponse addMember(Long groupId, AddGroupMemberRequest req, String addedBy) {
        Group group = findById(groupId);
        if (groupMemberRepository.existsByGroupGroupIdAndUserId(groupId, req.getUserId())) {
            throw new SmsException(ErrorCodes.MEMBER_ALREADY_IN_GROUP,
                "User is already a member of this group", 409);
        }

        LocalDate memberStart = null;
        LocalDate memberEnd = null;

        Subscription activeSub = subscriptionRepository.findByGroupIdAndStatus(
            groupId, Subscription.SubscriptionStatus.ACTIVE).stream().findFirst().orElse(null);

        if (activeSub != null) {
            // Enforce max-member cap defined on the plan
            Integer maxMembers = activeSub.getPlan().getMaxMembers();
            if (maxMembers != null) {
                long currentCount = groupMemberRepository.countByGroupGroupId(groupId);
                if (currentCount >= maxMembers) {
                    throw new SmsException(ErrorCodes.QUOTA_EXHAUSTED,
                        "Group has reached its maximum member limit of " + maxMembers, 422);
                }
            }

            // For INDIVIDUAL_START plans, calculate the member's own validity window
            if (activeSub.getPlan().getGroupMemberValidityMode() == Plan.GroupMemberValidityMode.INDIVIDUAL_START) {
                memberStart = LocalDate.now();
                memberEnd = memberStart.plusDays(activeSub.getPlan().getValidityDays());
            }
        }

        GroupMember member = GroupMember.builder()
            .group(group)
            .userId(req.getUserId())
            .role(req.getRole() != null ? req.getRole() : GroupMember.MemberRole.MEMBER)
            .addedBy(addedBy)
            .memberStartDate(memberStart)
            .memberEndDate(memberEnd)
            .build();
        return toMemberResponse(groupMemberRepository.save(member));
    }

    @Transactional
    public void removeMember(Long groupId, String userId) {
        if (!groupMemberRepository.existsByGroupGroupIdAndUserId(groupId, userId)) {
            throw new SmsException(ErrorCodes.MEMBER_NOT_IN_GROUP, "User is not a member of this group", 404);
        }
        groupMemberRepository.deleteByGroupGroupIdAndUserId(groupId, userId);
    }

    public SubscriptionResponse getGroupActiveSubscription(Long groupId) {
        findById(groupId);
        Subscription sub = subscriptionRepository.findByGroupIdAndStatus(groupId, Subscription.SubscriptionStatus.ACTIVE)
            .stream().findFirst()
            .orElseThrow(() -> new SmsException(ErrorCodes.SUBSCRIPTION_NOT_FOUND,
                "No active subscription for group: " + groupId, 404));
        return subscriptionService.getSubscriptionById(sub.getSubscriptionId());
    }

    public Group findById(Long groupId) {
        return groupRepository.findById(groupId)
            .orElseThrow(() -> new SmsException(ErrorCodes.GROUP_NOT_FOUND, "Group not found: " + groupId, 404));
    }

    private GroupResponse toResponse(Group g) {
        GroupResponse r = new GroupResponse();
        r.setGroupId(g.getGroupId());
        r.setGroupCode(g.getGroupCode());
        r.setGroupName(g.getGroupName());
        r.setUsername(g.getUsername());
        r.setContactEmail(g.getContactEmail());
        r.setContactPhone(g.getContactPhone());
        r.setRefContractNo(g.getRefContractNo());
        r.setPicEmails(g.getPicEmails());
        r.setStatus(g.getStatus().name());
        r.setCreatedBy(g.getCreatedBy());
        r.setCreatedAt(g.getCreatedAt());
        r.setMemberCount(groupMemberRepository.countByGroupGroupId(g.getGroupId()));
        return r;
    }

    private GroupMemberResponse toMemberResponse(GroupMember m) {
        GroupMemberResponse r = new GroupMemberResponse();
        r.setId(m.getId());
        r.setGroupId(m.getGroup().getGroupId());
        r.setUserId(m.getUserId());
        r.setRole(m.getRole().name());
        r.setJoinedAt(m.getJoinedAt());
        r.setAddedBy(m.getAddedBy());
        r.setMemberStartDate(m.getMemberStartDate());
        r.setMemberEndDate(m.getMemberEndDate());
        return r;
    }
}
