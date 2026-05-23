# Shared Frontend Specification - CRM Lead Management POC

## Mục đích

Tài liệu này là **reference chung** cho tất cả frontend platforms (Appsmith, Budibase, hoặc bất kỳ nền tảng nào khác) khi implement CRM Lead Management POC. Nội dung được validate qua implementation Appsmith thực tế.

**Ngày cập nhật**: 2026-05-20
**Source of truth**: Domain Service implementation + Appsmith export (`frontend-appsmith/temp/My first application.json`)

---

## 1. Architecture

```
+-------------------+
|   Frontend        |  ← Appsmith / Budibase / Other
|   (Any Platform)  |
+--------+----------+
         |
         | HTTPS (ngrok tunnel hoặc direct)
         |
         v
+-------------------+
|   Spring Boot     |
|   Domain Service  |
|   (port 8090)     |
+--------+----------+
         |
         | gRPC
         v
+-------------------+
|   Camunda 8 Zeebe |
|   (Docker local)  |
+-------------------+
```

**Nguyên tắc**: Frontend KHÔNG gọi trực tiếp Camunda. Mọi interaction đều qua Domain Service API.

---

## 2. Datasource Configuration

| Config | Value |
|---|---|
| Base URL | ngrok public URL (thay đổi mỗi session) |
| Headers | `ngrok-skip-browser-warning: true`, `Content-Type: application/json` |
| Methods | GET, POST, PUT |
| CORS | Enabled (`*`) trên Domain Service |

---

## 3. Lead Status & State Machine

### 3.1 Tất cả trạng thái

| Status | Code | Mô tả | Terminal? | Allocatable? |
|---|---|---|---|---|
| `NEW_LEAD` | 1007 | Lead mới tạo trong hệ thống | ❌ | ✅ |
| `NEW_IMPORTED_LEAD` | 106141 | Lead import từ hệ thống khác | ❌ | ✅ |
| `CONTACTED` | 2001 | Đã liên hệ khách hàng | ❌ | ❌ |
| `PROCESSING` | 3001 | Đang xử lý cơ hội | ❌ | ❌ |
| `DOCUMENT_COLLECTED` | 4001 | Đã thu thập hồ sơ | ❌ | ❌ |
| `COMPLETED` | 5001 | Hoàn thành | ✅ | ❌ |
| `REJECTED` | 9001 | Từ chối | ✅ | ❌ |

### 3.2 State Transitions (allowed)

```
NEW_LEAD          → CONTACTED, PROCESSING, REJECTED
NEW_IMPORTED_LEAD → CONTACTED, PROCESSING, REJECTED
CONTACTED         → PROCESSING, DOCUMENT_COLLECTED, REJECTED
PROCESSING        → DOCUMENT_COLLECTED, COMPLETED, REJECTED
DOCUMENT_COLLECTED → COMPLETED, REJECTED
COMPLETED         → (không chuyển tiếp)
REJECTED          → (không chuyển tiếp)
```

### 3.3 Color Coding (UI)

| Status | Background Color | Hex |
|---|---|---|
| NEW_LEAD | Light Blue | #E3F2FD |
| NEW_IMPORTED_LEAD | Light Blue | #E3F2FD |
| CONTACTED | Light Yellow | #FFF8E1 |
| PROCESSING | Light Orange | #FFF3E0 |
| DOCUMENT_COLLECTED | Light Teal | #E0F2F1 |
| COMPLETED | Light Green | #E8F5E9 |
| REJECTED | Light Red | #FFEBEE |

---

## 4. Workflow Actions (Lead Detail)

Frontend cần implement 2 loại actions trên Lead Detail:

### 4.1 Status Update (manual)
- Dropdown chọn trạng thái mới (chỉ hiển thị trạng thái hợp lệ theo state machine)
- API: `PUT /api/leads/{id}/status`

### 4.2 Workflow Actions (Camunda-integrated)
Các action này vừa update status VÀ advance Camunda workflow:

| Action | Trigger khi status = | Button text | Modal? | API |
|---|---|---|---|---|
| Liên hệ KH | NEW_LEAD, NEW_IMPORTED_LEAD | 📞 Liên hệ khách hàng | ✅ | POST /api/workflow/lead/{id}/contact |
| Xử lý cơ hội | CONTACTED | 📋 Xử lý cơ hội | ✅ | POST /api/workflow/lead/{id}/process |
| Thu thập hồ sơ | PROCESSING | 📄 Thu thập hồ sơ | ✅ | POST /api/workflow/lead/{id}/collect-documents |
| Từ chối | Mọi status (trừ terminal) | Từ chối | ✅ | POST /api/workflow/lead/{id}/reject |

### 4.3 Workflow Flow (Happy Path)

```
NEW_LEAD/NEW_IMPORTED_LEAD
  → [📞 Liên hệ KH] → CONTACTED
  → [📋 Xử lý cơ hội] → PROCESSING
  → [📄 Thu thập hồ sơ] → DOCUMENT_COLLECTED
  → (auto) → COMPLETED
```

