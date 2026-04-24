package com.rs.subscription.repository;

import com.rs.subscription.entity.GroupMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

public interface GroupMemberRepository extends JpaRepository<GroupMember, Long> {
    List<GroupMember> findByGroupGroupId(Long groupId);
    List<GroupMember> findByUserId(String userId);
    Optional<GroupMember> findByGroupGroupIdAndUserId(Long groupId, String userId);
    boolean existsByGroupGroupIdAndUserId(Long groupId, String userId);
    @Transactional
    void deleteByGroupGroupIdAndUserId(Long groupId, String userId);
    long countByGroupGroupId(Long groupId);
}
