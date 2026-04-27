package com.rs.subscription.service.impl;

import com.rs.subscription.service.*;

import com.rs.subscription.dto.PagedResponse;
import com.rs.subscription.dto.request.ChangePasswordRequest;
import com.rs.subscription.dto.request.CreateUserRequest;
import com.rs.subscription.dto.request.UpdateUserRequest;
import com.rs.subscription.dto.response.RoleResponse;
import com.rs.subscription.dto.response.UserResponse;
import com.rs.subscription.entity.Role;
import com.rs.subscription.entity.UserAccount;
import com.rs.subscription.enums.AuthEnums;
import com.rs.subscription.exception.ErrorCodes;
import com.rs.subscription.exception.SmsException;
import com.rs.subscription.repository.RefreshTokenRepository;
import com.rs.subscription.repository.RoleRepository;
import com.rs.subscription.repository.UserAccountRepository;
import com.rs.subscription.aop.Auditable;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Auditable(entityType = "USER")
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserAccountRepository userAccountRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleService roleService;

    @Transactional
    public UserResponse createUser(CreateUserRequest req, String createdBy) {
        if (userAccountRepository.existsByUsername(req.getUsername())) {
            throw new SmsException(ErrorCodes.USERNAME_ALREADY_EXISTS, "Username already exists: " + req.getUsername(), 409);
        }
        if (userAccountRepository.existsByEmail(req.getEmail())) {
            throw new SmsException(ErrorCodes.EMAIL_ALREADY_EXISTS, "Email already exists: " + req.getEmail(), 409);
        }
        validatePassword(req.getPassword());

        List<Role> roles = new ArrayList<>();
        if (req.getRoleIds() != null && !req.getRoleIds().isEmpty()) {
            for (Long rid : req.getRoleIds()) {
                roles.add(roleService.findById(rid));
            }
        } else {
            roleRepository.findByRoleName("ROLE_USER").ifPresent(roles::add);
        }

        UserAccount user = UserAccount.builder()
            .userId(UUID.randomUUID().toString())
            .username(req.getUsername())
            .email(req.getEmail())
            .fullName(req.getFullName())
            .passwordHash(passwordEncoder.encode(req.getPassword()))
            .authProvider(AuthEnums.AuthProvider.LOCAL.name())
            .status(AuthEnums.UserStatus.ACTIVE.name())
            .failedLoginAttempts(0)
            .createdBy(createdBy)
            .build();
        user.setRoles(roles);

        return toResponse(userAccountRepository.save(user));
    }

    public PagedResponse<UserResponse> listUsers(String status, String query, int page, int size) {
        Page<UserAccount> result;
        if (query != null && !query.isBlank()) {
            result = userAccountRepository.search(query, PageRequest.of(page, size));
        } else if (status != null && !status.isBlank()) {
            result = userAccountRepository.findByStatus(
                AuthEnums.normalize(status, AuthEnums.UserStatus.class, "status"),
                PageRequest.of(page, size)
            );
        } else {
            result = userAccountRepository.findAll(PageRequest.of(page, size));
        }
        return PagedResponse.<UserResponse>builder()
            .content(result.getContent().stream().map(this::toResponse).collect(Collectors.toList()))
            .totalElements(result.getTotalElements())
            .totalPages(result.getTotalPages())
            .page(page)
            .size(size)
            .build();
    }

    public UserResponse getUserById(String userId) {
        return toResponse(findById(userId));
    }

    @Transactional
    public UserResponse updateUser(String userId, UpdateUserRequest req) {
        UserAccount user = findById(userId);
        if (req.getEmail() != null && !req.getEmail().equals(user.getEmail())) {
            if (userAccountRepository.existsByEmail(req.getEmail())) {
                throw new SmsException(ErrorCodes.EMAIL_ALREADY_EXISTS, "Email already exists", 409);
            }
            user.setEmail(req.getEmail());
        }
        if (req.getFullName() != null) user.setFullName(req.getFullName());
        return toResponse(userAccountRepository.save(user));
    }

    @Transactional
    public void deleteUser(String userId, String requesterId) {
        if (userId.equals(requesterId)) {
            throw new SmsException(ErrorCodes.VALIDATION_FAILED, "Cannot delete your own account", 400);
        }
        UserAccount user = findById(userId);
        // Prevent deleting the last active admin
        boolean isAdmin = user.getRoles().stream().anyMatch(r -> "ROLE_ADMIN".equals(r.getRoleName()));
        if (isAdmin) {
            long adminCount = userAccountRepository.countByRoleName("ROLE_ADMIN");
            if (adminCount <= 1) {
                throw new SmsException(ErrorCodes.VALIDATION_FAILED, "Cannot delete the last admin account", 400);
            }
        }
        refreshTokenRepository.deleteAllByUserUserId(userId);
        userAccountRepository.delete(user);
    }

    @Transactional
    public void deactivateUser(String userId, String requesterId) {
        if (userId.equals(requesterId)) {
            throw new SmsException(ErrorCodes.VALIDATION_FAILED, "Cannot disable your own account", 400);
        }
        UserAccount user = findById(userId);
        user.setStatus(AuthEnums.UserStatus.INACTIVE.name());
        userAccountRepository.save(user);
    }

    @Transactional
    public void reactivateUser(String userId) {
        UserAccount user = findById(userId);
        user.setStatus(AuthEnums.UserStatus.ACTIVE.name());
        userAccountRepository.save(user);
    }

    @Transactional
    public void unlockUser(String userId) {
        UserAccount user = findById(userId);
        user.setStatus(AuthEnums.UserStatus.ACTIVE.name());
        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);
        userAccountRepository.save(user);
    }

    @Transactional
    public UserResponse assignRoles(String userId, List<Long> roleIds, String assignedBy) {
        UserAccount user = findById(userId);
        List<Role> roles = roleIds.stream().map(roleService::findById).collect(Collectors.toList());
        user.setRoles(roles);
        return toResponse(userAccountRepository.save(user));
    }

    @Transactional
    public void resetPassword(String userId, String newPassword) {
        UserAccount user = findById(userId);
        validatePassword(newPassword);
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userAccountRepository.save(user);
    }

    @Transactional
    public void changePassword(String userId, ChangePasswordRequest req) {
        UserAccount user = findById(userId);
        if (!passwordEncoder.matches(req.getCurrentPassword(), user.getPasswordHash())) {
            throw new SmsException(ErrorCodes.UNAUTHORIZED, "Current password is incorrect", 401);
        }
        validatePassword(req.getNewPassword());
        user.setPasswordHash(passwordEncoder.encode(req.getNewPassword()));
        userAccountRepository.save(user);
    }

    public UserResponse getMyProfile(String userId) {
        return getUserById(userId);
    }

    /** Gán manager trực tiếp cho user. Truyền null để xoá manager. */
    @Transactional
    public UserResponse setManager(String userId, String managerUserId) {
        UserAccount user = findById(userId);
        if (managerUserId == null || managerUserId.isBlank()) {
            user.setManager(null);
        } else {
            if (userId.equals(managerUserId)) {
                throw new SmsException(ErrorCodes.VALIDATION_FAILED, "User cannot be their own manager", 400);
            }
            UserAccount manager = findById(managerUserId);
            // Ngăn vòng lặp: managerUserId không được là cấp dưới của userId
            List<String> subordinates = userAccountRepository.findAllSubordinateIds(userId);
            if (subordinates.contains(managerUserId)) {
                throw new SmsException(ErrorCodes.VALIDATION_FAILED,
                    "Circular hierarchy: the designated manager is already a subordinate", 400);
            }
            user.setManager(manager);
        }
        return toResponse(userAccountRepository.save(user));
    }

    /** Trả về danh sách userId của tất cả cấp dưới (đệ quy). */
    @Transactional(readOnly = true)
    public List<String> getSubordinateIds(String managerId) {
        return userAccountRepository.findAllSubordinateIds(managerId);
    }

    /** Trả về danh sách cấp dưới trực tiếp (1 cấp). */
    @Transactional(readOnly = true)
    public List<UserResponse> getDirectSubordinates(String managerId) {
        return userAccountRepository.findDirectSubordinates(managerId)
            .stream().map(this::toResponse).toList();
    }

    public UserAccount findById(String userId) {
        return userAccountRepository.findById(userId)
            .orElseThrow(() -> new SmsException(ErrorCodes.VALIDATION_FAILED, "User not found: " + userId, 404));
    }

    private void validatePassword(String password) {
        if (password == null || password.length() < 8)
            throw new SmsException(ErrorCodes.PASSWORD_POLICY_VIOLATION, "Password must be at least 8 characters", 400);
        if (!password.matches(".*[A-Z].*"))
            throw new SmsException(ErrorCodes.PASSWORD_POLICY_VIOLATION, "Password must contain at least one uppercase letter", 400);
        if (!password.matches(".*[0-9].*"))
            throw new SmsException(ErrorCodes.PASSWORD_POLICY_VIOLATION, "Password must contain at least one digit", 400);
        if (!password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*"))
            throw new SmsException(ErrorCodes.PASSWORD_POLICY_VIOLATION, "Password must contain at least one special character", 400);
    }

    public UserResponse toResponse(UserAccount u) {
        UserResponse r = new UserResponse();
        r.setUserId(u.getUserId());
        r.setUsername(u.getUsername());
        r.setEmail(u.getEmail());
        r.setFullName(u.getFullName());
        r.setAuthProvider(u.getAuthProvider());
        r.setStatus(u.getStatus());
        r.setLastLoginAt(u.getLastLoginAt());
        r.setCreatedAt(u.getCreatedAt());
        if (u.getManager() != null) {
            r.setManagerUserId(u.getManager().getUserId());
            r.setManagerName(u.getManager().getFullName());
        }
        r.setRoles(u.getRoles().stream().map(role -> {
            RoleResponse rr = new RoleResponse();
            rr.setRoleId(role.getRoleId());
            rr.setRoleName(role.getRoleName());
            rr.setDisplayName(role.getDisplayName());
            rr.setIsSystemRole(role.getIsSystemRole());
            rr.setCreatedAt(role.getCreatedAt());
            return rr;
        }).collect(Collectors.toList()));
        return r;
    }
}
