# Tài liệu Phân quyền & Phê duyệt

> Dự án: `billing-CA`  
> Cập nhật lần cuối: 2026-05-11  
> Nguồn đối chiếu: backend `plan-mng-web` và `subscription-provider`

---

## 1. Tổng quan RBAC

Backend dùng mô hình RBAC:

```text
user_accounts -> user_roles -> roles -> role_permissions -> permissions
```

Spring Security đưa cả role name và permission key vào authority. Vì vậy backend có thể kiểm tra:

```java
hasAuthority('ROLE_ADMIN')
hasAuthority('role:update')
hasAnyAuthority('approval:level1', 'approval:level2', 'approval:level3')
```

### Bảng chính

| Bảng | Vai trò |
|---|---|
| `user_accounts` | Tài khoản người dùng, trạng thái, manager trực tiếp |
| `roles` | Vai trò hệ thống hoặc vai trò tùy chỉnh |
| `permissions` | Quyền hành động theo key, ví dụ `role:update` |
| `user_roles` | Gán role cho user |
| `role_permissions` | Gán permission cho role |
| `partner_group_access` | Cấp quyền partner xem group cụ thể |

### Role seed hiện tại

Trong `V1__init_schema.sql`, các role mặc định là:

| Role | Ý nghĩa |
|---|---|
| `ROLE_ADMIN` | Quản trị hệ thống, có bypass trong một số luồng approval |
| `ROLE_USER` | User mặc định |
| `ROLE_LEVEL_1` | Vai trò cấp 1 |
| `ROLE_LEVEL_2` | Vai trò cấp 2 |
| `ROLE_LEVEL_3` | Vai trò cấp 3 |
| `ROLE_LEVEL_4` | Vai trò cấp 4 |
| `ROLE_PARTNER` | Đối tác, xem báo cáo group được cấp |
| `ROLE_MANAGER` | Quản lý nội bộ, xem dữ liệu cấp dưới |

Lưu ý: backend phê duyệt đa cấp không kiểm tra role `APPROVAL_L1/L2/L3`. Service kiểm tra theo permission key `approval:level1`, `approval:level2`, `approval:level3`.

---

## 2. Permission và API bảo vệ

### Nhóm permission đang dùng

| Nhóm | Permission key |
|---|---|
| Dashboard | `dashboard:view` |
| Gói phổ thông | `plan:view`, `plan:create`, `plan:update`, `plan:delete` |
| Usage cá nhân | `individual:usage:view` |
| Khách hàng đại lý | `group:view`, `group:create`, `group:update` |
| Subscription | `subscription:view`, `subscription:create`, `subscription:update` |
| User | `user:view`, `user:create`, `user:update` |
| Role | `role:view`, `role:create`, `role:update` |
| Báo cáo | `report:view`, `report:group:view`, `report:individual:view`, `report:event:create` |
| Audit | `audit-log:view` |
| Data scope | `group:view:own`, `group:view:subordinates`, `group:assign:owner`, `report:view:own`, `report:view:subordinates`, `report:view:partner` |
| Partner access | `partner:access:grant`, `partner:access:revoke` |
| Approval | `approval:level1`, `approval:level2`, `approval:level3` |

### Ghi chú đồng bộ DB

Code hiện đang dùng các permission approval `approval:level1..3`. Tuy nhiên trong `V1__init_schema.sql` và `manual/reset_permissions.sql`, danh sách seed chính chỉ có 30 permissions và chưa insert các key approval. `V3__permission_data_fixes.sql` có phần insert approval permissions nhưng đang bị comment. `V4__approval_permission_labels.sql` chỉ update label nếu các key đã tồn tại.

Khi triển khai thật, DB cần có tối thiểu:

```sql
INSERT INTO permissions (permission_key, display_name, module_group, group_name, sort_order)
VALUES
  ('approval:level1', 'Trưởng phòng kinh doanh', 'PHE_DUYET', 'APPROVAL', 93),
  ('approval:level2', 'CFO (Finance Manager)', 'PHE_DUYET', 'APPROVAL', 94),
  ('approval:level3', 'CEO', 'PHE_DUYET', 'APPROVAL', 95)
ON DUPLICATE KEY UPDATE
  display_name = VALUES(display_name),
  module_group = VALUES(module_group),
  group_name = VALUES(group_name),
  sort_order = VALUES(sort_order);
```