### 4.4 Reject Flow
- Có thể reject ở BẤT KỲ bước nào (trừ khi đã COMPLETED/REJECTED)
- Button "Từ chối" luôn visible (trừ terminal states)

---

## 5. API Contract (Complete)

### 5.1 Lead APIs

| # | Method | Endpoint | Request Body | Response | Notes |
|---|---|---|---|---|---|
| 1 | GET | /api/leads | - | `Lead[]` | Danh sách tất cả leads |
| 2 | GET | /api/leads/{id} | - | `Lead` | Chi tiết lead + history |
| 3 | PUT | /api/leads/{id}/status | `{newStatus, updatedBy, note, reason}` | `Lead` | Manual status update |
| 4 | GET | /api/leads/allocatable | - | `Lead[]` | Leads có thể phân bổ (NEW_LEAD/NEW_IMPORTED_LEAD) |
| 5 | POST | /api/leads/allocate | `{leadIds, targetUserIds, requestedBy}` | `AllocationResult` | Phân bổ leads |

### 5.2 Workflow APIs (Camunda-integrated)

| # | Method | Endpoint | Request Body | Response | Notes |
|---|---|---|---|---|---|
| 6 | POST | /api/workflow/lead/{leadId}/contact | `{action, contactResult, note, performedBy}` | `{success, newStatus, processInstanceKey, ...}` | Start process + complete Contact task |
| 7 | POST | /api/workflow/lead/{leadId}/process | `{note, performedBy}` | `{success, newStatus, nextStep, ...}` | Complete Process task |
| 8 | POST | /api/workflow/lead/{leadId}/collect-documents | `{note, performedBy}` | `{success, newStatus, nextStep, ...}` | Complete Document task |
| 9 | POST | /api/workflow/lead/{leadId}/reject | `{note, reason, performedBy}` | `{success, newStatus, ...}` | Reject at any step |

### 5.3 User APIs

| # | Method | Endpoint | Request Body | Response | Notes |
|---|---|---|---|---|---|
| 10 | GET | /api/users/subordinates | - | `User[]` | Danh sách cán bộ dưới quyền |

### 5.4 Workflow Task APIs (Generic - cho Dynamic Form page)

| # | Method | Endpoint | Request Body | Response | Notes |
|---|---|---|---|---|---|
| 11 | GET | /api/workflow/tasks | - | `Task[]` | Active workflow tasks |
| 12 | GET | /api/forms/{taskType} | - | `FormSchema` | Form schema cho task type |
| 13 | POST | /api/workflow/tasks/{jobKey}/complete | `{form variables}` | `{success, ...}` | Complete task với form data |

---

## 6. Data Models

### 6.1 Lead Object

```json
{
  "id": "LEAD-001",
  "customerName": "Nguyễn Văn A",
  "status": "NEW_LEAD",
  "ownerId": "USR-MGR-01",
  "productType": "CREDIT_CARD",
  "productDetails": {
    "cardType": "Visa Platinum",
    "limit": 50000000,
    "bundledProduct": { ... }
  },
  "processInstanceKey": 2251799813685260,
  "createdAt": "2026-05-15T10:00:00",
  "updatedAt": "2026-05-15T10:00:00",
  "history": [
    {
      "timestamp": "2026-05-15T10:00:00",
      "fromStatus": null,
      "toStatus": "NEW_LEAD",
      "changedBy": "SYSTEM",
      "note": "Created"
    }
  ]
}
```

### 6.2 FormSchema Object

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

### 6.3 Workflow Task Object

```json
{
  "jobKey": 2251799813685270,
  "taskType": "contact-customer",
  "taskDefinitionId": "Task_ContactCustomer",
  "name": "Liên hệ khách hàng",
  "processName": "Lead Lifecycle Process",
  "assignee": "USR-STAFF-01",
  "creationDate": "2026-05-15T10:00:00",
  "formKey": "lead-contact-form"
}
```

---

## 7. Pages Specification

### 7.1 Page: Lead List

| Aspect | Specification |
|---|---|
| **API** | GET /api/leads |
| **Widget** | Table/DataGrid |
| **Columns hiển thị** | Status (color badge), Lead ID, Customer Name, Product Type, Process Instance Key, Created At |
| **Columns ẩn** | ownerId, productDetails, updatedAt, history |
| **Row click** | Navigate → Lead Detail với `leadId` parameter |
| **Status colors** | Xem Section 3.3 |
| **Load on page** | Auto-fetch khi page load |

### 7.2 Page: Lead Detail

| Aspect | Specification |
|---|---|
| **Input** | leadId (từ URL param hoặc navigation) |
| **API** | GET /api/leads/{leadId} |
| **Sections** | Info, Progress, Status Update, Workflow Actions, History |

