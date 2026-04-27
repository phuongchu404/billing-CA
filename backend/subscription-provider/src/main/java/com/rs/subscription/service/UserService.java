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
import java.util.UUID;
import java.util.stream.Collectors;

public interface UserService {

    UserResponse createUser(CreateUserRequest req, String createdBy);

    PagedResponse<UserResponse> listUsers(String status, String query, int page, int size);

    UserResponse getUserById(String userId);

    UserResponse updateUser(String userId, UpdateUserRequest req);

    void deleteUser(String userId, String requesterId);

    void deactivateUser(String userId, String requesterId);

    void reactivateUser(String userId);

    void unlockUser(String userId);

    UserResponse assignRoles(String userId, List<Long> roleIds, String assignedBy);

    void resetPassword(String userId, String newPassword);

    void changePassword(String userId, ChangePasswordRequest req);

    UserResponse getMyProfile(String userId);

    UserResponse setManager(String userId, String managerUserId);

    List<String> getSubordinateIds(String managerId);

    List<UserResponse> getDirectSubordinates(String managerId);

    UserAccount findById(String userId);

    UserResponse toResponse(UserAccount u);
}
