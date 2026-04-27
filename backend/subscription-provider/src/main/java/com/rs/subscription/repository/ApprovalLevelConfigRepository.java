package com.rs.subscription.repository;

import com.rs.subscription.entity.ApprovalLevelConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface ApprovalLevelConfigRepository extends JpaRepository<ApprovalLevelConfig, Long> {

    List<ApprovalLevelConfig> findByCustomerSegmentAndIsActiveTrueOrderByMinValueAsc(String customerSegment);

    /**
     * Tìm config phù hợp với customerSegment và giá trị hợp đồng.
     * minValue <= value < maxValue (maxValue null = không giới hạn)
     */
    @Query("""
        SELECT c FROM ApprovalLevelConfig c
        WHERE c.customerSegment = :segment
          AND c.isActive = true
          AND (:value >= COALESCE(c.minValue, 0))
          AND (c.maxValue IS NULL OR :value < c.maxValue)
        ORDER BY c.minValue ASC NULLS FIRST
        """)
    Optional<ApprovalLevelConfig> findMatchingConfig(
        @Param("segment") String segment,
        @Param("value") BigDecimal value
    );
}