**Section: Info**
- Hiển thị: customerName, productType, ownerId, productDetails (JSON)

**Section: Progress**
- Progress bar/stepper: NEW → CONTACTED → PROCESSING → DOCUMENT_COLLECTED → COMPLETED
- Highlight current step

**Section: Status Update (Manual)**
- Dropdown: chỉ hiển thị trạng thái hợp lệ (theo state machine)
- Input: Ghi chú
- Button: "Cập nhật" → PUT /api/leads/{id}/status
- Validation: Nếu chuyển sang CONTACTED → bắt buộc nhập ghi chú

**Section: Workflow Actions**
- Button "Hành động tiếp theo" (dynamic text theo status):
  - NEW_LEAD/NEW_IMPORTED_LEAD → "📞 Liên hệ khách hàng" → mở ModalContact
  - CONTACTED → "📋 Xử lý cơ hội" → mở ModalProcess
  - PROCESSING → "📄 Thu thập hồ sơ" → mở ModalDocument
  - COMPLETED/REJECTED/DOCUMENT_COLLECTED → ẩn button
- Button "Từ chối" → mở ModalReject (ẩn khi COMPLETED/REJECTED)

**Section: History**
- Table: timestamp, fromStatus, toStatus, changedBy, note

**Modals:**

| Modal | Fields | API | Body |
|---|---|---|---|
| ModalContact | action (select), contactResult (select), note (input) | POST /api/workflow/lead/{id}/contact | `{action, contactResult, note, performedBy: "USR-STAFF-01"}` |
| ModalProcess | note (input) | POST /api/workflow/lead/{id}/process | `{note, performedBy: "USR-STAFF-01"}` |
| ModalDocument | note (input) | POST /api/workflow/lead/{id}/collect-documents | `{note, performedBy: "USR-STAFF-01"}` |
| ModalReject | reason/note (input) | POST /api/workflow/lead/{id}/reject | `{note, reason: "KH tu choi", performedBy: "USR-STAFF-01"}` |

**After each action**: Refresh lead detail data.

### 7.3 Page: Lead Allocation

| Aspect | Specification |
|---|---|
| **API** | GET /api/leads/allocatable |
| **Widget** | Table với multi-select (checkbox) |
| **Display** | "Đã chọn: X lead(s)" |
| **Button** | "Phân bổ" → mở Modal |
| **Modal** | Table cán bộ (GET /api/users/subordinates) + checkbox |
| **Warning** | "Cơ hội sẽ được chia đều cho các cán bộ được chọn" |
| **Summary** | "Phân bổ X lead cho Y cán bộ" |
| **Confirm** | POST /api/leads/allocate `{leadIds, targetUserIds, requestedBy: "USR-MGR-01"}` |
| **After** | Refresh danh sách |

### 7.4 Page: Workflow Tasks (Dynamic Form)

| Aspect | Specification |
|---|---|
| **API** | GET /api/workflow/tasks |
| **Widget** | Table (left) + JSON Form (right) |
| **Columns** | jobKey, taskType, name, processName, assignee, creationDate |
| **Row click** | Fetch form schema: GET /api/forms/{taskType} |
| **Form** | Render dynamic form từ schema (JSON Form widget hoặc equivalent) |
| **Submit** | POST /api/workflow/tasks/{jobKey}/complete với form data |
| **After** | Refresh task list + show success message |
| **Field types** | TEXT, TEXTAREA, NUMBER, DROPDOWN, DATE, CHECKBOX, RADIO |
| **Visibility** | visibilityCondition: show/hide field dựa trên giá trị field khác |

---

## 8. Hardcoded Values (POC - No Auth)

| Value | Usage | Notes |
|---|---|---|
| `USR-STAFF-01` | performedBy trong workflow actions | Giả lập user đang login |
| `USR-MGR-01` | requestedBy trong allocation | Giả lập manager đang login |

---

## 9. Platform-Specific Notes

### Appsmith
- Đã implement đầy đủ 4 pages
- JSON Form widget native cho dynamic form
- Export: `frontend-appsmith/temp/My first application.json`
- Docs: `frontend-appsmith/docs/`

### Budibase
- Scope: Lead List + Lead Detail (2 pages)
- Dùng chung ngrok tunnel với Appsmith
- Cần evaluate: dynamic form capability, REST API integration
- Directory: `frontend-budibase/`

---

## 10. Evaluation Criteria (Platform Comparison)

| # | Tiêu chí | Mô tả |
|---|---|---|
| 1 | Setup time | Thời gian từ 0 → datasource connected |
| 2 | Dynamic form | Khả năng render form từ JSON schema |
| 3 | Conditional fields | Ease of use cho visibilityCondition |
| 4 | API performance | Response time khi gọi API |
| 5 | Export/Import | Khả năng export app dạng portable |
| 6 | Developer experience | Documentation, community, learning curve |
