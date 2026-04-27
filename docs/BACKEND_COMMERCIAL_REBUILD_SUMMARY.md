# Backend Commercial Rebuild Summary

## 1. Mục tiêu của đợt refactor

Đợt cập nhật này thay toàn bộ phần backend cũ đang dựa trên mô hình `Plan / Subscription / Group` đơn giản bằng một mô hình commercial rõ ràng hơn, tách được:

- plan template
- pricing rule
- group assignment workflow
- retail plan schedule workflow
- runtime subscription
- usage aggregate
- settlement statement
- audit timeline

Đồng thời:

- bỏ phần service/repository/controller cũ không còn phù hợp
- thay entity `UserRole` từ quan hệ `ManyToMany` ẩn thành entity riêng
- chuyển phần ghi audit log vào database sang AOP
- bỏ các controller cũ ở `plan-mng-web` và `subscription-api` để tránh phụ thuộc vào API cũ

Kết quả cuối cùng:

- compile toàn bộ `backend` đã thành công với:

```powershell
mvn -q -DskipTests compile
```

chạy tại [backend](/E:/PhuongCM/billing-CA/backend)

## 2. Mô hình nghiệp vụ mới

### 2.1 Các khối nghiệp vụ chính

Phần `plan` cũ trước đây đang trộn nhiều lớp nghiệp vụ vào một chỗ. Sau khi refactor, mô hình được tách thành các lớp sau:

- `PlanTemplate`: định nghĩa template thương mại
- `PlanPricingRule`: các rule giá áp dụng cho plan template
- `GroupPlanAssignment`: yêu cầu/gán plan cho đại lý hoặc group
- `RetailPlanSchedule`: lịch áp dụng plan cho luồng retail
- `Subscription`: entitlement/runtime subscription thực tế
- `UsageAggregate`: số liệu tổng hợp theo scope và kỳ
- `SettlementStatement`: chốt kỳ, đối soát, chốt tiền
- `GroupContact`: danh bạ liên hệ của group
- `AssignmentAudit`: audit cho assignment/schedule
- `SubscriptionAuditLog`: audit cho subscription runtime

### 2.2 Luồng commercial hiện tại

Luồng backend mới đang theo hướng:

1. tạo `PlanTemplate`
2. gắn nhiều `PlanPricingRule`
3. tạo `GroupPlanAssignment` hoặc `RetailPlanSchedule`
4. review approval
5. activate
6. issue `Subscription`
7. ghi usage
8. aggregate usage
9. generate settlement
10. query audit timeline

## 3. Thay đổi entity

### 3.1 Entity mới được thêm

Các entity mới đã được thêm trong [backend/subscription-provider/src/main/java/com/rs/subscription/entity](/E:/PhuongCM/billing-CA/backend/subscription-provider/src/main/java/com/rs/subscription/entity):

- [PlanTemplate.java](/E:/PhuongCM/billing-CA/backend/subscription-provider/src/main/java/com/rs/subscription/entity/PlanTemplate.java)
- [PlanPricingRule.java](/E:/PhuongCM/billing-CA/backend/subscription-provider/src/main/java/com/rs/subscription/entity/PlanPricingRule.java)
- [GroupContact.java](/E:/PhuongCM/billing-CA/backend/subscription-provider/src/main/java/com/rs/subscription/entity/GroupContact.java)
- [GroupPlanAssignment.java](/E:/PhuongCM/billing-CA/backend/subscription-provider/src/main/java/com/rs/subscription/entity/GroupPlanAssignment.java)
- [RetailPlanSchedule.java](/E:/PhuongCM/billing-CA/backend/subscription-provider/src/main/java/com/rs/subscription/entity/RetailPlanSchedule.java)
- [AssignmentAudit.java](/E:/PhuongCM/billing-CA/backend/subscription-provider/src/main/java/com/rs/subscription/entity/AssignmentAudit.java)
- [UsageAggregate.java](/E:/PhuongCM/billing-CA/backend/subscription-provider/src/main/java/com/rs/subscription/entity/UsageAggregate.java)
- [SettlementStatement.java](/E:/PhuongCM/billing-CA/backend/subscription-provider/src/main/java/com/rs/subscription/entity/SettlementStatement.java)
- [UserRole.java](/E:/PhuongCM/billing-CA/backend/subscription-provider/src/main/java/com/rs/subscription/entity/UserRole.java)
- [UserRoleId.java](/E:/PhuongCM/billing-CA/backend/subscription-provider/src/main/java/com/rs/subscription/entity/UserRoleId.java)

