-- ============================================================
-- V7: Phân quyền dữ liệu — ownership, user hierarchy, partner
--
-- Phase 1: Group ownership (user nào quản lý group nào)
-- Phase 2: User hierarchy  (manager → subordinates)
-- Phase 3: Partner access  (đối tác xem báo cáo group của mình)
-- ============================================================

-- ── Phase 1: Group ownership ─────────────────────────────────
-- Mỗi group thuộc về một user nội bộ (nhân viên kinh doanh phụ trách).
-- NULL = chưa gán / do admin tạo trực tiếp.
ALTER TABLE `groups`
    ADD COLUMN owner_user_id VARCHAR(36) NULL
        COMMENT 'User nội bộ phụ trách group này'
        AFTER updated_at,
    ADD CONSTRAINT fk_groups_owner
        FOREIGN KEY (owner_user_id) REFERENCES user_accounts(user_id)
        ON DELETE SET NULL;

CREATE INDEX idx_groups_owner ON `groups`(owner_user_id);

-- ── Phase 2: User hierarchy ───────────────────────────────────
-- Cây quản lý nội bộ: mỗi user có thể có 1 manager trực tiếp.
-- Self-referencing FK cho phép đệ quy tìm toàn bộ cấp dưới.
ALTER TABLE user_accounts
    ADD COLUMN manager_user_id VARCHAR(36) NULL
        COMMENT 'Manager trực tiếp của user này (NULL = không có manager / top-level)'
        AFTER created_by,
    ADD CONSTRAINT fk_user_accounts_manager
        FOREIGN KEY (manager_user_id) REFERENCES user_accounts(user_id)
        ON DELETE SET NULL;

CREATE INDEX idx_user_accounts_manager ON user_accounts(manager_user_id);

-- ── Phase 3: Partner group access ────────────────────────────
-- Bảng trung gian: đối tác (partner account) được phép xem báo cáo
-- của những group nào. Một đối tác có thể được gán nhiều groups.
CREATE TABLE partner_group_access (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    partner_user_id VARCHAR(36)  NOT NULL COMMENT 'User có role ROLE_PARTNER',
    group_id        BIGINT       NOT NULL COMMENT 'Group mà partner được xem báo cáo',
    granted_by      VARCHAR(36)  NOT NULL COMMENT 'Admin đã cấp quyền',
    granted_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    revoked_at      DATETIME     NULL     COMMENT 'NULL = còn hiệu lực',
    PRIMARY KEY (id),
    UNIQUE KEY uk_partner_group (partner_user_id, group_id),
    KEY idx_pga_partner   (partner_user_id),
    KEY idx_pga_group     (group_id),
    CONSTRAINT fk_pga_partner
        FOREIGN KEY (partner_user_id) REFERENCES user_accounts(user_id),
    CONSTRAINT fk_pga_group
        FOREIGN KEY (group_id) REFERENCES `groups`(group_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Đối tác được phép xem báo cáo của group nào';
