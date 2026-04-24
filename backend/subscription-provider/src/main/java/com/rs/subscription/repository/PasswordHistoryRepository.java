package com.rs.subscription.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

public interface PasswordHistoryRepository extends JpaRepository<PasswordHistoryRepository.PasswordHistory, Long> {

    @Query(value = "SELECT * FROM password_history WHERE user_id = :userId ORDER BY created_at DESC LIMIT :count", nativeQuery = true)
    List<PasswordHistory> findRecentByUserId(@Param("userId") String userId, @Param("count") int count);

    @Entity
    @Table(name = "password_history")
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    class PasswordHistory {
        @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
        @Column(nullable = false, length = 36) private String userId;
        @Column(nullable = false, length = 255) private String passwordHash;
        @Column(nullable = false, updatable = false) private LocalDateTime createdAt;
        @PrePersist void onCreate() { createdAt = LocalDateTime.now(); }
    }
}
