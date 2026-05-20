# Appsmith Cloud - Build Guide (Step-by-Step)

## Tổng quan

Hướng dẫn chi tiết xây dựng CRM Lead Management app trên Appsmith Cloud, kết nối với Domain Service qua ngrok tunnel.

**Thời gian ước tính**: 1-2 giờ

---

## Prerequisites

1. ✅ Domain Service đang chạy trên localhost:8090
2. ✅ ngrok đã cài đặt và cấu hình (xem `ngrok-setup.md`)
3. ✅ Tài khoản Appsmith Cloud (app.appsmith.com)
4. ✅ ngrok tunnel đang chạy (`ngrok http 8090`)

---

## Phần 1: Setup Datasource

### 1.1 Tạo Application
1. Login vào https://app.appsmith.com
2. Click **"+ New"** → **"Application"**
3. Đặt tên: `CRM Lead Management`

### 1.2 Tạo Datasource
1. Sidebar trái → **Queries** → **"+"** → **"New Datasource"**
2. Chọn **"Authenticated API"**
3. Cấu hình:
   - **Name**: `CRM_Domain_Service`
   - **URL**: Paste ngrok URL (VD: `https://abc123.ngrok-free.app`)
   - **Authentication Type**: None
4. **Headers** (Add 2 headers):
   - `ngrok-skip-browser-warning` = `true`
   - `Content-Type` = `application/json`
5. Click **"Save"**

### 1.3 Test Datasource
1. Click **"+ New Query"** từ datasource
2. Method: GET, Path: `/api/leads`
3. Click **"Run"** → verify JSON response

---

## Phần 2: Page 1 - Lead List

### 2.1 Rename Page
- Click chuột phải vào "Page1" → Rename → `Lead List`

### 2.2 Tạo Query: getLeads
1. **Queries** → **"+"** → Chọn `CRM_Domain_Service`
2. Cấu hình:
   - **Name**: `getLeads`
   - **Method**: GET
   - **URL**: `/api/leads`
3. Tab **Settings** → Enable **"Run on Page Load"**
4. Click **"Run"** để test

### 2.3 Thêm Title
1. Kéo **Text** widget vào canvas
2. **Text**: `Danh sách Lead`
3. **Font Size**: Heading 1

### 2.4 Thêm Table Widget
1. Kéo **Table** widget vào canvas (chiếm phần lớn page)
2. **Table Data**: `{{getLeads.data}}`
3. Cấu hình columns:

| Column | Label | Computed Value |
|---|---|---|
| status | Trạng thái | `{{currentRow.status}}` |
| id | Mã Lead | `{{currentRow.id}}` |
| customerName | Tên KH | `{{currentRow.customerName}}` |
| productType | Sản phẩm | `{{currentRow.productType}}` |
| createdAt | Ngày tạo | `{{moment(currentRow.createdAt).format('DD/MM/YYYY HH:mm')}}` |

### 2.5 Status Color Coding
Cho column `status`, vào **Style** → **Cell Background**:
```javascript
{{currentRow.status === 'NEW_LEAD' ? '#E3F2FD' : 
  currentRow.status === 'CONTACTED' ? '#FFF8E1' : 
  currentRow.status === 'PROCESSING' ? '#FFF3E0' : 
  currentRow.status === 'COMPLETED' ? '#E8F5E9' : 
  currentRow.status === 'REJECTED' ? '#FFEBEE' : '#F5F5F5'}}
```

### 2.6 Row Click Navigation
1. Chọn Table widget → Properties → **onRowSelected**
2. Action: **Navigate to** → Page: `Lead Detail`
3. Query params: `leadId: {{tbl_leads.selectedRow.id}}`

Hoặc dùng JS:
```javascript
{{navigateTo('Lead Detail', {leadId: Table1.selectedRow.id})}}
```

### 2.7 Empty State
- Table widget → **No Data Message**: `Không có Lead nào trong hệ thống`

---

## Phần 3: Page 2 - Lead Detail

### 3.1 Tạo Page mới
1. Sidebar → **Pages** → **"+"** → Đặt tên: `Lead Detail`

### 3.2 Tạo Query: getLeadDetail
1. **Name**: `getLeadDetail`
2. **Method**: GET
3. **URL**: `/api/leads/{{appsmith.URL.queryParams.leadId}}`
4. Enable **"Run on Page Load"**

