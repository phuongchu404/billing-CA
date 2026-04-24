package com.rs.subscription.repository;

import com.rs.subscription.entity.UserAccount;
import com.rs.subscription.entity.UserAccount.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface UserAccountRepository extends JpaRepository<UserAccount, String> {
    Optional<UserAccount> findByUsername(String username);
    Optional<UserAccount> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    Page<UserAccount> findByStatus(UserStatus status, Pageable pageable);

    @Query("SELECT u FROM UserAccount u WHERE lower(u.username) LIKE lower(concat('%',:q,'%')) OR lower(u.email) LIKE lower(concat('%',:q,'%')) OR lower(u.fullName) LIKE lower(concat('%',:q,'%'))")
    Page<UserAccount> search(@Param("q") String query, Pageable pageable);

    long countByStatus(UserStatus status);

    @Query("SELECT COUNT(u) FROM UserAccount u JOIN u.roles r WHERE r.roleName = :roleName")
    long countByRoleName(@Param("roleName") String roleName);
}
