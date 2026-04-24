package com.rs.subscription.repository;

import com.rs.subscription.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);

    @Modifying
    @Transactional
    @Query("UPDATE RefreshToken rt SET rt.status = 'EXPIRED' WHERE rt.expiresAt < current_timestamp AND rt.status = 'ACTIVE'")
    int expireOldTokens();

    List<RefreshToken> findByUserUserIdAndStatus(String userId, String status);

    @Modifying
    @Transactional
    void deleteAllByUserUserId(String userId);
}
