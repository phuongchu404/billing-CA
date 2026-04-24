package com.rs.subscription.repository;

import com.rs.subscription.entity.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByTokenAndUsedFalse(String token);


}
