package com.rs.subscription.repository;

import com.rs.subscription.entity.UserAccount;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {
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
    List<Long> findDirectSubordinateIds(@Param("managerId") Long managerId);

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
    List<Long> findAllSubordinateIds(@Param("managerId") Long managerId);

    // Lấy thông tin user kèm manager (dùng khi hiển thị cây tổ chức)
    @Query("SELECT u FROM UserAccount u LEFT JOIN FETCH u.manager WHERE u.manager.userId = :managerId")
    List<UserAccount> findDirectSubordinates(@Param("managerId") Long managerId);

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
    @Query(value = """
        SELECT DISTINCT u.* FROM user_accounts u
        JOIN user_roles ur ON ur.user_id = u.user_id
        JOIN role_permissions rp ON rp.role_id = ur.role_id
        JOIN permissions p ON p.permission_id = rp.permission_id
        WHERE u.status = 'ACTIVE'
          AND (p.permission_key = :permissionKey OR p.permission_key = '*')
        """, nativeQuery = true)
    List<UserAccount> findActiveUsersByPermissionKey(@Param("permissionKey") String permissionKey);

    @Query(value = """
        WITH RECURSIVE manager_chain AS (
            SELECT m.user_id, 1 AS level_no
            FROM user_accounts requester
            JOIN user_accounts m ON requester.manager_user_id = m.user_id
            WHERE requester.username = :username

            UNION ALL

            SELECT m.user_id, mc.level_no + 1
            FROM manager_chain mc
            JOIN user_accounts current_manager ON current_manager.user_id = mc.user_id
            JOIN user_accounts m ON current_manager.manager_user_id = m.user_id
        )
        SELECT ua.* FROM manager_chain mc
        JOIN user_accounts ua ON ua.user_id = mc.user_id
        JOIN user_roles ur ON ur.user_id = ua.user_id
        JOIN role_permissions rp ON rp.role_id = ur.role_id
        JOIN permissions p ON p.permission_id = rp.permission_id
        WHERE ua.status = 'ACTIVE'
          AND (p.permission_key = :permissionKey OR p.permission_key = '*')
        ORDER BY mc.level_no ASC
        """, nativeQuery = true)
    List<UserAccount> findActiveManagersByUsernameAndPermissionKey(
        @Param("username") String username,
        @Param("permissionKey") String permissionKey);

    Optional<UserAccount> findByUsernameAndStatus(String username, String status);
}



