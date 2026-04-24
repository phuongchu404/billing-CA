package com.rs.subscription.repository;

import com.rs.subscription.entity.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface GroupRepository extends JpaRepository<Group, Long> {
    Optional<Group> findByGroupCode(String groupCode);
    boolean existsByGroupCode(String groupCode);
    boolean existsByUsernameAndGroupIdNot(String username, Long groupId);
    boolean existsByUsername(String username);
}
