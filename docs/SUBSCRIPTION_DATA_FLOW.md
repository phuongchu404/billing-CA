# Luồng Dữ Liệu Subscription — Khách Hàng Phổ Thông & Đại Lý

> **Dự án:** billing-CA  
> **Cập nhật:** 2026-05-20  
> **Phạm vi:** Mô tả toàn bộ các bảng liên quan đến hai loại khách hàng, mối quan hệ giữa các bảng, luồng nghiệp vụ tạo subscription, và dữ liệu đi đâu sau khi subscription được tạo.

---

## Mục lục

1. [Tổng quan kiến trúc dữ liệu](#1-tổng-quan-kiến-trúc-dữ-liệu)
2. [Mô tả chi tiết từng bảng](#2-mô-tả-chi-tiết-từng-bảng)
3. [Mối quan hệ giữa các bảng](#3-mối-quan-hệ-giữa-các-bảng)
4. [Luồng nghiệp vụ — Khách hàng Phổ thông (INDIVIDUAL)](#4-luồng-nghiệp-vụ--khách-hàng-phổ-thông-individual)
5. [Luồng nghiệp vụ — Khách hàng Đại lý (GROUP)](#5-luồng-nghiệp-vụ--khách-hàng-đại-lý-group)
6. [Dữ liệu đi đâu sau khi Subscription được tạo](#6-dữ-liệu-đi-đâu-sau-khi-subscription-được-tạo)
7. [Vòng đời trạng thái](#7-vòng-đời-trạng-thái)
8. [Bảng tóm tắt nghiệp vụ vs bảng DB](#8-bảng-tóm-tắt-nghiệp-vụ-vs-bảng-db)

---

## 1. Tổng quan kiến trúc dữ liệu

Hệ thống quản lý hai loại khách hàng với luồng riêng biệt hội tụ tại bảng trung tâm `subscriptions`:

```
══════════════════════════════════════════════════════════════════════════════
                        KHÁCH HÀNG PHỔ THÔNG (INDIVIDUAL)
══════════════════════════════════════════════════════════════════════════════

  [plan_templates]──────────[plan_pricing_rules]
        │  customer_segment=INDIVIDUAL
        │
  [retail_plan_schedules]
        │  (lịch áp dụng gói, phải được duyệt)
        │
        ▼
  [approval_requests] ──── [approval_request_steps]
        │  (luồng duyệt đa cấp 1-3 level)
        │
        ▼ (sau khi APPROVED + ACTIVE)
  ╔══════════════════╗
  ║  subscriptions   ║   subscriber_type = INDIVIDUAL
  ║  user_id = ?     ║   (ID khách hàng từ RS Core — không có FK nội bộ)
  ╚══════════════════╝

══════════════════════════════════════════════════════════════════════════════
                        KHÁCH HÀNG ĐẠI LÝ (GROUP)
══════════════════════════════════════════════════════════════════════════════

  [groups] ──── [group_contacts]
     │      └── [group_members]
     │
  [plan_templates]──────────[plan_pricing_rules]
     │  customer_segment=GROUP
     │
  [group_plan_assignments]
     │  (gói gán cho đại lý, phải được duyệt)
     │
     ▼
  [approval_requests] ──── [approval_request_steps]
     │  (luồng duyệt đa cấp 1-3 level)
     │
     ▼ (sau khi APPROVED + ACTIVE)
  ╔══════════════════╗
  ║  subscriptions   ║   subscriber_type = GROUP
  ║  group_id = ?    ║   (FK trỏ về bảng groups)
  ╚══════════════════╝

══════════════════════════════════════════════════════════════════════════════
                   SAU KHI SUBSCRIPTION ĐƯỢC TẠO (cả hai loại)
══════════════════════════════════════════════════════════════════════════════

  [subscriptions]
        │
        ├──► [certificate_provisioning_records]   (cấp phát chứng thư số)
        │          │
        │          └──► [certificate_usage_records]  (ghi nhận lần ký/sử dụng)
        │
        ├──► [payment_records]                    (ghi nhận thanh toán)
        │
        └──► [usage_aggregates]                   (tổng hợp usage theo kỳ)
                   │
              (chỉ GROUP)
                   └──► [settlement_statements]   (sao kê thanh toán đại lý)
                              │
                              └──► [payment_records] (thanh toán settlement)
```

---

## 2. Mô tả chi tiết từng bảng

### 2.1 Nhóm bảng định nghĩa gói cước

#### `plan_templates` — Mẫu gói cước

Lưu trữ định nghĩa các gói dịch vụ. Mỗi gói áp dụng cho một loại khách hàng cụ thể.

| Cột | Kiểu | Mô tả |
|-----|------|-------|
| `plan_template_id` | BIGINT PK | Khóa chính |
| `plan_code` | VARCHAR UNIQUE | Mã gói, ví dụ: `PLN_INDIVIDUAL_001` |
| `plan_name` | VARCHAR | Tên gói hiển thị |
| `customer_segment` | ENUM | `INDIVIDUAL` hoặc `GROUP` — phân biệt loại KH |
| `template_scope` | ENUM | `PUBLIC` (hiển thị), `PARTNER_PRIVATE`, `SYSTEM` |
| `status` | ENUM | `DRAFT` → `AVAILABLE` → `INACTIVE` → `ARCHIVED` |
| `effective_from` / `effective_to` | DATE | Khoảng thời gian gói hợp lệ |
| `allow_bulk_signing` | BOOL | Cho phép ký hàng loạt |
| `allow_api_access` | BOOL | Cho phép truy cập qua API |
| `cloned_from_template_id` | FK self | Nếu gói này được clone từ gói khác |
| `version_no` | INT | Phiên bản gói |

**Quan hệ:**
- 1 template → N `plan_pricing_rules` (các mức giá khác nhau)
- 1 template → N `retail_plan_schedules` (lịch áp dụng cho KH phổ thông)
- 1 template → N `group_plan_assignments` (gán cho đại lý)
- 1 template → N `subscriptions` (subscription thực tế)

---

#### `plan_pricing_rules` — Quy tắc giá

Một template có nhiều rule giá, phân theo bậc số lượng hoặc loại chứng thư. **Quan trọng:** rule này được tham chiếu bởi `subscriptions` để tính tiền lúc settlement — không chỉ là audit.

| Cột | Kiểu | Mô tả |
|-----|------|-------|
| `plan_pricing_rule_id` | BIGINT PK | Khóa chính |
| `plan_template_id` | FK | Thuộc template nào |
| `subject_type` | ENUM | `INDIVIDUAL`, `ORGANIZATION`, `INDIVIDUAL_OF_ORG` |
| `certificate_validity_value` | INT | Thời hạn chứng thư (số) |
| `certificate_validity_unit` | ENUM | `DAY`, `MONTH`, `YEAR` |
| `pricing_metric` | ENUM | `CERTIFICATE_COUNT` (theo số chứng thư) hoặc `SIGNING_COUNT` (theo số lần ký) |
| `range_min` / `range_max` | INT | Bậc số lượng tối thiểu / tối đa |
| `unit_price` | DECIMAL | Đơn giá |
| `currency` | CHAR(3) | Tiền tệ, mặc định `VND` |
| `quota_total` | INT | Tổng hạn mức (null = không giới hạn) |
| `sort_order` | INT | Thứ tự hiển thị |
| `is_active` | BOOL | Rule có đang hiệu lực không |

> **Lưu ý thiết kế:** `unit_price` và `currency` **không được copy** vào `subscriptions` — lúc tính settlement, hệ thống đọc trực tiếp từ bảng này qua FK `pricing_rule_id`. Điều này có rủi ro nếu rule bị sửa sau khi subscription tồn tại.

---

### 2.2 Nhóm bảng khách hàng phổ thông

#### `retail_plan_schedules` — Lịch áp dụng gói phổ thông

Mỗi `plan_template` có thể có nhiều lịch áp dụng theo thời gian khác nhau. Lịch phải qua luồng duyệt trước khi ACTIVE.

| Cột | Kiểu | Mô tả |
|-----|------|-------|
| `retail_plan_schedule_id` | BIGINT PK | Khóa chính |
| `plan_template_id` | FK | Template gói tương ứng |
| `schedule_status` | ENUM | `AVAILABLE` → `REQUESTED` → `APPROVED` → `ACTIVE` → `INACTIVE` |
| `apply_from` / `apply_to` | DATE | Khoảng thời gian áp dụng |
| `requested_by` / `requested_at` | VARCHAR / DATETIME | Ai yêu cầu và khi nào |
| `approved_by` / `approved_at` | VARCHAR / DATETIME | Ai duyệt và khi nào |

**Quan hệ:**
- N:1 với `plan_templates`
- 1:N với `subscriptions` (mỗi khách hàng đăng ký → 1 subscription liên kết lịch này)
- 1:N với `assignment_audits` (lịch sử thay đổi trạng thái)

---

### 2.3 Nhóm bảng khách hàng đại lý

#### `groups` — Đại lý

Thông tin tổ chức của đại lý (khách hàng doanh nghiệp).

| Cột | Kiểu | Mô tả |
|-----|------|-------|
| `group_id` | BIGINT PK | Khóa chính |
| `group_code` | VARCHAR UNIQUE | Mã đại lý duy nhất |
| `group_name` | VARCHAR | Tên đại lý |
| `username` / `password` | VARCHAR | Tài khoản đăng nhập của đại lý vào hệ thống |
| `ref_contract_no` | VARCHAR | Số hợp đồng tham chiếu |
| `status` | ENUM | `ACTIVE` hoặc `INACTIVE` |
| `owner_user_id` | FK → `user_accounts` | Nhân viên kinh doanh phụ trách |

**Quan hệ:**
- 1:N với `group_contacts`
- 1:N với `group_members`
- 1:N với `group_plan_assignments`
- 1:N với `settlement_statements`
- 1:N với `subscriptions` (qua group_id)

---

#### `group_contacts` — Đầu mối liên hệ đại lý

Lưu các đầu mối liên hệ của đại lý theo từng mục đích: hợp đồng, thanh toán, kỹ thuật, phụ trách chính.

| Cột | Kiểu | Mô tả |
|-----|------|-------|
| `group_contact_id` | BIGINT PK | Khóa chính |
| `group_id` | FK → `groups` | Đại lý |
| `contact_type` | ENUM | `CONTRACT`, `BILLING`, `SUPPORT`, `PIC` |
| `email` | VARCHAR | Email |
| `full_name` | VARCHAR | Họ tên |
| `phone` | VARCHAR | Số điện thoại |
| `is_primary` | BOOL | Liên hệ chính trong loại này |
| `receive_usage_alert` | BOOL | Nhận cảnh báo khi gần hết quota |
| `is_active` | BOOL | Còn hiệu lực không |

---

#### `group_members` — Thành viên đại lý

Danh sách người dùng (user_id từ hệ thống ngoài) thuộc đại lý và vai trò của họ.

| Cột | Kiểu | Mô tả |
|-----|------|-------|
| `id` | BIGINT PK | Khóa chính |
| `group_id` | FK → `groups` | Đại lý |
| `user_id` | BIGINT (không FK) | ID người dùng từ hệ thống RS Core |
| `role` | ENUM | `OPERATOR` (quản trị viên đại lý) hoặc `MEMBER` |
| `joined_at` | DATETIME | Thời điểm tham gia |
| `added_by` | VARCHAR | Ai thêm thành viên này |
| `member_start_date` / `member_end_date` | DATE | Khoảng thời gian là thành viên |
| `source_assignment_id` | FK → `group_plan_assignments` | Thành viên được thêm từ gói nào |

---

#### `group_plan_assignments` — Gói gán cho đại lý

Mỗi bản ghi là một lần gán gói cước cho một đại lý, có khoảng thời gian áp dụng. Phải qua luồng duyệt trước khi ACTIVE.

| Cột | Kiểu | Mô tả |
|-----|------|-------|
| `group_plan_assignment_id` | BIGINT PK | Khóa chính |
| `group_id` | FK → `groups` | Đại lý |
| `plan_template_id` | FK → `plan_templates` | Gói cước áp dụng |
| `assignment_status` | ENUM | `REQUESTED` → `APPROVED` → `ACTIVE` → `STOPPED` / `EXPIRED` |
| `requested_by` / `requested_at` | — | Ai tạo yêu cầu |
| `approved_by` / `approved_at` | — | Ai duyệt |
| `rejected_by` / `rejected_at` | — | Ai từ chối (nếu có) |
| `apply_from` / `apply_to` | DATE | Khoảng thời gian hiệu lực |
| `activated_at` | DATETIME | Thời điểm kích hoạt |
| `stopped_at` / `stop_reason` | — | Dừng thủ công kèm lý do |

**Quan hệ:**
- N:1 với `groups` và `plan_templates`
- 1:N với `assignment_audits`
- 1:N với `subscriptions`
- 1:N với `certificate_provisioning_records`

---

### 2.4 Bảng duyệt chung (dùng cho cả hai loại KH)

#### `approval_requests` — Yêu cầu phê duyệt

Mỗi lần tạo lịch áp dụng (retail) hoặc gán gói (group) đều sinh ra 1 approval request và phải đi qua đây trước khi thực thi.

| Cột | Kiểu | Mô tả |
|-----|------|-------|
| `id` | BIGINT PK | Khóa chính |
| `request_type` | VARCHAR | Loại yêu cầu: `REQUEST_GROUP_PLAN_ASSIGNMENT`, `REQUEST_RETAIL_PLAN_SCHEDULE`, ... |
| `status` | VARCHAR | `DRAFT` → `IN_APPROVAL` → `APPROVED` / `REJECTED` / `NEED_REVISION` |
| `customer_segment` | VARCHAR | `INDIVIDUAL` hoặc `GROUP` |
| `requested_by` | VARCHAR | Người tạo yêu cầu |
| `entity_type` | VARCHAR | `RETAIL_PLAN_SCHEDULE` hoặc `GROUP_PLAN_ASSIGNMENT` |
| `entity_id` | VARCHAR | ID của schedule hoặc assignment tương ứng |
| `request_payload` | TEXT | Snapshot dữ liệu lúc tạo yêu cầu (JSON) |
| `contract_value` | DECIMAL | Giá trị hợp đồng — dùng để tính số cấp phải duyệt |
| `total_levels` | INT | Tổng số cấp duyệt (1, 2 hoặc 3) |
| `current_level` | INT | Đang ở cấp duyệt nào |

**Logic số cấp duyệt** được tra từ bảng `approval_level_configs` theo `customer_segment` và ngưỡng `contract_value`.

---

#### `approval_request_steps` — Từng bước duyệt

Mỗi approval request có 1-3 steps tương ứng 1-3 cấp duyệt.

| Cột | Kiểu | Mô tả |
|-----|------|-------|
| `id` | BIGINT PK | Khóa chính |
| `approval_request_id` | FK | Thuộc request nào |
| `step_level` | INT | Cấp duyệt: 1, 2 hoặc 3 |
| `required_approval_level` | ENUM | `LEVEL_1` (Trưởng phòng), `LEVEL_2` (Giám đốc), `LEVEL_3` (CFO) |
| `status` | ENUM | `PENDING` → `APPROVED` / `REJECTED` / `SKIPPED` |
| `decided_by` | VARCHAR | Ai duyệt bước này |
| `comment` | TEXT | Ghi chú khi duyệt |
| `decided_at` | DATETIME | Thời điểm quyết định |

---

### 2.5 Bảng trung tâm

#### `subscriptions` — Subscription thực tế của khách hàng

Đây là bảng **trung tâm** — mỗi bản ghi là một hợp đồng dịch vụ đang hoặc đã tồn tại của một khách hàng.

| Cột | Kiểu | Mô tả |
|-----|------|-------|
| `subscription_id` | BIGINT PK | Khóa chính |
| `subscriber_type` | ENUM | `INDIVIDUAL` hoặc `GROUP` |
| `user_id` | BIGINT (không có FK) | ID khách hàng từ RS Core — chỉ điền khi `INDIVIDUAL` |
| `group_id` | FK → `groups` | Đại lý — chỉ điền khi `GROUP` |
| `plan_template_id` | FK → `plan_templates` | Gói cước đã đăng ký |
| `pricing_rule_id` | FK → `plan_pricing_rules` | Rule giá được áp dụng — dùng để tính tiền lúc settlement |
| `group_plan_assignment_id` | FK | Liên kết về gán gói của đại lý (nếu GROUP) |
| `retail_plan_schedule_id` | FK | Liên kết về lịch áp dụng (nếu INDIVIDUAL) |
| `source_type` | ENUM | `RETAIL_PURCHASE`, `GROUP_ASSIGNMENT`, `MANUAL` |
| `source_id` | BIGINT | ID của nguồn tạo ra subscription |
| `status` | ENUM | `PENDING` → `ACTIVE` → `EXPIRED` / `CANCELLED` / `SUSPENDED` |
| `start_date` / `end_date` | DATE | Thời gian hiệu lực |
| `signing_quota_total` | INT | Hạn mức chữ ký — **copy từ `pricing_rule.quota_total`** lúc tạo, sau đó là field độc lập |
| `signing_quota_used` | INT | Đã dùng bao nhiêu |
| `activated_by` | VARCHAR | Ai kích hoạt |
| `payment_reference` | VARCHAR | Mã tham chiếu thanh toán |

> **Tại sao có cả `pricing_rule_id` lẫn `signing_quota_total`?**
> - `signing_quota_total` được **copy** từ `pricing_rule.quota_total` tại thời điểm tạo subscription → **cố định, không thay đổi dù rule bị sửa**.
> - `pricing_rule_id` (FK) được dùng để **đọc `unit_price` và `currency` live** khi tính tiền settlement. Đây là rủi ro tiềm ẩn nếu đơn giá bị sửa sau khi subscription đã tồn tại.

> **Tại sao `user_id` không có FK?**
> - `user_accounts` chứa nhân viên nội bộ (Sale, Manager, Approver).
> - Khách hàng phổ thông là người dùng **bên ngoài**, quản lý bởi hệ thống **RS Core**.
> - `user_id` trong subscriptions là ID từ RS Core — hệ thống billing chỉ lưu để tham chiếu.

---

### 2.6 Bảng sau subscription

#### `certificate_provisioning_records` — Cấp phát chứng thư số

Mỗi khi subscription ACTIVE, hệ thống gọi RS Core để cấp chứng thư. Mỗi lần gọi = 1 bản ghi.

| Cột | Kiểu | Mô tả |
|-----|------|-------|
| `id` | BIGINT PK | Khóa chính |
| `subscription_id` | FK (no constraint) | Subscription tương ứng |
| `group_plan_assignment_id` | FK (no constraint) | Assignment nếu là GROUP |
| `pricing_rule_id` | FK (no constraint) | Rule giá áp dụng cho chứng thư này |
| `user_id` | BIGINT | Người nhận chứng thư (từ RS Core) |
| `request_id` | VARCHAR UNIQUE | ID request gửi lên RS Core — dùng để chống duplicate |
| `status` | ENUM | `PENDING` → `COMPLETED` / `FAILED` / `FAILED_PERMANENT` |
| `cert_type` | TINYINT | `1`=Cá nhân, `2`=Cá nhân thuộc tổ chức, `3`=Tổ chức |
| `certificate_id` | VARCHAR | ID chứng thư trả về từ RS Core |
| `key_id` | VARCHAR | Key ID đi kèm chứng thư |
| `issued_at` / `expires_at` | DATETIME | Thời gian cấp và hết hạn chứng thư |
| `retry_count` | INT | Số lần thử lại khi thất bại |
| `usage_count` | INT | Số lần chứng thư này được dùng |
| `failure_reason` | TEXT | Lý do thất bại nếu có |

> **Tại sao FK là `NO_CONSTRAINT`?**  
> Cho phép xoá subscription mà không ảnh hưởng đến lịch sử provisioning. Đây là dữ liệu lịch sử — cần giữ lại ngay cả khi subscription bị xoá.

---

#### `certificate_usage_records` — Lịch sử sử dụng chứng thư

Mỗi lần khách hàng ký tài liệu hoặc tạo/gia hạn chứng thư = 1 bản ghi.

| Cột | Kiểu | Mô tả |
|-----|------|-------|
| `id` | BIGINT PK | Khóa chính |
| `certificate_id` | VARCHAR | ID chứng thư đã dùng |
| `user_id` | BIGINT | Người dùng thực hiện hành động |
| `subscription_id` | FK (no constraint) | Subscription tương ứng |
| `group_plan_assignment_id` | FK (no constraint) | Assignment (nếu GROUP) |
| `usage_type` | ENUM | `SIGNING`, `CERTIFICATE_CREATED`, `CERTIFICATE_RENEWED`, `CERTIFICATE_REVOKED` |
| `quantity` | INT | Số lượng (thường = 1) |
| `used_at` | DATETIME | Thời điểm sử dụng |

> **Vai trò trong billing đại lý:** Khi tính settlement, hệ thống quét toàn bộ `certificate_usage_records` trong kỳ, đọc `pricing_rule` qua subscription để tính `totalAmount`.

---

#### `payment_records` — Bản ghi thanh toán

Ghi nhận các giao dịch thanh toán, liên kết linh hoạt với subscription hoặc settlement.

| Cột | Kiểu | Mô tả |
|-----|------|-------|
| `payment_id` | BIGINT PK | Khóa chính |
| `subscription_id` | FK → `subscriptions` | Thanh toán cho subscription cụ thể |
| `group_plan_assignment_id` | FK | Thanh toán cho gói đại lý |
| `settlement_statement_id` | FK | Thanh toán theo sao kê |
| `external_reference` | VARCHAR UNIQUE | Mã giao dịch từ cổng thanh toán |
| `amount` | DECIMAL | Số tiền |
| `currency` | CHAR(3) | Tiền tệ |
| `payment_status` | ENUM | `SUCCESS`, `FAILED`, `REFUNDED` |
| `payment_scope` | ENUM | `SUBSCRIPTION` (cá nhân trả thẳng), `GROUP_ASSIGNMENT`, `SETTLEMENT` (đại lý thanh toán theo kỳ) |
| `payment_method` | VARCHAR | Phương thức: chuyển khoản, thẻ, ... |
| `paid_at` | DATETIME | Thời điểm thanh toán |
| `raw_payload` | JSON | Dữ liệu thô từ cổng thanh toán |

---

#### `usage_aggregates` — Tổng hợp usage theo kỳ

Bảng rollup — tổng hợp usage của group hoặc individual theo kỳ (ngày/tháng) để phục vụ báo cáo và tính settlement nhanh.

| Cột | Mô tả |
|-----|-------|
| `aggregate_scope` | `GROUP`, `GROUP_ASSIGNMENT`, `USER`, `RETAIL_PLAN` |
| `scope_id` | ID tương ứng với scope |
| `period_type` | `DAY` hoặc `MONTH` |
| `period_key` | Dạng `YYYYMMDD-YYYYMMDD` |
| `certificates_created` | Số chứng thư đã tạo |
| `signing_used` | Số lần ký |
| `amount_due` | Số tiền phát sinh |
| `currency` | Tiền tệ |

---

#### `settlement_statements` — Sao kê thanh toán (chỉ đại lý)

Tóm tắt chi phí của một đại lý trong một kỳ. Chỉ áp dụng cho `GROUP`.

| Cột | Kiểu | Mô tả |
|-----|------|-------|
| `settlement_statement_id` | BIGINT PK | Khóa chính |
| `group_id` | FK → `groups` | Đại lý |
| `from_date` / `to_date` | DATE | Kỳ sao kê |
| `status` | ENUM | `DRAFT` → `FINALIZED` → `EXPORTED` |
| `total_certificates` | INT | Tổng số chứng thư cấp trong kỳ |
| `total_signings` | INT | Tổng số lần ký trong kỳ |
| `total_amount` | DECIMAL | Tổng tiền phát sinh |
| `currency` | CHAR(3) | Tiền tệ |
| `generated_by` / `generated_at` | — | Ai tạo sao kê và khi nào |

---

## 3. Mối quan hệ giữa các bảng

```
plan_templates (1) ─────────────── (N) plan_pricing_rules
plan_templates (1) ─────────────── (N) retail_plan_schedules
plan_templates (1) ─────────────── (N) group_plan_assignments
plan_templates (1) ─────────────── (N) subscriptions

retail_plan_schedules (1) ──────── (N) subscriptions
retail_plan_schedules (1) ──────── (N) assignment_audits

groups (1) ─────────────────────── (N) group_contacts
groups (1) ─────────────────────── (N) group_members
groups (1) ─────────────────────── (N) group_plan_assignments
groups (1) ─────────────────────── (N) subscriptions
groups (1) ─────────────────────── (N) settlement_statements

group_plan_assignments (1) ──────── (N) subscriptions
group_plan_assignments (1) ──────── (N) assignment_audits
group_plan_assignments (1) ──────── (N) certificate_provisioning_records
group_plan_assignments (1) ──────── (N) group_members (via source_assignment_id)

approval_requests (1) ───────────── (N) approval_request_steps

subscriptions (1) ───────────────── (N) certificate_provisioning_records
subscriptions (1) ───────────────── (N) certificate_usage_records
subscriptions (1) ───────────────── (N) payment_records

settlement_statements (1) ──────── (N) payment_records

plan_pricing_rules (1) ─────────── (N) subscriptions          (snapshot giá)
plan_pricing_rules (1) ─────────── (N) certificate_provisioning_records
```

### Sơ đồ ERD rút gọn

```
┌─────────────────────┐         ┌──────────────────────────┐
│    plan_templates    │─────1:N─│    plan_pricing_rules    │
└──────────┬──────────┘         └────────────┬─────────────┘
           │                                 │
      ┌────┴──────────────────┐              │
      │                       │              │
 1:N  ▼                  1:N  ▼              │
┌──────────────────┐   ┌────────────────────┐│
│retail_plan_      │   │group_plan_         ││
│schedules         │   │assignments         ││
└────────┬─────────┘   └────────┬───────────┘│
         │                      │            │
         └──────────┬───────────┘            │
                    │  1:N                   │
                    ▼                        │
        ┌───────────────────────────────┐    │
        │         subscriptions         │◄───┘ (FK pricing_rule_id)
        │  subscriber_type: IND | GROUP │
        └──────────┬────────────────────┘
                   │
        ┌──────────┼──────────────────────┐
        │ 1:N      │ 1:N                  │ 1:N
        ▼          ▼                      ▼
┌───────────┐ ┌──────────────┐    ┌───────────────┐
│certificate│ │certificate_  │    │payment_records│
│_provision │ │usage_records │    └───────────────┘
│_records   │ └──────────────┘
└───────────┘         │
                      │ (tính settlement)
                      ▼
              ┌──────────────────┐
              │usage_aggregates  │
              └────────┬─────────┘
                       │ (chỉ GROUP)
                       ▼
              ┌──────────────────────┐
              │settlement_statements │
              └──────────────────────┘
```

---

## 4. Luồng nghiệp vụ — Khách hàng Phổ thông (INDIVIDUAL)

### Bước 1: Tạo gói cước

**Ai:** Admin  
**API:** `POST /api/v1/individual/plan-configs`

```
INSERT plan_templates          (customer_segment = INDIVIDUAL, status = AVAILABLE)
INSERT plan_pricing_rules (N)  (các mức giá theo bậc số lượng)
```

### Bước 2: Tạo lịch áp dụng và gửi duyệt

**Ai:** Admin  
**API:** `POST /api/v1/individual/plan-configs/{id}/request-apply`

```
INSERT retail_plan_schedules   (schedule_status = REQUESTED)
INSERT assignment_audits       (action = REQUEST)
INSERT approval_requests       (status = IN_APPROVAL, customer_segment = INDIVIDUAL,
                                entity_type = RETAIL_PLAN_SCHEDULE,
                                contract_value = max(unit_price) từ pricing_rules)
INSERT approval_request_steps  (1-3 steps tùy ngưỡng contract_value)
→ gửi email cho APPROVAL_L1
```

### Bước 3: Phê duyệt đa cấp

**Ai:** Approver (Level 1 / 2 / 3)  
**API:** `POST /api/v1/approval-requests/{id}/approve`

```
UPDATE approval_request_steps  (status = APPROVED cho step hiện tại)
UPDATE approval_requests       (current_level++)

Nếu đủ tất cả level:
UPDATE approval_requests       (status = APPROVED)
UPDATE retail_plan_schedules   (schedule_status = APPROVED, approved_by, approved_at)
INSERT assignment_audits       (action = APPROVE)
```

Nếu bị từ chối:
```
UPDATE approval_request_steps  (status = REJECTED)
UPDATE approval_requests       (status = REJECTED)
UPDATE retail_plan_schedules   (schedule_status = INACTIVE)
INSERT assignment_audits       (action = REJECT)
```

### Bước 4: Kích hoạt lịch

**Khi nào:** Scheduler chạy hàng ngày hoặc admin kích hoạt thủ công  
**Điều kiện:** `schedule_status = APPROVED` và `apply_from <= today`

```
UPDATE retail_plan_schedules   (schedule_status = ACTIVE)
INSERT assignment_audits       (action = ACTIVATE)
```

### Bước 5: Khách hàng đăng ký subscription

**Ai:** Khách hàng tự đăng ký (qua hệ thống bán hàng / portal) hoặc Sale tạo thủ công  
**API:** `POST /api/v1/commercial-flows/retail-plan-schedules/{scheduleId}/execute`

```
1. Kiểm tra schedule_status = ACTIVE
2. Resolve pricing rule (ưu tiên rule truyền vào, nếu không lấy rule mặc định)
3. INSERT subscriptions:
      subscriber_type = INDIVIDUAL
      user_id         = <ID khách hàng từ RS Core>
      plan_template_id
      pricing_rule_id
      retail_plan_schedule_id
      source_type     = RETAIL_PURCHASE
      status          = ACTIVE (hoặc PENDING nếu chờ thanh toán)
      start_date, end_date
      signing_quota_total = pricing_rule.quota_total  ← copy vào đây
      signing_quota_used  = 0
```

### Bước 6: Cấp phát chứng thư

**Khi nào:** Ngay sau khi subscription ACTIVE  
**Service:** `CertificateProvisioningService`

```
INSERT certificate_provisioning_records:
      subscription_id
      user_id          = <ID khách hàng>
      request_id       = UUID (gửi lên RS Core)
      status           = PENDING
      cert_type        = INDIVIDUAL (1)
→ Gọi RS Core API để tạo chứng thư
→ RS Core trả về certificate_id, key_id, expires_at
UPDATE certificate_provisioning_records:
      status           = COMPLETED
      certificate_id, key_id, issued_at, expires_at
```

### Bước 7: Khách hàng sử dụng chứng thư

```
INSERT certificate_usage_records:
      certificate_id
      user_id
      subscription_id
      usage_type       = SIGNING (hoặc CERTIFICATE_CREATED, ...)
      quantity         = 1
      used_at          = NOW()

UPDATE subscriptions:
      signing_quota_used++   ← trừ quota
```

### Bước 8: Hết hạn

**Khi nào:** Scheduler chạy, phát hiện `end_date < today`

```
UPDATE subscriptions                  (status = EXPIRED)
UPDATE retail_plan_schedules          (schedule_status = INACTIVE) nếu hết kỳ
INSERT assignment_audits              (action = EXPIRE)
```

---

## 5. Luồng nghiệp vụ — Khách hàng Đại lý (GROUP)

### Bước 1: Tạo đại lý

**Ai:** Admin  
**API:** `POST /api/v1/groups`

```
INSERT groups              (status = ACTIVE)
INSERT group_contacts (N)  (tùy chọn — thêm sau cũng được)
```

### Bước 2: Gán gói cước cho đại lý và gửi duyệt

**Ai:** Admin / Sale  
**API:** `POST /api/v1/groups/{groupId}/plan-assignments`  
hoặc `POST /api/v1/groups/{groupId}/add-plan` (tạo template mới luôn)  
hoặc `POST /api/v1/groups/provision` (tạo group + template + assignment cùng lúc)

```
INSERT plan_templates          (customer_segment = GROUP)    — nếu tạo mới
INSERT plan_pricing_rules (N)                                — nếu tạo mới
INSERT group_plan_assignments  (assignment_status = REQUESTED)
INSERT assignment_audits       (action = REQUEST)
INSERT approval_requests       (status = IN_APPROVAL, customer_segment = GROUP,
                                entity_type = GROUP_PLAN_ASSIGNMENT,
                                contract_value = max(unit_price * quota_total))
INSERT approval_request_steps  (1-3 steps)
→ gửi email cho APPROVAL_L1
```

### Bước 3: Phê duyệt đa cấp

Tương tự Individual. Khi đủ tất cả cấp:

```
UPDATE approval_requests       (status = APPROVED)
UPDATE group_plan_assignments  (assignment_status = APPROVED, approved_by, approved_at)
INSERT assignment_audits       (action = APPROVE)
```

### Bước 4: Kích hoạt gói

**Ai:** Admin hoặc Scheduler  
**API:** `POST /api/v1/commercial-flows/group-assignments/{assignmentId}/execute`

```
UPDATE group_plan_assignments  (assignment_status = ACTIVE, activated_at = NOW())
INSERT assignment_audits       (action = ACTIVATE)
```

### Bước 5: Tạo subscription cho đại lý

Xảy ra trong cùng request execute ở Bước 4, khi `issueSubscription = true`:

```
1. Kiểm tra chưa có subscription ACTIVE cho assignment này
2. Resolve pricing rule (SIGNING_COUNT được ưu tiên cho đại lý)
3. INSERT subscriptions:
      subscriber_type        = GROUP
      group_id               = <group_id>
      user_id                = NULL         ← không dùng cho GROUP
      plan_template_id
      pricing_rule_id
      group_plan_assignment_id
      source_type            = GROUP_ASSIGNMENT
      status                 = ACTIVE
      start_date, end_date
      signing_quota_total    = pricing_rule.quota_total  ← copy vào đây
      signing_quota_used     = 0
```

### Bước 6: Thêm thành viên đại lý

```
INSERT group_members:
      group_id
      user_id                = <ID từ RS Core>
      role                   = OPERATOR / MEMBER
      source_assignment_id   = group_plan_assignment_id
```

### Bước 7: Cấp phát chứng thư cho từng thành viên

```
Với mỗi user_id trong group_members:
INSERT certificate_provisioning_records:
      subscription_id
      group_plan_assignment_id
      pricing_rule_id
      user_id
      cert_type              = ORGANIZATION (3) hoặc INDIVIDUAL_OF_ORG (2)
      status                 = PENDING
→ Gọi RS Core API → nhận certificate_id, key_id, expires_at
UPDATE certificate_provisioning_records: status = COMPLETED
```

### Bước 8: Thành viên sử dụng chứng thư (ký tài liệu)

```
INSERT certificate_usage_records:
      certificate_id
      user_id
      subscription_id        ← subscription của group
      group_plan_assignment_id
      usage_type             = SIGNING
      quantity               = 1

UPDATE subscriptions:
      signing_quota_used++
```

### Bước 9: Tính sao kê cuối kỳ

**Ai:** Admin  
**API:** `POST /api/v1/commercial-flows/groups/{groupId}/settlement`

```
1. Quét certificate_usage_records trong khoảng from_date → to_date
2. Với mỗi record:
      - Lấy subscription → lấy pricing_rule (LIVE qua FK!)
      - Tính tiền: quantity × pricing_rule.unit_price
3. INSERT settlement_statements:
      group_id
      from_date, to_date
      status               = DRAFT (hoặc FINALIZED nếu finalizeNow=true)
      total_certificates
      total_signings
      total_amount
      currency             (lấy từ pricing_rule.currency)
4. UPSERT usage_aggregates:
      aggregate_scope = GROUP
      scope_id        = group_id
      period_key      = "YYYYMMDD-YYYYMMDD"
```

### Bước 10: Hết hạn gói

**Khi nào:** Scheduler chạy, phát hiện `apply_to < today`

```
UPDATE group_plan_assignments  (assignment_status = EXPIRED)
UPDATE subscriptions           (status = EXPIRED)
INSERT assignment_audits       (action = EXPIRE)
```

---

## 6. Dữ liệu đi đâu sau khi Subscription được tạo

```
                        ┌─────────────────┐
                        │  subscriptions  │ ← điểm bắt đầu
                        └────────┬────────┘
                                 │
          ┌──────────────────────┼──────────────────────┐
          │                      │                      │
          ▼                      ▼                      ▼
 ┌─────────────────┐   ┌──────────────────┐   ┌──────────────────┐
 │ Cấp phát chứng  │   │ Ghi nhận thanh   │   │ Trừ quota sử dụng│
 │ thư số          │   │ toán             │   │                  │
 │                 │   │                  │   │ signing_quota_   │
 │ certificate_    │   │ payment_records  │   │ used++           │
 │ provisioning_   │   │ (payment_scope = │   │                  │
 │ records         │   │  SUBSCRIPTION)   │   └──────────────────┘
 └────────┬────────┘   └──────────────────┘
          │
          │ Khách hàng dùng chứng thư
          ▼
 ┌─────────────────────┐
 │ certificate_usage_  │   ← mỗi lần ký / tạo / gia hạn chứng thư
 │ records             │
 └────────┬────────────┘
          │
          │ Rollup định kỳ (scheduler)
          ▼
 ┌─────────────────────┐
 │ usage_aggregates    │   ← tổng hợp theo ngày / tháng
 └────────┬────────────┘
          │
          │ Chỉ với GROUP — cuối kỳ tính sao kê
          ▼
 ┌─────────────────────┐
 │ settlement_         │   DRAFT → FINALIZED → EXPORTED
 │ statements          │
 └────────┬────────────┘
          │
          ▼
 ┌─────────────────────┐
 │ payment_records     │   ← đại lý thanh toán theo sao kê
 │ (payment_scope =    │   (payment_scope = SETTLEMENT)
 │  SETTLEMENT)        │
 └─────────────────────┘
```

### Điểm khác biệt then chốt giữa hai loại KH sau subscription

| Hành động | Khách hàng Phổ thông (INDIVIDUAL) | Khách hàng Đại lý (GROUP) |
|-----------|----------------------------------|--------------------------|
| Ai nhận chứng thư | Chính khách hàng (`user_id`) | Từng thành viên trong `group_members` |
| Loại chứng thư | `cert_type = 1` (Cá nhân) | `cert_type = 2` hoặc `3` |
| Thanh toán | Trả thẳng khi đăng ký (`SUBSCRIPTION`) | Thanh toán theo sao kê cuối kỳ (`SETTLEMENT`) |
| Sao kê | Không có | Có `settlement_statements` mỗi kỳ |
| Cách tính tiền | Trả trước theo gói | Tính theo thực tế sử dụng (ký / chứng thư) |
| `pricing_metric` | `CERTIFICATE_COUNT` | `SIGNING_COUNT` |
| Quota tracking | `signing_quota_total` / `_used` trong subscription | Tổng hợp qua `usage_aggregates` |

---

## 7. Vòng đời trạng thái

### `retail_plan_schedules.schedule_status`
```
AVAILABLE → REQUESTED → APPROVED → ACTIVE → INACTIVE
                │                              ▲
                └──────── REJECTED ────────────┘ (cũng là INACTIVE)
```

### `group_plan_assignments.assignment_status`
```
REQUESTED → APPROVED → ACTIVE → STOPPED
    │                    └────→ EXPIRED
    └────→ REJECTED
```

### `subscriptions.status`
```
PENDING → ACTIVE → EXPIRED
              ├──→ CANCELLED
              └──→ SUSPENDED
```

### `certificate_provisioning_records.status`
```
PENDING → COMPLETED
    └───→ FAILED (retry_count++) → FAILED_PERMANENT
```

### `approval_requests.status`
```
DRAFT → IN_APPROVAL → APPROVED
            ├────────→ REJECTED
            └────────→ NEED_REVISION → IN_APPROVAL (resubmit)
```

### `settlement_statements.status`
```
DRAFT → FINALIZED → EXPORTED
```

---

## 8. Bảng tóm tắt nghiệp vụ vs bảng DB

### Khách hàng Phổ thông

| Nghiệp vụ | INSERT | UPDATE |
|-----------|--------|--------|
| Tạo gói cước | `plan_templates`, `plan_pricing_rules` | — |
| Gửi duyệt lịch áp dụng | `retail_plan_schedules`, `assignment_audits`, `approval_requests`, `approval_request_steps` | — |
| Duyệt / Từ chối | `assignment_audits` | `retail_plan_schedules`, `approval_requests`, `approval_request_steps` |
| Kích hoạt lịch | `assignment_audits` | `retail_plan_schedules` |
| KH đăng ký subscription | `subscriptions` | — |
| Cấp phát chứng thư | `certificate_provisioning_records` | `certificate_provisioning_records` |
| KH dùng chứng thư | `certificate_usage_records` | `subscriptions` (quota_used++) |
| Hết hạn | `assignment_audits` | `subscriptions`, `retail_plan_schedules` |

### Khách hàng Đại lý

| Nghiệp vụ | INSERT | UPDATE |
|-----------|--------|--------|
| Tạo đại lý | `groups`, `group_contacts` | — |
| Thêm thành viên | `group_members` | — |
| Tạo gói + gán | `plan_templates`, `plan_pricing_rules`, `group_plan_assignments`, `assignment_audits`, `approval_requests`, `approval_request_steps` | — |
| Duyệt / Từ chối | `assignment_audits` | `group_plan_assignments`, `approval_requests`, `approval_request_steps` |
| Kích hoạt gói | `assignment_audits`, `subscriptions` | `group_plan_assignments` |
| Cấp chứng thư cho thành viên | `certificate_provisioning_records` | `certificate_provisioning_records` |
| Thành viên ký tài liệu | `certificate_usage_records` | `subscriptions` (quota_used++) |
| Tính sao kê | `settlement_statements`, `usage_aggregates` | — |
| Đại lý thanh toán sao kê | `payment_records` | — |
| Hết hạn gói | `assignment_audits` | `group_plan_assignments`, `subscriptions` |
