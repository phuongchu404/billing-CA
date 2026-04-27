package com.rs.subscription.service;

import com.rs.subscription.dto.request.CreateRoleRequest;
import com.rs.subscription.dto.response.PermissionResponse;
import com.rs.subscription.dto.response.RolePermissionMatrixResponse;
import com.rs.subscription.dto.response.RolePermissionMatrixResponse.ModuleGroupResponse;
import com.rs.subscription.dto.response.RolePermissionMatrixResponse.PermissionGroupResponse;
import com.rs.subscription.dto.response.RoleResponse;
import com.rs.subscription.entity.Permission;
import com.rs.subscription.entity.Role;
import com.rs.subscription.entity.RolePermission;
import com.rs.subscription.exception.ErrorCodes;
import com.rs.subscription.exception.SmsException;
import com.rs.subscription.repository.PermissionRepository;
import com.rs.subscription.repository.RolePermissionRepository;
import com.rs.subscription.repository.RoleRepository;
import com.rs.subscription.repository.UserRoleRepository;
import com.rs.subscription.aop.Auditable;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import java.util.stream.Collectors;

public interface RoleService {

    List<RoleResponse> listRoles();

    RoleResponse createRole(CreateRoleRequest req);

    RoleResponse updateRole(Long roleId, CreateRoleRequest req);

    void deleteRole(Long roleId);

    Role findById(Long roleId);

    List<ModuleGroupResponse> listPermissionTree();

    RolePermissionMatrixResponse getPermissionMatrix();

    void assignPermissions(Long roleId, List<Long> permissionIds);

    List<Long> getRolePermissionIds(Long roleId);

    RoleResponse toResponse(Role r);
}
