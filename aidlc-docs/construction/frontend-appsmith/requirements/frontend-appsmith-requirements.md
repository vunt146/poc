# Requirements Document - Frontend Appsmith (CRM Lead Management POC)

## Intent Analysis

| Attribute | Value |
|---|---|
| **User Request** | Xây dựng Frontend Appsmith cho CRM Lead Management POC, sử dụng Appsmith Cloud (không Docker) |
| **Request Type** | New Unit (Frontend) trong dự án đang tiếp tục |
| **Scope Estimate** | Single Component - 4 Pages trên Appsmith Cloud |
| **Complexity Estimate** | Moderate - Lowcode platform + Dynamic form rendering + API integration |

## Constraint Changes (so với Requirements gốc)

| NFR gốc | Thay đổi | Lý do |
|---|---|---|
| NFR-03.3: Appsmith chạy trên Docker | Appsmith Cloud (app.appsmith.com) | Máy không đủ mạnh chạy Docker cho Appsmith |
| NFR-03.4: docker-compose cho toàn bộ stack | Appsmith tách riêng trên Cloud | Kết nối qua ngrok tunnel |
| Datasource URL: Docker network internal | Public URL qua ngrok | Appsmith Cloud cần internet-accessible endpoint |

---

## Architecture (Updated for Appsmith Cloud)

```
+-------------------+                    +-------------------+
|   Appsmith Cloud  |                    |   Camunda 8       |
|   (app.appsmith   |                    |   (Docker local)  |
|    .com)          |                    +--------+----------+
+--------+----------+                             |
         |                                        |
         | HTTPS (ngrok tunnel)                   | gRPC
         |                                        |
         v                                        v
+-------------------+                    +-------------------+
|   ngrok           |                    |   Camunda Zeebe   |
|   (tunnel)        |                    |   (port 26500)    |
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
- Appsmith Cloud (internet) connects via HTTPS to ngrok tunnel
- ngrok tunnel forwards to localhost:8090 (Domain Service)
- Domain Service communicates with Camunda Zeebe via gRPC (local Docker)
- Frontend NEVER communicates directly with Camunda - all via Domain Service API

---

## Functional Requirements (Frontend Appsmith)

### FA-01: Datasource Setup (ngrok + Appsmith Cloud)

| ID | Requirement |
|---|---|
| FA-01.1 | Cài đặt ngrok và tạo tunnel tới localhost:8090 |
| FA-01.2 | Tạo Datasource trên Appsmith Cloud với ngrok public URL |
| FA-01.3 | Datasource phải hỗ trợ tất cả HTTP methods (GET, POST, PUT) |
| FA-01.4 | CORS đã được enable trên Domain Service (CrossOrigin origins = "*") |

### FA-02: Page 1 - Lead List (Danh sách Lead)

| ID | Requirement |
|---|---|
| FA-02.1 | Hiển thị danh sách Lead dạng Table widget |
| FA-02.2 | Columns: Status (badge/tag), Lead ID, Customer Name, Product Type, Created At |
| FA-02.3 | API: GET /api/leads (optional filter: ?ownerId=xxx) |
| FA-02.4 | Click row → navigate tới Lead Detail page với leadId parameter |
| FA-02.5 | Sắp xếp theo createdAt (mới nhất trước) |
| FA-02.6 | Hiển thị thông báo khi danh sách rỗng |
| FA-02.7 | Status column hiển thị dạng color-coded badge (New=blue, Contacted=yellow, Processing=orange, Done=green, Rejected=red) |

### FA-03: Page 2 - Lead Detail (Chi tiết Lead)

| ID | Requirement |
|---|---|
| FA-03.1 | Nhận leadId từ URL parameter hoặc navigation |
| FA-03.2 | API: GET /api/leads/{id} |
| FA-03.3 | Hiển thị thông tin khách hàng: customerName, productType, productDetails |
| FA-03.4 | Thanh tiến trình (Progress widget): New → Contacted → Processing → Document Collected → Completed |
| FA-03.5 | Hiển thị lịch sử trạng thái (Table widget): timestamp, fromStatus, toStatus, changedBy, note |
| FA-03.6 | Dropdown cập nhật trạng thái: chỉ hiển thị trạng thái hợp lệ tiếp theo (dựa trên state machine) |
| FA-03.7 | Button "Cập nhật" → PUT /api/leads/{id}/status với body {newStatus, updatedBy, note, reason} |
| FA-03.8 | Sau khi cập nhật thành công → refresh data |
| FA-03.9 | Hiển thị productDetails dạng key-value (dynamic theo productType) |

### FA-04: Page 3 - Lead Allocation (Phân bổ Lead)

| ID | Requirement |
|---|---|
| FA-04.1 | API: GET /api/leads/allocatable?ownerId=USR-MGR-01 (lấy leads có thể phân bổ) |
| FA-04.2 | Table với checkbox cho phép chọn nhiều Lead |
| FA-04.3 | Nút "Chọn tất cả" / "Bỏ chọn tất cả" |
| FA-04.4 | Button "Phân bổ" → mở Modal |
| FA-04.5 | Modal: API GET /api/users/subordinates → hiển thị danh sách cán bộ với checkbox |
| FA-04.6 | Khi chọn >= 2 cán bộ → hiển thị cảnh báo "Cơ hội sẽ được chia đều" |
| FA-04.7 | Button "Phân bổ cơ hội" → POST /api/leads/allocate với body {leadIds, targetUserIds, requestedBy} |
| FA-04.8 | Hiển thị kết quả phân bổ (allocation result) sau khi thành công |
| FA-04.9 | Refresh danh sách sau khi phân bổ |

### FA-05: Page 4 - Dynamic Form (Workflow Task Form)

| ID | Requirement |
|---|---|
| FA-05.1 | Hiển thị danh sách active tasks: API GET /api/workflow/tasks |
| FA-05.2 | Khi chọn task → lấy form schema: API GET /api/forms/{taskType} |
| FA-05.3 | Render form động dựa trên FormSchema JSON response |
| FA-05.4 | Hỗ trợ field types: TEXT, TEXTAREA, NUMBER, DROPDOWN, DATE, CHECKBOX, RADIO |
| FA-05.5 | Hỗ trợ visibilityCondition: show/hide field dựa trên giá trị field khác |
| FA-05.6 | Hỗ trợ validation rules: required, min/max length |
| FA-05.7 | Submit form → POST /api/workflow/tasks/{jobKey}/complete với form data |
| FA-05.8 | Sau khi submit → refresh task list |
| FA-05.9 | Hiển thị form title và description từ schema |

### FA-06: Navigation & Layout

| ID | Requirement |
|---|---|
| FA-06.1 | Sidebar navigation với 4 menu items: Lead List, Lead Detail, Lead Allocation, Workflow Tasks |
| FA-06.2 | Header hiển thị tên ứng dụng: "CRM Lead Management" |
| FA-06.3 | Responsive layout (Appsmith default) |
| FA-06.4 | Consistent styling across pages |

---

## Non-Functional Requirements (Frontend Appsmith)

### NFA-01: Deployment

| ID | Requirement |
|---|---|
| NFA-01.1 | Appsmith Cloud (app.appsmith.com) - không cần Docker |
| NFA-01.2 | Domain Service exposed qua ngrok tunnel |
| NFA-01.3 | Ngrok free tier đủ cho POC (session limit 2h, có thể restart) |

### NFA-02: Deliverables

| ID | Requirement |
|---|---|
| NFA-02.1 | Appsmith JSON export file (importable) |
| NFA-02.2 | Step-by-step guide để build từ đầu |
| NFA-02.3 | Ngrok setup guide |
| NFA-02.4 | Screenshots/mockups cho từng page |

### NFA-03: Evaluation Criteria (cho Platform Comparison)

| ID | Requirement |
|---|---|
| NFA-03.1 | Ghi nhận thời gian setup datasource |
| NFA-03.2 | Đánh giá khả năng render dynamic form từ JSON |
| NFA-03.3 | Đánh giá ease of use cho conditional fields (visibilityCondition) |
| NFA-03.4 | Đánh giá performance khi gọi API |
| NFA-03.5 | Đánh giá khả năng export/import app |

---

## API Contract Summary (Domain Service → Frontend)

| # | Method | Endpoint | Request Body | Response | Page |
|---|---|---|---|---|---|
| 1 | GET | /api/leads | - (query: ?ownerId) | List of Lead | Lead List |
| 2 | GET | /api/leads/{id} | - | Lead (full detail + history) | Lead Detail |
| 3 | PUT | /api/leads/{id}/status | {newStatus, updatedBy, note, reason} | Updated Lead | Lead Detail |
| 4 | GET | /api/leads/allocatable | query: ?ownerId | List of Lead (allocatable only) | Allocation |
| 5 | POST | /api/leads/allocate | {leadIds, targetUserIds, requestedBy} | AllocationResult | Allocation |
| 6 | GET | /api/users/subordinates | query: ?managerId | List of User | Allocation |
| 7 | GET | /api/workflow/tasks | - | List of active tasks | Dynamic Form |
| 8 | GET | /api/forms/{taskType} | - | FormSchema (JSON) | Dynamic Form |
| 9 | POST | /api/workflow/tasks/{jobKey}/complete | {form variables} | Completion result | Dynamic Form |

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
  "productDetails": { "cardType": "Visa Platinum", "limit": 50000000 },
  "createdAt": "2026-05-15T10:00:00",
  "updatedAt": "2026-05-15T10:00:00",
  "history": [
    { "timestamp": "...", "fromStatus": null, "toStatus": "NEW_LEAD", "changedBy": "SYSTEM", "note": "Created" }
  ]
}
```

