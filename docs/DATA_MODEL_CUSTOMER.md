# Mô Hình Dữ Liệu — Khách Hàng Đại Lý & Khách Hàng Phổ Thông

## 1. Sơ đồ quan hệ tổng thể

```
┌─────────────────────────────────────────────────────────────────────────────────┐
│                         KHÁCH HÀNG ĐẠI LÝ (GROUP)                              │
│                                                                                  │
│  ┌──────────────┐   1    ┌─────────────────┐   N   ┌──────────────────────────┐ │
│  │    groups    │───────►│  group_contacts  │       │    partner_group_access   │ │
│  └──────┬───────┘        └─────────────────┘       │  (partner ↔ group)       │ │
│         │ 1                                         └──────────────────────────┘ │
│         │                                                      ▲                 │
│         ├──────────────────────────────────────────────────────┘ N               │
│         │ N                                                                       │
│  ┌──────▼───────┐                                                                │
│  │ group_members│                                                                │
│  └──────────────┘                                                                │
│         │                                                                        │
│         │ (source_assignment_id)                                                 │
│         │                                                                        │
│  ┌──────▼──────────────┐  N    ┌───────────────┐  1   ┌──────────────────────┐  │
│  │ group_plan_          │──────►│  plan_templates│─────►│  plan_pricing_rules  │  │
│  │   assignments        │      └───────────────┘      └──────────────────────┘  │
│  └──────┬──────────────┘                │                                        │
│         │ 1                             │                                        │
└─────────┼─────────────────────────────────────────────────────────────────────┘
          │                               │
          │          ┌────────────────────┘
          │          │          ┌──────────────────────────────────────────────┐
          │          │          │           KHÁCH HÀNG PHỔ THÔNG               │
          │          │          │                                               │
          │          │          │  ┌──────────────────────┐                    │
          │          │          │  │ retail_plan_schedules │                    │
          │          │          │  └──────────┬───────────┘                    │
          │          │          │             │ 1                               │
          │          │          └─────────────┼───────────────────────────────┘
          │          │                        │
          ▼          ▼                        ▼
     ┌────────────────────────────────────────────────┐
     │                  subscriptions                  │
     │   subscriber_type: GROUP | INDIVIDUAL           │
     └──────────────────┬─────────────────────────────┘
                        │ 1
                        │ N
          ┌─────────────▼──────────────────┐
          │  certificate_provisioning_      │
          │         records                 │
          └────────────────────────────────┘

     ┌───────────────────────┐
     │  settlement_statements│◄──── groups (1:N)
     └───────────────────────┘

     ┌───────────────────────┐
     │   assignment_audits   │◄──── group_plan_assignments (1:N)
     └───────────────────────┘      retail_plan_schedules (1:N)
```

---

## 2. Sơ đồ chi tiết từng nhóm

### 2.1 Nhóm bảng đại lý

