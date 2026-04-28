package com.rs.subscription.repository;

import com.rs.subscription.entity.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface GroupRepository extends JpaRepository<Group, Long> {
    Optional<Group> findByGroupCode(String groupCode);
    boolean existsByGroupCode(String groupCode);
    boolean existsByUsernameAndGroupIdNot(String username, Long groupId);
    boolean existsByUsername(String username);
    List<Group> findByStatusOrderByGroupId(String status);

    // Phase 1: lấy groups theo owner (nhân viên phụ trách)
    List<Group> findByOwnerUserIdOrderByGroupId(Long ownerUserId);
    List<Group> findByOwnerUserIdAndStatusOrderByGroupId(Long ownerUserId, String status);

    // Phase 2: lấy groups của nhiều owners (manager xem groups của cấp dưới)
    @Query("SELECT g FROM Group g WHERE g.owner.userId IN :ownerIds ORDER BY g.groupId")
    List<Group> findByOwnerUserIdInOrderByGroupId(@Param("ownerIds") Collection<Long> ownerIds);

    @Query("SELECT g FROM Group g WHERE g.owner.userId IN :ownerIds AND g.status = :status ORDER BY g.groupId")
    List<Group> findByOwnerUserIdInAndStatusOrderByGroupId(
        @Param("ownerIds") Collection<Long> ownerIds,
        @Param("status") String status
    );

    // Phase 3: lấy groups mà partner được cấp quyền xem (còn hiệu lực)
    @Query("""
        SELECT g FROM Group g
        JOIN PartnerGroupAccess pga ON pga.group = g
        WHERE pga.partner.userId = :partnerUserId
          AND pga.revokedAt IS NULL
        ORDER BY g.groupId
        """)
    List<Group> findAccessibleByPartner(@Param("partnerUserId") Long partnerUserId);
}



