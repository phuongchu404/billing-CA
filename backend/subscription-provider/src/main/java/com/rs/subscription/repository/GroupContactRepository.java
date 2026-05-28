package com.rs.subscription.repository;

import com.rs.subscription.entity.GroupContact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface GroupContactRepository extends JpaRepository<GroupContact, Long> {
    List<GroupContact> findByGroupGroupIdOrderByIsPrimaryDescGroupContactIdAsc(Long groupId);

    @Modifying
    @Query("DELETE FROM GroupContact c WHERE c.group.groupId = :groupId")
    void deleteByGroupGroupId(@Param("groupId") Long groupId);

    /** Tìm các email đã tồn tại trong hệ thống (dùng khi tạo mới đại lý) */
    @Query("SELECT DISTINCT c.email FROM GroupContact c WHERE c.email IN :emails")
    List<String> findExistingEmails(@Param("emails") Collection<String> emails);

    /** Tìm các email đã tồn tại ở đại lý KHÁC (dùng khi cập nhật đại lý) */
    @Query("SELECT DISTINCT c.email FROM GroupContact c WHERE c.email IN :emails AND c.group.groupId != :excludeGroupId")
    List<String> findExistingEmailsExcludingGroup(@Param("emails") Collection<String> emails,
                                                   @Param("excludeGroupId") Long excludeGroupId);
}