```
                        ┌─────────────────────────────┐
                        │           groups             │
                        ├─────────────────────────────┤
                        │ PK group_id                  │
                        │    group_code (UNIQUE)        │
                        │    group_name                 │
                        │    username (UNIQUE)          │
                        │    password                   │
                        │    ref_contract_no            │
                        │    status                     │
                        │    owner_user_id              │
                        │    created_by                 │
                        └──────┬──────────────┬────────┘
                               │              │
              ┌────────────────┘              └──────────────────┐
              │ 1:N                                         1:N   │
              ▼                                                    ▼
┌─────────────────────────┐              ┌──────────────────────────────┐
│      group_contacts      │              │       group_members           │
├─────────────────────────┤              ├──────────────────────────────┤
│ PK group_contact_id      │              │ PK id                         │
│ FK group_id              │              │ FK group_id                   │
│    contact_type          │              │    user_id                    │
│    email                 │              │    role                       │
│    full_name             │              │    joined_at                  │
│    phone                 │              │    added_by                   │
│    is_primary            │              │    member_start_date          │
│    receive_usage_alert   │              │    member_end_date            │
│    is_active             │              │ FK source_assignment_id ──────┼──┐
└─────────────────────────┘              └──────────────────────────────┘  │
                                                                             │
              ┌──────────────────────────────────────────────────────────┐  │
              │               partner_group_access                        │  │
              ├──────────────────────────────────────────────────────────┤  │
              │ PK  id                                                     │  │
              │ FK  partner_user_id → user_accounts                       │  │
              │ FK  group_id        → groups                              │  │
              │     granted_by                                             │  │
              │     granted_at                                             │  │
              │     revoked_at   (NULL = còn hiệu lực)                    │  │
              │     revoked_by                                             │  │
              └──────────────────────────────────────────────────────────┘  │
                                                                             │
              ┌──────────────────────────────────────────────────────────┐  │
              │              group_plan_assignments                        │◄─┘
              ├──────────────────────────────────────────────────────────┤
              │ PK  group_plan_assignment_id                               │
              │ FK  group_id         → groups                             │
              │ FK  plan_template_id → plan_templates                     │
              │     assignment_status                                      │
              │     requested_by / requested_at                            │
              │     approved_by  / approved_at                             │
              │     rejected_by  / rejected_at                             │
              │     apply_from   / apply_to                               │
              │     activated_at / stopped_at / stop_reason               │
              └──────────────────────┬───────────────────────────────────┘
                                     │ 1:N
                                     ▼
              ┌──────────────────────────────────────────────────────────┐
              │                  assignment_audits                         │
              ├──────────────────────────────────────────────────────────┤
              │ PK  assignment_audit_id                                    │
              │ FK  group_plan_assignment_id                               │
              │ FK  retail_plan_schedule_id                               │
              │     assignment_type  (GROUP / RETAIL)                     │
              │     action           (CREATED/APPROVED/REJECTED/...)      │
              │     old_status / new_status                               │
              │     actor / note                                           │
              └──────────────────────────────────────────────────────────┘
```

---

### 2.2 Nhóm bảng gói dịch vụ

```
┌──────────────────────────────────────────────────────────┐
│                     plan_templates                        │
├──────────────────────────────────────────────────────────┤
│ PK  plan_template_id                                      │
│     plan_code            (UNIQUE)                         │
│     plan_name                                             │
│     description                                           │
│     customer_segment     INDIVIDUAL | GROUP               │
│     template_scope       PUBLIC | PRIVATE                 │
│     status               DRAFT→ACTIVE→INACTIVE/DEPRECATED│
│     effective_from / effective_to                         │
│     allow_bulk_signing / allow_api_access                 │
│ FK  cloned_from_template_id → plan_templates (self)       │
│     version_no                                            │
└──────────────┬───────────────────────────────────────────┘
               │
       ┌───────┴───────┐
       │ 1:N           │ 1:N
       ▼               ▼
┌──────────────┐  ┌───────────────────────────┐
│plan_pricing_ │  │   retail_plan_schedules    │
│   rules      │  ├───────────────────────────┤
├──────────────┤  │ PK retail_plan_schedule_id │
│ PK id        │  │ FK plan_template_id        │
│ FK plan_     │  │    schedule_status         │
│  template_id │  │    apply_from / apply_to   │
│ subject_type │  │    requested_by            │
│ cert_validity│  │    approved_by             │
│  _value/unit │  └─────────────┬─────────────┘
│ pricing_     │                │ 1:N
│  metric      │                │ (audit)
│ range_min    │                ▼
│ range_max    │  ┌───────────────────────────┐
│ unit_price   │  │     assignment_audits      │
│ currency     │  │  (retail_plan_schedule_id) │
│ quota_total  │  └───────────────────────────┘
│ is_active    │
└──────────────┘
```

---

### 2.3 Bảng `subscriptions` — trung tâm nối hai luồng

