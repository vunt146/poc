# Appsmith Cloud - Datasource Setup Guide

## Tổng quan

Hướng dẫn tạo và cấu hình REST API Datasource trên Appsmith Cloud để kết nối với Domain Service qua ngrok tunnel.

---

## 1. Đăng nhập Appsmith Cloud

1. Truy cập https://app.appsmith.com
2. Đăng nhập với tài khoản đã có
3. Bạn sẽ thấy Appsmith Dashboard

---

## 2. Tạo Application mới

1. Click **"+ New"** hoặc **"Create New"**
2. Chọn **"Application"**
3. Đặt tên: `CRM Lead Management`
4. Application sẽ mở với 1 page mặc định (Page1)

---

## 3. Tạo Datasource

### 3.1 Mở Datasource panel
1. Trong Editor, click **"+"** ở sidebar trái (phần Queries)
2. Hoặc vào **Explorer** → **Datasources** → **+ New Datasource**

### 3.2 Chọn loại Datasource
1. Chọn **"REST API"** (Authenticated API)
2. Đặt tên: `CRM_Domain_Service`

### 3.3 Cấu hình Datasource
| Field | Value |
|---|---|
| **Name** | CRM_Domain_Service |
| **URL** | `https://xxxx-xxx-xxx.ngrok-free.app` (URL từ ngrok) |
| **Headers** | (xem bên dưới) |
| **Authentication** | None |

### 3.4 Headers (quan trọng)
Thêm header để bypass ngrok browser warning:

| Key | Value |
|---|---|
| `ngrok-skip-browser-warning` | `true` |
| `Content-Type` | `application/json` |

### 3.5 Test Connection
1. Click **"Test"** button
2. Nếu thành công → hiển thị "Connection successful"
3. Click **"Save"**

---

## 4. Verify Datasource hoạt động

### Tạo test query
1. Click **"+ New Query"** từ datasource vừa tạo
2. Cấu hình:
   - **Method**: GET
   - **URL**: `/api/leads`
3. Click **"Run"**
4. Verify response trả về JSON array với lead data

### Expected response
```json
[
  {
    "id": "LEAD-001",
    "customerName": "Nguyễn Văn A",
    "status": "NEW_LEAD",
    "ownerId": "USR-MGR-01",
    "productType": "CREDIT_CARD",
    ...
  }
]
```

---

## 5. Cấu hình Datasource cho các API endpoints

Appsmith cho phép tạo nhiều queries từ cùng 1 datasource. Các queries sẽ dùng:

| Query Name | Method | Path |
|---|---|---|
| getLeads | GET | /api/leads |
| getLeadDetail | GET | /api/leads/{{leadId}} |
| updateLeadStatus | PUT | /api/leads/{{leadId}}/status |
| getAllocatableLeads | GET | /api/leads/allocatable?ownerId=USR-MGR-01 |
| allocateLeads | POST | /api/leads/allocate |
| getSubordinates | GET | /api/users/subordinates |
| getWorkflowTasks | GET | /api/workflow/tasks |
| getFormSchema | GET | /api/forms/{{taskType}} |
| completeTask | POST | /api/workflow/tasks/{{jobKey}}/complete |

---

## 6. Khi ngrok URL thay đổi

Mỗi lần restart ngrok, URL mới được tạo. Cập nhật:

1. Vào **Datasources** → click `CRM_Domain_Service`
2. Sửa **URL** thành ngrok URL mới
3. Click **"Save"**
4. Test lại connection

---

## 7. Troubleshooting

| Vấn đề | Nguyên nhân | Giải pháp |
|---|---|---|
| Connection timeout | ngrok chưa chạy | Start ngrok: `ngrok http 8090` |
| 502 Bad Gateway | Domain Service chưa chạy | Start Spring Boot service |
| CORS error | Không nên xảy ra (server-side call) | Verify CrossOrigin annotation trên controllers |
| 403/404 | URL sai | Kiểm tra ngrok URL + API path |
| Empty response | Data chưa load | Restart Domain Service |
| ngrok warning page | Header thiếu | Thêm `ngrok-skip-browser-warning: true` |
