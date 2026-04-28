package com.rs.subscription.repository;

import com.rs.subscription.entity.GroupMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

public interface GroupMemberRepository extends JpaRepository<GroupMember, Long> {
    List<GroupMember> findByGroupGroupId(Long groupId);
    List<GroupMember> findByUserId(Long userId);
    Optional<GroupMember> findByGroupGroupIdAndUserId(Long groupId, Long userId);
    boolean existsByGroupGroupIdAndUserId(Long groupId, Long userId);
    @Transactional
    void deleteByGroupGroupIdAndUserId(Long groupId, Long userId);
    long countByGroupGroupId(Long groupId);
}



