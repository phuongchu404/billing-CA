package com.rs.subscription.service.impl;

import com.rs.subscription.service.*;

import com.rs.subscription.dto.request.CreateGroupContactRequest;
import com.rs.subscription.dto.response.GroupContactResponse;
import com.rs.subscription.enums.CommercialEnums;
import com.rs.subscription.entity.Group;
import com.rs.subscription.entity.GroupContact;
import com.rs.subscription.exception.ErrorCodes;
import com.rs.subscription.exception.SmsException;
import com.rs.subscription.repository.GroupContactRepository;
import com.rs.subscription.repository.GroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GroupContactServiceImpl implements GroupContactService {

    private final GroupRepository groupRepository;
    private final GroupContactRepository groupContactRepository;

    public List<GroupContactResponse> listByGroup(Long groupId) {
        return groupContactRepository.findByGroupGroupIdOrderByIsPrimaryDescGroupContactIdAsc(groupId)
            .stream().map(this::toResponse).toList();
    }

    @Transactional
    public GroupContactResponse create(CreateGroupContactRequest request) {
        if (request.getGroupId() == null) {
            throw new SmsException(ErrorCodes.VALIDATION_FAILED, "Group id is required", 400);
        }
        Group group = groupRepository.findById(request.getGroupId())
            .orElseThrow(() -> new SmsException(ErrorCodes.GROUP_NOT_FOUND, "Group not found: " + request.getGroupId(), 404));
        GroupContact entity = GroupContact.builder()
            .group(group)
            .contactType(CommercialEnums.normalize(request.getContactType(), CommercialEnums.ContactType.class, "contactType"))
            .email(request.getEmail())
            .fullName(request.getFullName())
            .phone(request.getPhone())
            .isPrimary(request.getIsPrimary())
            .receiveUsageAlert(request.getReceiveUsageAlert())
            .isActive(request.getIsActive())
            .build();
        if (Boolean.TRUE.equals(request.getIsPrimary())) {
            clearPrimaryContacts(group.getGroupId(), null);
        }
        return toResponse(groupContactRepository.save(entity));
    }

    @Transactional
    public GroupContactResponse update(Long id, CreateGroupContactRequest request) {
        GroupContact entity = groupContactRepository.findById(id)
            .orElseThrow(() -> new SmsException(ErrorCodes.GROUP_CONTACT_NOT_FOUND, "Group contact not found: " + id, 404));
        if (request.getGroupId() != null && !entity.getGroup().getGroupId().equals(request.getGroupId())) {
            throw new SmsException(ErrorCodes.VALIDATION_FAILED, "Cannot move contact to another group", 400);
        }
        entity.setContactType(CommercialEnums.normalize(request.getContactType(), CommercialEnums.ContactType.class, "contactType"));
        entity.setEmail(request.getEmail());
        entity.setFullName(request.getFullName());
        entity.setPhone(request.getPhone());
        entity.setIsPrimary(request.getIsPrimary());
        entity.setReceiveUsageAlert(request.getReceiveUsageAlert());
        entity.setIsActive(request.getIsActive());
        if (Boolean.TRUE.equals(request.getIsPrimary())) {
            clearPrimaryContacts(entity.getGroup().getGroupId(), entity.getGroupContactId());
        }
        return toResponse(groupContactRepository.save(entity));
    }

    @Transactional
    public void delete(Long id) {
        if (!groupContactRepository.existsById(id)) {
            throw new SmsException(ErrorCodes.GROUP_CONTACT_NOT_FOUND, "Group contact not found: " + id, 404);
        }
        groupContactRepository.deleteById(id);
    }

    private void clearPrimaryContacts(Long groupId, Long excludedId) {
        groupContactRepository.findByGroupGroupIdOrderByIsPrimaryDescGroupContactIdAsc(groupId).forEach(contact -> {
            if (excludedId == null || !contact.getGroupContactId().equals(excludedId)) {
                contact.setIsPrimary(false);
            }
        });
    }

    private GroupContactResponse toResponse(GroupContact entity) {
        GroupContactResponse response = new GroupContactResponse();
        response.setGroupContactId(entity.getGroupContactId());
        response.setGroupId(entity.getGroup().getGroupId());
        response.setContactType(entity.getContactType());
        response.setEmail(entity.getEmail());
        response.setFullName(entity.getFullName());
        response.setPhone(entity.getPhone());
        response.setIsPrimary(entity.getIsPrimary());
        response.setReceiveUsageAlert(entity.getReceiveUsageAlert());
        response.setIsActive(entity.getIsActive());
        return response;
    }
}
