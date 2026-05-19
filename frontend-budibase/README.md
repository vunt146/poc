# Frontend Budibase - CRM Lead Management POC

## Tổng quan

Budibase app cho CRM Lead Management, cùng chức năng với Appsmith để so sánh.

## Setup

### 1. Khởi động Budibase
```bash
# Thêm Budibase vào docker-compose hoặc chạy riêng
docker run -d -p 8081:80 budibase/budibase:latest
```
Truy cập: http://localhost:8081

### 2. Tạo Datasource
- Vào Data → Add Source → REST API
- Name: `CRM Domain Service`
- URL: `http://host.docker.internal:8090` (hoặc IP của host)

### 3. Tạo Screens

#### Screen 1: Lead List
- **Component**: Table
- **Data**: REST query GET /api/leads
- **Columns**: Status, ID, Customer Name, Product
- **Row click**: Navigate to detail screen

#### Screen 2: Lead Detail
- **Data**: REST query GET /api/leads/{{id}}
- **Components**:
  - Headline + Paragraph cho thông tin
  - Progress Steps cho trạng thái
  - Repeater cho history
  - Form + Dropdown cho cập nhật

#### Screen 3: Lead Allocation
- **Data**: GET /api/leads/allocatable?ownerId=USR-MGR-01
- **Components**:
  - Checkbox Group cho Leads
  - Button → Modal
  - Modal: Checkbox Group cho Users
  - Confirm button → POST /api/leads/allocate

#### Screen 4: Dynamic Form
- **Data**: GET /api/forms/{{taskType}}
- **Logic**: Custom JS để parse schema và render
- **Components**: Dynamic Form Builder hoặc custom component

## Dynamic Form Rendering

Budibase approach:
1. Fetch form schema từ API
2. Dùng Budibase Bindings + Conditional logic
3. Render fields dựa trên schema type
4. Handle visibility conditions qua JS bindings
5. Submit → complete workflow task

## So sánh Notes

Ghi nhận các điểm sau khi build:
- Thời gian setup datasource
- Khả năng render dynamic form
- Conditional field handling
- Component library richness
- Self-hosted deployment ease
- Community & documentation quality
