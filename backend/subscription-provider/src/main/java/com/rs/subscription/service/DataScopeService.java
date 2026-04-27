package com.rs.subscription.service;

import com.rs.subscription.repository.GroupRepository;
import com.rs.subscription.repository.PartnerGroupAccessRepository;
import com.rs.subscription.repository.UserAccountRepository;
import com.rs.subscription.security.service.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public interface DataScopeService {

    List<Long> resolveVisibleGroupIds();

    List<String> resolveVisibleOwnerIds();

    String currentUserId();

    boolean canAccessGroup(Long groupId);
}
