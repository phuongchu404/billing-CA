# De xuat thiet ke lai entity va database

## Pham vi da doc

Tai lieu nay duoc tong hop sau khi doc:

- Toan bo file con lai trong `frontend/src/views`
- Toan bo entity trong `backend/subscription-provider/src/main/java/com/rs/subscription/entity`
- Kieu du lieu frontend trong `frontend/src/types/index.ts`

Muc tieu khong chi la sua man `plans`, ma la de xuat lai model entity/database hien tai sao cho dap ung dong bo:

- quan ly dai ly
- quan ly goi cho dai ly
- quan ly goi cho khach hang pho thong
- subscription runtime
- provisioning certificate
- usage tracking
- reporting
- approval workflow
- audit

## 1. Nhan xet tong the

He thong hien tai dang co 3 lop nghiep vu bi tron vao nhau:

1. `Product / commercial configuration`
La dinh nghia goi, bang gia, dieu kien, doi tuong, hieu luc.

2. `Assignment / selling workflow`
La viec mot goi duoc de xuat, phe duyet, ap dung cho dai ly hoac cho toan bo khach hang pho thong.

3. `Runtime entitlement / operational execution`
La subscription dang chay, certificate da cap, usage da phat sinh, thanh toan, doi soat.

Backend hien tai moi tach duoc mot phan:

- `Plan` = mot catalog don gian
- `Subscription` = mot runtime record

Nhung frontend lai dang can nhieu hon the:

- nhieu dong pricing rule
- copy plan mau
- apply plan co workflow
- usage va settlement
- current plan / next plan / history plan
- report theo nhom khach hang va theo loai CTS

Vi vay database hien tai chua du va dang lech nghiep vu.

## 2. Doc theo tung cum man hinh

## 2.1 Cum `frontend/src/views/plans`

Cum nay dang dai dien cho nghiep vu dai ly.

Nhu cau tu UI:

- Tao dai ly moi cung thong tin lien he
- Tao goi rieng cho dai ly
- Clone goi mau
- Quan ly danh sach goi cua tung dai ly
- Request apply / approve / active / stop apply
- Xem history ap dung
- Xuat doi soat
- Xem so lieu `ctsCreated`, `signingUsed`, `%`

Van de:

- UI dang coi "plan" vua la goi mau, vua la assignment, vua la history, vua la report snapshot.
- `Create.vue` con dang tron viec tao `group` va tao `plan`.

## 2.2 Cum `frontend/src/views/individual`

Cum nay mo ta nghiep vu khach hang pho thong.

Nhu cau tu UI:

- Quan ly catalog goi cho khach hang pho thong
- Goi co nhieu dong rule theo:
  - `INDIVIDUAL`
  - `ORGANIZATION`
  - `INDIVIDUAL_OF_ORG`
- Moi rule co:
  - `durationMonths`
  - `condition`
  - `minValue`
  - `maxValue`
  - `fee`
- Co workflow apply plan:
  - `AVAILABLE`
  - `UNAVAILABLE`
  - `PENDING`
  - `APPROVED`
  - `APPLYING`
- Co lich su cap nhat trang thai
- Co man `Subscriptions.vue` cho runtime subscription
- Co man `UsageTracking.vue` cho usage theo account / CTS / fee

Van de:

- `Plans.vue` dang lam catalog level.
- `PlanConfig*.vue` dang lam pricing template level.
- `Subscriptions.vue` dang la runtime entitlement level.
- `UsageTracking.vue` dang la reporting / settlement level.

Bon lop nay hien tai chua duoc phan ra ro trong entity backend.

## 2.3 Cum `reports`

Bao cao dang can:

- thong ke cho `GROUP`
- thong ke cho `INDIVIDUAL`
- SL CTS moi
- SL luot ky
- tan suat, growth
- expiring soon
- ratio signings / certificate

Nghia la can mot lop du lieu aggregate/reporting rieng. Neu lay truc tiep tu `plans` hoac `subscriptions` se rat kho va de sai nghia.

## 2.4 Cum `audit-logs`, `users`, `roles`, `home`

Phan nay cho thay:

- he thong can audit thao tac admin
- co RBAC
- co user account
- co refresh token

