# Code Generation Plan - Frontend Budibase

## Unit Context
- **Unit**: Frontend Budibase (Unit 4)
- **Platform**: Budibase Cloud (budibase.app)
- **Scope**: 2 screens (Lead List + Lead Detail)
- **Dependencies**: Domain Service (port 8090), ngrok tunnel (dùng chung với Appsmith)
- **Target Directory**: `/frontend-budibase/`

## Budibase Platform Notes
- REST API datasource: Workspace Settings > Connections > APIs
- Screens: URL-based routing with URL variables (e.g., `/lead/:leadId`)
- Bindings: Handlebars syntax `{{ bindingName }}` cho dynamic values
- Navigation: "Navigate To" action với URL parameters
- Components: Table, Form, Button, Container, Headline, Paragraph, Modal
- Conditional display: Show/hide via conditions trên component settings
- Export: Workspace export (JSON format)

---

## Execution Steps

### Step 1: Datasource Setup Guide
- [x] Tạo `frontend-budibase/docs/datasource-setup.md`
- [ ] Hướng dẫn reuse ngrok tunnel (đã setup cho Appsmith)
- [ ] Hướng dẫn tạo REST API connection trên Budibase Cloud
- [ ] Cấu hình base URL = ngrok public URL
- [ ] Cấu hình default headers: `ngrok-skip-browser-warning: true`, `Content-Type: application/json`
- [ ] Tạo các REST queries cần thiết:
  - GET /api/leads (Lead List)
  - GET /api/leads/{{leadId}} (Lead Detail)
  - PUT /api/leads/{{leadId}}/status (Update Status)
  - POST /api/workflow/lead/{{leadId}}/contact (Contact)
  - POST /api/workflow/lead/{{leadId}}/process (Process)
  - POST /api/workflow/lead/{{leadId}}/collect-documents (Collect Documents)
  - POST /api/workflow/lead/{{leadId}}/reject (Reject)

### Step 2: Screen 1 - Lead List
- [x] Tạo screen `/leads` (Lead List)
- [ ] Thêm Table component với data từ GET /api/leads
- [ ] Cấu hình columns: Status (badge), Lead ID, Customer Name, Product Type, Process Instance Key, Created At
- [ ] Ẩn columns: ownerId, productDetails, updatedAt, history
- [ ] Cấu hình row click → Navigate to `/lead/{{id}}`
- [ ] Thêm sorting theo createdAt (mới nhất trước)
- [ ] Thêm empty state message khi không có data
- [ ] Status column: color-coded (NEW_LEAD=blue, CONTACTED=yellow, PROCESSING=orange, COMPLETED=green, REJECTED=red)

### Step 3: Screen 2 - Lead Detail (Basic Info + Progress)
- [x] Tạo screen `/lead/:leadId` (Lead Detail)
- [ ] Cấu hình URL variable `leadId`
- [ ] Data Provider: GET /api/leads/{{url.leadId}}
- [ ] Hiển thị thông tin: customerName, productType, ownerId, processInstanceKey
- [ ] Hiển thị productDetails dạng JSON stringify
- [ ] Progress/Steps component: New → Contacted → Processing → Document Collected → Completed
- [ ] Button "← Quay lại" → Navigate to /leads

### Step 4: Screen 2 - Lead Detail (History Table)
- [x] Thêm Table component cho history
- [ ] Columns: timestamp, fromStatus, toStatus, changedBy, note
- [ ] Data binding từ lead.history array

### Step 5: Screen 2 - Lead Detail (Status Update)
- [x] Dropdown cho valid next statuses (dựa trên state machine)
- [ ] Conditional logic: chỉ hiển thị statuses hợp lệ theo current status
- [ ] Input fields: note, reason (conditional)
- [ ] Button "Cập nhật" → PUT /api/leads/{{leadId}}/status
- [ ] Body: {newStatus, updatedBy: "USR-STAFF-01", note, reason}
- [ ] Validation: nếu newStatus = CONTACTED → bắt buộc nhập note
- [ ] Sau khi thành công → refresh data