### 3.3 Tạo Query: updateLeadStatus
1. **Name**: `updateLeadStatus`
2. **Method**: PUT
3. **URL**: `/api/leads/{{appsmith.URL.queryParams.leadId}}/status`
4. **Body** (JSON):
```json
{
  "newStatus": "{{sel_new_status.selectedOptionValue}}",
  "updatedBy": "USR-STAFF-01",
  "note": "{{inp_note.text}}",
  "reason": "Manual update"
}
```
5. **KHÔNG** enable "Run on Page Load"

### 3.4 Layout - Phần trên

> **Cách bố trí**: Appsmith dùng drag-and-drop grid. Bạn kéo từng widget vào canvas rồi sắp xếp vị trí bằng cách kéo thả. Các mô tả bên dưới chỉ gợi ý vị trí tương đối (trên/dưới, trái/phải).

**Phía trên cùng page: Title**
- Kéo **Text** widget vào góc trên: `Chi tiết Lead: {{getLeadDetail.data.id}}` (Heading 1)
- (Optional) Kéo **Button** widget bên trái title: `← Quay lại` → onClick: `{{navigateTo('Lead List')}}` — giúp user quay lại Lead List mà không cần sidebar

**Bên dưới title, nửa trái: Thông tin khách hàng**

Kéo 1 **Container** widget (chiếm ~6 columns bên trái), bên trong kéo các Text widget:
- Text (bold): `Khách hàng`
- Text: `{{getLeadDetail.data.customerName}}`
- Text (bold): `Sản phẩm`
- Text: `{{getLeadDetail.data.productType}}`
- Text (bold): `Người phụ trách`
- Text: `{{getLeadDetail.data.ownerId}}`

**Bên dưới title, nửa phải: Progress trạng thái**

Kéo 1 **Container** widget (chiếm ~6 columns bên phải, ngang hàng với container trái), bên trong:
- Text (bold): `Tiến trình trạng thái`
- **Progress Bar** widget:
  - Value:
```javascript
{{(() => {
  const statusMap = {
    'NEW_LEAD': 10, 'NEW_IMPORTED_LEAD': 15,
    'CONTACTED': 35, 'PROCESSING': 55,
    'DOCUMENT_COLLECTED': 75, 'COMPLETED': 100, 'REJECTED': 100
  };
  return statusMap[getLeadDetail.data.status] || 0;
})()}}
```
  - Fill Color:
```javascript
{{getLeadDetail.data.status === 'REJECTED' ? '#F44336' : 
  getLeadDetail.data.status === 'COMPLETED' ? '#4CAF50' : '#2196F3'}}
```

### 3.5 Layout - Cập nhật trạng thái

**Bên dưới phần info, nửa trái: Form cập nhật status**

Kéo 1 **Container** widget (~6 columns bên trái), bên trong:
- Text (bold): `Cập nhật trạng thái`
- **Select** widget (`sel_new_status`):
  - Options (JS):
```javascript
{{(() => {
  const currentStatus = getLeadDetail.data?.status;
  const transitions = {
    'NEW_LEAD': ['CONTACTED', 'REJECTED'],
    'NEW_IMPORTED_LEAD': ['CONTACTED', 'REJECTED'],
    'CONTACTED': ['PROCESSING', 'REJECTED'],
    'PROCESSING': ['DOCUMENT_COLLECTED', 'REJECTED'],
    'DOCUMENT_COLLECTED': ['COMPLETED', 'REJECTED']
  };
  const nextStatuses = transitions[currentStatus] || [];
  return nextStatuses.map(s => ({label: s, value: s}));
})()}}
```
- **Input** widget (`inp_note`): Label = "Ghi chú"
- **Button**: `Cập nhật`
  - onClick:
```javascript
{{updateLeadStatus.run().then(() => {
  getLeadDetail.run();
  showAlert('Cập nhật thành công!', 'success');
}).catch((e) => showAlert('Lỗi: ' + e.message, 'error'))}}
```

Bên phải (Container):
- Text (bold): `Chi tiết sản phẩm`
- Text: `{{JSON.stringify(getLeadDetail.data.productDetails, null, 2)}}`

### 3.6 Layout - Lịch sử

**Bên dưới phần update status: History Table (full width)**
- Text (Heading 2): `Lịch sử trạng thái`
- **Table** widget:
  - Data: `{{getLeadDetail.data.history}}`
  - Columns:

| Column | Label | Value |
|---|---|---|
| timestamp | Thời gian | `{{moment(currentRow.timestamp).format('DD/MM/YYYY HH:mm')}}` |
| fromStatus | Từ | `{{currentRow.fromStatus || '-'}}` |
| toStatus | Đến | `{{currentRow.toStatus}}` |
| changedBy | Người thay đổi | `{{currentRow.changedBy}}` |
| note | Ghi chú | `{{currentRow.note || '-'}}` |

---

## Phần 4: Page 3 - Lead Allocation

### 4.1 Tạo Page
- **Pages** → **"+"** → Đặt tên: `Lead Allocation`

### 4.2 Tạo Queries

**getAllocatableLeads:**
- Method: GET
- URL: `/api/leads/allocatable?ownerId=USR-MGR-01`
- Run on Page Load: Yes

**getSubordinates:**
- Method: GET
- URL: `/api/users/subordinates`
- Run on Page Load: Yes

**allocateLeads:**
- Method: POST
- URL: `/api/leads/allocate`
- Body:
```json
{
  "leadIds": {{JSON.stringify(Table1.selectedRows.map(r => r.id))}},
  "targetUserIds": {{JSON.stringify(Table2.selectedRows.map(r => r.id))}},
  "requestedBy": "USR-MGR-01"
}
```
- Run on Page Load: No

### 4.3 Layout

**Row 1: Title + Buttons**
- Text (H1): `Phân bổ Lead`
- Button: `Chọn tất cả` → `{{Table1.selectAllRows()}}`
- Button: `Bỏ chọn` → `{{Table1.deselectAllRows()}}`

**Row 2: Allocatable Leads Table**
- Table widget (enable **Multi-row selection**):
  - Data: `{{getAllocatableLeads.data}}`
  - Columns: id, customerName, status, productType

**Row 3: Selected count + Allocate button**
- Text: `Đã chọn: {{Table1.selectedRows.length}} lead(s)`
- Button: `Phân bổ`
  - Disabled: `{{Table1.selectedRows.length === 0}}`
  - onClick: `{{showModal('modal_allocation')}}`

### 4.4 Modal: Chọn cán bộ

1. Kéo **Modal** widget → Name: `modal_allocation`
2. Bên trong Modal:

- Text (H2): `Chọn cán bộ phân bổ`
- **Text** (warning, conditional):
  - Text: `⚠️ Cơ hội sẽ được chia đều cho các cán bộ được chọn`
  - Color: `#FF9800`
  - Visible: `{{Table2.selectedRows.length >= 2}}`
- **Table** (multi-select):
  - Data: `{{getSubordinates.data}}`
  - Columns: id, name, username
- Text: `Phân bổ {{Table1.selectedRows.length}} lead cho {{Table2.selectedRows.length}} cán bộ`
- Button: `Phân bổ cơ hội`
  - Disabled: `{{Table2.selectedRows.length === 0}}`
  - onClick:
```javascript
{{allocateLeads.run().then(() => {
  closeModal('modal_allocation');
  getAllocatableLeads.run();
  showAlert('Phân bổ thành công!', 'success');
}).catch((e) => showAlert('Lỗi: ' + e.message, 'error'))}}
```
- Button: `Hủy` → `{{closeModal('modal_allocation')}}`

---

## Phần 5: Page 4 - Workflow Tasks (Dynamic Form)

### 5.1 Tạo Page
- **Pages** → **"+"** → Đặt tên: `Workflow Tasks`

### 5.2 Tạo Queries

**getWorkflowTasks:**
- Method: GET
- URL: `/api/workflow/tasks`
- Run on Page Load: Yes

**getFormSchema:**
- Method: GET
- URL: `/api/forms/{{Table1.selectedRow.taskType}}`
- Run on Page Load: No

**completeTask:**
- Method: POST
- URL: `/api/workflow/tasks/{{Table1.selectedRow.jobKey}}/complete`
- Body: `{{JSON.stringify(JSONForm1.formData)}}`
- Run on Page Load: No

### 5.3 Layout

**Left Panel (5 columns): Task List**
- Text (H1): `Workflow Tasks`
- Table widget:
  - Data: `{{getWorkflowTasks.data}}`
  - Columns: jobKey, taskType, elementId
  - onRowSelected: `{{getFormSchema.run()}}`