### 3.2 Entity cũ bị bỏ

Các entity cũ đã bị loại bỏ hoặc không còn được dùng theo model cũ:

- `Plan.java` đã bị xóa
- `UserAccount.roles` kiểu `@ManyToMany` đã bị thay bằng `UserRole`

### 3.3 Entity được chỉnh sửa

Các entity sau đã được chỉnh để phù hợp mô hình mới:

- [Subscription.java](/E:/PhuongCM/billing-CA/backend/subscription-provider/src/main/java/com/rs/subscription/entity/Subscription.java)
- [Group.java](/E:/PhuongCM/billing-CA/backend/subscription-provider/src/main/java/com/rs/subscription/entity/Group.java)
- [GroupMember.java](/E:/PhuongCM/billing-CA/backend/subscription-provider/src/main/java/com/rs/subscription/entity/GroupMember.java)
- [ApprovalRequest.java](/E:/PhuongCM/billing-CA/backend/subscription-provider/src/main/java/com/rs/subscription/entity/ApprovalRequest.java)
- [CertificateProvisioningRecord.java](/E:/PhuongCM/billing-CA/backend/subscription-provider/src/main/java/com/rs/subscription/entity/CertificateProvisioningRecord.java)
- [CertificateUsageRecord.java](/E:/PhuongCM/billing-CA/backend/subscription-provider/src/main/java/com/rs/subscription/entity/CertificateUsageRecord.java)
- [PaymentRecord.java](/E:/PhuongCM/billing-CA/backend/subscription-provider/src/main/java/com/rs/subscription/entity/PaymentRecord.java)
- [SubscriptionAuditLog.java](/E:/PhuongCM/billing-CA/backend/subscription-provider/src/main/java/com/rs/subscription/entity/SubscriptionAuditLog.java)
- [UserAccount.java](/E:/PhuongCM/billing-CA/backend/subscription-provider/src/main/java/com/rs/subscription/entity/UserAccount.java)
- [Role.java](/E:/PhuongCM/billing-CA/backend/subscription-provider/src/main/java/com/rs/subscription/entity/Role.java)

## 4. `UserRole` đã được dựng lại thành entity riêng

### 4.1 Trạng thái trước khi sửa

Trước đây `UserAccount` đang dùng:

- `@ManyToMany`
- join table `user_roles`
- không có class `UserRole`

Điều đó làm:

- khó audit grant/revoke role
- khó thêm metadata cho user-role
- khó mở rộng theo hướng `assignedBy`, `assignedAt`, `revokedAt`, `status`

### 4.2 Trạng thái sau khi sửa

Hiện tại:

- [UserRole.java](/E:/PhuongCM/billing-CA/backend/subscription-provider/src/main/java/com/rs/subscription/entity/UserRole.java) là entity riêng
- [UserRoleId.java](/E:/PhuongCM/billing-CA/backend/subscription-provider/src/main/java/com/rs/subscription/entity/UserRoleId.java) là khóa ghép
- [UserAccount.java](/E:/PhuongCM/billing-CA/backend/subscription-provider/src/main/java/com/rs/subscription/entity/UserAccount.java) dùng `@OneToMany userRoles`
- [Role.java](/E:/PhuongCM/billing-CA/backend/subscription-provider/src/main/java/com/rs/subscription/entity/Role.java) cũng có `@OneToMany userRoles`

### 4.3 Tương thích ngược trong code

Để tránh phải sửa toàn bộ auth/user flow cùng lúc, `UserAccount` vẫn giữ helper:

