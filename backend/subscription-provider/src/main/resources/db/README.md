# Hướng dẫn tài nguyên database

Thư mục `backend/subscription-provider/src/main/resources/db` chứa toàn bộ SQL phục vụ migration, reset, seed dữ liệu và các script tùy chọn khi deploy.

## Cấu trúc thư mục

### `migration/`

Chứa các migration do Flyway tự động chạy. Với cấu hình hiện tại:

```yaml
spring.flyway.locations: classpath:db/migration
```

Khi deploy server hoặc tạo database mới, Flyway chỉ chạy các file trong thư mục này.

Thứ tự migration hiện tại:

1. `V1__init_schema.sql`
2. `V2__plan_subject_config_minio.sql`

Các migration fix/seed cũ như `V3`, `V4`, `V5`, `V7`, `V8` đã được gộp vào `V1__init_schema.sql` hoặc chuyển ra khỏi luồng tự động. Không có `V6` trong `migration/` vì partition là tùy chọn và phụ thuộc môi trường.

### `manual/`

Chứa các script chạy tay để bảo trì hoặc reset database.

- `reset_schema_for_flyway_rerun.sql`: xóa toàn bộ bảng ứng dụng và `flyway_schema_history` để Flyway chạy lại từ `V1`.
- `truncate_all_application_tables.sql`: xóa dữ liệu bằng `TRUNCATE`, giữ nguyên schema và lịch sử Flyway.
- `reset_permissions.sql`: script reset/phục hồi permission.

Nếu muốn xóa sạch và chạy lại từ đầu, dùng `reset_schema_for_flyway_rerun.sql`, sau đó start backend để Flyway tự migrate lại.

### `optional/`

Chứa script tùy chọn, không tự động chạy bởi Flyway.

- `partition_high_volume_tables.sql`
- `partition_additional_event_tables.sql`

Các file này dùng cho partition hoặc tối ưu hiệu năng. Chỉ chạy sau khi đã kiểm tra kỹ trên môi trường server tương ứng. Tên file cố ý không dùng dạng `V...__...sql` để tránh Flyway nhận nhầm là migration.

### `seed/`

Chứa dữ liệu seed/demo cho dev, QA hoặc kiểm thử màn hình báo cáo. Không tự động chạy khi deploy production.

- `seed/report-usage-tracking/seed_reports_usage_tracking.sql`: seed dữ liệu cho trang report và individual usage tracking.
- `seed/legacy/`: các seed demo cũ, giữ lại để tham khảo hoặc test local.

### `archive/`

Chứa các migration cũ đã được thay thế, chỉ dùng để tham khảo. Không đưa các file này lại vào `migration/` khi reset database từ đầu.

## Seed report và usage tracking

File seed chính:

```text
db/seed/report-usage-tracking/seed_reports_usage_tracking.sql
```

File này phục vụ các API:

- `GET /api/v1/reports/group`
- `GET /api/v1/reports/individual`
- `GET /api/v1/individual/usage-tracking`

Mapping frontend:

| File Vue | Helper | Endpoint backend |
| --- | --- | --- |
| `frontend/src/views/reports/index.vue` | `getGroupReport(periodKey)` | `GET /api/v1/reports/group?periodKey=yyyy-MM` |
| `frontend/src/views/reports/IndividualReport.vue` | `getIndividualReport(periodKey)` | `GET /api/v1/reports/individual?periodKey=yyyy-MM` |
| `frontend/src/views/individual/UsageTracking.vue` | `getIndividualUsageTracking(params)` | `GET /api/v1/individual/usage-tracking` |

Các bảng được seed:

1. `user_accounts`
2. `plan_templates`
3. `plan_pricing_rules`
4. `groups`
5. `partner_group_access`
6. `group_plan_assignments`
7. `subscriptions`
8. `usage_aggregates`
9. `certificate_provisioning_records`
10. `certificate_usage_records`
11. `document_upload_records`
12. `certificate_auth_failure_records`

Script tạo tối thiểu 15 bản ghi cho mỗi bảng, dùng dải ID `900000+` và `ON DUPLICATE KEY UPDATE` để có thể chạy lại nhiều lần.

## Hướng dẫn deploy production

Khi deploy production:

1. Giữ cấu hình `spring.flyway.locations=classpath:db/migration`.
2. Không thêm `manual/`, `optional/`, `seed/`, hoặc `archive/` vào `flyway.locations`.
3. Chỉ chạy script ngoài `migration/` khi có mục tiêu rõ ràng và đã xác nhận đúng môi trường.
4. Nếu cần reset database để chạy lại từ đầu, chạy `manual/reset_schema_for_flyway_rerun.sql`, rồi start backend để Flyway chạy lại từ `V1`.

## Ghi chú

`TRUNCATE` chỉ xóa dữ liệu, không làm Flyway chạy lại migration. Muốn Flyway chạy lại từ đầu thì phải xóa cả schema và bảng `flyway_schema_history`, vì vậy hãy dùng `reset_schema_for_flyway_rerun.sql`.