**Right Panel (7 columns): Dynamic Form**
- Container (visible when form loaded):
  - Text (H2): `{{getFormSchema.data?.title || 'Chọn task'}}`
  - Text: `{{getFormSchema.data?.description || ''}}`
  - **JSON Form** widget:
    - Source Data (JS):
```javascript
{{(() => {
  const schema = getFormSchema.data;
  if (!schema || !schema.fields) return {};
  const formSource = {};
  schema.fields.forEach(field => {
    switch(field.type) {
      case 'TEXT':
      case 'TEXTAREA':
        formSource[field.id] = '';
        break;
      case 'NUMBER':
        formSource[field.id] = 0;
        break;
      case 'DROPDOWN':
      case 'RADIO':
        formSource[field.id] = field.options?.[0]?.value || '';
        break;
      case 'DATE':
        formSource[field.id] = new Date().toISOString().split('T')[0];
        break;
      case 'CHECKBOX':
        formSource[field.id] = false;
        break;
      default:
        formSource[field.id] = '';
    }
  });
  return formSource;
})()}}
```
    - onSubmit:
```javascript
{{completeTask.run().then(() => {
  getWorkflowTasks.run();
  resetWidget('JSONForm1');
  showAlert('Task completed!', 'success');
}).catch((e) => showAlert('Error: ' + e.message, 'error'))}}
```

### 5.4 Visibility Condition Logic

Cho các field có `visibilityCondition`, sử dụng widget **Visible** property:

```javascript
{{(() => {
  // Lấy field config từ schema
  const schema = getFormSchema.data;
  const field = schema?.fields?.find(f => f.id === 'callbackDate');
  if (!field || !field.visibilityCondition) return true;
  
  const condition = field.visibilityCondition;
  const dependValue = JSONForm1.formData?.[condition.dependsOn];
  
  if (condition.operator === 'EQUALS') return dependValue === condition.value;
  if (condition.operator === 'NOT_EQUALS') return dependValue !== condition.value;
  return true;
})()}}
```

**Lưu ý**: JSON Form widget của Appsmith tự động generate form từ sourceData. Để custom visibility, bạn có thể:
1. Sử dụng JSON Form auto-generate (đơn giản nhất)
2. Hoặc build custom form với individual widgets (linh hoạt hơn cho visibility conditions)

**Khuyến nghị cho POC**: Dùng JSON Form auto-generate. Nếu cần visibility conditions phức tạp, chuyển sang custom widgets.

---

## Phần 6: Navigation

Appsmith tự động tạo sidebar navigation từ danh sách Pages. Đảm bảo:

1. **Page order** (kéo thả trong sidebar):
   - Lead List (default)
   - Lead Detail
   - Lead Allocation
   - Workflow Tasks

2. **App name**: Click tên app ở góc trên trái → đổi thành `CRM Lead Management`

3. **Theme**: Settings → Theme → chọn theme phù hợp (Default hoặc Classic)

---

## Phần 7: Deploy & Test

### 7.1 Deploy
1. Click **"Deploy"** (góc trên phải)
2. App sẽ được publish với URL public trên Appsmith Cloud

### 7.2 Test Checklist

| # | Test Case | Expected |
|---|---|---|
| 1 | Mở Lead List | Hiển thị table với data từ API |
| 2 | Click row | Navigate sang Lead Detail với đúng leadId |
| 3 | Xem Lead Detail | Hiển thị info + progress + history |
| 4 | Cập nhật status | Dropdown chỉ hiện valid statuses, update thành công |
| 5 | Mở Lead Allocation | Hiển thị allocatable leads |
| 6 | Chọn leads + Phân bổ | Modal hiện, chọn cán bộ, phân bổ thành công |
| 7 | Warning >= 2 cán bộ | Hiển thị warning message |
| 8 | Mở Workflow Tasks | Hiển thị active tasks |
| 9 | Chọn task | Form render từ schema |
| 10 | Submit form | Task completed, list refresh |

---

## Troubleshooting

| Vấn đề | Giải pháp |
|---|---|
| Query trả về empty | Kiểm tra ngrok đang chạy + Domain Service đang chạy |
| 502 error | Domain Service chưa start hoặc đang restart |
| CORS error | Không nên xảy ra (Appsmith gọi server-side). Nếu có, verify @CrossOrigin annotation |
| Form không render | Kiểm tra getFormSchema response format |
| Navigation không hoạt động | Verify page name chính xác (case-sensitive) |
| ngrok URL expired | Restart ngrok, cập nhật datasource URL |
