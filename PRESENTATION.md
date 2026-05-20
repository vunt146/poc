# CRM Lead Management POC - Tổng quan Kiến trúc

## Mục tiêu POC

Chứng minh kiến trúc **Camunda 8 + Spring Boot + Lowcode Frontend** cho phép:
1. Thay đổi workflow mà KHÔNG cần redeploy backend/frontend
2. Frontend render dynamic form từ schema do backend cung cấp
3. Luồng nghiệp vụ Lead lifecycle hoạt động end-to-end

---

## Kiến trúc Tổng thể

```
+---------------------------+
|    Appsmith Cloud         |
|    (Frontend Lowcode)     |
+------------+--------------+
             |
             | HTTPS (ngrok tunnel)
             v
+---------------------------+         +---------------------------+
|    Spring Boot            |  gRPC   |    Camunda 8 Zeebe        |
|    Domain Service         |<------->|    (Workflow Engine)       |
|    port 8090              |         |    port 26500             |
+------------+--------------+         +---------------------------+
             |                                    |
             v                                    v
+---------------------------+         +---------------------------+
|    JSON Files             |         |    Elasticsearch          |
|    (Sample Data)          |         |    (Process History)      |
+---------------------------+         +---------------------------+
                                                  |
                                      +-----------+-----------+
                                      |                       |
                                      v                       v
                              +-------------+         +-------------+
                              |  Operate    |         |  Tasklist   |
                              |  port 8083  |         |  port 8084  |
                              +-------------+         +-------------+
```

---

## Nguyên tắc Thiết kế

| Nguyên tắc | Mô tả |
|---|---|
| **Frontend → Domain Service only** | Frontend KHÔNG gọi trực tiếp Camunda. Mọi tương tác qua REST API của Domain Service |
| **Workflow-driven** | Camunda 8 orchestrate luồng nghiệp vụ. Thay đổi BPMN = thay đổi luồng |
| **Dynamic Form** | Form schema lưu ở backend, frontend render động. Thêm/sửa form không cần redeploy FE |
| **In-memory data** | POC dùng JSON files, không database. Reset data bằng 1 API call |

---

## Luồng Nghiệp vụ - Lead Lifecycle

```
[Tạo Lead] → [Liên hệ KH] → Gateway
                                 |
                    +------------+------------+
                    |                         |
              KH quan tâm              KH từ chối
                    |                         |
                    v                         v
            [Xử lý cơ hội]              [Kết thúc]
                    |
                    v
           [Thu thập hồ sơ]
                    |
                    v
         [Cập nhật TT - auto]
                    |
                    v
              [Hoàn thành]
```

**Trạng thái Lead**: NEW_LEAD → CONTACTED → PROCESSING → DOCUMENT_COLLECTED → COMPLETED (hoặc REJECTED)

---

## Tech Stack

| Component | Technology | Vai trò |
|---|---|---|
| Workflow Engine | Camunda 8 Zeebe | Orchestrate business process |
| Backend | Spring Boot 3.2, Java 17 | Domain logic + API + Job Workers |
| Frontend | Appsmith Cloud | Lowcode UI, dynamic form rendering |
| Monitoring | Camunda Operate | Xem process instances, debug workflow |
| Data | JSON files (in-memory) | Sample data cho POC |
| Tunnel | ngrok | Expose localhost cho Appsmith Cloud |
| Container | Docker Compose | Chạy Camunda + Elasticsearch + Domain Service |

---

## API Endpoints

| Method | Endpoint | Mô tả |
|---|---|---|
| GET | /api/leads | Danh sách Lead |
| GET | /api/leads/{id} | Chi tiết Lead |
| POST | /api/workflow/lead/{id}/contact | Liên hệ KH (start workflow) |
| POST | /api/workflow/lead/{id}/process | Xử lý cơ hội |
| POST | /api/workflow/lead/{id}/collect-documents | Thu thập hồ sơ |
| POST | /api/workflow/lead/{id}/reject | Từ chối Lead |
| GET | /api/leads/allocatable | Leads có thể phân bổ |
| POST | /api/leads/allocate | Phân bổ Lead cho cán bộ |
| GET | /api/forms/{taskType} | Lấy form schema |
| POST | /api/admin/reset | Reset toàn bộ data |

---

## Frontend - Appsmith Cloud (4 Pages)

| Page | Chức năng |
|---|---|
| **Lead List** | Danh sách Lead, color-coded status, click xem chi tiết |
| **Lead Detail** | Thông tin KH + Progress bar + Workflow actions (Liên hệ/Xử lý/Thu thập/Từ chối) |
| **Lead Allocation** | Chọn leads + chọn cán bộ + phân bổ (chia đều) |
| **Workflow Tasks** | (Placeholder - tasks hiển thị qua Lead Detail) |

---

## Demo Flow (cho PGĐK)

### Scenario 1: Lead thành công (Happy path)
1. Mở **Lead List** → click lead NEW_LEAD
2. **Lead Detail** → click "📞 Liên hệ KH" → chọn "Quan tâm" → Submit
3. Lead → CONTACTED, progress bar cập nhật
4. Click "📋 Xử lý cơ hội" → Submit → Lead → PROCESSING
5. Click "📁 Thu thập hồ sơ" → Submit → Lead → DOCUMENT_COLLECTED → auto COMPLETED
6. Mở **Camunda Operate** → thấy process instance hoàn thành

### Scenario 2: Lead từ chối
1. Lead List → click lead → "📞 Liên hệ KH" → chọn "Không quan tâm" → Submit
2. Lead → REJECTED, workflow kết thúc
3. Camunda Operate → process instance kết thúc ở End Event "KH từ chối"

### Scenario 3: Phân bổ Lead
1. Mở **Lead Allocation** → chọn leads → click "Phân bổ"
2. Chọn cán bộ (>= 2 → warning "chia đều") → Submit
3. Leads được phân bổ cho cán bộ

---

## Điểm chứng minh cho PGĐK

| Mục tiêu | Chứng minh |
|---|---|
| **Workflow flexibility** | Thay đổi BPMN trên Camunda → deploy → frontend tự phản ánh (không redeploy) |
| **Dynamic form** | Form schema từ API → frontend render động. Thêm field = sửa JSON, không sửa code |
| **End-to-end** | Lead lifecycle chạy xuyên suốt: Frontend → API → Camunda → Job Worker → Update |
| **Lowcode** | Appsmith Cloud build UI nhanh, không cần code frontend truyền thống |
| **Monitoring** | Camunda Operate theo dõi realtime process instances |

---

## Hạn chế POC (cần lưu ý)

- Data in-memory (mất khi restart) — production sẽ dùng database
- Không có authentication — production cần SSO/OAuth
- ngrok tunnel (URL thay đổi) — production sẽ deploy trên cloud
- Appsmith Cloud free tier — production cần self-hosted hoặc enterprise

---

## Tiếp theo (nếu approve)

1. Đánh giá Budibase (so sánh với Appsmith)
2. Thiết kế database schema (thay in-memory)
3. Tích hợp authentication (SSO)
4. Deploy lên cloud environment
5. Performance testing với data thực