```
┌──────────────────────────────┐        ┌──────────────────────────────┐
│    group_plan_assignments     │        │    retail_plan_schedules      │
│    (GROUP flow)               │        │    (INDIVIDUAL flow)          │
└──────────────┬───────────────┘        └──────────────┬───────────────┘
               │ 1                                      │ 1
               │                                        │
               ▼ N                                      ▼ N
┌─────────────────────────────────────────────────────────────────────────┐
│                           subscriptions                                  │
├─────────────────────────────────────────────────────────────────────────┤
│ PK  subscription_id                                                      │
│     subscriber_type          ◄── GROUP hoặc INDIVIDUAL                  │
│     user_id                  ◄── có giá trị khi INDIVIDUAL              │
│ FK  group_id                 ◄── có giá trị khi GROUP                   │
│ FK  plan_template_id         → plan_templates                            │
│ FK  pricing_rule_id          → plan_pricing_rules                        │
│ FK  group_plan_assignment_id → group_plan_assignments  (GROUP)           │
│ FK  retail_plan_schedule_id  → retail_plan_schedules   (INDIVIDUAL)      │
│     source_type              MANUAL | API | BULK                         │
│     status                   PENDING→ACTIVE→EXPIRED/REVOKED/SUSPENDED   │
│     start_date / end_date                                                │
│     signing_quota_total / signing_quota_used                             │
│     payment_reference                                                    │
└──────────────────────────────────────┬──────────────────────────────────┘
                                       │ 1:N
                                       ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                  certificate_provisioning_records                         │
├─────────────────────────────────────────────────────────────────────────┤
│ PK  id                                                                   │
│ FK  subscription_id          → subscriptions                             │
│ FK  group_plan_assignment_id → group_plan_assignments  (trace về đại lý) │
│ FK  pricing_rule_id          → plan_pricing_rules                        │
│     user_id                  (người nhận chứng thư)                      │
│     request_id               (UNIQUE, tracking ID)                       │
│     status                   PENDING | ISSUED | FAILED | EXPIRED         │
│     cert_type                1=Cá nhân, 2=Cá nhân/tổ chức, 3=Tổ chức   │
│     certificate_id           (ID từ CA)                                  │
│     issued_at / expires_at                                               │
│     retry_count / usage_count / failure_reason                           │
└─────────────────────────────────────────────────────────────────────────┘
```

---

### 2.4 Bảng thanh toán đại lý

```
┌──────────────────────────────────────────────────────────┐
│                    settlement_statements                   │
├──────────────────────────────────────────────────────────┤
│ PK  settlement_statement_id                               │
│ FK  group_id  → groups          (1 group : N statements) │
│     from_date / to_date         (kỳ sao kê)              │
│     status    DRAFT→FINALIZED→PAID / OVERDUE              │
│     total_certificates                                     │
│     total_signings                                        │
│     total_amount / currency                               │
│     generated_at / generated_by                           │
└──────────────────────────────────────────────────────────┘
```

---

## 3. Luồng dữ liệu

### 3.1 Khách hàng đại lý (GROUP)

```
Tạo Group
    │
    ├──► INSERT groups
    └──► INSERT group_contacts
              │
              ▼
    INSERT group_members (user_id, role)
              │
              ▼
    INSERT group_plan_assignments     ──► INSERT assignment_audits
         status = REQUESTED                  action = CREATED
              │
              ▼ (phê duyệt)
    UPDATE group_plan_assignments     ──► INSERT assignment_audits
         status = APPROVED                   action = APPROVED
              │
              ▼ (kích hoạt, scheduler hoặc thủ công)
    UPDATE group_plan_assignments     ──► INSERT assignment_audits
         status = ACTIVE                     action = ACTIVATED
              │
              ▼
    INSERT subscriptions
         subscriber_type = GROUP
         group_id, group_plan_assignment_id
              │
              ▼
    INSERT certificate_provisioning_records (mỗi user trong group)
         status = PENDING → ISSUED
              │
              ▼ (hết kỳ)
    UPDATE group_plan_assignments  status = EXPIRED
    UPDATE subscriptions           status = EXPIRED
              │
              ▼
    INSERT settlement_statements (DRAFT → FINALIZED → PAID)
```

