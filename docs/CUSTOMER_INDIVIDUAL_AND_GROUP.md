# Tài liệu Khách hàng Phổ thông & Khách hàng Đại lý

> **Dự án:** billing-CA  
> **Cập nhật lần cuối:** 2026-04-27

---

## Mục lục

1. [Tổng quan hai loại khách hàng](#1-tổng-quan-hai-loại-khách-hàng)
2. [Khách hàng Phổ thông (Individual)](#2-khách-hàng-phổ-thông-individual)
   - 2.1 [Luồng nghiệp vụ chi tiết](#21-luồng-nghiệp-vụ-chi-tiết)
   - 2.2 [Sơ đồ trạng thái](#22-sơ-đồ-trạng-thái)
   - 2.3 [Các bảng liên quan](#23-các-bảng-liên-quan)
3. [Khách hàng Đại lý (Group)](#3-khách-hàng-đại-lý-group)
   - 3.1 [Luồng nghiệp vụ chi tiết](#31-luồng-nghiệp-vụ-chi-tiết)
   - 3.2 [Sơ đồ trạng thái](#32-sơ-đồ-trạng-thái)
   - 3.3 [Các bảng liên quan](#33-các-bảng-liên-quan)
4. [Bảng chia sẻ giữa hai luồng](#4-bảng-chia-sẻ-giữa-hai-luồng)
5. [Cấu hình Backend](#5-cấu-hình-backend)
6. [Cấu hình Frontend](#6-cấu-hình-frontend)
7. [API Endpoints](#7-api-endpoints)
8. [Phân quyền](#8-phân-quyền)

---

## 1. Tổng quan hai loại khách hàng

| Tiêu chí | Khách hàng Phổ thông (Individual) | Khách hàng Đại lý (Group) |
|----------|-----------------------------------|--------------------------|
| `customerSegment` | `INDIVIDUAL` | `GROUP` |
| Đơn vị quản lý | Từng cá nhân | Tổ chức / Đại lý |
| Entity gói cước | `RetailPlanSchedule` | `GroupPlanAssignment` |
| Số cấp phê duyệt | 1–3 cấp (theo giá trị hợp đồng) | 1–3 cấp (ngưỡng cao hơn) |
| Ai tạo gói | Admin hệ thống | Admin hệ thống |
| Entity chứa thông tin tổ chức | Không có | `Group` (đại lý) |
| Quản lý thành viên | Không | `GroupMember` |
| Quản lý liên hệ | Không | `GroupContact` |

---

## 2. Khách hàng Phổ thông (Individual)

### 2.1 Luồng nghiệp vụ chi tiết

#### Bước 1 — Tạo gói cước (PlanTemplate)

**Ai thực hiện:** Admin hệ thống  
**Endpoint:** `POST /api/v1/individual/plan-configs`  
**Permission:** `plan:create`  
**Service:** `IndividualPlanConfigService.create()`

**Các thao tác DB:**
1. Tạo bản ghi `plan_templates` với `customer_segment = 'INDIVIDUAL'`, `status = 'AVAILABLE'`
2. Tạo một hoặc nhiều bản ghi `plan_pricing_rules` liên kết vào template (giá theo bậc số lượng)

**Dữ liệu đầu vào:**
```json
{
  "name": "Gói phổ thông 2024",
  "requestedBy": "admin@example.com",
  "applyFrom": "2024-01-01",
  "applyUntil": "2024-12-31",
  "pricingRules": [
    {
      "subject": "INDIVIDUAL",
      "durationMonths": 12,
      "condition": "CERTIFICATE_COUNT",
      "minValue": 1,
      "maxValue": 100,
      "fee": 50000,
      "sortOrder": 0
    }
  ]
}
```

> Nếu `applyFrom` / `applyUntil` được truyền khi tạo → hệ thống tự động thực hiện luôn Bước 2.

---

#### Bước 2 — Yêu cầu áp dụng gói (RetailPlanSchedule)

**Ai thực hiện:** Admin hệ thống  
**Endpoint:** `POST /api/v1/individual/plan-configs/{id}/request-apply`  
**Permission:** `plan:update`  
**Service:** `IndividualPlanConfigService.requestApply()`

**Điều kiện tiên quyết:**
- Template phải có `status = 'AVAILABLE'`
- Không có lịch nào đang ở trạng thái `REQUESTED` hoặc `APPROVED`

**Các thao tác DB:**
1. Tạo bản ghi `retail_plan_schedules` với `schedule_status = 'REQUESTED'`
2. Tạo bản ghi `assignment_audits`: `action = REQUEST`, `oldStatus = null`, `newStatus = REQUESTED`
3. Tính `contractValue` = max(`unit_price`) trong các `plan_pricing_rules` đang active
4. Tạo bản ghi `approval_requests` với `status = 'DRAFT'`, `customer_segment = 'INDIVIDUAL'`
5. Gọi `MultiLevelApprovalService.createAndSubmit()` → `approval_requests.status` → `IN_APPROVAL`
6. Tạo các bản ghi `approval_request_steps` (1–3 steps tùy ngưỡng giá trị)
7. Gửi email thông báo cho người có role `APPROVAL_L1`

---

#### Bước 3 — Phê duyệt (qua luồng Approval)

**Endpoint:** `POST /api/v1/approval-requests/{id}/approve`  
**Permission:** `approval:level1` / `approval:level2` / `approval:level3`

Xem chi tiết luồng phê duyệt tại [PERMISSION_AND_APPROVAL.md](./PERMISSION_AND_APPROVAL.md).

**Khi approval hoàn tất (tất cả cấp đã duyệt):**
1. `approval_requests.status` → `APPROVED`
2. `retail_plan_schedules.schedule_status` → `APPROVED`
3. Cập nhật `approved_by`, `approved_at` trong `retail_plan_schedules`
4. Tạo bản ghi `assignment_audits`: `action = APPROVE`, `oldStatus = REQUESTED`, `newStatus = APPROVED`

---

#### Bước 4 — Tự động kích hoạt (Scheduler)

**Khi nào:** Job scheduler chạy hàng ngày, kiểm tra ngày `apply_from`

- Khi `today >= schedule.apply_from` và `status = APPROVED` → `status` → `ACTIVE`
- Tạo `assignment_audits`: `action = ACTIVATE`

---

#### Bước 5 — Tự động hết hạn (Scheduler)

- Khi `today > schedule.apply_to` và `status = ACTIVE` → `status` → `INACTIVE`
- Tạo `assignment_audits`: `action = EXPIRE`

---

#### Bước 6 — Dừng thủ công

**Endpoint:** `POST /api/v1/individual/plan-configs/{id}/stop`  
**Permission:** `plan:update`

- `retail_plan_schedules.schedule_status` → `INACTIVE`
- Tạo `assignment_audits`: `action = STOP`

**Hủy kích hoạt cả template:**  
**Endpoint:** `POST /api/v1/individual/plan-configs/{id}/deactivate`

- `retail_plan_schedules.schedule_status` → `INACTIVE`
- `plan_templates.status` → `INACTIVE`

---

#### Bước 7 — Từ chối (Reject)

**Endpoint:** `POST /api/v1/approval-requests/{id}/reject` (qua luồng approval)  
hoặc `POST /api/v1/individual/plan-configs/{id}/reject` (trực tiếp)

- `retail_plan_schedules.schedule_status` → `INACTIVE`
- `approval_requests.status` → `REJECTED`
- Tạo `assignment_audits`: `action = REJECT`

---

### 2.2 Sơ đồ trạng thái

#### PlanTemplate (`plan_templates.status`)

```
DRAFT ──────────────────────────── AVAILABLE
                                       │
                               deactivate() / reject final
                                       │
                                   INACTIVE ──── ARCHIVED
```

#### RetailPlanSchedule (`retail_plan_schedules.schedule_status`)

```
                ┌─────────────────────┐
                │      AVAILABLE      │ (template tạo, chưa có lịch)
                └──────────┬──────────┘
                           │ requestApply()
                           ▼
                ┌─────────────────────┐
                │      REQUESTED      │ (đang chờ phê duyệt)
                └──────────┬──────────┘
            ┌──────────────┴──────────────┐
       reject()                       approve()
            │                             │
            ▼                             ▼
    ┌──────────────┐            ┌──────────────────┐
    │   INACTIVE   │            │     APPROVED     │ (chờ đến ngày apply_from)
    └──────────────┘            └────────┬─────────┘
                                         │ [Scheduler: apply_from ≤ today]
                                         ▼
                                ┌──────────────────┐
                                │      ACTIVE      │ (đang áp dụng)
                                └────────┬─────────┘
                             ┌───────────┴──────────────┐
                        stop()                  [Scheduler: today > apply_to]
                             │                           │
                             ▼                           ▼
                    ┌──────────────┐           ┌──────────────┐
                    │   INACTIVE   │           │   INACTIVE   │
                    │  (dừng thủ  │           │  (hết hạn)   │
                    │   công)      │           └──────────────┘
                    └──────────────┘
```

**Map trạng thái kỹ thuật → tên hiển thị trên UI:**

| DB Status | Hiển thị trên UI |
|-----------|-----------------|
| `AVAILABLE` | Khả dụng |
| `REQUESTED` | Đang chờ duyệt |
| `APPROVED` | Đã duyệt |
| `ACTIVE` | Đang áp dụng |
| `INACTIVE` | Không khả dụng |

---

### 2.3 Các bảng liên quan

#### `plan_templates`

```sql
CREATE TABLE plan_templates (
    plan_template_id    BIGINT PRIMARY KEY AUTO_INCREMENT,
    plan_code           VARCHAR(100) UNIQUE,          -- PLN_INDIVIDUAL_<timestamp>
    plan_name           VARCHAR(300) NOT NULL,
    description         TEXT,
    customer_segment    ENUM('INDIVIDUAL','GROUP'),
    template_scope      ENUM('PUBLIC','PARTNER_PRIVATE','SYSTEM'),
    status              ENUM('DRAFT','AVAILABLE','INACTIVE','ARCHIVED'),
    effective_from      DATE,
    effective_to        DATE,
    is_visible          BOOLEAN DEFAULT TRUE,
    allow_bulk_signing  BOOLEAN DEFAULT FALSE,
    allow_api_access    BOOLEAN DEFAULT FALSE,
    version_no          INT DEFAULT 1,
    cloned_from_template BIGINT REFERENCES plan_templates(plan_template_id),
    created_by          VARCHAR(100),
    created_at          DATETIME,
    updated_at          DATETIME
);
```

**Dữ liệu được tạo bởi:** Admin qua `POST /api/v1/individual/plan-configs`

---

#### `plan_pricing_rules`

```sql
CREATE TABLE plan_pricing_rules (
    plan_pricing_rule_id    BIGINT PRIMARY KEY AUTO_INCREMENT,
    plan_template_id        BIGINT REFERENCES plan_templates(plan_template_id),
    subject_type            ENUM('INDIVIDUAL','ORGANIZATION','INDIVIDUAL_OF_ORG'),
    certificate_validity_value INT,
    certificate_validity_unit ENUM('MONTH','DAY','YEAR'),
    pricing_metric          ENUM('SIGNING_COUNT','CERTIFICATE_COUNT'),
    range_min               INT,                  -- Bậc số lượng tối thiểu
    range_max               INT,                  -- Bậc số lượng tối đa
    unit_price              DECIMAL(20,2),        -- Đơn giá (VND)
    currency                VARCHAR(10) DEFAULT 'VND',
    quota_total             INT,                  -- Tổng hạn mức (nullable)
    sort_order              INT,
    is_active               BOOLEAN DEFAULT TRUE,
    created_at              DATETIME,
    updated_at              DATETIME
);
```

**Dữ liệu được tạo bởi:** Cùng lúc tạo `PlanTemplate`

---

#### `retail_plan_schedules`

```sql
CREATE TABLE retail_plan_schedules (
    retail_plan_schedule_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    plan_template_id        BIGINT REFERENCES plan_templates(plan_template_id),
    schedule_status         ENUM('AVAILABLE','REQUESTED','APPROVED','ACTIVE','INACTIVE'),
    apply_from              DATE,
    apply_to                DATE,
    requested_by            VARCHAR(100),
    requested_at            DATETIME,
    approved_by             VARCHAR(100),
    approved_at             DATETIME,
    created_at              DATETIME,
    updated_at              DATETIME,
    INDEX idx_rps_status (schedule_status),
    INDEX idx_rps_apply_range (apply_from, apply_to)
);
```

**Dữ liệu được tạo bởi:** `IndividualPlanConfigService.requestApply()`  
**Cập nhật bởi:**
- Approval → `approved_by`, `approved_at`, `schedule_status = APPROVED`
- Scheduler → `schedule_status = ACTIVE` hoặc `INACTIVE`
- Stop manual → `schedule_status = INACTIVE`

---

#### `assignment_audits`

```sql
CREATE TABLE assignment_audits (
    assignment_audit_id     BIGINT PRIMARY KEY AUTO_INCREMENT,
    group_plan_assignment_id BIGINT NULL,          -- Null nếu là individual
    retail_plan_schedule_id  BIGINT NULL,          -- Null nếu là group
    assignment_type         ENUM('RETAIL_PLAN','GROUP_PLAN'),
    action                  ENUM('REQUEST','APPROVE','REJECT','ACTIVATE','STOP','EXPIRE'),
    old_status              VARCHAR(50),
    new_status              VARCHAR(50),
    actor                   VARCHAR(200),          -- username người thực hiện
    note                    TEXT,
    created_at              DATETIME,
    INDEX idx_aa_retail (retail_plan_schedule_id),
    INDEX idx_aa_group (group_plan_assignment_id)
);
```

**Dữ liệu được tạo bởi:** Mỗi khi status thay đổi trong `IndividualPlanConfigService`

---

**Tóm tắt: nghiệp vụ ↔ bảng bị tác động (Individual)**

| Nghiệp vụ | Bảng thêm dữ liệu | Bảng cập nhật |
|-----------|-------------------|---------------|
| Tạo gói cước | `plan_templates`, `plan_pricing_rules` | — |
| Yêu cầu áp dụng | `retail_plan_schedules`, `assignment_audits`, `approval_requests`, `approval_request_steps` | — |
| Phê duyệt | `assignment_audits` | `retail_plan_schedules`, `approval_requests`, `approval_request_steps` |
| Kích hoạt (scheduler) | `assignment_audits` | `retail_plan_schedules` |
| Hết hạn (scheduler) | `assignment_audits` | `retail_plan_schedules` |
| Dừng thủ công | `assignment_audits` | `retail_plan_schedules` |
| Từ chối | `assignment_audits` | `retail_plan_schedules`, `approval_requests` |

---

## 3. Khách hàng Đại lý (Group)

### 3.1 Luồng nghiệp vụ chi tiết

#### Bước 1 — Tạo đại lý (Group)

**Ai thực hiện:** Admin hệ thống  
**Endpoint:** `POST /api/v1/groups`  
**Permission:** `group:create`  
**Service:** `GroupService.create()`

**Các thao tác DB:**
1. Tạo bản ghi `groups` với `status = 'ACTIVE'`
2. (Tùy chọn) Gán `owner` — FK đến `user_accounts` (cán bộ quản lý đại lý)

**Dữ liệu đầu vào:**
```json
{
  "groupCode": "DEALER_001",
  "groupName": "Thương lái ABC",
  "username": "dealer_abc",
  "password": "..."
}
```

> Sau bước này đại lý chưa có gói cước nào.

---

#### Bước 2 — Thêm thông tin liên hệ (GroupContact)

**Endpoint:** `POST /api/v1/groups/{groupId}/contacts`  
**Permission:** `group:create`  
**Service:** `GroupContactService`

**Các thao tác DB:**
1. Tạo bản ghi `group_contacts` với `is_active = true`
2. Ràng buộc unique: `(group_id, contact_type, email)`

**Loại liên hệ (`contact_type`):**

| Giá trị | Mô tả |
|---------|-------|
| `CONTRACT` | Liên hệ hợp đồng |
| `BILLING` | Liên hệ thanh toán |
| `SUPPORT` | Liên hệ kỹ thuật |
| `PIC` | Người phụ trách chính |

---

#### Bước 3 — Tạo và gán gói cước cho đại lý (GroupPlanAssignment)

Có hai cách:

**Cách A — Gán template đã có sẵn:**  
**Endpoint:** `POST /api/v1/groups/{groupId}/plan-assignments`  
**Permission:** `group:create`  
**Service:** `GroupPlanAssignmentService.create()`

**Cách B — Tạo template mới và gán luôn (one-shot):**  
**Endpoint:** `POST /api/v1/groups/{groupId}/add-plan`  
**Permission:** `group:update`  
**Service:** `GroupProvisioningService.addPlanToGroup()`

**Cách C — Tạo đại lý + template + assignment cùng lúc:**  
**Endpoint:** `POST /api/v1/groups/provision`  
**Permission:** `group:create AND plan:create`  
**Service:** `GroupProvisioningService.provision()`

**Các thao tác DB (tất cả các cách đều thực hiện):**
1. (Cách B/C) Tạo `plan_templates` với `customer_segment = 'GROUP'`
2. (Cách B/C) Tạo `plan_pricing_rules`
3. Tạo `group_plan_assignments` với `assignment_status = 'REQUESTED'`
4. Tạo `assignment_audits`: `action = REQUEST`
5. Tính `contract_value` = max(`unit_price × quota_total`) trong `plan_pricing_rules`
6. Tạo `approval_requests` với `status = 'DRAFT'`, `customer_segment = 'GROUP'`
7. Gọi `MultiLevelApprovalService.createAndSubmit()` → `approval_requests.status` → `IN_APPROVAL`
8. Tạo `approval_request_steps` (1–3 steps theo ngưỡng giá trị)
9. Gửi email thông báo APPROVAL_L1

**Dữ liệu đầu vào (Cách A):**
```json
{
  "groupId": 123,
  "planTemplateId": 456,
  "applyFrom": "2024-01-01",
  "applyTo": "2024-12-31",
  "requestedBy": "admin@example.com"
}
```

---

#### Bước 4 — Phê duyệt đa cấp

**Endpoint:** `POST /api/v1/approval-requests/{id}/approve`  
**Permission:** `approval:level1` / `approval:level2` / `approval:level3`

Xem chi tiết tại [PERMISSION_AND_APPROVAL.md](./PERMISSION_AND_APPROVAL.md).

**Khi approval hoàn tất:**
1. `approval_requests.status` → `APPROVED`
2. `group_plan_assignments.assignment_status` → `APPROVED`
3. Cập nhật `approved_by`, `approved_at`
4. Tạo `assignment_audits`: `action = APPROVE`

---

#### Bước 5 — Kích hoạt (Activate)

**Endpoint:** `POST /api/v1/groups/plan-assignments/{assignmentId}/review`  
**Body:** `{ "decision": "ACTIVATE", "actor": "admin@example.com" }`  
**Permission:** `group:update`  
**Service:** `GroupPlanAssignmentService.review()`

hoặc tự động qua Scheduler khi `apply_from ≤ today`.

**Các thao tác DB:**
1. `group_plan_assignments.assignment_status` → `ACTIVE`
2. Tạo `assignment_audits`: `action = ACTIVATE`
3. (Tùy chọn) Tạo bản ghi `subscriptions` với `source_type = 'GROUP_ASSIGNMENT'`

---

#### Bước 6 — Dừng thủ công (Stop)

**Endpoint:** `POST /api/v1/groups/plan-assignments/{assignmentId}/review`  
**Body:** `{ "decision": "STOP", "actor": "...", "note": "Lý do dừng" }`

**Các thao tác DB:**
1. `group_plan_assignments.assignment_status` → `STOPPED`
2. Ghi `stop_reason`, `stopped_at`
3. Tạo `assignment_audits`: `action = STOP`

---

#### Bước 7 — Tự động hết hạn (Scheduler)

- Khi `today > apply_to` và `status = ACTIVE` → `status` → `EXPIRED`
- Tạo `assignment_audits`: `action = EXPIRE`

---

#### Bước 8 — Từ chối (Reject)

Qua luồng approval: `POST /api/v1/approval-requests/{id}/reject`

**Các thao tác DB:**
1. `group_plan_assignments.assignment_status` → `REJECTED`
2. Ghi `rejected_by`, `rejected_at`
3. `approval_requests.status` → `REJECTED`
4. Tạo `assignment_audits`: `action = REJECT`

---

#### Bước 9 — Thêm thành viên vào đại lý (GroupMember)

**Service:** `GroupMemberService`

**Các thao tác DB:**
1. Tạo `group_members` với `role = OPERATOR` hoặc `MEMBER`
2. Ghi `joined_at`, `member_start_date`
3. (Tùy chọn) Liên kết `source_assignment_id` nếu thêm từ gói cước cụ thể

---

### 3.2 Sơ đồ trạng thái

#### Group (`groups.status`)

```
ACTIVE ──────── suspend() ──────── INACTIVE
  │                                    │
  └──────────── activate() ────────────┘
```

#### GroupPlanAssignment (`group_plan_assignments.assignment_status`)

```
           ┌────────────────────────┐
           │       REQUESTED        │ (mới tạo, chờ duyệt)
           └────────────┬───────────┘
           ┌────────────┴───────────┐
      reject()                  approve()
           │                        │
           ▼                        ▼
    ┌──────────────┐       ┌──────────────────┐
    │   REJECTED   │       │     APPROVED     │ (chờ kích hoạt)
    └──────────────┘       └────────┬─────────┘
                                    │ activate() hoặc
                                    │ [Scheduler: apply_from ≤ today]
                                    ▼
                           ┌──────────────────┐
                           │      ACTIVE      │ (đang hoạt động)
                           └────────┬─────────┘
                        ┌───────────┴─────────────┐
                   stop()                  [Scheduler: today > apply_to]
                        │                          │
                        ▼                          ▼
                ┌──────────────┐         ┌──────────────┐
                │   STOPPED    │         │   EXPIRED    │
                └──────────────┘         └──────────────┘
```

---

### 3.3 Các bảng liên quan

#### `groups`

```sql
CREATE TABLE groups (
    group_id        BIGINT PRIMARY KEY AUTO_INCREMENT,
    group_code      VARCHAR(100) UNIQUE NOT NULL,
    group_name      VARCHAR(300) NOT NULL,
    username        VARCHAR(200) UNIQUE NOT NULL,
    password        VARCHAR(500),                    -- Hashed
    ref_contract_no VARCHAR(200),                    -- Số hợp đồng tham chiếu
    status          ENUM('ACTIVE','INACTIVE') DEFAULT 'ACTIVE',
    owner_user_id   VARCHAR(100) REFERENCES user_accounts(user_id),
    created_by      VARCHAR(100),
    created_at      DATETIME,
    updated_at      DATETIME
);
```

**Dữ liệu được tạo bởi:** Admin qua `POST /api/v1/groups`

---

#### `group_contacts`

```sql
CREATE TABLE group_contacts (
    group_contact_id    BIGINT PRIMARY KEY AUTO_INCREMENT,
    group_id            BIGINT REFERENCES groups(group_id),
    contact_type        ENUM('CONTRACT','BILLING','SUPPORT','PIC'),
    email               VARCHAR(300) NOT NULL,
    full_name           VARCHAR(300),
    phone               VARCHAR(50),
    is_primary          BOOLEAN DEFAULT FALSE,
    receive_usage_alert BOOLEAN DEFAULT FALSE,
    is_active           BOOLEAN DEFAULT TRUE,
    created_at          DATETIME,
    updated_at          DATETIME,
    UNIQUE KEY uk_contact (group_id, contact_type, email)
);
```

**Dữ liệu được tạo bởi:** Admin qua `POST /api/v1/groups/{groupId}/contacts`

---

#### `group_members`

```sql
CREATE TABLE group_members (
    id                      BIGINT PRIMARY KEY AUTO_INCREMENT,
    group_id                BIGINT REFERENCES groups(group_id),
    user_id                 VARCHAR(200) NOT NULL,       -- username thành viên
    role                    ENUM('OPERATOR','MEMBER') DEFAULT 'MEMBER',
    joined_at               DATETIME,
    added_by                VARCHAR(200),
    member_start_date       DATE,
    member_end_date         DATE,
    source_assignment_id    BIGINT REFERENCES group_plan_assignments(group_plan_assignment_id)
);
```

**Dữ liệu được tạo bởi:** Admin hoặc Operator thêm thành viên

---

#### `group_plan_assignments`

```sql
CREATE TABLE group_plan_assignments (
    group_plan_assignment_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    group_id                 BIGINT REFERENCES groups(group_id),
    plan_template_id         BIGINT REFERENCES plan_templates(plan_template_id),
    assignment_status        ENUM('REQUESTED','APPROVED','ACTIVE','REJECTED','STOPPED','EXPIRED'),
    requested_by             VARCHAR(100),
    requested_at             DATETIME,
    approved_by              VARCHAR(100),
    approved_at              DATETIME,
    rejected_by              VARCHAR(100),
    rejected_at              DATETIME,
    activated_at             DATETIME,
    stopped_at               DATETIME,
    stop_reason              TEXT,
    apply_from               DATE,
    apply_to                 DATE,
    created_at               DATETIME,
    updated_at               DATETIME,
    INDEX idx_gpa_group_status (group_id, assignment_status),
    INDEX idx_gpa_apply_range (apply_from, apply_to)
);
```

**Dữ liệu được tạo bởi:** `GroupPlanAssignmentService.create()`  
**Cập nhật bởi:**
- Approval → `approved_by`, `approved_at`, `assignment_status = APPROVED`
- Review/activate → `activated_at`, `assignment_status = ACTIVE`
- Stop → `stopped_at`, `stop_reason`, `assignment_status = STOPPED`
- Scheduler → `assignment_status = EXPIRED`

---

**Tóm tắt: nghiệp vụ ↔ bảng bị tác động (Group)**

| Nghiệp vụ | Bảng thêm dữ liệu | Bảng cập nhật |
|-----------|-------------------|---------------|
| Tạo đại lý | `groups` | — |
| Thêm liên hệ | `group_contacts` | — |
| Thêm thành viên | `group_members` | — |
| Tạo gói cước + gán | `plan_templates`, `plan_pricing_rules`, `group_plan_assignments`, `assignment_audits`, `approval_requests`, `approval_request_steps` | — |
| Phê duyệt | `assignment_audits` | `group_plan_assignments`, `approval_requests`, `approval_request_steps` |
| Kích hoạt | `assignment_audits`, (tùy chọn) `subscriptions` | `group_plan_assignments` |
| Hết hạn (scheduler) | `assignment_audits` | `group_plan_assignments` |
| Dừng thủ công | `assignment_audits` | `group_plan_assignments` |
| Từ chối | `assignment_audits` | `group_plan_assignments`, `approval_requests` |

---

## 4. Bảng chia sẻ giữa hai luồng

#### `plan_pricing_rules`

Dùng chung cho cả Individual và Group template. Phân biệt qua `plan_templates.customer_segment`.

#### `assignment_audits`

Dùng chung cho cả hai luồng. Phân biệt qua:
- `assignment_type = 'RETAIL_PLAN'` → Individual
- `assignment_type = 'GROUP_PLAN'` → Group

Chỉ một trong hai FK (`group_plan_assignment_id` hoặc `retail_plan_schedule_id`) khác null.

#### `approval_requests` & `approval_request_steps`

Dùng chung toàn hệ thống. Phân biệt qua:
- `customer_segment = 'INDIVIDUAL'` / `'GROUP'`
- `entity_type = 'RETAIL_PLAN_SCHEDULE'` / `'GROUP_PLAN_ASSIGNMENT'`
- `entity_id` = ID của bản ghi tương ứng

#### `subscriptions`

Tạo ra khi gói cước được kích hoạt. Phân biệt qua:
- `subscriber_type = 'INDIVIDUAL'` hoặc `'GROUP'`
- `source_type = 'RETAIL_PURCHASE'` hoặc `'GROUP_ASSIGNMENT'`

---

## 5. Cấu hình Backend

### 5.1 Service chính cần nắm

| File | Chức năng |
|------|-----------|
| `IndividualPlanConfigService.java` | Toàn bộ nghiệp vụ khách hàng phổ thông |
| `GroupService.java` | Tạo/cập nhật/khóa đại lý |
| `GroupPlanAssignmentService.java` | Tạo và quản lý gán gói cước cho đại lý |
| `GroupProvisioningService.java` | Tạo đại lý + gói cước trong một transaction |
| `GroupMemberService.java` | Quản lý thành viên trong đại lý |
| `GroupContactService.java` | Quản lý liên hệ của đại lý |
| `MultiLevelApprovalService.java` | Orchestrator luồng phê duyệt đa cấp |
| `DataScopeService.java` | Lọc dữ liệu theo phạm vi quyền của user |

### 5.2 Controller & Endpoint mapping

| Controller | URL Prefix | Chức năng |
|------------|-----------|-----------|
| `IndividualPlanConfigController.java` | `/api/v1/individual/plan-configs` | Toàn bộ nghiệp vụ phổ thông |
| `GroupController.java` | `/api/v1/groups` | CRUD đại lý |
| `GroupCommercialController.java` | `/api/v1/groups` | Gán gói, review, add-plan |
| `ApprovalRequestController.java` | `/api/v1/approval-requests` | Phê duyệt đa cấp |

### 5.3 Enum quan trọng — file `CommercialEnums.java`

```java
// Phân khúc khách hàng
enum CustomerSegment { GROUP, INDIVIDUAL }

// Trạng thái gói cá nhân
enum ScheduleStatus { AVAILABLE, REQUESTED, APPROVED, ACTIVE, INACTIVE }

// Trạng thái gán gói đại lý
enum AssignmentStatus { REQUESTED, APPROVED, ACTIVE, REJECTED, STOPPED, EXPIRED }

// Hành động audit
enum AuditAction { REQUEST, APPROVE, REJECT, ACTIVATE, STOP, EXPIRE }

// Loại audit
enum AssignmentType { RETAIL_PLAN, GROUP_PLAN }

// Phạm vi template
enum TemplateScope { PUBLIC, PARTNER_PRIVATE, SYSTEM }

// Loại liên hệ đại lý
enum ContactType { CONTRACT, BILLING, SUPPORT, PIC }

// Vai trò thành viên trong đại lý
enum MemberRole { OPERATOR, MEMBER }
```

### 5.4 Điều chỉnh logic nghiệp vụ

**Thay đổi ngưỡng giá trị để tính số cấp phê duyệt:**
→ Cập nhật bảng `approval_level_configs` trong DB (không cần sửa code)

**Thay đổi trạng thái hiển thị trên UI:**
→ Sửa method `mapScheduleStatus()` trong `IndividualPlanConfigService.java`

**Thêm hành động audit mới:**
→ Thêm giá trị vào enum `AuditAction` trong `CommercialEnums.java`

**Thêm loại liên hệ mới:**
→ Thêm giá trị vào enum `ContactType` trong `CommercialEnums.java`

**Thay đổi logic tự động kích hoạt/hết hạn:**
→ Sửa Scheduler service (class có `@Scheduled` annotation) — tìm các method `findApprovedReadyToActivate()` và `findActiveReadyToExpire()` trong repository tương ứng

---

## 6. Cấu hình Frontend

### 6.1 API calls

| File | Chức năng |
|------|-----------|
| `frontend/src/api/individual.ts` (hoặc `individualPlanConfig.ts`) | API calls cho khách hàng phổ thông |
| `frontend/src/api/groups.ts` | API calls cho đại lý |
| `frontend/src/api/approvals.ts` | API calls phê duyệt |
| `frontend/src/api/plans.ts` | API calls template gói cước |

### 6.2 Views chính

**Individual:**

| File | Chức năng |
|------|-----------|
| `frontend/src/views/individual/PlanConfigList.vue` (hoặc tương tự) | Danh sách gói phổ thông |
| `frontend/src/views/individual/PlanConfigDetail.vue` | Chi tiết + các nút hành động (request-apply, stop, deactivate) |

**Group:**

| File | Chức năng |
|------|-----------|
| `frontend/src/views/plans/AddPlan.vue` | Tạo đại lý mới / thêm gói cho đại lý |
| `frontend/src/views/plans/` | Danh sách đại lý, chi tiết đại lý |

### 6.3 Router

File: `frontend/src/router/index.ts`

**Thêm page mới cho Individual:**
```typescript
{
  path: '/individual-plan-config',
  component: PlanConfigList,
  meta: { permission: 'plan:view' }
}
```

**Thêm page mới cho Group:**
```typescript
{
  path: '/groups',
  component: GroupList,
  meta: { permission: 'group:view' }
}
```

### 6.4 Navigation Menu

File: `frontend/src/common/nav.ts`

Mỗi menu item có `permissionKey` để ẩn/hiện theo quyền của user. Sửa tại đây để:
- Thêm menu item mới
- Thay đổi quyền hiển thị menu
- Thay đổi đường dẫn route

---

## 7. API Endpoints

### Khách hàng Phổ thông

| Method | URL | Permission | Mô tả |
|--------|-----|-----------|-------|
| GET | `/api/v1/individual/plan-configs` | `plan:view` | Danh sách gói phổ thông |
| POST | `/api/v1/individual/plan-configs` | `plan:create` | Tạo gói mới |
| GET | `/api/v1/individual/plan-configs/{id}` | `plan:view` | Chi tiết gói |
| POST | `/api/v1/individual/plan-configs/{id}/request-apply` | `plan:update` | Yêu cầu áp dụng |
| POST | `/api/v1/individual/plan-configs/{id}/approve` | `plan:update` | Phê duyệt trực tiếp |
| POST | `/api/v1/individual/plan-configs/{id}/reject` | `plan:update` | Từ chối |
| POST | `/api/v1/individual/plan-configs/{id}/stop` | `plan:update` | Dừng lịch áp dụng |
| POST | `/api/v1/individual/plan-configs/{id}/deactivate` | `plan:update` | Hủy kích hoạt cả template |

### Khách hàng Đại lý

| Method | URL | Permission | Mô tả |
|--------|-----|-----------|-------|
| GET | `/api/v1/groups` | `group:view` | Danh sách đại lý |
| POST | `/api/v1/groups` | `group:create` | Tạo đại lý mới |
| GET | `/api/v1/groups/{id}` | `group:view` | Chi tiết đại lý |
| PUT | `/api/v1/groups/{id}` | `group:update` | Cập nhật thông tin đại lý |
| PATCH | `/api/v1/groups/{id}/suspend` | `group:update` | Tạm khóa đại lý |
| PATCH | `/api/v1/groups/{id}/activate` | `group:update` | Kích hoạt lại đại lý |
| POST | `/api/v1/groups/{groupId}/contacts` | `group:create` | Thêm liên hệ |
| GET | `/api/v1/groups/{groupId}/plan-assignments` | `group:view` | Danh sách gói đã gán |
| POST | `/api/v1/groups/{groupId}/plan-assignments` | `group:create` | Gán gói cước |
| POST | `/api/v1/groups/{groupId}/add-plan` | `group:update` | Tạo + gán gói mới |
| POST | `/api/v1/groups/provision` | `group:create` | Tạo đại lý + gói cước (one-shot) |
| POST | `/api/v1/groups/plan-assignments/{id}/review` | `group:update` | Review/activate/stop gói |
| GET | `/api/v1/groups/{groupId}/plan-history` | `group:view` | Lịch sử gói cước |

---

## 8. Phân quyền

### Khách hàng Phổ thông

| Permission Key | Mô tả |
|---------------|-------|
| `plan:view` | Xem danh sách và chi tiết gói phổ thông |
| `plan:create` | Tạo gói mới |
| `plan:update` | Request apply, approve, reject, stop, deactivate |

### Khách hàng Đại lý

| Permission Key | Mô tả |
|---------------|-------|
| `group:view` | Xem danh sách đại lý (có data scope) |
| `group:view:own` | Chỉ xem đại lý của mình |
| `group:view:subordinates` | Xem đại lý của mình + cấp dưới |
| `group:create` | Tạo đại lý, thêm liên hệ, gán gói cước |
| `group:update` | Cập nhật, suspend/activate, review gói cước |

### Phê duyệt (dùng chung)

| Permission Key | Mô tả |
|---------------|-------|
| `approval:view` | Xem danh sách approval requests |
| `approval:level1` | Phê duyệt cấp 1 (Trưởng phòng) |
| `approval:level2` | Phê duyệt cấp 2 (Giám đốc) |
| `approval:level3` | Phê duyệt cấp 3 (CFO) |

---

## Checklist nâng cấp thường gặp

### Thêm loại liên hệ mới cho đại lý

- [ ] Thêm giá trị mới vào enum `ContactType` trong `CommercialEnums.java`
- [ ] Cập nhật UI form thêm liên hệ trong frontend

### Thêm trạng thái mới cho gói phổ thông

- [ ] Thêm vào enum `ScheduleStatus` trong `CommercialEnums.java`
- [ ] Xử lý trong `IndividualPlanConfigService`
- [ ] Cập nhật method `mapScheduleStatus()` cho UI
- [ ] Cập nhật migration DB để `schedule_status` ENUM có giá trị mới

### Thêm trạng thái mới cho gói đại lý

- [ ] Thêm vào enum `AssignmentStatus` trong `CommercialEnums.java`
- [ ] Xử lý trong `GroupPlanAssignmentService.review()`
- [ ] Cập nhật migration DB

### Thêm hành động audit mới

- [ ] Thêm vào enum `AuditAction` trong `CommercialEnums.java`
- [ ] Gọi `assignmentAuditRepository.save(new AssignmentAudit(...))` tại điểm nghiệp vụ mới

### Thêm role mới cho thành viên đại lý

- [ ] Thêm vào enum `MemberRole` trong `CommercialEnums.java`
- [ ] Xử lý trong `GroupMemberService` các quyền mà role mới có
