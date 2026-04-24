package com.rs.subscription.repository;

import com.rs.subscription.entity.PasswordHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

public interface PasswordHistoryRepository extends JpaRepository<PasswordHistory, Long> {

    @Query(value = "SELECT * FROM password_history WHERE user_id = :userId ORDER BY created_at DESC LIMIT :count", nativeQuery = true)
    List<PasswordHistory> findRecentByUserId(@Param("userId") String userId, @Param("count") int count);


}
