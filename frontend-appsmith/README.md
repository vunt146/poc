# Frontend Appsmith - CRM Lead Management POC

## Tổng quan

Appsmith app cho CRM Lead Management, kết nối với Domain Service REST API.

## Setup

### 1. Khởi động Appsmith
```bash
# Appsmith đã có trong docker-compose.yml
docker-compose up -d appsmith
```
Truy cập: http://localhost:8080

### 2. Tạo Datasource
- Vào Settings → Datasources → New Datasource → REST API
- Name: `CRM Domain Service`
- URL: `http://domain-service:8090` (trong Docker network)
- Hoặc `http://localhost:8090` (nếu Appsmith chạy ngoài Docker)

### 3. Tạo Pages

#### Page 1: Lead List
- **Widget**: Table hoặc List
- **Query**: GET /api/leads
- **Columns**: Status, Lead ID, Customer Name, Product Type
- **Actions**: Click row → navigate to Lead Detail

#### Page 2: Lead Detail
- **Query**: GET /api/leads/{{leadId}}
- **Widgets**: 
  - Text widgets cho thông tin KH
  - Progress bar cho trạng thái
  - Table cho history
  - Dropdown + Button cho cập nhật trạng thái

#### Page 3: Lead Allocation
- **Query**: GET /api/leads/allocatable?ownerId=USR-MGR-01
- **Widgets**:
  - Checkbox List cho chọn Leads
  - Button "Phân bổ" → mở Modal
  - Modal: Checkbox list cán bộ (GET /api/users/subordinates)
  - Button "Phân bổ cơ hội" → POST /api/leads/allocate

#### Page 4: Dynamic Form
- **Query**: GET /api/forms/{{taskType}}
- **Widget**: JSON Form hoặc custom form builder
- **Logic**: Parse JSON schema → render fields dynamically
- **Conditional**: Show/hide fields based on visibilityCondition

## Dynamic Form Rendering

Appsmith hỗ trợ JSON Form widget:
1. Gọi API lấy form schema
2. Transform schema thành Appsmith JSON Form format
3. Render form dynamically
4. Submit form data → POST /api/workflow/tasks/{id}/complete

## So sánh Notes

Ghi nhận các điểm sau khi build:
- Thời gian setup datasource
- Khả năng render dynamic form từ JSON
- Ease of use cho conditional fields
- Performance khi polling API
- Export/Import app capability
