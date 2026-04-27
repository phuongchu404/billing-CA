package com.rs.subscription.repository;

import com.rs.subscription.entity.UserAccount;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface UserAccountRepository extends JpaRepository<UserAccount, String> {
    Optional<UserAccount> findByUsername(String username);
    Optional<UserAccount> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    Page<UserAccount> findByStatus(String status, Pageable pageable);

    @Query("SELECT u FROM UserAccount u WHERE lower(u.username) LIKE lower(concat('%',:q,'%')) OR lower(u.email) LIKE lower(concat('%',:q,'%')) OR lower(u.fullName) LIKE lower(concat('%',:q,'%'))")
    Page<UserAccount> search(@Param("q") String query, Pageable pageable);

    long countByStatus(String status);

    @Query("SELECT COUNT(DISTINCT u) FROM UserAccount u JOIN u.userRoles ur JOIN ur.role r WHERE r.roleName = :roleName")
    long countByRoleName(@Param("roleName") String roleName);

    // Phase 2: lấy danh sách user_id của tất cả cấp dưới trực tiếp
    @Query("SELECT u.userId FROM UserAccount u WHERE u.manager.userId = :managerId")
    List<String> findDirectSubordinateIds(@Param("managerId") String managerId);

    // Lấy tất cả cấp dưới đệ quy bằng CTE (native query — MySQL 8+)
    @Query(value = """
        WITH RECURSIVE subordinates AS (
            SELECT user_id FROM user_accounts WHERE manager_user_id = :managerId
            UNION ALL
            SELECT ua.user_id FROM user_accounts ua
            JOIN subordinates s ON ua.manager_user_id = s.user_id
        )
        SELECT user_id FROM subordinates
        """, nativeQuery = true)
    List<String> findAllSubordinateIds(@Param("managerId") String managerId);

    // Lấy thông tin user kèm manager (dùng khi hiển thị cây tổ chức)
    @Query("SELECT u FROM UserAccount u LEFT JOIN FETCH u.manager WHERE u.manager.userId = :managerId")
    List<UserAccount> findDirectSubordinates(@Param("managerId") String managerId);

    // Tìm user đang ACTIVE có role tên cụ thể — dùng để gửi notification cho approver
    @Query("""
        SELECT u FROM UserAccount u
        JOIN u.userRoles ur
        JOIN ur.role r
        WHERE r.roleName = :roleName
          AND u.status = 'ACTIVE'
        """)
    List<UserAccount> findActiveUsersByRoleName(@Param("roleName") String roleName);

    // Tìm user theo username để lấy email (gửi notification cho requester)
    Optional<UserAccount> findByUsernameAndStatus(String username, String status);
}