Sau đó gán các permission này cho role approver tương ứng qua `role_permissions` hoặc UI quản trị role.

---

## 3. Quản lý role và user

### Role API

Controller: `RoleController`, prefix `/api/v1/admin/roles`.

| Method | URL | Quyền |
|---|---|---|
| `GET` | `/api/v1/admin/roles` | `role:view` |
| `POST` | `/api/v1/admin/roles` | `role:create` |
| `PUT` | `/api/v1/admin/roles/{roleId}` | `role:update` |
| `DELETE` | `/api/v1/admin/roles/{roleId}` | `role:update` |
| `GET` | `/api/v1/admin/roles/permissions` | `role:view` |
| `GET` | `/api/v1/admin/roles/permissions/matrix` | `role:view` |
| `PUT` | `/api/v1/admin/roles/{roleId}/permissions` | `role:update` |

Ràng buộc nghiệp vụ:

- Role hệ thống `is_system_role = true` không được sửa/xóa.
- Role đang gán cho user không được xóa.
- Mỗi lần tạo/sửa/xóa/gán quyền role đều ghi audit qua `AdminAuditLogService`.

### User API

Controller: `UserController`.

| Method | URL | Quyền |
|---|---|---|
| `POST` | `/api/v1/admin/users` | `user:create` |
| `GET` | `/api/v1/admin/users` | `user:view` hoặc `group:assign:owner` |
| `GET` | `/api/v1/admin/users/{userId}` | `user:view` |
| `PUT` | `/api/v1/admin/users/{userId}` | `user:update` |
| `PATCH` | `/api/v1/admin/users/{userId}/deactivate` | `user:update` |
| `PATCH` | `/api/v1/admin/users/{userId}/reactivate` | `user:update` |
| `PATCH` | `/api/v1/admin/users/{userId}/unlock` | `user:update` |
| `DELETE` | `/api/v1/admin/users/{userId}` | `user:update` |
| `PUT` | `/api/v1/admin/users/{userId}/roles` | `user:update` và `role:update` |
| `PATCH` | `/api/v1/admin/users/{userId}/password` | `user:update` |
| `PATCH` | `/api/v1/admin/users/{userId}/manager` | `user:update` |
| `GET` | `/api/v1/admin/users/{userId}/subordinates` | `user:view` hoặc `group:view:subordinates` |
| `GET` | `/api/v1/users/me` | đã đăng nhập |
| `PUT` | `/api/v1/users/me/password` | đã đăng nhập |

---

## 4. Data scope

Service: `DataScopeServiceImpl`.

| Trường hợp | Dữ liệu được xem |
|---|---|
| `ROLE_ADMIN` | Không giới hạn |
| Có `group:view:own` hoặc `report:view:own` | Dữ liệu do chính user phụ trách |
| Có `group:view:subordinates` hoặc `report:view:subordinates` | Dữ liệu của user và toàn bộ cấp dưới trong cây `manager_user_id` |
| `ROLE_PARTNER` hoặc `report:view:partner` | Group được cấp trong `partner_group_access` |

Các API partner access:

| Method | URL | Quyền |
|---|---|---|
| `POST` | `/api/v1/partner-access` | `partner:access:grant` |
| `DELETE` | `/api/v1/partner-access/{accessId}` | `partner:access:revoke` |
| `DELETE` | `/api/v1/partner-access/partner/{partnerUserId}/group/{groupId}` | `partner:access:revoke` |
| `GET` | `/api/v1/partner-access/partner/{partnerUserId}` | `partner:access:grant` hoặc chính partner đó |
| `GET` | `/api/v1/partner-access/partner/{partnerUserId}/history` | `partner:access:grant` |

---

## 5. Luồng phê duyệt đa cấp

### Entity và trạng thái