- `getRoles()`
- `setRoles(List<Role>)`

Nhờ vậy:

- `AuthService`
- `JwtService`
- `LocalAuthFilter`
- `UserService`

vẫn có thể chạy logic cũ theo danh sách role, nhưng dưới DB/JPA thì mapping thật đã là `UserRole`.

### 4.4 Repository liên quan

Đã thêm:

- [UserRoleRepository.java](/E:/PhuongCM/billing-CA/backend/subscription-provider/src/main/java/com/rs/subscription/repository/UserRoleRepository.java)

Đã sửa:

- [UserAccountRepository.java](/E:/PhuongCM/billing-CA/backend/subscription-provider/src/main/java/com/rs/subscription/repository/UserAccountRepository.java)
- [RoleService.java](/E:/PhuongCM/billing-CA/backend/subscription-provider/src/main/java/com/rs/subscription/service/RoleService.java)

## 5. Repository mới và repository được chỉnh

### 5.1 Repository mới

Trong [backend/subscription-provider/src/main/java/com/rs/subscription/repository](/E:/PhuongCM/billing-CA/backend/subscription-provider/src/main/java/com/rs/subscription/repository) đã thêm:

- [PlanTemplateRepository.java](/E:/PhuongCM/billing-CA/backend/subscription-provider/src/main/java/com/rs/subscription/repository/PlanTemplateRepository.java)
- [PlanPricingRuleRepository.java](/E:/PhuongCM/billing-CA/backend/subscription-provider/src/main/java/com/rs/subscription/repository/PlanPricingRuleRepository.java)
- [GroupContactRepository.java](/E:/PhuongCM/billing-CA/backend/subscription-provider/src/main/java/com/rs/subscription/repository/GroupContactRepository.java)
- [GroupPlanAssignmentRepository.java](/E:/PhuongCM/billing-CA/backend/subscription-provider/src/main/java/com/rs/subscription/repository/GroupPlanAssignmentRepository.java)
- [RetailPlanScheduleRepository.java](/E:/PhuongCM/billing-CA/backend/subscription-provider/src/main/java/com/rs/subscription/repository/RetailPlanScheduleRepository.java)
- [AssignmentAuditRepository.java](/E:/PhuongCM/billing-CA/backend/subscription-provider/src/main/java/com/rs/subscription/repository/AssignmentAuditRepository.java)
- [UsageAggregateRepository.java](/E:/PhuongCM/billing-CA/backend/subscription-provider/src/main/java/com/rs/subscription/repository/UsageAggregateRepository.java)
- [SettlementStatementRepository.java](/E:/PhuongCM/billing-CA/backend/subscription-provider/src/main/java/com/rs/subscription/repository/SettlementStatementRepository.java)
- [UserRoleRepository.java](/E:/PhuongCM/billing-CA/backend/subscription-provider/src/main/java/com/rs/subscription/repository/UserRoleRepository.java)

### 5.2 Repository được chỉnh

- [SubscriptionRepository.java](/E:/PhuongCM/billing-CA/backend/subscription-provider/src/main/java/com/rs/subscription/repository/SubscriptionRepository.java)
- [ApprovalRequestRepository.java](/E:/PhuongCM/billing-CA/backend/subscription-provider/src/main/java/com/rs/subscription/repository/ApprovalRequestRepository.java)
- [PaymentRecordRepository.java](/E:/PhuongCM/billing-CA/backend/subscription-provider/src/main/java/com/rs/subscription/repository/PaymentRecordRepository.java)
- [CertificateUsageRecordRepository.java](/E:/PhuongCM/billing-CA/backend/subscription-provider/src/main/java/com/rs/subscription/repository/CertificateUsageRecordRepository.java)
- [CertificateProvisioningRepository.java](/E:/PhuongCM/billing-CA/backend/subscription-provider/src/main/java/com/rs/subscription/repository/CertificateProvisioningRepository.java)

## 6. DTO mới

### 6.1 DTO request mới

Đã thêm nhiều request DTO mới trong [dto/request](/E:/PhuongCM/billing-CA/backend/subscription-provider/src/main/java/com/rs/subscription/dto/request):

