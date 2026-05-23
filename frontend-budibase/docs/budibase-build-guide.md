# Budibase Build Guide - CRM Lead Management

## Tổng quan

Hướng dẫn chi tiết xây dựng CRM Lead Management app trên Budibase Cloud với 2 screens:
1. **Lead List** - Danh sách tất cả leads
2. **Lead Detail** - Chi tiết lead + cập nhật trạng thái + workflow actions

---

## Prerequisites

- Tài khoản Budibase Cloud (budibase.app)
- Domain Service đang chạy (localhost:8090)
- ngrok tunnel đang chạy (dùng chung với Appsmith)
- Đã hoàn thành Datasource Setup (xem `datasource-setup.md`)

---

## Phần 1: Tạo App

1. Đăng nhập [budibase.app](https://budibase.app)
2. Click **"+ Create app"**
3. Đặt tên: **CRM Lead Management**
4. Click **Create**

---

## Phần 2: Setup Data (REST API)

Xem chi tiết tại `datasource-setup.md`. Tóm tắt:

1. **Data** → **+ Add source** → **REST API**
2. Name: `CRM Domain Service`
3. Base URL: `https://<ngrok-url>.ngrok-free.app`
4. Default Headers:
   - `ngrok-skip-browser-warning`: `true`
   - `Content-Type`: `application/json`
5. Tạo 7 queries (getAllLeads, getLeadById, updateLeadStatus, contactLead, processLead, collectDocuments, rejectLead)

---

## Phần 3: Screen 1 - Lead List

### 3.1 Tạo Screen

1. Vào **Design** section (sidebar trái)
2. Click **"+ Add screen"**
3. Chọn **"Blank screen"**
4. Route: `/leads`
5. Đặt làm **Home screen** (Set as home)

### 3.2 Thêm Header

1. Thêm component **Headline**
2. Text: `Danh sách Lead`
3. Size: `H2`

### 3.3 Thêm Data Provider

1. Thêm component **Data Provider**
2. Data source: chọn query `getAllLeads`
3. Sort: `createdAt` - Descending

### 3.4 Thêm Table (bên trong Data Provider)

1. Bên trong Data Provider, thêm component **Table**
2. Data: chọn Data Provider ở trên
3. Cấu hình Columns:

| Column | Label | Type | Visible |
|---|---|---|---|
| status | Trạng thái | Text | ✅ |
| id | Lead ID | Text | ✅ |
| customerName | Khách hàng | Text | ✅ |
| productType | Sản phẩm | Text | ✅ |
| processInstanceKey | Process Key | Text | ✅ |
| createdAt | Ngày tạo | Text | ✅ |
| ownerId | - | - | ❌ (ẩn) |
| productDetails | - | - | ❌ (ẩn) |
| updatedAt | - | - | ❌ (ẩn) |
| history | - | - | ❌ (ẩn) |

### 3.5 Color-coded Status Badge

Để hiển thị status dạng color badge, sử dụng **Tag** component hoặc conditional styling:

**Option A - Dùng Bindings trong Table column:**

Trong column `status`, set custom display bằng binding:

```handlebars
{{ Row.status }}
```

Thêm conditional color class dựa trên status value. Trong Budibase, dùng **Conditions** trên component:
- Condition: `{{ Row.status }}` equals `NEW_LEAD` → Background: Blue
- Condition: `{{ Row.status }}` equals `CONTACTED` → Background: Yellow
- Condition: `{{ Row.status }}` equals `PROCESSING` → Background: Orange
- Condition: `{{ Row.status }}` equals `COMPLETED` → Background: Green
- Condition: `{{ Row.status }}` equals `REJECTED` → Background: Red

**Option B - Dùng Tag component trong custom column:**

1. Thêm custom column trong Table
2. Bên trong, thêm **Tag** component
3. Text: `{{ Row.status }}`
4. Dùng Conditions để set color

### 3.6 Row Click Navigation

1. Chọn Table component
2. Trong Settings, tìm **"On row click"** action
3. Thêm action: **Navigate To**
4. Destination: Screen
5. Screen: `/lead/{{ Row.id }}`

### 3.7 Empty State

1. Thêm component **Paragraph** bên ngoài Table (hoặc dùng Table empty state)
2. Text: `Không có lead nào`
3. Condition: Show when Data Provider row count = 0

---

## Phần 4: Screen 2 - Lead Detail

### 4.1 Tạo Screen

1. Click **"+ Add screen"**
2. Chọn **"Blank screen"**
3. Route: `/lead/:leadId`
4. URL variable: `leadId`

### 4.2 Data Provider

1. Thêm component **Data Provider**
2. Data source: query `getLeadById`
3. Binding: `leadId` = `{{ url.leadId }}`

### 4.3 Back Button

1. Thêm **Button** component (ngoài Data Provider hoặc trên cùng)
2. Text: `← Quay lại`
3. Action: **Navigate To** → Screen `/leads`
4. Variant: Secondary/Quiet

### 4.4 Lead Info Section

Bên trong Data Provider, thêm các components:

1. **Headline** - Text: `{{ getLeadById.data.customerName }}`
2. **Container** (horizontal layout) với các **Paragraph**:
   - `Sản phẩm: {{ getLeadById.data.productType }}`
   - `Chủ sở hữu: {{ getLeadById.data.ownerId }}`
   - `Process Key: {{ getLeadById.data.processInstanceKey }}`
3. **Paragraph** - Text: `Chi tiết sản phẩm: {{ stringify getLeadById.data.productDetails }}`

### 4.5 Progress Steps

Budibase không có native Progress Steps component. Workaround:

**Option A - Dùng Container + Tags:**

1. Thêm **Container** (horizontal layout)
2. Thêm 5 **Tag** components:
   - Tag 1: `New` - Condition: always show, highlight if status matches
   - Tag 2: `Contacted` - highlight if status >= CONTACTED
   - Tag 3: `Processing` - highlight if status >= PROCESSING
   - Tag 4: `Document Collected` - highlight if status >= DOCUMENT_COLLECTED
   - Tag 5: `Completed` - highlight if status = COMPLETED

**Conditional highlighting logic** (dùng Conditions trên mỗi Tag):
```
Tag "New": Background Green if status IN [NEW_LEAD, NEW_IMPORTED_LEAD, CONTACTED, PROCESSING, DOCUMENT_COLLECTED, COMPLETED]
Tag "Contacted": Background Green if status IN [CONTACTED, PROCESSING, DOCUMENT_COLLECTED, COMPLETED]
Tag "Processing": Background Green if status IN [PROCESSING, DOCUMENT_COLLECTED, COMPLETED]
Tag "Document Collected": Background Green if status IN [DOCUMENT_COLLECTED, COMPLETED]
Tag "Completed": Background Green if status = COMPLETED
```

**Option B - Dùng custom HTML/Markdown:**

1. Thêm **Markdown Viewer** component
2. Binding content dựa trên status để hiển thị progress text

### 4.6 History Table

1. Thêm **Headline** - Text: `Lịch sử trạng thái`
2. Thêm **Data Provider** (nested) - Data: `{{ getLeadById.data.history }}`
   - Hoặc dùng **Repeater** component
3. Thêm **Table** bên trong:

| Column | Label |
|---|---|
| timestamp | Thời gian |
| fromStatus | Từ trạng thái |
| toStatus | Đến trạng thái |
| changedBy | Người thay đổi |
| note | Ghi chú |

> **Lưu ý**: Nếu Budibase không hỗ trợ nested array trực tiếp trong Table, dùng **Repeater** component với Cards layout.

### 4.7 Status Update Section

1. Thêm **Headline** - Text: `Cập nhật trạng thái`
2. Thêm **Form** component
3. Bên trong Form:

**Dropdown - New Status:**
- Thêm **Options Picker** (hoặc Select)
- Label: `Trạng thái mới`
- Options: Dynamic dựa trên current status

**Conditional Options Logic** (dùng JavaScript binding):
```javascript
// Trong Options binding
var status = $("getLeadById.data.status");
var options = [];
if (status === "NEW_LEAD" || status === "NEW_IMPORTED_LEAD") {
  options = [{label: "Đã liên hệ", value: "CONTACTED"}];
} else if (status === "CONTACTED") {
  options = [{label: "Đang xử lý", value: "PROCESSING"}];
} else if (status === "PROCESSING") {
  options = [{label: "Đã thu thập hồ sơ", value: "DOCUMENT_COLLECTED"}];
} else if (status === "DOCUMENT_COLLECTED") {
  options = [{label: "Hoàn thành", value: "COMPLETED"}];
}
return options;
```

**Input - Note:**
- Thêm **Text Field**
- Label: `Ghi chú`
- Placeholder: `Nhập ghi chú...`

**Input - Reason:**
- Thêm **Text Field**
- Label: `Lý do`
- Condition: Show only when needed

**Button - Cập nhật:**
- Text: `Cập nhật trạng thái`
- Action chain:
  1. Execute query `updateLeadStatus` với bindings:
     - leadId: `{{ url.leadId }}`
     - newStatus: `{{ Form.Fields.newStatus }}`
     - updatedBy: `USR-STAFF-01`
     - note: `{{ Form.Fields.note }}`
     - reason: `{{ Form.Fields.reason }}`
  2. Refresh Data Provider
  3. Show Notification: "Cập nhật thành công"
  4. Clear Form

**Condition**: Ẩn toàn bộ section khi status = COMPLETED hoặc REJECTED

### 4.8 Workflow Action Buttons

**Button "Hành động tiếp theo":**

1. Thêm **Button** component
2. Dynamic label dùng binding:
```javascript
var status = $("getLeadById.data.status");
if (status === "NEW_LEAD" || status === "NEW_IMPORTED_LEAD") return "📞 Liên hệ khách hàng";
if (status === "CONTACTED") return "📋 Xử lý cơ hội";
if (status === "PROCESSING") return "📄 Thu thập hồ sơ";
return "";
```
3. Condition: Hide when status IN [COMPLETED, REJECTED, DOCUMENT_COLLECTED]
4. Action: Open Modal (tương ứng với status)

**Button "Từ chối":**

1. Thêm **Button** component
2. Text: `❌ Từ chối`
3. Variant: Destructive/Warning
4. Condition: Hide when status IN [COMPLETED, REJECTED]
5. Action: Open Modal Reject

### 4.9 Modal - Liên hệ khách hàng (Contact)

1. Thêm **Modal** component (hoặc Side Panel)
2. Title: `Liên hệ khách hàng`
3. Bên trong Modal, thêm **Form**:

| Field | Type | Label | Options |
|---|---|---|---|
| action | Options Picker | Hành động | CALL (Gọi điện), EMAIL (Gửi email), VISIT (Gặp trực tiếp) |
| contactResult | Options Picker | Kết quả | INTERESTED (Quan tâm), NOT_INTERESTED (Không quan tâm), CALLBACK (Hẹn gọi lại) |
| note | Text Area | Ghi chú | - |

4. Button "Xác nhận":
   - Action chain:
     1. Execute query `contactLead`:
        - leadId: `{{ url.leadId }}`
        - action: `{{ Form.Fields.action }}`
        - contactResult: `{{ Form.Fields.contactResult }}`
        - note: `{{ Form.Fields.note }}`
     2. Close Modal
     3. Refresh Data Provider
     4. Show Notification: "Đã cập nhật kết quả liên hệ"

### 4.10 Modal - Xử lý cơ hội (Process)

1. Thêm **Modal** component
2. Title: `Xử lý cơ hội`
3. Form fields:

| Field | Type | Label |
|---|---|---|
| note | Text Area | Ghi chú xử lý |

4. Button "Xác nhận":
   - Execute query `processLead`:
     - leadId: `{{ url.leadId }}`
     - note: `{{ Form.Fields.note }}`
   - Close Modal → Refresh → Notification

### 4.11 Modal - Thu thập hồ sơ (Collect Documents)

1. Thêm **Modal** component
2. Title: `Thu thập hồ sơ`
3. Form fields:

| Field | Type | Label |
|---|---|---|
| note | Text Area | Ghi chú hồ sơ |

4. Button "Xác nhận":
   - Execute query `collectDocuments`:
     - leadId: `{{ url.leadId }}`
     - note: `{{ Form.Fields.note }}`
   - Close Modal → Refresh → Notification

### 4.12 Modal - Từ chối (Reject)

1. Thêm **Modal** component
2. Title: `Từ chối Lead`
3. Form fields:

| Field | Type | Label |
|---|---|---|
| reason | Text Field | Lý do từ chối |
| note | Text Area | Ghi chú thêm |

4. Button "Xác nhận từ chối":
   - Variant: Destructive
   - Execute query `rejectLead`:
     - leadId: `{{ url.leadId }}`
     - reason: `{{ Form.Fields.reason }}`
     - note: `{{ Form.Fields.note }}`
   - Close Modal → Refresh → Notification

---

## Phần 5: Navigation & Layout

### 5.1 App Navigation

1. Vào **Settings** (gear icon) hoặc **Navigation** section
2. Cấu hình navigation links:

| Label | Path |
|---|---|
| Danh sách Lead | /leads |
| Chi tiết Lead | /lead/:leadId |

3. Set navigation type: **Sidebar** hoặc **Top**
4. App title: `CRM Lead Management`

### 5.2 Home Screen

1. Set `/leads` (Lead List) làm home screen
2. Khi user mở app → tự động vào Lead List

### 5.3 Styling

- Sử dụng Budibase default theme
- Consistent padding/margin across screens
- Button variants: Primary (actions), Secondary (back), Destructive (reject)

---

## Phần 6: Testing

### Test 1: Lead List
1. Mở app → verify Lead List hiển thị
2. Verify columns đúng (Status, ID, Name, Product, Process Key, Created At)
3. Verify sorting (mới nhất trước)
4. Click row → verify navigate tới Lead Detail

### Test 2: Lead Detail - Basic
1. Verify thông tin lead hiển thị đúng
2. Verify progress steps highlight đúng theo status
3. Verify history table hiển thị đúng
4. Click "Quay lại" → verify navigate về Lead List

### Test 3: Status Update
1. Chọn new status từ dropdown
2. Nhập note
3. Click "Cập nhật" → verify status thay đổi
4. Verify history table có entry mới

### Test 4: Workflow Actions
1. Click "Liên hệ khách hàng" → verify modal mở
2. Điền form → Submit → verify status thay đổi
3. Repeat cho Process, Collect Documents, Reject

### Test 5: Conditional Logic
1. Verify buttons ẩn/hiện đúng theo status
2. Verify dropdown options đúng theo state machine
3. Verify COMPLETED/REJECTED → không có action buttons

---

## Phần 7: Export App

1. Vào **Settings** → **Export**
2. Hoặc: Workspace level → Export workspace
3. Chọn export format (JSON)
4. Lưu file → đặt tại `frontend-budibase/budibase-export.json`

> **Lưu ý**: Budibase export bao gồm app structure, screens, queries, nhưng KHÔNG bao gồm datasource credentials. Khi import cần reconfigure datasource URL.

