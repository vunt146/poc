# Requirements Document - Frontend Budibase (CRM Lead Management POC)

## Intent Analysis

| Attribute | Value |
|---|---|
| **User Request** | Xây dựng Frontend Budibase cho CRM Lead Management POC, sử dụng Budibase Cloud (budibase.app), triển khai giống hệt Appsmith để so sánh |
| **Request Type** | New Unit (Frontend) trong dự án đang tiếp tục |
| **Scope Estimate** | Single Component - 2 Pages trên Budibase Cloud |
| **Complexity Estimate** | Moderate - Lowcode platform + API integration + Status update workflow |

## Constraint Changes (so với Requirements gốc)

| NFR gốc | Thay đổi | Lý do |
|---|---|---|
| NFR-03.3: Budibase chạy trên Docker | Budibase Cloud (budibase.app) | Máy không đủ mạnh, cần giống approach Appsmith Cloud |
| NFR-03.4: docker-compose cho toàn bộ stack | Budibase tách riêng trên Cloud | Kết nối qua ngrok tunnel (dùng chung với Appsmith) |
| Scope: 4 pages đầy đủ | 2 pages (Lead List + Lead Detail) | Đủ để so sánh core capabilities |

## Scope Comparison (Appsmith vs Budibase)

| Feature | Appsmith | Budibase |
|---|---|---|
| Lead List page | ✅ | ✅ |
| Lead Detail page | ✅ | ✅ |
| Lead Allocation page | ✅ | ❌ (skip) |
| Dynamic Form page | ✅ | ❌ (skip) |
| ngrok tunnel | ✅ | ✅ (dùng chung) |
| Cloud deployment | ✅ | ✅ |
| JSON export | ✅ | ✅ |

**Rationale**: 2 pages đủ để đánh giá:
- Khả năng hiển thị danh sách (Table/Data Provider)
- Khả năng hiển thị chi tiết + form update
- API integration (GET, PUT, POST)
- Navigation giữa các screens
- Workflow action (status update, contact, process, reject)

---

## Architecture (Updated for Budibase Cloud)

```
+-------------------+                    +-------------------+
|  Budibase Cloud   |                    |   Camunda 8       |
|  (budibase.app)   |                    |   (Docker local)  |
+--------+----------+                    +--------+----------+
         |                                        |
         | HTTPS (ngrok tunnel)                   | gRPC
         |                                        |
         v                                        v
+-------------------+                    +-------------------+
|   ngrok           |                    |   Camunda Zeebe   |
|   (tunnel chung)  |                    |   (port 26500)    |
+--------+----------+                    +-------------------+
         |
         | localhost:8090
         v
+-------------------+
|   Spring Boot     |
|   Domain Service  |
|   (port 8090)     |
+--------+----------+
         |
         v
+-------------------+
|   JSON/YAML Files |
|   (Sample Data)   |
+-------------------+
```

### Text Alternative
- Budibase Cloud (internet) connects via HTTPS to ngrok tunnel (dùng chung với Appsmith)
- ngrok tunnel forwards to localhost:8090 (Domain Service)
- Domain Service communicates with Camunda Zeebe via gRPC (local Docker)
- Frontend NEVER communicates directly with Camunda - all via Domain Service API

---

## Functional Requirements (Frontend Budibase)

### FB-01: Datasource Setup (ngrok + Budibase Cloud)

| ID | Requirement |
|---|---|
| FB-01.1 | Sử dụng ngrok tunnel đang có (dùng chung với Appsmith) tới localhost:8090 |
| FB-01.2 | Tạo Datasource "CRM_Domain_Service" trên Budibase Cloud với ngrok public URL |
| FB-01.3 | Datasource phải hỗ trợ tất cả HTTP methods (GET, POST, PUT) |
| FB-01.4 | CORS đã được enable trên Domain Service (CrossOrigin origins = "*") |
| FB-01.5 | Headers bắt buộc: `ngrok-skip-browser-warning: true`, `Content-Type: application/json` |

### FB-02: Screen 1 - Lead List (Danh sách Lead)

