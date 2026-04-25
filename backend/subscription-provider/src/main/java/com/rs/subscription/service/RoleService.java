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
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final PermissionRepository permissionRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final EntityManager entityManager;

    public List<RoleResponse> listRoles() {
        return roleRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional
    public RoleResponse createRole(CreateRoleRequest req) {
        if (roleRepository.existsByRoleName(req.getRoleName())) {
            throw new SmsException(ErrorCodes.VALIDATION_FAILED, "Role name already exists: " + req.getRoleName(), 400);
        }
        Role role = Role.builder()
            .roleName(req.getRoleName())
            .displayName(req.getDisplayName())
            .description(req.getDescription())
            .isSystemRole(false)
            .build();
        return toResponse(roleRepository.save(role));
    }

    @Transactional
    public RoleResponse updateRole(Long roleId, CreateRoleRequest req) {
        Role role = findById(roleId);
        if (Boolean.TRUE.equals(role.getIsSystemRole())) {
            throw new SmsException(ErrorCodes.CANNOT_MODIFY_SYSTEM_ROLE, "System roles cannot be modified", 400);
        }
        role.setDisplayName(req.getDisplayName());
        role.setDescription(req.getDescription());
        return toResponse(roleRepository.save(role));
    }

    @Transactional
    public void deleteRole(Long roleId) {
        Role role = findById(roleId);
        if (Boolean.TRUE.equals(role.getIsSystemRole())) {
            throw new SmsException(ErrorCodes.CANNOT_MODIFY_SYSTEM_ROLE, "System roles cannot be deleted", 400);
        }
        userRoleRepository.deleteAllByRoleRoleId(roleId);
        rolePermissionRepository.deleteAllByRoleRoleId(roleId);
        entityManager.flush();
        roleRepository.deleteById(roleId);
    }

    public Role findById(Long roleId) {
        return roleRepository.findById(roleId)
            .orElseThrow(() -> new SmsException(ErrorCodes.VALIDATION_FAILED, "Role not found: " + roleId, 404));
    }

    // ── Permission Tree ──

    public List<ModuleGroupResponse> listPermissionTree() {
        List<Permission> allPerms = permissionRepository.findAllByOrderBySortOrderAsc();
        Map<String, Map<String, List<Permission>>> moduleMap = new LinkedHashMap<>();
        for (Permission p : allPerms) {
            moduleMap
                .computeIfAbsent(p.getModuleGroup(), k -> new LinkedHashMap<>())
                .computeIfAbsent(p.getGroupName(), k -> new ArrayList<>())
                .add(p);
        }
        List<ModuleGroupResponse> result = new ArrayList<>();
        for (Map.Entry<String, Map<String, List<Permission>>> moduleEntry : moduleMap.entrySet()) {
            ModuleGroupResponse module = new ModuleGroupResponse();
            module.setModuleName(moduleEntry.getKey());
            List<PermissionGroupResponse> permGroups = new ArrayList<>();
            for (Map.Entry<String, List<Permission>> groupEntry : moduleEntry.getValue().entrySet()) {
                PermissionGroupResponse g = new PermissionGroupResponse();
                g.setGroupName(groupEntry.getKey());
                g.setPermissions(groupEntry.getValue().stream().map(this::toPermissionResponse).collect(Collectors.toList()));
                permGroups.add(g);
            }
            module.setPermissionGroups(permGroups);
            result.add(module);
        }
        return result;
    }

    // ── Permission Matrix ──

    public RolePermissionMatrixResponse getPermissionMatrix() {
        List<Role> roles = roleRepository.findAll();
        List<Permission> allPerms = permissionRepository.findAllByOrderBySortOrderAsc();
        List<RolePermission> allRolePerms = rolePermissionRepository.findAll();

        // Build 3-level: module -> group -> permissions
        Map<String, Map<String, List<Permission>>> moduleMap = new LinkedHashMap<>();
        for (Permission p : allPerms) {
            moduleMap
                .computeIfAbsent(p.getModuleGroup(), k -> new LinkedHashMap<>())
                .computeIfAbsent(p.getGroupName(), k -> new ArrayList<>())
                .add(p);
        }

        List<ModuleGroupResponse> moduleGroups = new ArrayList<>();
        for (Map.Entry<String, Map<String, List<Permission>>> moduleEntry : moduleMap.entrySet()) {
            ModuleGroupResponse module = new ModuleGroupResponse();
            module.setModuleName(moduleEntry.getKey());
            List<PermissionGroupResponse> permGroups = new ArrayList<>();
            for (Map.Entry<String, List<Permission>> groupEntry : moduleEntry.getValue().entrySet()) {
                PermissionGroupResponse g = new PermissionGroupResponse();
                g.setGroupName(groupEntry.getKey());
                g.setPermissions(groupEntry.getValue().stream().map(this::toPermissionResponse).collect(Collectors.toList()));
                permGroups.add(g);
            }
            module.setPermissionGroups(permGroups);
            moduleGroups.add(module);
        }

        // Build roleId -> permissionId list
        Map<Long, List<Long>> rolePermsMap = new HashMap<>();
        for (Role r : roles) {
            rolePermsMap.put(r.getRoleId(), new ArrayList<>());
        }
        for (RolePermission rp : allRolePerms) {
            List<Long> list = rolePermsMap.get(rp.getRole().getRoleId());
            if (list != null) {
                list.add(rp.getPermission().getPermissionId());
            }
        }

        // Build roleId -> user count
        Map<Long, Integer> roleUserCounts = new HashMap<>();
        for (Role r : roles) roleUserCounts.put(r.getRoleId(), 0);
        for (Object[] row : userRoleRepository.countUsersByRole()) {
            Long roleId = (Long) row[0];
            Long count  = (Long) row[1];
            roleUserCounts.put(roleId, count.intValue());
        }

        RolePermissionMatrixResponse response = new RolePermissionMatrixResponse();
        response.setRoles(roles.stream().map(this::toResponse).collect(Collectors.toList()));
        response.setModuleGroups(moduleGroups);
        response.setRolePermissions(rolePermsMap);
        response.setRoleUserCounts(roleUserCounts);
        return response;
    }

    @Transactional
    public void assignPermissions(Long roleId, List<Long> permissionIds) {
        Role role = findById(roleId);
        rolePermissionRepository.deleteAllByRoleRoleId(roleId);
        entityManager.flush();

        if (permissionIds != null && !permissionIds.isEmpty()) {
            List<Permission> perms = permissionRepository.findAllById(permissionIds);
            List<RolePermission> rolePerms = perms.stream()
                .map(p -> RolePermission.builder().role(role).permission(p).build())
                .collect(Collectors.toList());
            rolePermissionRepository.saveAll(rolePerms);
        }
    }

    public List<Long> getRolePermissionIds(Long roleId) {
        findById(roleId); // validate role exists
        return rolePermissionRepository.findAllByRoleRoleId(roleId).stream()
            .map(rp -> rp.getPermission().getPermissionId())
            .collect(Collectors.toList());
    }

    // ── Mappers ──

    public RoleResponse toResponse(Role r) {
        RoleResponse res = new RoleResponse();
        res.setRoleId(r.getRoleId());
        res.setRoleName(r.getRoleName());
        res.setDisplayName(r.getDisplayName());
        res.setDescription(r.getDescription());
        res.setIsSystemRole(r.getIsSystemRole());
        res.setCreatedAt(r.getCreatedAt());
        return res;
    }

    private PermissionResponse toPermissionResponse(Permission p) {
        PermissionResponse res = new PermissionResponse();
        res.setPermissionId(p.getPermissionId());
        res.setPermissionKey(p.getPermissionKey());
        res.setDisplayName(p.getDisplayName());
        res.setModuleGroup(p.getModuleGroup());
        res.setGroupName(p.getGroupName());
        res.setSortOrder(p.getSortOrder());
        return res;
    }
}