### FormSchema Object
```json
{
  "formId": "form-001",
  "formKey": "lead-contact-result",
  "taskType": "contact-customer",
  "title": "Kết quả liên hệ khách hàng",
  "description": "Nhập kết quả sau khi liên hệ khách hàng",
  "fields": [
    {
      "id": "contactResult",
      "type": "DROPDOWN",
      "label": "Kết quả liên hệ",
      "required": true,
      "options": [
        { "value": "INTERESTED", "label": "Khách hàng quan tâm" },
        { "value": "NOT_INTERESTED", "label": "Không quan tâm" },
        { "value": "CALLBACK", "label": "Hẹn gọi lại" }
      ],
      "visibilityCondition": null,
      "validation": null
    },
    {
      "id": "callbackDate",
      "type": "DATE",
      "label": "Ngày hẹn gọi lại",
      "required": true,
      "options": null,
      "visibilityCondition": {
        "dependsOn": "contactResult",
        "operator": "EQUALS",
        "value": "CALLBACK"
      },
      "validation": null
    }
  ]
}
```

---

## Constraints & Assumptions

### Constraints
- Appsmith Cloud free tier (có giới hạn về số app/queries)
- Ngrok free tier (session 2h, URL thay đổi mỗi lần restart)
- Domain Service phải đang chạy trên localhost:8090 khi demo
- Không có authentication (POC)

### Assumptions
- Appsmith Cloud hỗ trợ JSON Form widget hoặc custom form rendering
- Ngrok tunnel ổn định đủ cho demo
- CORS đã được cấu hình đúng trên Domain Service
- Appsmith Cloud cho phép export/import app dạng JSON
