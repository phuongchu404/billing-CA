package com.rs.subscription.repository;

import com.rs.subscription.entity.UserRole;
import com.rs.subscription.entity.UserRoleId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserRoleRepository extends JpaRepository<UserRole, UserRoleId> {
    List<UserRole> findByUserUserId(Long userId);
    List<UserRole> findByRoleRoleId(Long roleId);
    boolean existsByRoleRoleId(Long roleId);
    void deleteAllByUserUserId(Long userId);
    void deleteAllByRoleRoleId(Long roleId);

    @Query("SELECT ur.role.roleId, COUNT(ur) FROM UserRole ur GROUP BY ur.role.roleId")
    List<Object[]> countUsersByRole();

    @Query("SELECT DISTINCT ur.role.roleName FROM UserRole ur WHERE ur.user.userId = :userId")
    List<String> findRoleNamesByUserId(@Param("userId") Long userId);
}



