# Frontend Appsmith - CRM Lead Management POC

## Tổng quan

Appsmith Cloud app cho CRM Lead Management, kết nối với Domain Service REST API qua ngrok tunnel.

**Platform**: Appsmith Cloud (app.appsmith.com) - không cần Docker

---

## Architecture

```
Appsmith Cloud (internet)
        |
        | HTTPS
        v
ngrok tunnel (public URL)
        |
        | localhost:8090
        v
Spring Boot Domain Service
        |
        | gRPC
        v
Camunda 8 Zeebe (Docker local)
```

**Nguyên tắc**: Frontend chỉ giao tiếp qua Domain Service API. Không gọi trực tiếp Camunda.

---

## Quick Start

### 1. Start Domain Service + Camunda
```bash
# Từ workspace root
docker compose up -d
# Đợi ~60s cho Elasticsearch healthy
# Verify:
curl http://localhost:8090/api/leads
```

### 2. Start ngrok tunnel
```bash
ngrok http 8090
# Ghi lại URL: https://xxxx.ngrok-free.app
```

### 3. Import Appsmith App
1. Login https://app.appsmith.com
2. **Import** → Upload `appsmith-export.json`
3. Cập nhật Datasource URL = ngrok URL
4. Test: mở Lead List page

### Hoặc: Build từ đầu
Xem `docs/appsmith-build-guide.md` cho hướng dẫn step-by-step.

---

## Pages

| # | Page | Chức năng | API chính |
|---|---|---|---|
| 1 | Lead List | Danh sách Lead, click xem chi tiết | GET /api/leads |
| 2 | Lead Detail | Chi tiết + progress + cập nhật trạng thái | GET/PUT /api/leads/{id} |
| 3 | Lead Allocation | Chọn leads + phân bổ cho cán bộ | POST /api/leads/allocate |
| 4 | Workflow Tasks | Active tasks + dynamic form rendering | GET /api/workflow/tasks |

---

## Files

```
frontend-appsmith/
├── README.md                          # File này
├── appsmith-export.json               # JSON export (importable)
└── docs/
    ├── ngrok-setup.md                 # Hướng dẫn cài ngrok
    ├── datasource-setup.md            # Cấu hình datasource
    ├── appsmith-build-guide.md        # Build guide step-by-step
    └── evaluation-notes.md            # Template đánh giá platform
```

---

## API Endpoints (Domain Service)

| Method | Endpoint | Mô tả |
|---|---|---|
| GET | /api/leads | Danh sách Lead |
| GET | /api/leads/{id} | Chi tiết Lead |
| PUT | /api/leads/{id}/status | Cập nhật trạng thái |
| GET | /api/leads/allocatable?ownerId= | Leads có thể phân bổ |
| POST | /api/leads/allocate | Phân bổ Lead |
| GET | /api/users/subordinates | Danh sách cán bộ |
| GET | /api/workflow/tasks | Active workflow tasks |
| GET | /api/forms/{taskType} | Form schema cho task |
| POST | /api/workflow/tasks/{jobKey}/complete | Complete task |

---

## Lưu ý

- **ngrok URL thay đổi** mỗi lần restart → cập nhật Datasource trên Appsmith
- **Domain Service phải đang chạy** khi sử dụng app
- **Không cần authentication** (POC mode)
- **CORS đã enable** trên Domain Service (`@CrossOrigin(origins = "*")`)

---

## Evaluation

Sau khi build xong, điền đánh giá vào `docs/evaluation-notes.md` để so sánh với Budibase.
