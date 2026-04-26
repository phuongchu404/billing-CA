package com.rs.subscription.repository;

import com.rs.subscription.entity.PartnerGroupAccess;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface PartnerGroupAccessRepository extends JpaRepository<PartnerGroupAccess, Long> {

    // Lấy tất cả access records còn hiệu lực của một partner
    @Query("SELECT pga FROM PartnerGroupAccess pga WHERE pga.partner.userId = :partnerUserId AND pga.revokedAt IS NULL")
    List<PartnerGroupAccess> findActiveByPartner(@Param("partnerUserId") String partnerUserId);

    // Lấy tất cả access records (kể cả đã thu hồi) — dùng cho audit
    List<PartnerGroupAccess> findByPartnerUserIdOrderByGrantedAtDesc(String partnerUserId);

    // Kiểm tra partner có quyền xem group cụ thể không
    @Query("""
        SELECT pga FROM PartnerGroupAccess pga
        WHERE pga.partner.userId = :partnerUserId
          AND pga.group.groupId = :groupId
          AND pga.revokedAt IS NULL
        """)
    Optional<PartnerGroupAccess> findActiveAccess(
        @Param("partnerUserId") String partnerUserId,
        @Param("groupId") Long groupId
    );

    // Lấy danh sách groupId mà partner được xem (dùng để filter report)
    @Query("SELECT pga.group.groupId FROM PartnerGroupAccess pga WHERE pga.partner.userId = :partnerUserId AND pga.revokedAt IS NULL")
    List<Long> findActiveGroupIdsByPartner(@Param("partnerUserId") String partnerUserId);
}
