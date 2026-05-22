# Docker Compose options trong thư mục deploy

File này tổng hợp các lệnh và option hay dùng khi chạy hệ thống trong thư mục `/opt/billing-ca/deploy`.

Compose file hiện tại có các service:

- `mysql`
- `redis`
- `minio`
- `billing-backend`
- `billing-frontend`

Frontend đang chạy bằng nginx container. Nội dung FE được copy từ `frontend/dist/` vào Docker image tại lúc build, theo `deploy/frontend.Dockerfile`.

## Cấu trúc lệnh cơ bản

Chạy từ thư mục deploy trên server:

```bash
cd /opt/billing-ca/deploy
docker compose --env-file .env <command> [options] [service]
```

Ví dụ:

```bash
docker compose --env-file .env up -d --build
```

## Option chung

### `--env-file .env`

Dùng file `.env` trong thư mục deploy để nạp biến môi trường cho Docker Compose.

Ví dụ trong repo này, `.env` cung cấp các biến như:

- `APP_VERSION`
- `FRONTEND_PORT`
- `BACKEND_PORT`
- `DB_NAME`
- `DB_USERNAME`
- `DB_PASSWORD`
- `REDIS_PORT`
- `MINIO_API_PORT`

Nên luôn dùng:

```bash
docker compose --env-file .env ...
```

Nếu không dùng `--env-file .env`, Compose có thể không đọc đúng port, password, image tag hoặc các config khác.

## `up`

`up` dùng để tạo, build nếu cần, recreate và chạy container.

### Chạy toàn bộ hệ thống

```bash
docker compose --env-file .env up -d --build
```

Ý nghĩa:

- Tạo network và volume nếu chưa có.
- Build image backend và frontend nếu cần.
- Tạo hoặc recreate container.
- Chạy container ở background nhờ `-d`.

### `-d`

Viết tắt của `--detach`.

Chạy container ở background, terminal không bị treo ở log.

```bash
docker compose --env-file .env up -d
```

Nếu không có `-d`, terminal sẽ hiển thị log trực tiếp và giữ phiên chạy.

### `--build`

Build lại image trước khi chạy container.

```bash
docker compose --env-file .env up -d --build
```

Dùng khi:

- Có JAR backend mới.
- Có `frontend/dist` mới.
- Có thay đổi trong `backend.Dockerfile`, `frontend.Dockerfile`, `nginx.conf`.

Với frontend của project này, sau khi copy `dist` mới lên server, phải dùng `--build` hoặc chạy `docker compose build` trước, vì `dist` được `COPY` vào image.

### `--no-build`

Không build image, chỉ dùng image đang có.

```bash
docker compose --env-file .env up -d --no-build
```

Dùng khi chỉ muốn chạy lại container từ image hiện tại.

Không dùng option này nếu vừa thay `frontend/dist` hoặc JAR backend.

### `--no-deps`

Chỉ chạy service được chỉ định, không tự chạy hoặc recreate service phụ thuộc.

```bash
docker compose --env-file .env up -d --no-deps --build billing-frontend
```

Trong repo này, `billing-frontend` có:

```yaml
depends_on:
  - billing-backend
```

Nếu không có `--no-deps`, Compose có thể xử lý thêm `billing-backend`.

Dùng `--no-deps` khi chỉ muốn chạy lại riêng FE hoặc riêng backend.

### `--force-recreate`

Bắt buộc tạo lại container, kể cả khi Compose thấy config không đổi.

```bash
docker compose --env-file .env up -d --force-recreate billing-frontend
```

Dùng khi container có vẻ chưa được recreate dù image hoặc config đã thay đổi.

Với FE, nếu muốn chắc chắn lấy image mới:

```bash
docker compose --env-file .env up -d --no-deps --build --force-recreate billing-frontend
```

### `--no-recreate`

Không recreate container nếu container đã tồn tại.

```bash
docker compose --env-file .env up -d --no-recreate
```

Dùng khi chỉ muốn start những container chưa chạy.

Không phù hợp nếu vừa đổi config, image, JAR hoặc `frontend/dist`.

### `--remove-orphans`

Xóa các container cũ thuộc cùng project nhưng không còn được khai báo trong `docker-compose.yml`.

```bash
docker compose --env-file .env up -d --remove-orphans
```

Dùng sau khi đổi tên service hoặc xóa service khỏi compose file.

## `build`

`build` chỉ build image, không chạy container.

### Build toàn bộ image

```bash
docker compose --env-file .env build
```

### Build riêng frontend

```bash
docker compose --env-file .env build billing-frontend
```

### Build riêng backend

```bash
docker compose --env-file .env build billing-backend
```

### `--no-cache`

Build lại từ đầu, không dùng cache Docker layer cũ.

```bash
docker compose --env-file .env build --no-cache billing-frontend
```

Dùng khi:

- Đã copy `dist` mới nhưng FE vẫn hiện giao diện cũ.
- Docker build nghi dùng cache cũ.
- Muốn chắc chắn image được build sạch.

Sau đó chạy lại container:

```bash
docker compose --env-file .env up -d --no-deps billing-frontend
```

Có thể gộp thành:

```bash
docker compose --env-file .env build --no-cache billing-frontend
docker compose --env-file .env up -d --no-deps billing-frontend
```

### `--pull`

Kéo base image mới trước khi build.

```bash
docker compose --env-file .env build --pull billing-frontend
```

