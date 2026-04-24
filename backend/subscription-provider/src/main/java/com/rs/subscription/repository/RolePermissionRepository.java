package com.rs.subscription.repository;

import com.rs.subscription.entity.RolePermission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RolePermissionRepository extends JpaRepository<RolePermission, Long> {
    List<RolePermission> findAllByRoleRoleId(Long roleId);
    List<RolePermission> findAllByRoleRoleIdIn(List<Long> roleIds);
    void deleteAllByRoleRoleId(Long roleId);
}
