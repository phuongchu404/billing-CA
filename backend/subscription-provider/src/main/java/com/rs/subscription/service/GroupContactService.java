package com.rs.subscription.service;

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

public interface GroupContactService {

    List<GroupContactResponse> listByGroup(Long groupId);

    GroupContactResponse create(CreateGroupContactRequest request);

    GroupContactResponse update(Long id, CreateGroupContactRequest request);

    void delete(Long id);
}
