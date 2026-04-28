package com.rs.subscription.service;

import com.rs.subscription.dto.request.GrantPartnerAccessRequest;
import com.rs.subscription.dto.response.PartnerGroupAccessResponse;
import com.rs.subscription.entity.Group;
import com.rs.subscription.entity.PartnerGroupAccess;
import com.rs.subscription.entity.UserAccount;
import com.rs.subscription.exception.ErrorCodes;
import com.rs.subscription.exception.SmsException;
import com.rs.subscription.repository.GroupRepository;
import com.rs.subscription.repository.PartnerGroupAccessRepository;
import com.rs.subscription.repository.UserAccountRepository;
import com.rs.subscription.aop.Auditable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

public interface PartnerGroupAccessService {

    PartnerGroupAccessResponse grant(GrantPartnerAccessRequest request, String grantedBy);

    void revoke(Long accessId, String revokedBy);

    void revokeByPartnerAndGroup(Long partnerUserId, Long groupId, String revokedBy);

    List<PartnerGroupAccessResponse> listActiveForPartner(Long partnerUserId);

    List<PartnerGroupAccessResponse> listHistoryForPartner(Long partnerUserId);
}