Các bảng chính:

| Bảng | Vai trò |
|---|---|
| `approval_requests` | Header của request phê duyệt |
| `approval_request_steps` | Các step level 1-3 |
| `approval_level_configs` | Cấu hình số cấp theo segment và giá trị hợp đồng |
| `group_plan_assignments` | Entity gốc cho khách hàng đại lý |
| `retail_plan_schedules` | Entity gốc cho khách hàng phổ thông |

Trạng thái request:

```text
DRAFT -> IN_APPROVAL -> APPROVED
                   -> REJECTED
                   -> NEED_REVISION -> IN_APPROVAL
```

Trạng thái step:

| Status | Ý nghĩa |
|---|---|
| `PENDING` | Đang chờ xử lý |
| `APPROVED` | Step đã duyệt |
| `REJECTED` | Step bị từ chối |
| `SKIPPED` | Step bị bỏ qua, ví dụ admin bypass hoặc request đã reject |

### Tạo request

Có hai luồng tạo approval tự động:

| Entity | Service | Điều kiện tạo approval |
|---|---|---|
| `GROUP_PLAN_ASSIGNMENT` | `GroupPlanAssignmentServiceImpl.create()` | `assignmentStatus = REQUESTED` |
| `RETAIL_PLAN_SCHEDULE` | `IndividualPlanConfigServiceImpl.requestApply()` | Tạo schedule với `scheduleStatus = REQUESTED` |

Hai service tạo `ApprovalRequest` ở trạng thái `DRAFT`, sau đó gọi `MultiLevelApprovalService.createAndSubmit()`. Vì vậy request được chuyển ngay sang `IN_APPROVAL`, tạo steps và gửi email cho approver level 1.

### Số cấp phê duyệt

Backend đang ưu tiên `approvalLevel` được truyền từ request. Nếu không truyền thì dùng fallback trong service tạo entity, hiện mặc định là `1`.

Hàm `resolveRequiredLevels(customerSegment, contractValue)` vẫn tồn tại và có fallback theo ngưỡng:

| Segment | Giá trị | Số cấp fallback |
|---|---:|---:|
| `INDIVIDUAL` | `< 5,000,000` | 1 |
| `INDIVIDUAL` | `5,000,000 - < 50,000,000` | 2 |
| `INDIVIDUAL` | `>= 50,000,000` | 3 |
| `GROUP` | `< 50,000,000` | 1 |
| `GROUP` | `50,000,000 - < 500,000,000` | 2 |
| `GROUP` | `>= 500,000,000` | 3 |

Nhưng trong code hiện tại, `submit()` và `createAndSubmit()` gọi `resolveRequestedLevels(...)`, không gọi trực tiếp `resolveRequiredLevels(...)`. Vì vậy tài liệu triển khai cần hiểu `approvalLevel` là nguồn quyết định trực tiếp trong luồng hiện tại.

### Level và quyền

| Step | `required_approval_level` | Permission bắt buộc |
|---|---|---|
| 1 | `LEVEL_1` | `approval:level1` |
| 2 | `LEVEL_2` | `approval:level2` |
| 3 | `LEVEL_3` | `approval:level3` |

`MultiLevelApprovalServiceImpl.validateCurrentApprover()` xử lý như sau:

1. Tìm manager chain của `requested_by`.
2. Nếu có manager ACTIVE trong chain có permission đúng level, chỉ user đó được duyệt step.
3. Nếu không có manager phù hợp, fallback sang bất kỳ user ACTIVE nào có permission đúng level.
4. Nếu không tìm thấy actor hợp lệ, trả lỗi `APPROVAL_WRONG_LEVEL`.

`ROLE_ADMIN` có bypass riêng khi approve: admin có thể approve và service sẽ `SKIPPED` toàn bộ pending step còn lại, sau đó finalize request thành `APPROVED`.

### Hành động approval

Controller: `ApprovalRequestController`, prefix `/api/v1/approval-requests`.

