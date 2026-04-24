package com.rs.subscription.repository;

import com.rs.subscription.entity.GroupContact;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GroupContactRepository extends JpaRepository<GroupContact, Long> {
    List<GroupContact> findByGroupGroupIdOrderByIsPrimaryDescGroupContactIdAsc(Long groupId);
    void deleteByGroupGroupId(Long groupId);
}

