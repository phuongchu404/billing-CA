package com.rs.subscription.repository;

import com.rs.subscription.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByRoleName(String roleName);
    List<Role> findAllByIsSystemRoleTrue();
    boolean existsByRoleName(String roleName);
}