---

### 3.2 Khách hàng phổ thông (INDIVIDUAL)

```
Chọn gói
    │
    SELECT plan_templates  (customer_segment = INDIVIDUAL, status = ACTIVE)
    JOIN   retail_plan_schedules (schedule_status = ACTIVE, ngày hiện tại trong apply_from..apply_to)
              │
              ▼
    INSERT subscriptions
         subscriber_type = INDIVIDUAL
         user_id, retail_plan_schedule_id, pricing_rule_id
         status = PENDING
              │
              ▼ (xác nhận thanh toán)
    UPDATE subscriptions  status = ACTIVE, start_date, end_date
              │
              ▼
    INSERT certificate_provisioning_records
         status = PENDING → ISSUED
              │
              ▼ (hết hạn)
    UPDATE subscriptions               status = EXPIRED
    UPDATE certificate_provisioning_records  status = EXPIRED
```

---

### 3.3 Cấp quyền partner xem group

```
Tạo UserAccount (ROLE_PARTNER)
              │
              ▼
    INSERT partner_group_access
         partner_user_id, group_id
         granted_at = NOW(), revoked_at = NULL
              │
              ▼ (partner query dữ liệu)
    SELECT groups g
    INNER JOIN partner_group_access pga
           ON pga.group_id = g.group_id
          AND pga.partner_user_id = :partnerId
          AND pga.revoked_at IS NULL
              │
              ▼ (thu hồi quyền)
    UPDATE partner_group_access
         SET revoked_at = NOW(), revoked_by = :admin
```

---

## 4. Vòng đời trạng thái

### `group_plan_assignments`
```
REQUESTED ──► APPROVED ──► ACTIVE ──► STOPPED
    │                          └─────► EXPIRED
    └──► REJECTED
```

### `subscriptions`
```
PENDING ──► ACTIVE ──► EXPIRED
               ├──────► REVOKED
               └──────► SUSPENDED
```

### `certificate_provisioning_records`
```
PENDING ──► ISSUED ──► EXPIRED
    └──────► FAILED (retry_count tăng dần)
```

### `settlement_statements`
```
DRAFT ──► FINALIZED ──► PAID
                   └──► OVERDUE
```

---

## 5. Bảng tóm tắt quan hệ

| Bảng | Quan hệ với |
|------|-------------|
| `groups` | `group_contacts` (1:N), `group_members` (1:N), `group_plan_assignments` (1:N), `partner_group_access` (1:N), `settlement_statements` (1:N) |
| `group_plan_assignments` | `groups` (N:1), `plan_templates` (N:1), `subscriptions` (1:N), `assignment_audits` (1:N), `group_members.source_assignment_id` (1:N) |
| `plan_templates` | `plan_pricing_rules` (1:N), `retail_plan_schedules` (1:N), `group_plan_assignments` (1:N), `subscriptions` (1:N) |
| `retail_plan_schedules` | `plan_templates` (N:1), `subscriptions` (1:N), `assignment_audits` (1:N) |
| `subscriptions` | `groups` (N:1), `plan_templates` (N:1), `plan_pricing_rules` (N:1), `group_plan_assignments` (N:1), `retail_plan_schedules` (N:1), `certificate_provisioning_records` (1:N) |
| `certificate_provisioning_records` | `subscriptions` (N:1), `group_plan_assignments` (N:1), `plan_pricing_rules` (N:1) |
| `partner_group_access` | `groups` (N:1), `user_accounts` (N:1) |
| `settlement_statements` | `groups` (N:1) |
| `assignment_audits` | `group_plan_assignments` (N:1), `retail_plan_schedules` (N:1) |