| ID | Requirement |
|---|---|
| FB-02.1 | Hiển thị danh sách Lead dạng Table component |
| FB-02.2 | Columns hiển thị: Status (badge/tag), Lead ID, Customer Name, Product Type, Process Instance Key, Created At |
| FB-02.3 | API: GET /api/leads |
| FB-02.4 | Click row → navigate tới Lead Detail screen với leadId parameter |
| FB-02.5 | Sắp xếp theo createdAt (mới nhất trước) |
| FB-02.6 | Hiển thị thông báo khi danh sách rỗng |
| FB-02.7 | Status column hiển thị dạng color-coded badge (NEW_LEAD=blue, CONTACTED=yellow, PROCESSING=orange, COMPLETED=green, REJECTED=red) |
| FB-02.8 | Columns ẩn: ownerId, productDetails, updatedAt, history |

### FB-03: Screen 2 - Lead Detail (Chi tiết Lead)

| ID | Requirement |
|---|---|
| FB-03.1 | Nhận leadId từ URL parameter hoặc navigation state |
| FB-03.2 | API: GET /api/leads/{id} |
| FB-03.3 | Hiển thị thông tin khách hàng: customerName, productType, productDetails, ownerId |
| FB-03.4 | Thanh tiến trình (Progress/Steps component): New → Contacted → Processing → Document Collected → Completed |
| FB-03.5 | Hiển thị lịch sử trạng thái (Table component): timestamp, fromStatus, toStatus, changedBy, note |
| FB-03.6 | Dropdown cập nhật trạng thái: chỉ hiển thị trạng thái hợp lệ tiếp theo (dựa trên state machine) |
| FB-03.7 | Button "Cập nhật" → PUT /api/leads/{id}/status với body {newStatus, updatedBy, note, reason}. Validate: nếu status = CONTACTED thì bắt buộc nhập ghi chú |
| FB-03.8 | Sau khi cập nhật thành công → refresh data |
| FB-03.9 | Hiển thị productDetails (bundledProduct) dạng JSON stringify |
| FB-03.10 | Button "← Quay lại" → navigate về Lead List |
| FB-03.11 | Button "Hành động tiếp theo" - dynamic theo trạng thái hiện tại: NEW_LEAD/NEW_IMPORTED_LEAD → "Liên hệ khách hàng", CONTACTED → "Xử lý cơ hội", PROCESSING → "Thu thập hồ sơ". Ẩn khi status = COMPLETED/REJECTED/DOCUMENT_COLLECTED |
| FB-03.12 | Button "Từ chối" → mở Modal Reject. Ẩn khi status = COMPLETED/REJECTED |
| FB-03.13 | Modal Contact: Liên hệ khách hàng - fields: action (select), contactResult (select), note (input). API: POST /api/workflow/lead/{leadId}/contact |
| FB-03.14 | Modal Process: Xử lý cơ hội - fields: note (input). API: POST /api/workflow/lead/{leadId}/process |
| FB-03.15 | Modal Document: Thu thập hồ sơ - fields: note (input). API: POST /api/workflow/lead/{leadId}/collect-documents |
| FB-03.16 | Modal Reject: Từ chối Lead - fields: reason/note (input). API: POST /api/workflow/lead/{leadId}/reject |
| FB-03.17 | Hỗ trợ trạng thái `NEW_IMPORTED_LEAD` (lead được import từ hệ thống khác) - xử lý tương tự NEW_LEAD |

### FB-04: Navigation & Layout

| ID | Requirement |
|---|---|
| FB-04.1 | Navigation với 2 menu items: Lead List, Lead Detail |
| FB-04.2 | Header hiển thị tên ứng dụng: "CRM Lead Management" |
| FB-04.3 | Responsive layout (Budibase default) |
| FB-04.4 | Consistent styling across screens |

---

## Non-Functional Requirements (Frontend Budibase)

### NFB-01: Deployment

| ID | Requirement |
|---|---|
| NFB-01.1 | Budibase Cloud (budibase.app) - không cần Docker |
| NFB-01.2 | Domain Service exposed qua ngrok tunnel (dùng chung với Appsmith) |
| NFB-01.3 | Ngrok free tier đủ cho POC (session limit 2h, có thể restart) |

### NFB-02: Deliverables

| ID | Requirement |
|---|---|
| NFB-02.1 | Budibase JSON export file (importable) |
| NFB-02.2 | Step-by-step guide để build từ đầu |
| NFB-02.3 | Datasource setup guide (reuse ngrok từ Appsmith) |
| NFB-02.4 | Screenshots/mockups cho từng screen |