- `CreatePlanTemplateRequest`
- `PlanPricingRuleRequest`
- `CreateGroupContactRequest`
- `CreateGroupPlanAssignmentRequest`
- `CreateRetailPlanScheduleRequest`
- `CreateRuntimeSubscriptionRequest`
- `UpsertUsageAggregateRequest`
- `CreateSettlementStatementRequest`
- `ReviewCommercialRequest`
- `ExecuteGroupAssignmentFlowRequest`
- `ExecuteRetailPlanFlowRequest`
- `GenerateSettlementFlowRequest`

### 6.2 DTO response mới

Đã thêm trong [dto/response](/E:/PhuongCM/billing-CA/backend/subscription-provider/src/main/java/com/rs/subscription/dto/response):

- `PlanTemplateResponse`
- `PlanPricingRuleResponse`
- `GroupContactResponse`
- `GroupPlanAssignmentResponse`
- `AssignmentAuditResponse`
- `RetailPlanScheduleResponse`
- `RuntimeSubscriptionResponse`
- `UsageAggregateResponse`
- `SettlementStatementResponse`
- `CommercialFlowResponse`
- `AuditTimelineResponse`
- `AuditTimelineEntryResponse`

### 6.3 DTO cũ đã bỏ

Đã xóa các DTO cũ không còn phù hợp:

- `AssignGroupPlanRequest`
- `CreatePlanRequest`
- `CreateSubscriptionRequest`
- `ReviewApprovalRequest`
- `SubmitApprovalRequest`
- `PartnerPlanActionResponse`
- `PlanResponse`
- `SubscriptionResponse`

## 7. Service mới

### 7.1 Service domain mới

Đã thêm:

- [PlanTemplateService.java](/E:/PhuongCM/billing-CA/backend/subscription-provider/src/main/java/com/rs/subscription/service/PlanTemplateService.java)
- [GroupContactService.java](/E:/PhuongCM/billing-CA/backend/subscription-provider/src/main/java/com/rs/subscription/service/GroupContactService.java)
- [GroupPlanAssignmentService.java](/E:/PhuongCM/billing-CA/backend/subscription-provider/src/main/java/com/rs/subscription/service/GroupPlanAssignmentService.java)
- [RetailPlanScheduleService.java](/E:/PhuongCM/billing-CA/backend/subscription-provider/src/main/java/com/rs/subscription/service/RetailPlanScheduleService.java)
- [RuntimeSubscriptionService.java](/E:/PhuongCM/billing-CA/backend/subscription-provider/src/main/java/com/rs/subscription/service/RuntimeSubscriptionService.java)
- [ApprovalRequestService.java](/E:/PhuongCM/billing-CA/backend/subscription-provider/src/main/java/com/rs/subscription/service/ApprovalRequestService.java)
- [UsageAggregateService.java](/E:/PhuongCM/billing-CA/backend/subscription-provider/src/main/java/com/rs/subscription/service/UsageAggregateService.java)
- [SettlementStatementService.java](/E:/PhuongCM/billing-CA/backend/subscription-provider/src/main/java/com/rs/subscription/service/SettlementStatementService.java)

### 7.2 Service orchestration mới

Đã thêm:

- [CommercialOrchestrationService.java](/E:/PhuongCM/billing-CA/backend/subscription-provider/src/main/java/com/rs/subscription/service/CommercialOrchestrationService.java)

Service này đang xử lý:

- approve assignment/schedule
- activate assignment/schedule
- issue runtime subscription
- summarize usage
- generate settlement
- upsert aggregate theo group và kỳ

### 7.3 Service timeline mới

Đã thêm:

- [AuditTimelineService.java](/E:/PhuongCM/billing-CA/backend/subscription-provider/src/main/java/com/rs/subscription/service/AuditTimelineService.java)

Timeline hiện gom dữ liệu từ:

- `SubscriptionAuditLog`
- `AssignmentAudit`
- `ApprovalRequest`
- `PaymentRecord`
- `SettlementStatement`