Backend da co nen tang nay, nhung audit hien tai van la generic. Chua co audit chuyen biet cho assignment / pricing / approval workflow.

## 3. Doc theo entity backend hien tai

## 3.1 `Plan`

`Plan` hien tai dang gom:

- `planCode`, `planName`
- `price`, `currency`
- `planType`
- `validityDays`
- `validityAmount`, `validityUnit`
- `maxSigningQuota`
- `maxMembers`
- `effectiveFrom`, `effectiveTo`
- `isVisible`, `isActive`
- `allowBulkSigning`, `allowApiAccess`
- `isGroupPlan`
- `groupMemberValidityMode`

Van de:

- Model nay phu hop cho mot goi don gian 1 gia, 1 quota, 1 validity.
- Khong phu hop voi UI dang can nhieu dong bang gia.
- `price` va `maxSigningQuota` la thuoc tinh "1 dong", nhung UI dang la nhieu dong theo range.
- `isGroupPlan` dang tron 2 domain:
  - group partner plan
  - retail catalog

Ket luan:

- `Plan` khong nen tiep tuc la entity duy nhat cho nghiep vu plan.

## 3.2 `Subscription`

`Subscription` hien tai dung cho runtime:

- `subscriberType`
- `userId` hoac `group`
- `plan`
- `status`
- `startDate`, `endDate`
- `signingQuotaTotal`, `signingQuotaUsed`
- `paymentReference`

Model nay hop ly neu xem subscription la entitlement dang chay.

Van de:

- frontend workflow `pending -> approved -> applying` cua plan dang bi ep map vao `Subscription.status`.
- `SubscriptionStatus` hien tai la:
  - `PENDING`
  - `ACTIVE`
  - `EXPIRED`
  - `CANCELLED`
  - `SUSPENDED`

No khong du de bieu dien assignment workflow.

Ket luan:

- `Subscription` nen duoc giu lam runtime record.
- khong nen dung no de ganh nghiep vu commercial approval/apply.

## 3.3 `Group` va `GroupMember`

`Group` hien tai dang dung la dai ly/partner.

Hop ly:

- co `groupCode`, `groupName`, `status`
- co `refContractNo`
- co `createdBy`

Chua hop ly:

- `contactEmail` va `picEmails` dang de `TEXT`
- frontend xu ly nhu list email
- khong co contact role / primary / notify flag

`GroupMember` dang co:

- `userId`
- `role`
- `memberStartDate`, `memberEndDate`

No dang phan anh mot phan `groupMemberValidityMode`, nhung chua lien ket ro voi assignment runtime nao.

## 3.4 `ApprovalRequest`

`ApprovalRequest` la entity generic:

- `requestType`
- `requestPayload`
- `status`

Co the dung duoc cho nhieu workflow, nhung hien tai:

- payload dang JSON text
- khong co hard FK toi assignment/template
- kho query dashboard va history nghiep vu

Ket luan:

- nen giu cho generic approval neu can
- nhung voi billing/plan workflow nen co bang domain-specific de query va audit cho de

## 3.5 `CertificateProvisioningRecord` va `CertificateUsageRecord`

Hai bang nay cho thay runtime level dang ton tai:

- cap certificate theo subscription
- ghi usage theo `certificateId`

Van de:

- `CertificateUsageRecord` khong lien ket truc tiep `subscription_id`
- usage reporting can phai suy nguoc
- chua co aggregate theo ky doi soat

Ket luan:

- can them lop aggregate/reporting thay vi dung record runtime raw cho tat ca man bao cao.

## 3.6 `PaymentRecord`

Dang lien voi `Subscription`.

Hop ly neu payment la cho runtime subscription.

Van de:

- doi voi nghiep vu bang gia nhieu tier, payment co the can reference toi:
  - assignment
  - invoice / settlement statement
  - snapshot usage

## 3.7 `AdminAuditLog`

Dang la generic audit.

Hop ly cho audit he thong.

Nhung van thieu:

- audit lifecycle cua plan template
- audit assignment workflow
- audit pricing rule changes

## 4. Ket luan kien truc nghiep vu nen tach

He thong nen tach thanh 5 domain ro rang:

1. `Identity & Access`
- `UserAccount`
- `Role`
- `Permission`
- `RefreshToken`

2. `Customer / Organization`
- `Group`
- `GroupMember`
- `GroupContact` moi