| Method | URL | Quyền | Service |
|---|---|---|---|
| `GET` | `/api/v1/approval-requests` | `subscription:view` hoặc `approval:level1..3` | `listAll()` |
| `GET` | `/api/v1/approval-requests/{id}` | `subscription:view` hoặc `approval:level1..3` | `getById()` |
| `POST` | `/api/v1/approval-requests/{id}/submit` | `subscription:update` | `submit()` |
| `POST` | `/api/v1/approval-requests/{id}/approve` | `approval:level1..3` | `approveStep()` |
| `POST` | `/api/v1/approval-requests/{id}/reject` | `approval:level1..3` | `reject()` |
| `POST` | `/api/v1/approval-requests/{id}/revision` | `approval:level1..3` | `requestRevision()` |
| `POST` | `/api/v1/approval-requests/{id}/resubmit` | `subscription:update` | `resubmit()` |
| `GET` | `/api/v1/approval-requests/level-configs` | `subscription:view` | `listLevelConfigs()` |
| `GET` | `/api/v1/approval-requests/legacy` | `subscription:view` | legacy single-level |
| `POST` | `/api/v1/approval-requests/{id}/review` | `subscription:update` | legacy single-level |

### Quy tắc nghiệp vụ

| Quy tắc | Backend hiện tại |
|---|---|
| Submit | Chỉ submit request `DRAFT`; submit tạo lại toàn bộ steps |
| Resubmit | Chỉ resubmit request `NEED_REVISION`; resubmit xóa và tạo lại steps |
| Approve | Chỉ xử lý khi request `IN_APPROVAL` |
| Self approve | Không cho `requested_by` tự approve |
| Approver đúng cấp | Kiểm tra permission theo current step |
| Reject | Set current step `REJECTED`, các pending step sau `SKIPPED`, request `REJECTED` |
| Revision | Reset toàn bộ steps về `PENDING`, current level về `1`, request `NEED_REVISION` |
| Fully approved | Request `APPROVED`, cập nhật entity gốc |

### Propagate sang entity gốc

| Entity | Khi approve cuối | Khi reject |
|---|---|---|
| `GROUP_PLAN_ASSIGNMENT` | `assignment_status = APPROVED`, set `approved_by`, `approved_at` | `assignment_status = AVAILABLE`, set `rejected_by`, `rejected_at`, `stop_reason` |
| `RETAIL_PLAN_SCHEDULE` | `schedule_status = APPROVED`, set `approved_by`, `approved_at` | `schedule_status = INACTIVE` |

---

## 6. API liên quan entity gốc

### Group plan assignment

Controller: `GroupCommercialController`.

| Method | URL | Quyền |
|---|---|---|
| `GET` | `/api/v1/groups/plan-assignments` | `group:view` |
| `GET` | `/api/v1/groups/plan-assignments/{assignmentId}` | `group:view` hoặc `approval:level1..3` |
| `GET` | `/api/v1/groups/{groupId}/plan-assignments` | `group:view` |
| `POST` | `/api/v1/groups/{groupId}/plan-assignments` | `group:create` |
| `POST` | `/api/v1/groups/plan-assignments/{assignmentId}/review` | `group:update` hoặc `approval:level1..3` |
| `POST` | `/api/v1/groups/{groupId}/add-plan` | `group:update` |

### Retail plan schedule và individual plan config

| Controller | Method | URL | Quyền |
|---|---|---|---|
| `RetailPlanScheduleController` | `GET` | `/api/v1/retail-plan-schedules` | `plan:view` |
| `RetailPlanScheduleController` | `GET` | `/api/v1/retail-plan-schedules/{id}` | `plan:view` hoặc `approval:level1..3` |
| `RetailPlanScheduleController` | `POST` | `/api/v1/retail-plan-schedules` | `plan:create` |
| `RetailPlanScheduleController` | `POST` | `/api/v1/retail-plan-schedules/{id}/review` | `plan:update` |
| `IndividualPlanConfigController` | `GET` | `/api/v1/individual/plan-configs` | `plan:view` |
| `IndividualPlanConfigController` | `POST` | `/api/v1/individual/plan-configs` | `plan:create` |
| `IndividualPlanConfigController` | `POST` | `/api/v1/individual/plan-configs/{id}/request-apply` | `plan:update` |
| `IndividualPlanConfigController` | `POST` | `/api/v1/individual/plan-configs/{id}/approve` | `plan:update` |
| `IndividualPlanConfigController` | `POST` | `/api/v1/individual/plan-configs/{id}/reject` | `plan:update` |

