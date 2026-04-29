package com.rs.subscription.security.service;

import com.rs.subscription.entity.UserAccount;
import com.rs.subscription.repository.PermissionRepository;
import com.rs.subscription.repository.RolePermissionRepository;
import com.rs.subscription.repository.UserAccountRepository;
import com.rs.subscription.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserAccountRepository userAccountRepository;
    private final UserRoleRepository userRoleRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final PermissionRepository permissionRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String usernameOrUserId) throws UsernameNotFoundException {
        UserAccount user = userAccountRepository.findByUsername(usernameOrUserId)
                .or(() -> findByNumericId(usernameOrUserId))
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + usernameOrUserId));

        Set<String> authorities = new LinkedHashSet<>();
        authorities.addAll(userRoleRepository.findRoleNamesByUserId(user.getUserId()));

        List<String> permissionKeys = rolePermissionRepository.findPermissionKeysByUserId(user.getUserId());
        if (permissionKeys.contains("*")) {
            // Wildcard: nạp toàn bộ permission keys — admin xem được mọi dữ liệu
            permissionRepository.findAllByOrderBySortOrderAsc()
                .forEach(p -> authorities.add(p.getPermissionKey()));
        } else {
            authorities.addAll(permissionKeys);
        }

        return new CustomUserDetails(
                user.getUserId(),
                user.getUsername(),
                user.getEmail(),
                user.getFullName(),
                user.getStatus(),
                user.getPasswordHash(),
                authorities
        );
    }

    private java.util.Optional<UserAccount> findByNumericId(String value) {
        try {
            return userAccountRepository.findById(Long.valueOf(value));
        } catch (NumberFormatException ex) {
            return java.util.Optional.empty();
        }
    }
}


