package com.rs.subscription.repository;

import com.rs.subscription.entity.GroupContact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface GroupContactRepository extends JpaRepository<GroupContact, Long> {
    List<GroupContact> findByGroupGroupIdOrderByIsPrimaryDescGroupContactIdAsc(Long groupId);

    @Modifying
    @Query("DELETE FROM GroupContact c WHERE c.group.groupId = :groupId")
    void deleteByGroupGroupId(@Param("groupId") Long groupId);
}

