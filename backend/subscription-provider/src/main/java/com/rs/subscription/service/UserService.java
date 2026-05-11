package com.rs.subscription.service;

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
import java.util.stream.Collectors;

public interface UserService {

    UserResponse createUser(CreateUserRequest req, String createdBy);

    PagedResponse<UserResponse> listUsers(String status, String query, int page, int size);

    PagedResponse<UserResponse> listUsersByRole(String roleName, int page, int size);

    UserResponse getUserById(Long userId);

    UserResponse updateUser(Long userId, UpdateUserRequest req);

    void deleteUser(Long userId, Long requesterId);

    void deactivateUser(Long userId, Long requesterId);

    void reactivateUser(Long userId);

    void unlockUser(Long userId);

    UserResponse assignRoles(Long userId, List<Long> roleIds, Long assignedBy);

    void resetPassword(Long userId, String newPassword);

    void changePassword(Long userId, ChangePasswordRequest req);

    UserResponse getMyProfile(Long userId);

    UserResponse setManager(Long userId, Long managerUserId);

    List<Long> getSubordinateIds(Long managerId);

    List<UserResponse> getDirectSubordinates(Long managerId);

    UserAccount findById(Long userId);

    UserResponse toResponse(UserAccount u);
}