## 8. AOP cho audit log database

### 8.1 Mục tiêu

Yêu cầu mới là phần ghi log vào database không được để service business tự `save` thủ công nữa, mà phải dùng AOP.

### 8.2 Cài đặt

Đã thêm dependency:

- `spring-boot-starter-aop`

Đã thêm annotation:

- [TrackSubscriptionAudit.java](/E:/PhuongCM/billing-CA/backend/subscription-provider/src/main/java/com/rs/subscription/aop/TrackSubscriptionAudit.java)
- [TrackAssignmentAudit.java](/E:/PhuongCM/billing-CA/backend/subscription-provider/src/main/java/com/rs/subscription/aop/TrackAssignmentAudit.java)
- [TrackAdminAudit.java](/E:/PhuongCM/billing-CA/backend/subscription-provider/src/main/java/com/rs/subscription/aop/TrackAdminAudit.java)

Đã thêm aspect:

- [DatabaseAuditAspect.java](/E:/PhuongCM/billing-CA/backend/subscription-provider/src/main/java/com/rs/subscription/aop/DatabaseAuditAspect.java)

### 8.3 Audit đang được AOP ghi

Aspect hiện đang tự ghi vào:

- `subscription_audit_logs`
- `assignment_audits`
- `admin_audit_logs`

### 8.4 Các service đã chuyển sang AOP

- [RuntimeSubscriptionService.java](/E:/PhuongCM/billing-CA/backend/subscription-provider/src/main/java/com/rs/subscription/service/RuntimeSubscriptionService.java)
- [GroupPlanAssignmentService.java](/E:/PhuongCM/billing-CA/backend/subscription-provider/src/main/java/com/rs/subscription/service/GroupPlanAssignmentService.java)
- [RetailPlanScheduleService.java](/E:/PhuongCM/billing-CA/backend/subscription-provider/src/main/java/com/rs/subscription/service/RetailPlanScheduleService.java)
- [AdminAuditLogService.java](/E:/PhuongCM/billing-CA/backend/subscription-provider/src/main/java/com/rs/subscription/service/AdminAuditLogService.java)
- [AuditLogService.java](/E:/PhuongCM/billing-CA/backend/subscription-provider/src/main/java/com/rs/subscription/service/AuditLogService.java)

Hiện tại các thao tác `save` log cho các bảng audit chỉ còn nằm trong aspect.

## 9. API controller mới

### 9.1 Controller mới trong `subscription-provider`

Đã thêm các controller sau trong [backend/subscription-provider/src/main/java/com/rs/subscription/controller](/E:/PhuongCM/billing-CA/backend/subscription-provider/src/main/java/com/rs/subscription/controller):

- [PlanTemplateController.java](/E:/PhuongCM/billing-CA/backend/subscription-provider/src/main/java/com/rs/subscription/controller/PlanTemplateController.java)
- [GroupCommercialController.java](/E:/PhuongCM/billing-CA/backend/subscription-provider/src/main/java/com/rs/subscription/controller/GroupCommercialController.java)
- [RetailPlanScheduleController.java](/E:/PhuongCM/billing-CA/backend/subscription-provider/src/main/java/com/rs/subscription/controller/RetailPlanScheduleController.java)
- [RuntimeSubscriptionController.java](/E:/PhuongCM/billing-CA/backend/subscription-provider/src/main/java/com/rs/subscription/controller/RuntimeSubscriptionController.java)
- [ApprovalRequestController.java](/E:/PhuongCM/billing-CA/backend/subscription-provider/src/main/java/com/rs/subscription/controller/ApprovalRequestController.java)
- [UsageAggregateController.java](/E:/PhuongCM/billing-CA/backend/subscription-provider/src/main/java/com/rs/subscription/controller/UsageAggregateController.java)
- [SettlementStatementController.java](/E:/PhuongCM/billing-CA/backend/subscription-provider/src/main/java/com/rs/subscription/controller/SettlementStatementController.java)
- [CommercialOrchestrationController.java](/E:/PhuongCM/billing-CA/backend/subscription-provider/src/main/java/com/rs/subscription/controller/CommercialOrchestrationController.java)
- [AuditTimelineController.java](/E:/PhuongCM/billing-CA/backend/subscription-provider/src/main/java/com/rs/subscription/controller/AuditTimelineController.java)

