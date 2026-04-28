package com.rs.subscription.repository;

import com.rs.subscription.entity.RolePermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RolePermissionRepository extends JpaRepository<RolePermission, Long> {
    List<RolePermission> findAllByRoleRoleId(Long roleId);
    List<RolePermission> findAllByRoleRoleIdIn(List<Long> roleIds);
    void deleteAllByRoleRoleId(Long roleId);

    @Query("""
            SELECT DISTINCT p.permissionKey
            FROM UserRole ur
            JOIN RolePermission rp ON rp.role = ur.role
            JOIN rp.permission p
            WHERE ur.user.userId = :userId
            """)
    List<String> findPermissionKeysByUserId(@Param("userId") Long userId);
}