### NFB-03: Evaluation Criteria (cho Platform Comparison - giống Appsmith)

| ID | Requirement |
|---|---|
| NFB-03.1 | Ghi nhận thời gian setup datasource |
| NFB-03.2 | Đánh giá khả năng hiển thị danh sách + chi tiết |
| NFB-03.3 | Đánh giá ease of use cho conditional logic (dynamic buttons, valid status) |
| NFB-03.4 | Đánh giá performance khi gọi API |
| NFB-03.5 | Đánh giá khả năng export/import app |

---

## API Contract Summary (Domain Service → Frontend Budibase)

| # | Method | Endpoint | Request Body | Response | Screen |
|---|---|---|---|---|---|
| 1 | GET | /api/leads | - | List of Lead | Lead List |
| 2 | GET | /api/leads/{id} | - | Lead (full detail + history) | Lead Detail |
| 3 | PUT | /api/leads/{id}/status | {newStatus, updatedBy, note, reason} | Updated Lead | Lead Detail |
| 4 | POST | /api/workflow/lead/{leadId}/contact | {action, contactResult, note, performedBy} | Workflow result | Lead Detail |
| 5 | POST | /api/workflow/lead/{leadId}/process | {note, performedBy} | Workflow result | Lead Detail |
| 6 | POST | /api/workflow/lead/{leadId}/collect-documents | {note, performedBy} | Workflow result | Lead Detail |
| 7 | POST | /api/workflow/lead/{leadId}/reject | {note, reason, performedBy} | Workflow result | Lead Detail |

---

## Data Models (Frontend perspective)

### Lead Object
```json
{
  "id": "LEAD-001",
  "customerName": "Nguyễn Văn A",
  "status": "NEW_LEAD",
  "ownerId": "USR-MGR-01",
  "productType": "CREDIT_CARD",
  "productDetails": { "cardType": "Visa Platinum", "limit": 50000000, "bundledProduct": {...} },
  "processInstanceKey": 2251799813685260,
  "createdAt": "2026-05-15T10:00:00",
  "updatedAt": "2026-05-15T10:00:00",
  "history": [
    { "timestamp": "...", "fromStatus": null, "toStatus": "NEW_LEAD", "changedBy": "SYSTEM", "note": "Created" }
  ]
}
```

### Lead Status Values
- `NEW_LEAD` - Lead mới tạo
- `NEW_IMPORTED_LEAD` - Lead được import từ hệ thống khác
- `CONTACTED` - Đã liên hệ khách hàng
- `PROCESSING` - Đang xử lý
- `DOCUMENT_COLLECTED` - Đã thu thập hồ sơ
- `COMPLETED` - Hoàn thành
- `REJECTED` - Từ chối

### State Machine (Valid Transitions)
- NEW_LEAD → CONTACTED, REJECTED
- NEW_IMPORTED_LEAD → CONTACTED, REJECTED
- CONTACTED → PROCESSING, REJECTED
- PROCESSING → DOCUMENT_COLLECTED, REJECTED
- DOCUMENT_COLLECTED → COMPLETED, REJECTED
- COMPLETED → (terminal)
- REJECTED → (terminal)

---

## Constraints & Assumptions

### Constraints
- Budibase Cloud free tier (có giới hạn về số app/rows)
- Ngrok free tier (session 2h, URL thay đổi mỗi lần restart) - dùng chung tunnel
- Domain Service phải đang chạy trên localhost:8090 khi demo
- Không có authentication (POC) - hardcode `performedBy: "USR-STAFF-01"`
- Header `ngrok-skip-browser-warning` bắt buộc để bypass ngrok interstitial page
- Scope giới hạn 2 screens (Lead List + Lead Detail) để tập trung so sánh

### Assumptions
- Budibase Cloud hỗ trợ REST API datasource với custom headers
- Budibase Cloud cho phép navigation giữa screens với parameters
- Budibase Cloud hỗ trợ conditional logic cho dynamic buttons
- Budibase Cloud cho phép export/import app dạng JSON
- ngrok tunnel đang hoạt động (đã setup cho Appsmith)