3. `Commercial Catalog`
- `PlanTemplate` moi
- `PlanPricingRule` moi
- `PlanTemplateVersion` hoac immutable template version

4. `Commercial Assignment & Approval`
- `GroupPlanAssignment` moi
- `RetailPlanSchedule` moi
- `AssignmentAudit` moi
- co the lien thong voi `ApprovalRequest`

5. `Runtime & Reporting`
- `Subscription`
- `CertificateProvisioningRecord`
- `CertificateUsageRecord`
- `UsageAggregate` moi
- `SettlementStatement` moi neu can doi soat

## 5. Model moi de xuat

## 5.1 `PlanTemplate`

De xuat thay `Plan` bang khung nghia moi:

- `plan_template_id`
- `plan_code`
- `plan_name`
- `customer_segment` = `GROUP` | `INDIVIDUAL`
- `template_scope` = `PUBLIC` | `PARTNER_PRIVATE` | `SYSTEM`
- `status` = `DRAFT` | `AVAILABLE` | `INACTIVE` | `ARCHIVED`
- `effective_from`
- `effective_to`
- `is_visible`
- `allow_bulk_signing`
- `allow_api_access`
- `created_by`
- `created_at`
- `updated_at`
- `cloned_from_template_id` nullable
- `version_no`

Y nghia:

- `price`, `validityDays`, `maxSigningQuota` dua xuong bang rule.
- `customer_segment` giup tach retail va partner.
- `template_scope` giup biet plan cong khai hay plan rieng cua dai ly.

## 5.2 `PlanPricingRule`

Day la bang trung tam de map voi UI.

Cot de xuat:

- `plan_pricing_rule_id`
- `plan_template_id`
- `subject_type` = `INDIVIDUAL` | `ORGANIZATION` | `INDIVIDUAL_OF_ORG`
- `certificate_validity_value`
- `certificate_validity_unit` = `MONTH` | `YEAR`
- `pricing_metric` = `SIGNING_COUNT` | `CERTIFICATE_COUNT`
- `range_min`
- `range_max` nullable
- `unit_price`
- `currency`
- `quota_total` nullable
- `sort_order`
- `is_active`
- `created_at`
- `updated_at`

Giai thich:

- frontend `condition` map vao `pricing_metric`
- `durationMonths` map vao `certificate_validity_value/unit`
- `fee` map vao `unit_price`
- `min/max` map truc tiep

Rang buoc:

- khong overlap range trong cung 1 `plan_template_id + subject_type + pricing_metric + validity`
- `range_max` null nghia la unlimited

## 5.3 `GroupContact`

Thay cho `Group.contactEmail` va `Group.picEmails`.

Cot de xuat:

- `group_contact_id`
- `group_id`
- `contact_type` = `PIC` | `REPRESENTATIVE` | `BILLING`
- `email`
- `full_name` nullable
- `phone` nullable
- `is_primary`
- `receive_usage_alert`
- `is_active`
- `created_at`
- `updated_at`

## 5.4 `GroupPlanAssignment`

Day la bang con thieu nhat hien nay.

Cot de xuat:

- `group_plan_assignment_id`
- `group_id`
- `plan_template_id`
- `assignment_status` = `REQUESTED` | `APPROVED` | `ACTIVE` | `REJECTED` | `STOPPED` | `EXPIRED`
- `requested_by`
- `requested_at`
- `approved_by` nullable
- `approved_at` nullable
- `rejected_by` nullable
- `rejected_at` nullable
- `apply_from`
- `apply_to`
- `activated_at` nullable
- `stopped_at` nullable
- `stop_reason` nullable
- `created_at`
- `updated_at`

Mapping UI:

- `pending` -> `REQUESTED`
- `approved` -> `APPROVED`
- `active/applying` -> `ACTIVE`
- history la toan bo record cua bang nay

## 5.5 `RetailPlanSchedule`

Frontend `individual/PlanConfig*.vue` cho thay retail cung can workflow apply.

Neu retail co 1 goi current + 1 goi next theo thoi gian, thi nen co bang schedule rieng thay vi dung `Subscription`.

Cot de xuat:

- `retail_plan_schedule_id`
- `plan_template_id`
- `schedule_status` = `AVAILABLE` | `REQUESTED` | `APPROVED` | `ACTIVE` | `INACTIVE`
- `apply_from`
- `apply_to`
- `requested_by`
- `approved_by`
- `created_at`
- `updated_at`

Neu nghiep vu retail khong can approval phuc tap, co the don gian hon bang `plan_activation_schedules`.

## 5.6 `AssignmentAudit`

De xuat bang audit chuyen biet:

- `assignment_audit_id`
- `assignment_type` = `GROUP_PLAN` | `RETAIL_PLAN`
- `assignment_id`
- `action` = `REQUEST` | `APPROVE` | `REJECT` | `ACTIVATE` | `STOP` | `EXPIRE`
- `old_status`
- `new_status`
- `actor`
- `note`
- `created_at`

Ly do:

- `AdminAuditLog` la audit he thong tong quat
- `AssignmentAudit` la audit domain, phuc vu list history va dashboard

## 5.7 `Subscription`

Giu lai `Subscription`, nhung doi vai tro ro hon:

- subscription la `runtime entitlement`
- duoc sinh ra tu:
  - retail purchase
  - group plan assignment active

Nen them:

- `source_type` = `RETAIL_PURCHASE` | `GROUP_ASSIGNMENT` | `MANUAL`
- `source_id`
- `plan_template_id` snapshot FK hoac reference
- `pricing_rule_id` nullable

Neu can truy vet nhieu hon:

- `group_plan_assignment_id` nullable
- `retail_plan_schedule_id` nullable

## 5.8 `CertificateProvisioningRecord`

Nen bo sung:

- `group_plan_assignment_id` nullable
- `pricing_rule_id` nullable

de truy vet cap certificate thuoc commercial context nao.

## 5.9 `CertificateUsageRecord`

Nen bo sung:

- `subscription_id` nullable nhung uu tien co
- `group_plan_assignment_id` nullable
- `usage_type`
- `quantity` default 1

De sau nay aggregate theo assignment / statement cho de.

## 5.10 `UsageAggregate`

Day la bang de phuc vu bao cao va doi soat.

Cot de xuat:

- `usage_aggregate_id`
- `aggregate_scope` = `GROUP_ASSIGNMENT` | `RETAIL_PLAN` | `GROUP` | `USER`
- `scope_id`
- `period_type` = `DAY` | `MONTH`
- `period_key`
- `certificates_created`
- `signing_used`
- `active_certificates`
- `expired_certificates`
- `revoked_certificates`
- `amount_due`
- `currency`
- `created_at`
- `updated_at`

Nhu vay:

- `UsageTracking.vue` doc tu bang nay
- `Reports` doc tu bang nay
- `plans/index.vue` cung lay current usage tong hop tu day

## 5.11 `SettlementStatement`

Neu doi soat la mot nghiep vu thuc su, nen co bang rieng:

- `settlement_statement_id`
- `statement_scope` = `GROUP`
- `group_id`
- `from_date`
- `to_date`
- `status` = `DRAFT` | `FINALIZED` | `EXPORTED`
- `total_certificates`
- `total_signings`
- `total_amount`
- `currency`
- `generated_at`
- `generated_by`

Khong nen coi export doi soat chi la mot action tam thoi.

## 6. Entity hien tai nen sua the nao

## 6.1 `Plan`

Khuyen nghi:

### Cach dung hon

- Tao entity moi `PlanTemplate`
- Chuyen `Plan` hien tai thanh `PlanTemplate`
- Tach `PlanPricingRule`

### Neu muon it pha vo

Giu entity `Plan` nhung:

- xem no la template header
- deprecate cac truong:
  - `price`
  - `currency`
  - `validityDays`
  - `validityAmount`
  - `validityUnit`
  - `maxSigningQuota`
  - `maxMembers`
  - `planType`
- bo sung:
  - `customerSegment`
  - `templateScope`
  - `status`
  - `clonedFromPlanId`
  - `versionNo`

## 6.2 `Subscription`

Can sua:

- them `sourceType`
- them `sourceId`
- them `pricingRuleId`
- them lien ket ve assignment neu la group assignment

Khong nen them cac status commercial nhu `AVAILABLE`, `APPROVED`.

## 6.3 `Group`

Can sua:

- bo `contactEmail` TEXT
- bo `picEmails` TEXT
- thay bang relation `List<GroupContact>`

Neu chua doi ngay:

- giu cot cu de migration
- danh dau deprecated trong service/DTO

## 6.4 `GroupMember`

Can bo sung hoac quy dinh ro:

- `memberStartDate/memberEndDate` duoc sinh tu assignment nao

Tot hon:

- them `sourceAssignmentId` nullable

## 6.5 `ApprovalRequest`

Khuyen nghi:

- khong xoa
- tiep tuc dung cho generic workflow

Nhung:

- them `entityType`
- them `entityId`
- hoac tao reference domain-specific de query

Neu khong, moi thu se nam trong `requestPayload` va rat kho truy van.

## 6.6 `CertificateProvisioningRecord`

Can bo sung lien ket nguon commercial, nhu tren.

## 6.7 `CertificateUsageRecord`

Can bo sung:

- `subscription_id`
- `group_plan_assignment_id`
- `quantity`

Bang nay hien tai qua toi gian de phuc vu reporting.

## 6.8 `PaymentRecord`

Can xem xet them:

- `statement_id` nullable
- `assignment_id` nullable
- `payment_scope`

de khong chi thanh toan cho runtime subscription ma con co the doi soat theo ky.

## 7. Thiet ke database de xuat

## 7.1 Bang giu nguyen

- `user_accounts`
- `roles`
- `permissions`
- `role_permissions`
- `refresh_tokens`
- `admin_audit_logs`
- `system_settings`

## 7.2 Bang giu nhung sua

- `groups`
- `group_members`
- `plans` hoac doi thanh `plan_templates`
- `subscriptions`
- `certificate_provisioning_records`
- `certificate_usage_records`
- `payment_records`
- `approval_requests`

## 7.3 Bang moi nen tao

- `group_contacts`
- `plan_pricing_rules`
- `group_plan_assignments`
- `retail_plan_schedules`
- `assignment_audits`
- `usage_aggregates`
- `settlement_statements`

Neu can immutable versioning:

- `plan_template_versions`

## 8. Mapping giua cac man hinh va schema moi

## 8.1 `src/views/plans/index.vue`

Doc tu:

- `groups`
- current `group_plan_assignments`
- `usage_aggregates`

Khong doc tu `Partner` DTO don le nhu hien tai.

## 8.2 `src/views/plans/Detail.vue`

Section thong tin dai ly:

- `groups`
- `group_contacts`

Section quan ly goi:

- `group_plan_assignments`

Popup chi tiet goi:

- `plan_templates`
- `plan_pricing_rules`

Section lich su:

- `assignment_audits`

## 8.3 `src/views/plans/Create.vue`

Nen tach logic:

1. Tao `group`
2. Tao hoac clone `plan_template`
3. Neu co apply range thi tao `group_plan_assignment`

Khong nen coi day la mot API tao plan duy nhat.

## 8.4 `src/views/plans/AddPlan.vue`

Nen la:

- tao plan template rieng cua group
hoac
- clone template co san roi tao assignment moi

## 8.5 `src/views/individual/PlanConfig*.vue`

Nen dung:

- `plan_templates` voi `customer_segment = INDIVIDUAL`
- `plan_pricing_rules`
- `retail_plan_schedules`
- `assignment_audits`

## 8.6 `src/views/individual/Plans.vue`

Man nay hien dang dung `Plan` theo kieu 1 gia.

Neu giu man nay, can doi muc dich:

- day la "catalog summary view"
- hien so rule, hieu luc, trang thai, segment

Khong nen la nguon duy nhat de sua goi phuc hop.

## 8.7 `src/views/individual/Subscriptions.vue`

Map vao:

- `subscriptions`
- `subscription_audit_logs`

Man nay nen van giu rieng, vi no la runtime.

## 8.8 `src/views/individual/UsageTracking.vue`

Nen doc tu:

- `usage_aggregates`
- hoac mot reporting view/materialized view

Khong nen suy truc tiep tu raw `certificate_usage_records` tren UI.

## 8.9 `src/views/reports/*`

Nen doc tu:

- `usage_aggregates`
- `settlement_statements`
- `group_plan_assignments`
- `subscriptions`

Va co the can materialized monthly fact table neu du lieu lon.