### Step 6: Screen 2 - Lead Detail (Workflow Action Buttons + Modals)
- [x] Button "Hành động tiếp theo" - dynamic label theo status:
  - NEW_LEAD/NEW_IMPORTED_LEAD → "Liên hệ khách hàng"
  - CONTACTED → "Xử lý cơ hội"
  - PROCESSING → "Thu thập hồ sơ"
  - Ẩn khi COMPLETED/REJECTED/DOCUMENT_COLLECTED
- [ ] Button "Từ chối" - ẩn khi COMPLETED/REJECTED
- [ ] Modal Contact: fields (action, contactResult, note) → POST /api/workflow/lead/{{leadId}}/contact
- [ ] Modal Process: fields (note) → POST /api/workflow/lead/{{leadId}}/process
- [ ] Modal Document: fields (note) → POST /api/workflow/lead/{{leadId}}/collect-documents
- [ ] Modal Reject: fields (reason, note) → POST /api/workflow/lead/{{leadId}}/reject
- [ ] Tất cả modals: body include `performedBy: "USR-STAFF-01"`
- [ ] Sau khi submit → close modal + refresh data

### Step 7: Navigation & Layout
- [x] Cấu hình app navigation (sidebar hoặc top nav)
- [ ] Menu items: Lead List, Lead Detail
- [ ] Header: "CRM Lead Management - Budibase"
- [ ] Consistent styling across screens
- [ ] Set Lead List as home screen

### Step 8: Budibase Build Guide
- [x] Tạo `frontend-budibase/docs/budibase-build-guide.md`
- [ ] Step-by-step guide chi tiết từ đầu:
  - Đăng nhập Budibase Cloud
  - Tạo App mới
  - Setup REST API connection
  - Tạo REST queries
  - Build Screen 1 (Lead List)
  - Build Screen 2 (Lead Detail)
  - Cấu hình navigation
  - Test end-to-end
- [ ] Include screenshots/mockups mô tả cho từng bước

### Step 9: Budibase JSON Export
- [x] Tạo `frontend-budibase/budibase-export.json`
- [ ] Export structure phản ánh app configuration
- [ ] Include: screens, queries, datasource config, navigation
- [ ] Note: Budibase export format khác Appsmith (workspace-level export)

### Step 10: Platform Evaluation Notes
- [x] Tạo `frontend-budibase/docs/evaluation-notes.md`
- [ ] Template đánh giá với cùng tiêu chí như Appsmith:
  - Thời gian setup datasource
  - Khả năng hiển thị danh sách + chi tiết
  - Ease of use cho conditional logic
  - Performance khi gọi API
  - Khả năng export/import app
- [ ] Thêm comparison matrix (Appsmith vs Budibase)

### Step 11: Update README
- [x] Cập nhật `frontend-budibase/README.md`
- [ ] Reflect Budibase Cloud approach (thay vì Docker)
- [ ] Quick start guide
- [ ] Link tới docs/

### Step 12: Code Generation Summary
- [x] Tạo `aidlc-docs/construction/frontend-budibase/code/code-generation-summary.md`
- [ ] Liệt kê tất cả files generated
- [ ] Key decisions
- [ ] Architecture decisions
- [ ] Limitations & workarounds
- [ ] Requirements coverage matrix

---

## Deliverables Summary

| # | File | Purpose |
|---|---|---|
| 1 | frontend-budibase/docs/datasource-setup.md | REST API connection + queries setup |
| 2 | frontend-budibase/docs/budibase-build-guide.md | Comprehensive step-by-step build guide |
| 3 | frontend-budibase/budibase-export.json | Budibase app export (importable) |
| 4 | frontend-budibase/docs/evaluation-notes.md | Platform evaluation + comparison |
| 5 | frontend-budibase/README.md | Updated project overview |
| 6 | aidlc-docs/construction/frontend-budibase/code/code-generation-summary.md | Generation summary |