---

## 7. Notification

Service: `ApprovalNotificationServiceImpl`.

Notification chạy async và gửi email qua `MailService`.

| Sự kiện | Người nhận |
|---|---|
| Submit / resubmit | Approver level 1 |
| Approve chưa phải level cuối | Approver level tiếp theo |
| Fully approved | Người tạo request |
| Rejected | Người tạo request |
| Need revision | Người tạo request |

Resolver người nhận approval dùng cùng logic với service duyệt: ưu tiên manager chain có permission đúng level, sau đó fallback sang user active có permission đúng level.

---

## 8. Error code liên quan

File: `ErrorCodes.java`.

| Code | Constant | Ý nghĩa |
|---:|---|---|
| 2013 | `APPROVAL_NOT_FOUND` | Không tìm thấy approval request |
| 2014 | `APPROVAL_ALREADY_REVIEWED` | Approval legacy đã được review |
| 2015 | `APPROVAL_EXECUTION_FAILED` | Thực thi approval lỗi |
| 2022 | `APPROVAL_STEP_NOT_FOUND` | Không tìm thấy step hiện tại |
| 2023 | `APPROVAL_NOT_IN_PROGRESS` | Request không ở trạng thái `IN_APPROVAL` hoặc submit sai trạng thái |
| 2024 | `APPROVAL_SELF_APPROVE` | Người tạo tự duyệt request của mình |
| 2025 | `APPROVAL_WRONG_LEVEL` | Actor không có quyền duyệt đúng cấp |
| 2026 | `APPROVAL_NOT_REVISABLE` | Request không ở trạng thái `NEED_REVISION` khi resubmit |
| 2027 | `APPROVAL_LEVEL_CONFIG_NOT_FOUND` | Không tìm thấy cấu hình số cấp |
| 3011 | `USERNAME_ALREADY_EXISTS` | Username đã tồn tại |
| 3012 | `EMAIL_ALREADY_EXISTS` | Email đã tồn tại |
| 3013 | `CANNOT_MODIFY_SYSTEM_ROLE` | Không được sửa role hệ thống |
| 3014 | `ROLE_IN_USE` | Role đang được gán cho user |

---

## 9. Checklist khi thay đổi approval

### Thêm level phê duyệt mới

- Thêm permission mới, ví dụ `approval:level4`.
- Gán permission cho role approver tương ứng.
- Tăng giới hạn trong `MultiLevelApprovalServiceImpl.LEVEL_ROLES` và `LEVEL_PERMISSION_KEYS`.
- Cập nhật validate `approvalLevel must be between 1 and 3`.
- Cập nhật `@PreAuthorize` ở `ApprovalRequestController` và các controller entity gốc đang cho approver xem chi tiết.
- Cập nhật notification display name trong `ApprovalNotificationServiceImpl`.
- Cập nhật UI hiển thị step và action.

### Thêm entity mới vào approval

- Thêm `RequestType` mới trong `CommercialEnums`.
- Khi tạo entity, tạo `ApprovalRequest` ở `DRAFT` rồi gọi `createAndSubmit()`.
- Set đúng `entity_type`, `entity_id`, `requested_by`, `customer_segment`, `request_payload`, `description`, `contract_value`, `total_levels`.
- Bổ sung case trong `propagateApproval()` và `propagateRejection()`.
- Mở quyền xem chi tiết entity cho `approval:level1..3` nếu approver cần xem payload gốc.

### Đồng bộ permission seed

- Bỏ comment hoặc viết migration chính thức để insert `approval:level1..3`.
- Gán permission approval cho role approver.
- Nếu frontend dùng `approval:view`, thêm permission này hoặc đổi frontend/backend về cùng một key.