## 9. De xuat migration thuc te

## Phase 1: Bo sung schema moi

Tao bang:

- `group_contacts`
- `plan_pricing_rules`
- `group_plan_assignments`
- `retail_plan_schedules`
- `assignment_audits`
- `usage_aggregates`
- `settlement_statements`

Them cot vao bang cu:

- `subscriptions.source_type`
- `subscriptions.source_id`
- `subscriptions.pricing_rule_id`
- `certificate_usage_records.subscription_id`
- `certificate_usage_records.group_plan_assignment_id`

## Phase 2: Backfill

- Tach `groups.contactEmail` va `groups.picEmails` sang `group_contacts`
- Map `plans` hien tai thanh header records
- Sinh `plan_pricing_rules` mac dinh tu cac `price/validity/quota` cu neu can
- Map `subscriptions` group hien co thanh `group_plan_assignments` lich su

## Phase 3: Chuyen service

Tach service:

- `PlanTemplateService`
- `PlanPricingRuleService`
- `GroupPlanAssignmentService`
- `RetailPlanScheduleService`
- `UsageAggregateService`

`SubscriptionService` chi giu runtime.

## Phase 4: Chuyen frontend

- `plans/*` dung assignment APIs moi
- `individual/PlanConfig*` dung template + pricing rule APIs moi
- `UsageTracking` va `reports` dung aggregate APIs moi

## Phase 5: Deprecate field cu

Deprecate:

- `plans.price`
- `plans.currency`
- `plans.validity_days`
- `plans.validity_amount`
- `plans.validity_unit`
- `plans.max_signing_quota`
- `plans.max_members`
- `groups.contactEmail`
- `groups.picEmails`

Khong nen xoa vat ly ngay.

## 10. Rang buoc du lieu nen bo sung

## 10.1 `plan_pricing_rules`

- unique logic theo `template + subject + metric + validity + range`
- cam overlap range
- `range_min >= 1`
- `range_max is null or range_max >= range_min`

## 10.2 `group_plan_assignments`

- 1 `group` khong duoc co 2 assignment `ACTIVE` overlap
- `apply_to >= apply_from`
- chi 1 assignment `APPROVED` hoac `REQUESTED` cho cung khoang neu nghiep vu cam overlap

## 10.3 `retail_plan_schedules`

- chi 1 plan retail `ACTIVE` tai mot thoi diem
- schedule overlap phai bi chan

## 10.4 `group_contacts`

- unique `(group_id, email, contact_type)`
- co index cho `receive_usage_alert`

## 10.5 `usage_aggregates`

- unique `(aggregate_scope, scope_id, period_type, period_key)`

## 11. Cau truc entity de xuat chot

Neu phai chot huong sua lai entity/backend, toi khuyen nghi:

### Giu

- `UserAccount`
- `Role`
- `Permission`
- `RolePermission`
- `RefreshToken`
- `AdminAuditLog`
- `SystemSetting`

### Sua

- `Group`
- `GroupMember`
- `Plan` -> doi nghia thanh `PlanTemplate`
- `Subscription`
- `CertificateProvisioningRecord`
- `CertificateUsageRecord`
- `PaymentRecord`
- `ApprovalRequest`

### Tao moi

- `GroupContact`
- `PlanPricingRule`
- `GroupPlanAssignment`
- `RetailPlanSchedule`
- `AssignmentAudit`
- `UsageAggregate`
- `SettlementStatement`

## 12. Ket luan

Van de hien tai khong phai chi la "man plan chua hop ly", ma la database dang thieu tang commercial model.

Neu khong tach tang nay, he thong se tiep tuc gap 4 van de:

1. `Plan` bi nhan qua nhieu y nghia.
2. `Subscription` bi ep ganh workflow khong dung ban chat.
3. `Usage/reporting` kho tinh va kho doi soat.
4. Frontend retail va partner se tiep tuc phan ky va phai mock nhieu field khong co trong backend.

Huong sua dung la:

- tach `template`
- tach `pricing rule`
- tach `assignment/schedule`
- giu `subscription` cho runtime
- tao tang `usage aggregate/settlement`

Do la huong entity/database toi khuyen nghi de dap ung cac man hinh va du lieu hien co trong repo nay.