### 9.2 Route hiện tại

Các nhóm route chính:

- `/api/v1/plan-templates`
- `/api/v1/groups/{groupId}/contacts`
- `/api/v1/groups/{groupId}/plan-assignments`
- `/api/v1/groups/plan-assignments`
- `/api/v1/retail-plan-schedules`
- `/api/v1/runtime-subscriptions`
- `/api/v1/approval-requests`
- `/api/v1/usage-aggregates`
- `/api/v1/settlement-statements`
- `/api/v1/commercial-flows/...`
- `/api/v1/audit-timelines/...`

## 10. Audit timeline API

Đã thêm các endpoint timeline trong [AuditTimelineController.java](/E:/PhuongCM/billing-CA/backend/subscription-provider/src/main/java/com/rs/subscription/controller/AuditTimelineController.java):

- `GET /api/v1/audit-timelines/subscriptions/{subscriptionId}`
- `GET /api/v1/audit-timelines/group-plan-assignments/{assignmentId}`
- `GET /api/v1/audit-timelines/retail-plan-schedules/{scheduleId}`
- `GET /api/v1/audit-timelines/settlement-statements/{settlementStatementId}`

Nguồn dữ liệu timeline:

- `subscription`: `SubscriptionAuditLog` + `PaymentRecord`
- `group assignment`: `ApprovalRequest` + `AssignmentAudit` + `PaymentRecord`
- `retail schedule`: `ApprovalRequest` + `AssignmentAudit`
- `settlement`: `SettlementStatement` + `PaymentRecord`

## 11. Commercial flow API

Đã thêm orchestration controller:

- [CommercialOrchestrationController.java](/E:/PhuongCM/billing-CA/backend/subscription-provider/src/main/java/com/rs/subscription/controller/CommercialOrchestrationController.java)

API:

- `POST /api/v1/commercial-flows/group-assignments/{assignmentId}/execute`
- `POST /api/v1/commercial-flows/retail-plan-schedules/{scheduleId}/execute`
- `POST /api/v1/commercial-flows/groups/{groupId}/settlement`

Luồng được hỗ trợ:

- approve
- activate
- issue subscription
- aggregate usage
- generate settlement

## 12. Migration database

Đã thêm migration:

- [V27__commercial_domain_rebuild.sql](/E:/PhuongCM/billing-CA/backend/subscription-provider/src/main/resources/db/migration/V27__commercial_domain_rebuild.sql)

Migration này phục vụ mô hình commercial mới.

Lưu ý:

- phần `UserRole` hiện đã được dựng thành entity riêng trong code
- nếu muốn tận dụng metadata mở rộng cho `user_roles` như `assigned_by`, `assigned_at`, `status`, `revoked_at` thì cần thêm migration tiếp theo

## 13. Loại bỏ service/controller cũ

### 13.1 Service cũ đã bỏ

Đã xóa nhiều service/scheduler cũ không còn phù hợp mô hình mới:

- `ApprovalService`
- `PlanService`
- `SubscriptionService`
- `GroupService`
- `ReportService`
- `ValidationService`
- `PaymentRecordService`
- `CertificateProvisioningService`
- `NotificationService`
- các scheduler cũ liên quan

### 13.2 Controller cũ ở `plan-mng-web` đã bỏ

Đã xóa:

- [ApprovalController.java](/E:/PhuongCM/billing-CA/backend/plan-mng-web/src/main/java/com/rs/subscription/controller/ApprovalController.java)
- [GroupController.java](/E:/PhuongCM/billing-CA/backend/plan-mng-web/src/main/java/com/rs/subscription/controller/GroupController.java)
- [InternalController.java](/E:/PhuongCM/billing-CA/backend/plan-mng-web/src/main/java/com/rs/subscription/controller/InternalController.java)
- [PlanController.java](/E:/PhuongCM/billing-CA/backend/plan-mng-web/src/main/java/com/rs/subscription/controller/PlanController.java)
- [ReportController.java](/E:/PhuongCM/billing-CA/backend/plan-mng-web/src/main/java/com/rs/subscription/controller/ReportController.java)
- [SubscriptionController.java](/E:/PhuongCM/billing-CA/backend/plan-mng-web/src/main/java/com/rs/subscription/controller/SubscriptionController.java)
- [WebhookController.java](/E:/PhuongCM/billing-CA/backend/plan-mng-web/src/main/java/com/rs/subscription/controller/WebhookController.java)
- [InternalCertificateController.java](/E:/PhuongCM/billing-CA/backend/plan-mng-web/src/main/java/com/rs/subscription/controller/internal/InternalCertificateController.java)

Đồng thời [SystemSettingController.java](/E:/PhuongCM/billing-CA/backend/plan-mng-web/src/main/java/com/rs/subscription/controller/SystemSettingController.java) đã được sửa để bỏ phụ thuộc `NotificationService` cũ.

### 13.3 Controller cũ ở `subscription-api` đã bỏ

Đã xóa:

- [CertificateApiController.java](/E:/PhuongCM/billing-CA/backend/subscription-api/src/main/java/com/rs/subscription/controller/CertificateApiController.java)
- [PlanApiController.java](/E:/PhuongCM/billing-CA/backend/subscription-api/src/main/java/com/rs/subscription/controller/PlanApiController.java)
- [SubscriptionApiController.java](/E:/PhuongCM/billing-CA/backend/subscription-api/src/main/java/com/rs/subscription/controller/SubscriptionApiController.java)
- [VerifyApiController.java](/E:/PhuongCM/billing-CA/backend/subscription-api/src/main/java/com/rs/subscription/controller/VerifyApiController.java)

Lý do:

- các controller này vẫn bám mô hình `PlanService / SubscriptionService / ValidationService / CertificateProvisioningService` cũ
- sau khi xóa model cũ thì build toàn backend sẽ vỡ
- bộ controller mới đã có sẵn trong `subscription-provider`, nên việc giữ controller cũ là dư thừa và gây nhiễu

## 14. Trạng thái compile

### 14.1 Compile module `subscription-provider`

Đã compile thành công:

```powershell
mvn -q -pl subscription-provider -am -DskipTests compile
```

chạy tại [backend](/E:/PhuongCM/billing-CA/backend)

### 14.2 Compile toàn backend

Đã compile thành công:

```powershell
mvn -q -DskipTests compile
```

chạy tại [backend](/E:/PhuongCM/billing-CA/backend)

Điều này xác nhận:

- `subscription-provider`
- `plan-mng-web`
- `subscription-api`
- `thirdparty-api`

đã không còn lỗi compile source ở trạng thái hiện tại.

## 15. Những điểm còn mở

Mặc dù compile đã qua, vẫn còn một số phần có thể cần làm tiếp:

- viết migration riêng cho `UserRole` nếu muốn lưu metadata đầy đủ
- bổ sung audit riêng cho settlement nếu không muốn timeline chỉ dựng từ `SettlementStatement + PaymentRecord`
- thêm alias controller nếu frontend hoặc service cũ vẫn đang gọi URL cũ
- cập nhật frontend để dùng route mới thay vì route `plan/subscription/report/group` cũ
- thêm integration test cho:
  - commercial flow
  - audit timeline
  - AOP audit
  - user-role grant/revoke

## 16. Kết luận

Backend hiện tại đã được chuyển sang mô hình commercial mới với các đặc điểm chính:

- schema nghiệp vụ rõ ràng hơn
- `UserRole` là entity riêng
- audit database được ghi bằng AOP
- timeline audit truy vấn được theo domain
- flow commercial có orchestration riêng
- controller cũ ở các module web/api đã bị loại bỏ để tránh phụ thuộc mô hình cũ
- compile toàn bộ backend hiện đã thành công