Ví dụ FE dùng base image:

```dockerfile
FROM nginx:1.25-alpine
```

`--pull` sẽ thử kéo lại image `nginx:1.25-alpine` mới nhất tương ứng tag đó.

## `restart`

Restart container đang tồn tại, không build lại image.

```bash
docker compose --env-file .env restart billing-frontend
```

Chỉ dùng khi:

- Muốn restart nginx frontend container hiện tại.
- Không thay `frontend/dist`.
- Không thay Dockerfile hoặc nginx config.

Không đủ nếu vừa copy `dist` mới, vì `dist` đã nằm trong image.

## `stop`

Dừng container nhưng không xóa container, image, volume.

```bash
docker compose --env-file .env stop billing-frontend
```

Dừng toàn bộ hệ thống:

```bash
docker compose --env-file .env stop
```

## `start`

Start lại container đã bị stop, không build, không recreate.

```bash
docker compose --env-file .env start billing-frontend
```

Không dùng nếu cần lấy image mới.

## `down`

Dừng và xóa container + network của project Compose.

```bash
docker compose --env-file .env down
```

Mặc định không xóa named volumes như:

- `mysql_data`
- `redis_data`
- `minio_data`

### `down -v`

Xóa cả volumes.

```bash
docker compose --env-file .env down -v
```

Cẩn thận: lệnh này có thể xóa dữ liệu database, redis và minio vì project đang dùng named volumes.

Không dùng trên production trừ khi chắc chắn muốn xóa dữ liệu.

## `ps`

Xem trạng thái container.

```bash
docker compose --env-file .env ps
```

Xem riêng FE:

```bash
docker compose --env-file .env ps billing-frontend
```

## `logs`

Xem log container.

### Xem log frontend

```bash
docker compose --env-file .env logs billing-frontend
```

### Xem log backend

```bash
docker compose --env-file .env logs billing-backend
```

### `-f`

Follow log realtime.

```bash
docker compose --env-file .env logs -f billing-frontend
```

### `--tail`

Chỉ xem N dòng cuối.

```bash
docker compose --env-file .env logs --tail=100 billing-backend
```

Kết hợp:

```bash
docker compose --env-file .env logs -f --tail=100 billing-backend
```

## `exec`

Chạy command bên trong container đang chạy.

### Vào shell frontend container

```bash
docker compose --env-file .env exec billing-frontend sh
```

### Kiểm tra file FE trong nginx container

```bash
docker compose --env-file .env exec billing-frontend ls -la /usr/share/nginx/html
```

### Vào shell backend container

```bash
docker compose --env-file .env exec billing-backend sh
```

## `config`

Render file compose sau khi đã thay biến từ `.env`.

```bash
docker compose --env-file .env config
```

Dùng để kiểm tra Compose đang hiểu config cuối cùng như thế nào.

Ví dụ kiểm tra port FE:

```bash
docker compose --env-file .env config | grep FRONTEND_PORT
```

Hoặc xem service frontend sau khi render:

```bash
docker compose --env-file .env config
```

## Các kịch bản hay dùng

### 1. Deploy lần đầu hoặc chạy lại toàn bộ

```bash
cd /opt/billing-ca/deploy
docker compose --env-file .env up -d --build
```

### 2. Chỉ chạy lại frontend sau khi copy `dist` mới

Copy dist:

```bash
sudo mkdir -p /opt/billing-ca/frontend
sudo rm -rf /opt/billing-ca/frontend/dist
sudo cp -a /home/tungns/dist /opt/billing-ca/frontend/dist
```

Build và chạy lại riêng FE:

```bash
cd /opt/billing-ca/deploy
docker compose --env-file .env up -d --no-deps --build billing-frontend
```

### 3. Chỉ chạy lại frontend và bỏ cache build

```bash
cd /opt/billing-ca/deploy
docker compose --env-file .env build --no-cache billing-frontend
docker compose --env-file .env up -d --no-deps billing-frontend
```

### 4. Chỉ restart frontend, không lấy code mới

```bash
cd /opt/billing-ca/deploy
docker compose --env-file .env restart billing-frontend
```

### 5. Chỉ chạy lại backend sau khi thay JAR

```bash
cd /opt/billing-ca/deploy
docker compose --env-file .env up -d --no-deps --build billing-backend
```

### 6. Xem trạng thái hệ thống

```bash
cd /opt/billing-ca/deploy
docker compose --env-file .env ps
```

### 7. Xem log khi có lỗi

Frontend:

```bash
docker compose --env-file .env logs -f --tail=100 billing-frontend
```

Backend:

```bash
docker compose --env-file .env logs -f --tail=100 billing-backend
```

### 8. Kiểm tra file frontend đã nằm trong container chưa

```bash
docker compose --env-file .env exec billing-frontend ls -la /usr/share/nginx/html
```

## Ghi nhớ nhanh

Sau khi thay `frontend/dist`, dùng:

```bash
docker compose --env-file .env up -d --no-deps --build billing-frontend
```

Nếu vẫn thấy FE cũ, dùng:

```bash
docker compose --env-file .env build --no-cache billing-frontend
docker compose --env-file .env up -d --no-deps billing-frontend
```

Không chỉ dùng:

```bash
docker compose --env-file .env restart billing-frontend
```

Vì `restart` không build lại image, nên không lấy `dist` mới.
