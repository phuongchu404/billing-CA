package com.rs.subscription.repository;

import com.rs.subscription.entity.UserRole;
import com.rs.subscription.entity.UserRoleId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserRoleRepository extends JpaRepository<UserRole, UserRoleId> {
    List<UserRole> findByUserUserId(String userId);
    List<UserRole> findByRoleRoleId(Long roleId);
    boolean existsByRoleRoleId(Long roleId);
    void deleteAllByUserUserId(String userId);
}
