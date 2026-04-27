# Tài liệu Phân Quyền & Phê Duyệt (Permission & Approval)

> **Dự án:** billing-CA  
> **Cập nhật lần cuối:** 2026-04-27

---

## Mục lục

1. [Tổng quan hệ thống phân quyền (RBAC)](#1-tổng-quan-hệ-thống-phân-quyền-rbac)
2. [Luồng nghiệp vụ Phân Quyền](#2-luồng-nghiệp-vụ-phân-quyền)
3. [Luồng nghiệp vụ Phê Duyệt Đa Cấp](#3-luồng-nghiệp-vụ-phê-duyệt-đa-cấp)
4. [Các bảng liên quan & dữ liệu](#4-các-bảng-liên-quan--dữ-liệu)
5. [Cấu hình Backend](#5-cấu-hình-backend)
6. [Cấu hình Frontend](#6-cấu-hình-frontend)
7. [API Endpoints](#7-api-endpoints)
8. [Mã lỗi liên quan](#8-mã-lỗi-liên-quan)

---

## 1. Tổng quan hệ thống phân quyền (RBAC)

Hệ thống sử dụng mô hình **Role-Based Access Control (RBAC)** với 3 lớp:

```
UserAccount  →  UserRole  →  Role  →  RolePermission  →  Permission
(Người dùng)    (Gán role)  (Vai trò) (Mapping)           (Quyền cụ thể)
```

### Phân cấp Permission

Mỗi `Permission` được tổ chức theo cấu trúc 3 tầng:

```
Module Group (ví dụ: "Quản lý Gói Cước")
  └── Permission Group (ví dụ: "Phê duyệt")
        └── Permission Key (ví dụ: "approval:level1")
```

### Data Scope (Phạm vi dữ liệu nhìn thấy)

Ngoài quyền hành động, hệ thống còn kiểm soát **dữ liệu nào người dùng nhìn thấy**:

| Loại user          | Permission                  | Dữ liệu nhìn thấy                             |
|--------------------|-----------------------------|------------------------------------------------|
| Admin / Super User | Không giới hạn scope        | Tất cả groups                                  |
| User nội bộ        | `group:view:own`            | Chỉ groups của mình                            |
| User nội bộ        | `group:view:subordinates`   | Groups của mình + cấp dưới trong cây tổ chức   |
| Partner            | `report:view:partner`       | Các groups được cấp qua bảng `PartnerGroupAccess` |

---

## 2. Luồng nghiệp vụ Phân Quyền

### 2.1 Tạo Role mới

```
Admin → Tạo Role (tên, mô tả) → Gán Permission cho Role → Gán Role cho User
```

**Các bước chi tiết:**

1. **Tạo Role**: Admin tạo role mới qua `POST /api/v1/admin/roles`
2. **Gán Permission**: Admin chọn permissions từ cây permission và gán vào role qua `PUT /api/v1/admin/roles/{roleId}/permissions`
3. **Gán Role cho User**: Khi tạo user hoặc cập nhật user, chọn role cần gán

**Ràng buộc:**
- Role hệ thống (`isSystemRole = true`) không được sửa/xóa
- Không thể xóa role đang được gán cho user (`ROLE_IN_USE`)

### 2.2 Xem ma trận phân quyền

Xem tổng quan quyền của tất cả roles qua `GET /api/v1/admin/roles/permissions/matrix`.  
Response trả về ma trận: **Role × Permission** với số lượng user của mỗi role.

### 2.3 Kiểm soát truy cập tại Controller

Mỗi endpoint được bảo vệ bằng `@PreAuthorize`. Ví dụ:

```java
@PreAuthorize("hasAuthority('subscription:view')")    // Xem danh sách phê duyệt
@PreAuthorize("hasAuthority('subscription:update')")  // Submit / Resubmit
@PreAuthorize("hasAnyAuthority('approval:level1', 'approval:level2', 'approval:level3')")  // Phê duyệt
@PreAuthorize("hasAuthority('role:create')")          // Tạo role
```

---

## 3. Luồng nghiệp vụ Phê Duyệt Đa Cấp

### 3.1 Trạng thái Approval Request

```
DRAFT  →  IN_APPROVAL  →  APPROVED
                       →  REJECTED
                       →  NEED_REVISION  →  IN_APPROVAL (resubmit)
```

| Trạng thái      | Mô tả                                               |
|-----------------|-----------------------------------------------------|
| `DRAFT`         | Mới tạo, chưa nộp phê duyệt                         |
| `IN_APPROVAL`   | Đang trong quá trình phê duyệt (có step đang PENDING)|
| `NEED_REVISION` | Bị yêu cầu chỉnh sửa lại                            |
| `APPROVED`      | Được duyệt toàn bộ các cấp                          |
| `REJECTED`      | Bị từ chối tại một cấp nào đó                       |

### 3.2 Trạng thái Approval Step (từng bước)

| Trạng thái | Mô tả                                    |
|------------|------------------------------------------|
| `PENDING`  | Đang chờ quyết định ở cấp này            |
| `APPROVED` | Cấp này đã duyệt, chuyển cấp tiếp theo   |
| `REJECTED` | Cấp này từ chối                          |
| `SKIPPED`  | Bị bỏ qua (do cấp trước đã reject)       |

### 3.3 Số cấp phê duyệt

Số cấp phê duyệt được tính tự động dựa trên **loại khách hàng** và **giá trị hợp đồng**, lấy từ bảng `approval_level_configs`:

| Phân khúc    | Giá trị hợp đồng          | Số cấp |
|--------------|--------------------------|--------|
| `INDIVIDUAL` | < 5,000,000              | 1 cấp  |
| `INDIVIDUAL` | 5,000,000 – 50,000,000   | 2 cấp  |
| `INDIVIDUAL` | ≥ 50,000,000             | 3 cấp  |
| `GROUP`      | < 50,000,000             | 1 cấp  |
| `GROUP`      | 50,000,000 – 500,000,000 | 2 cấp  |
| `GROUP`      | ≥ 500,000,000            | 3 cấp  |

> **Lưu ý:** Cấu hình này lưu trong DB (bảng `approval_level_configs`) và có thể thay đổi mà không cần deploy lại code.

### 3.4 Cấp phê duyệt và Role tương ứng

| Level     | Role DB        | Chức danh               |
|-----------|----------------|-------------------------|
| `LEVEL_1` | `APPROVAL_L1`  | Trưởng phòng            |
| `LEVEL_2` | `APPROVAL_L2`  | Giám đốc                |
| `LEVEL_3` | `APPROVAL_L3`  | CFO (Giám đốc Tài chính)|

### 3.5 Luồng phê duyệt chi tiết từng bước

#### Bước 1: Tạo yêu cầu (Tạo GroupPlanAssignment hoặc RetailPlanSchedule)

- Khi tạo `GroupPlanAssignment` hoặc `RetailPlanSchedule`, hệ thống tự động:
  1. Tạo bản ghi `ApprovalRequest` với status = `DRAFT`
  2. Gọi `createAndSubmit()` → tự submit ngay
  3. Tính số cấp phê duyệt dựa trên `ApprovalLevelConfig`
  4. Tạo các bản ghi `ApprovalRequestStep` (1–3 steps tùy số cấp)
  5. Step đầu tiên set `status = PENDING`, các step còn lại `PENDING`
  6. Gửi email thông báo cho người có role `APPROVAL_L1`

#### Bước 2: Phê duyệt cấp 1 (LEVEL_1)

Người có quyền `approval:level1` gọi `POST /api/v1/approval-requests/{id}/approve`:

- **Approve** → Step LEVEL_1 chuyển `APPROVED`
  - Nếu còn level tiếp theo: `currentLevel` tăng lên, step tiếp theo vẫn `PENDING`, gửi email cho LEVEL_2
  - Nếu đây là level cuối: toàn bộ request → `APPROVED`, cập nhật entity gốc
- **Reject** → Step LEVEL_1 chuyển `REJECTED`, các step còn lại → `SKIPPED`, request → `REJECTED`, cập nhật entity gốc, gửi email thông báo người nộp
- **Request Revision** → Request → `NEED_REVISION`, gửi email thông báo người nộp chỉnh sửa

#### Bước 3: Phê duyệt cấp 2, cấp 3 (nếu có)

Tương tự bước 2, nhưng cần quyền `approval:level2` / `approval:level3`.

#### Bước 4: Resubmit (sau khi bị yêu cầu sửa)

Người nộp gọi `POST /api/v1/approval-requests/{id}/resubmit`:

- Request → `IN_APPROVAL`
- **Reset toàn bộ steps về `PENDING`**, bắt đầu lại từ LEVEL_1
- Gửi email thông báo LEVEL_1

#### Ràng buộc nghiệp vụ

| Quy tắc | Mô tả |
|---------|-------|
| Không tự duyệt | Người nộp không thể tự phê duyệt yêu cầu của mình |
| Reject cần lý do | `reason` bắt buộc khi reject hoặc request revision |
| Chỉ submit khi DRAFT/NEED_REVISION | Trạng thái khác không cho phép submit |
| Reject lan truyền | Reject bất kỳ cấp nào → các step còn lại đều `SKIPPED` |
| Approval lan truyền | Duyệt xong cấp cuối → cập nhật status entity gốc |

### 3.6 Sơ đồ trạng thái đầy đủ

```
                         ┌──────────────┐
                         │    DRAFT     │
                         └──────┬───────┘
                                │ submit()
                                ▼
                    ┌───────────────────────┐
                    │      IN_APPROVAL      │
                    │  (Step L1: PENDING)   │
                    └───┬───────┬───────────┘
                        │       │
              approveStep()   reject()
                        │       │
              (if more levels)  ▼
                        │  ┌──────────┐
                        │  │ REJECTED │
                        │  └──────────┘
                        ▼
          ┌─────────────────────────────┐
          │        IN_APPROVAL          │
          │  (Step L2: PENDING, ...)    │
          └──────┬────────┬─────────────┘
                 │        │
     last approve()    requestRevision()
                 │        │
                 ▼        ▼
          ┌──────────┐  ┌───────────────┐
          │ APPROVED │  │ NEED_REVISION │
          └──────────┘  └───────┬───────┘
                                │ resubmit()
                                ▼
                    ┌───────────────────────┐
                    │      IN_APPROVAL      │
                    │  (Reset to L1 PENDING)│
                    └───────────────────────┘
```

---

## 4. Các bảng liên quan & dữ liệu

### 4.1 Bảng phân quyền

#### `roles`
```sql
CREATE TABLE roles (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    role_name       VARCHAR(100) UNIQUE NOT NULL,  -- Key kỹ thuật: APPROVAL_L1, ROLE_ADMIN
    display_name    VARCHAR(200),                  -- Tên hiển thị: Trưởng phòng
    description     TEXT,
    is_system_role  BOOLEAN DEFAULT FALSE,         -- TRUE = không sửa/xóa được
    created_at      DATETIME,
    updated_at      DATETIME
);
```
**Dữ liệu được tạo ra bởi:** Admin qua `POST /api/v1/admin/roles`, hoặc seeder khi khởi tạo hệ thống.

#### `permissions`
```sql
CREATE TABLE permissions (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    permission_key  VARCHAR(200) UNIQUE NOT NULL,  -- Ví dụ: approval:level1
    display_name    VARCHAR(300),
    module_group    VARCHAR(200),                  -- Nhóm module lớn
    group_name      VARCHAR(200),                  -- Nhóm con
    description     TEXT,
    created_at      DATETIME
);
```
**Dữ liệu được tạo ra bởi:** Seeder (cố định, không tạo qua UI).

#### `role_permissions`
```sql
CREATE TABLE role_permissions (
    role_id         BIGINT REFERENCES roles(id),
    permission_id   BIGINT REFERENCES permissions(id),
    PRIMARY KEY (role_id, permission_id)
);
```
**Dữ liệu được tạo ra bởi:** Admin gán permission cho role qua `PUT /api/v1/admin/roles/{roleId}/permissions`.

#### `user_roles`
```sql
CREATE TABLE user_roles (
    user_id         VARCHAR(100) REFERENCES user_accounts(user_id),
    role_id         BIGINT REFERENCES roles(id),
    PRIMARY KEY (user_id, role_id)
);
```
**Dữ liệu được tạo ra bởi:** Khi tạo user (`POST /api/v1/admin/users`) hoặc cập nhật user.

#### `user_accounts`
```sql
CREATE TABLE user_accounts (
    user_id         VARCHAR(100) PRIMARY KEY,
    username        VARCHAR(200) UNIQUE NOT NULL,
    email           VARCHAR(300),
    full_name       VARCHAR(300),
    status          ENUM('ACTIVE','INACTIVE','LOCKED'),
    manager_user_id VARCHAR(100) REFERENCES user_accounts(user_id),  -- Cây tổ chức
    auth_provider   ENUM('LOCAL','SSO'),
    created_at      DATETIME,
    updated_at      DATETIME
);
```
**Dữ liệu được tạo ra bởi:** Admin tạo user qua `POST /api/v1/admin/users`.

#### `partner_group_access`
```sql
CREATE TABLE partner_group_access (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    partner_user_id VARCHAR(100) REFERENCES user_accounts(user_id),
    group_id        BIGINT REFERENCES groups(id),
    granted_at      DATETIME,
    revoked_at      DATETIME  -- NULL = đang active
);
```
**Dữ liệu được tạo ra bởi:** Admin cấp/thu hồi quyền xem group cho partner.

### 4.2 Bảng phê duyệt

#### `approval_requests`
```sql
CREATE TABLE approval_requests (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    request_type    VARCHAR(100),     -- REQUEST_GROUP_PLAN_ASSIGNMENT, REQUEST_RETAIL_PLAN_SCHEDULE
    customer_segment VARCHAR(50),     -- GROUP, INDIVIDUAL
    status          VARCHAR(50),      -- DRAFT, IN_APPROVAL, NEED_REVISION, APPROVED, REJECTED
    requested_by    VARCHAR(100),     -- userId người nộp
    entity_type     VARCHAR(100),     -- GROUP_PLAN_ASSIGNMENT, RETAIL_PLAN_SCHEDULE
    entity_id       VARCHAR(200),     -- ID của entity gốc
    description     TEXT,
    contract_value  DECIMAL(20,2),    -- Giá trị hợp đồng → xác định số cấp
    total_levels    INT,              -- Tổng số cấp cần phê duyệt
    current_level   INT,             -- Cấp hiện tại đang chờ phê duyệt
    payload         JSON,             -- Snapshot dữ liệu lúc submit
    created_at      DATETIME,
    updated_at      DATETIME,
    INDEX idx_ar_status (status),
    INDEX idx_ar_entity (entity_type, entity_id)
);
```
**Dữ liệu được tạo ra bởi:**
- Tự động khi tạo `GroupPlanAssignment` (service: `GroupPlanAssignmentService`)
- Tự động khi tạo `RetailPlanSchedule` (service: `IndividualPlanConfigService`)

#### `approval_request_steps`
```sql
CREATE TABLE approval_request_steps (
    id                      BIGINT PRIMARY KEY AUTO_INCREMENT,
    approval_request_id     BIGINT REFERENCES approval_requests(id),
    step_level              INT,           -- 1, 2, 3
    required_approval_level VARCHAR(50),   -- LEVEL_1, LEVEL_2, LEVEL_3
    status                  VARCHAR(50),   -- PENDING, APPROVED, REJECTED, SKIPPED
    decided_by              VARCHAR(100),  -- userId người quyết định
    comment                 TEXT,          -- Ghi chú khi duyệt/từ chối
    decided_at              DATETIME,
    created_at              DATETIME,
    INDEX idx_ars_request_level (approval_request_id, step_level),
    INDEX idx_ars_status (status)
);
```
**Dữ liệu được tạo ra bởi:** Tự động khi `submit()` hoặc `resubmit()` được gọi trong `MultiLevelApprovalService`.

#### `approval_level_configs`
```sql
CREATE TABLE approval_level_configs (
    id                BIGINT PRIMARY KEY AUTO_INCREMENT,
    customer_segment  VARCHAR(50),     -- GROUP, INDIVIDUAL
    min_value         DECIMAL(20,2),   -- Giá trị tối thiểu (NULL = không giới hạn dưới)
    max_value         DECIMAL(20,2),   -- Giá trị tối đa (NULL = không giới hạn trên)
    required_levels   INT,             -- Số cấp phê duyệt
    description       TEXT,
    is_active         BOOLEAN DEFAULT TRUE,
    created_at        DATETIME
);
```
**Dữ liệu được tạo ra bởi:** Seeder hoặc Admin cấu hình. **Không thay đổi thường xuyên.**

### 4.3 Bảng entity gốc (trigger phê duyệt)

#### `group_plan_assignments`
```sql
CREATE TABLE group_plan_assignments (
    id                BIGINT PRIMARY KEY AUTO_INCREMENT,
    group_id          BIGINT,
    plan_template_id  BIGINT,
    assignment_status VARCHAR(50),   -- REQUESTED → APPROVED → ACTIVE → STOPPED/EXPIRED
    approved_by       VARCHAR(100),
    approved_at       DATETIME,
    apply_from        DATE,
    apply_to          DATE,
    created_by        VARCHAR(100),
    created_at        DATETIME,
    INDEX idx_gpa_group_status (group_id, assignment_status),
    INDEX idx_gpa_apply_range (apply_from, apply_to)
);
```

#### `retail_plan_schedules`
```sql
CREATE TABLE retail_plan_schedules (
    id                BIGINT PRIMARY KEY AUTO_INCREMENT,
    -- ... thông tin cá nhân
    schedule_status   VARCHAR(50),   -- REQUESTED → APPROVED → ACTIVE → INACTIVE
    approved_by       VARCHAR(100),
    approved_at       DATETIME,
    created_at        DATETIME
);
```

**Luồng cập nhật status các bảng này:**

| Sự kiện                      | Bảng cập nhật               | Cột thay đổi           |
|------------------------------|-----------------------------|------------------------|
| Tạo GroupPlanAssignment      | `group_plan_assignments`    | `assignment_status = REQUESTED` |
| Tạo RetailPlanSchedule       | `retail_plan_schedules`     | `schedule_status = REQUESTED`   |
| ApprovalRequest APPROVED     | entity gốc tương ứng        | status → `APPROVED`    |
| ApprovalRequest REJECTED     | entity gốc tương ứng        | status → `REJECTED`/`INACTIVE` |

---

## 5. Cấu hình Backend

### 5.1 Service chính cần nắm

| File | Vị trí | Chức năng |
|------|--------|-----------|
| `MultiLevelApprovalService.java` | `service/` | Orchestrator toàn bộ luồng phê duyệt đa cấp |
| `ApprovalRequestService.java` | `service/` | Phê duyệt đơn cấp (legacy, backward compat) |
| `RoleService.java` | `service/` | Quản lý role và permission |
| `UserService.java` | `service/` | Quản lý user account |
| `DataScopeService.java` | `service/` | Kiểm soát phạm vi dữ liệu nhìn thấy |
| `ApprovalNotificationService.java` | `service/` | Gửi email thông báo (async) |
| `GroupPlanAssignmentService.java` | `service/` | Tạo GroupPlanAssignment + trigger approval |
| `IndividualPlanConfigService.java` | `service/` | Tạo RetailPlanSchedule + trigger approval |

### 5.2 Controller & Endpoint mapping

| File | Vị trí | Prefix |
|------|--------|--------|
| `ApprovalRequestController.java` | `controller/` | `/api/v1/approval-requests` |

**Chỉnh sửa số cấp phê duyệt:**
→ Sửa dữ liệu trong bảng `approval_level_configs` hoặc sửa logic trong `MultiLevelApprovalService.resolveRequiredLevels()`.

**Chỉnh sửa mapping Level → Role:**
→ Sửa constant `LEVEL_ROLES` trong `MultiLevelApprovalService.java`:
```java
// Thay đổi tại đây nếu muốn đổi tên role cho từng cấp
private static final Map<ApprovalLevel, String> LEVEL_ROLES = Map.of(
    ApprovalLevel.LEVEL_1, "APPROVAL_L1",
    ApprovalLevel.LEVEL_2, "APPROVAL_L2",
    ApprovalLevel.LEVEL_3, "APPROVAL_L3"
);
```

**Thêm loại entity mới vào luồng phê duyệt:**
→ Sửa `propagateApproval()` và `propagateRejection()` trong `MultiLevelApprovalService.java` để xử lý `entityType` mới.

### 5.3 Enum quan trọng

File: `enums/CommercialEnums.java`

```java
// Thêm loại request mới vào đây
enum RequestType {
    CREATE_PLAN_TEMPLATE,
    REQUEST_GROUP_PLAN_ASSIGNMENT,
    REQUEST_RETAIL_PLAN_SCHEDULE,
    CANCEL_SUBSCRIPTION,
    SUSPEND_SUBSCRIPTION
}

// Trạng thái phê duyệt đa cấp
enum MultiApprovalRequestStatus {
    DRAFT, IN_APPROVAL, NEED_REVISION, APPROVED, REJECTED
}

// Trạng thái từng step
enum ApprovalStepStatus {
    PENDING, APPROVED, REJECTED, SKIPPED
}

// Các cấp phê duyệt
enum ApprovalLevel {
    LEVEL_1, LEVEL_2, LEVEL_3
}
```

### 5.4 Security Configuration

**Phân quyền tại controller** – file `ApprovalRequestController.java`:
```java
// Xem danh sách / chi tiết
@PreAuthorize("hasAuthority('subscription:view')")

// Nộp / tái nộp phê duyệt
@PreAuthorize("hasAuthority('subscription:update')")

// Phê duyệt / từ chối / yêu cầu sửa (bất kỳ cấp nào)
@PreAuthorize("hasAnyAuthority('approval:level1', 'approval:level2', 'approval:level3')")
```

**Nếu muốn tách quyền theo từng cấp** – cần sửa annotation `@PreAuthorize` trong controller và thêm logic kiểm tra level trong service.

### 5.5 Cấu hình Email Notification

File: `service/ApprovalNotificationService.java`

- Tất cả methods đều dùng `@Async` → gửi email không block luồng chính
- Để thay đổi template email, sửa trong `ApprovalNotificationService`
- Để thêm kênh thông báo khác (Slack, SMS), thêm method mới vào service này

---

## 6. Cấu hình Frontend

### 6.1 API calls – nơi định nghĩa các call tới backend

| File | Chức năng |
|------|-----------|
| `frontend/src/api/approvals.ts` | Tất cả API call liên quan phê duyệt |
| `frontend/src/api/roles.ts` | Tất cả API call liên quan role/permission |
| `frontend/src/api/users.ts` | API call quản lý user |

**Sửa endpoint:** Nếu backend thay đổi URL → sửa trong file `.ts` tương ứng.

### 6.2 Views (giao diện)

| File | Chức năng |
|------|-----------|
| `frontend/src/views/approvals/ApprovalList.vue` | Danh sách yêu cầu phê duyệt + filter |
| `frontend/src/views/approvals/ApprovalDetail.vue` | Chi tiết + các nút hành động (Approve/Reject/Revision) |
| `frontend/src/views/roles/` | Quản lý role và gán permission |
| `frontend/src/views/individual/PlanConfigDetail.vue` | Chi tiết cấu hình gói cước cá nhân |
| `frontend/src/views/plans/AddPlan.vue` | Thêm gói cước (trigger approval khi tạo) |

**Thêm nút hành động mới trong phê duyệt** → sửa `ApprovalDetail.vue`:
- Thêm dialog mới
- Gọi API function tương ứng trong `approvals.ts`

### 6.3 Router – kiểm soát truy cập trang

File: `frontend/src/router/index.ts`

```typescript
// Thêm/sửa route mới cho approval
{ path: '/approvals',     component: ApprovalList,   meta: { permission: 'approval:view' } }
{ path: '/approvals/:id', component: ApprovalDetail, meta: { permission: 'approval:view' } }
{ path: '/roles',         component: RoleList,        meta: { permission: 'role:view' } }
```

**Thêm page mới với kiểm soát quyền:**
1. Thêm route với `meta.permission` tương ứng
2. Đảm bảo permission key đã có trong DB và được gán cho role thích hợp

### 6.4 Navigation Menu

File: `frontend/src/common/nav.ts`

Điều chỉnh menu item nào hiển thị với quyền nào tại đây. Mỗi menu item có `permissionKey` để kiểm soát hiển thị.

---

## 7. API Endpoints

### Phê duyệt

| Method | URL | Quyền cần | Mô tả |
|--------|-----|-----------|-------|
| GET | `/api/v1/approval-requests` | `subscription:view` | Danh sách tất cả approval request |
| GET | `/api/v1/approval-requests/{id}` | `subscription:view` | Chi tiết approval request |
| POST | `/api/v1/approval-requests/{id}/submit` | `subscription:update` | Nộp phê duyệt (DRAFT → IN_APPROVAL) |
| POST | `/api/v1/approval-requests/{id}/approve` | `approval:level*` | Phê duyệt một cấp |
| POST | `/api/v1/approval-requests/{id}/reject` | `approval:level*` | Từ chối |
| POST | `/api/v1/approval-requests/{id}/revision` | `approval:level*` | Yêu cầu sửa lại |
| POST | `/api/v1/approval-requests/{id}/resubmit` | `subscription:update` | Tái nộp sau khi sửa |
| GET | `/api/v1/approval-requests/level-configs` | `subscription:view` | Xem cấu hình số cấp phê duyệt |

### Phân quyền (Role & Permission)

| Method | URL | Quyền cần | Mô tả |
|--------|-----|-----------|-------|
| GET | `/api/v1/admin/roles` | `role:view` | Danh sách roles |
| POST | `/api/v1/admin/roles` | `role:create` | Tạo role mới |
| PUT | `/api/v1/admin/roles/{roleId}` | `role:update` | Cập nhật role |
| DELETE | `/api/v1/admin/roles/{roleId}` | `role:update` | Xóa role |
| GET | `/api/v1/admin/roles/permissions` | `role:view` | Cây permissions |
| GET | `/api/v1/admin/roles/permissions/matrix` | `role:view` | Ma trận phân quyền |
| PUT | `/api/v1/admin/roles/{roleId}/permissions` | `role:update` | Gán permissions cho role |

---

## 8. Mã lỗi liên quan

File: `exception/ErrorCodes.java`

### Phê duyệt

| Mã lỗi | Hằng số | Mô tả |
|--------|---------|-------|
| 2013 | `APPROVAL_NOT_FOUND` | Không tìm thấy approval request |
| 2014 | `APPROVAL_ALREADY_REVIEWED` | Đã được xem xét rồi |
| 2015 | `APPROVAL_EXECUTION_FAILED` | Thực thi phê duyệt thất bại |
| 2022 | `APPROVAL_STEP_NOT_FOUND` | Không tìm thấy step ở cấp hiện tại |
| 2023 | `APPROVAL_NOT_IN_PROGRESS` | Request không ở trạng thái IN_APPROVAL |
| 2024 | `APPROVAL_SELF_APPROVE` | Không được tự phê duyệt yêu cầu của mình |
| 2025 | `APPROVAL_WRONG_LEVEL` | Sai cấp phê duyệt |
| 2026 | `APPROVAL_NOT_REVISABLE` | Request không ở trạng thái NEED_REVISION |
| 2027 | `APPROVAL_LEVEL_CONFIG_NOT_FOUND` | Không tìm thấy cấu hình số cấp |

### Role & User

| Mã lỗi | Hằng số | Mô tả |
|--------|---------|-------|
| 3011 | `USERNAME_ALREADY_EXISTS` | Username đã tồn tại |
| 3012 | `EMAIL_ALREADY_EXISTS` | Email đã tồn tại |
| 3013 | `CANNOT_MODIFY_SYSTEM_ROLE` | Không được sửa role hệ thống |
| 3014 | `ROLE_IN_USE` | Role đang được gán cho user, không thể xóa |

---

## Checklist nâng cấp thường gặp

### Thêm cấp phê duyệt mới (ví dụ LEVEL_4)

- [ ] Thêm `LEVEL_4` vào enum `ApprovalLevel` trong `CommercialEnums.java`
- [ ] Tạo Role `APPROVAL_L4` mới trong DB
- [ ] Thêm mapping `LEVEL_4 → APPROVAL_L4` trong `LEVEL_ROLES` của `MultiLevelApprovalService.java`
- [ ] Cập nhật bảng `approval_level_configs` thêm ngưỡng giá trị mới → 4 cấp
- [ ] Thêm quyền `approval:level4` vào DB và gán cho role `APPROVAL_L4`
- [ ] Cập nhật `@PreAuthorize` trong `ApprovalRequestController.java`
- [ ] Cập nhật UI `ApprovalDetail.vue` hiển thị 4 steps

### Thêm loại entity mới vào luồng phê duyệt (ví dụ ContractRequest)

- [ ] Thêm `REQUEST_CONTRACT` vào enum `RequestType`
- [ ] Khi tạo entity → gọi `MultiLevelApprovalService.createAndSubmit()`
- [ ] Thêm case xử lý `entityType = "CONTRACT_REQUEST"` trong `propagateApproval()` và `propagateRejection()`

### Thay đổi ngưỡng giá trị để tính số cấp phê duyệt

- [ ] Chỉ cần UPDATE bảng `approval_level_configs` trong DB — không cần sửa code
