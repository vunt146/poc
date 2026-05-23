# Datasource Setup - Budibase Cloud + ngrok

## Tổng quan

Budibase Cloud (budibase.app) kết nối tới Domain Service (localhost:8090) thông qua ngrok tunnel.
Dùng chung ngrok tunnel đã setup cho Appsmith.

---

## Bước 1: Đảm bảo ngrok đang chạy

Nếu ngrok chưa chạy, khởi động tunnel:

```bash
ngrok http 8090
```

Ghi nhận URL public (ví dụ: `https://abc123.ngrok-free.app`)

> **Lưu ý**: Nếu đã chạy ngrok cho Appsmith, dùng chung URL đó. Không cần tạo tunnel mới.

---

## Bước 2: Tạo REST API Connection trên Budibase Cloud

1. Đăng nhập vào [budibase.app](https://budibase.app)
2. Mở App (hoặc tạo App mới)
3. Vào **Data** section (sidebar trái)
4. Click **"+ Add source"** → chọn **"REST API"**
5. Cấu hình connection:

| Field | Value |
|---|---|
| **Name** | CRM Domain Service |
| **Base URL** | `https://abc123.ngrok-free.app` (thay bằng URL ngrok thực tế) |

6. Thêm **Default Headers**:

| Header | Value |
|---|---|
| `ngrok-skip-browser-warning` | `true` |
| `Content-Type` | `application/json` |

7. Click **Save**

---

## Bước 3: Tạo REST Queries

Sau khi tạo connection, tạo các queries sau:

### Query 1: Get All Leads

| Field | Value |
|---|---|
| **Name** | getAllLeads |
| **Method** | GET |
| **Path** | /api/leads |

### Query 2: Get Lead By ID

| Field | Value |
|---|---|
| **Name** | getLeadById |
| **Method** | GET |
| **Path** | /api/leads/{{ leadId }} |
| **Bindings** | leadId (Text, default: "LEAD-001") |

### Query 3: Update Lead Status

| Field | Value |
|---|---|
| **Name** | updateLeadStatus |
| **Method** | PUT |
| **Path** | /api/leads/{{ leadId }}/status |
| **Bindings** | leadId (Text) |
| **Body (JSON)** | `{"newStatus": "{{ newStatus }}", "updatedBy": "{{ updatedBy }}", "note": "{{ note }}", "reason": "{{ reason }}"}` |

### Query 4: Contact Lead (Workflow)

| Field | Value |
|---|---|
| **Name** | contactLead |
| **Method** | POST |
| **Path** | /api/workflow/lead/{{ leadId }}/contact |
| **Bindings** | leadId (Text) |
| **Body (JSON)** | `{"action": "{{ action }}", "contactResult": "{{ contactResult }}", "note": "{{ note }}", "performedBy": "USR-STAFF-01"}` |

### Query 5: Process Lead (Workflow)

| Field | Value |
|---|---|
| **Name** | processLead |
| **Method** | POST |
| **Path** | /api/workflow/lead/{{ leadId }}/process |
| **Bindings** | leadId (Text) |
| **Body (JSON)** | `{"note": "{{ note }}", "performedBy": "USR-STAFF-01"}` |

### Query 6: Collect Documents (Workflow)

| Field | Value |
|---|---|
| **Name** | collectDocuments |
| **Method** | POST |
| **Path** | /api/workflow/lead/{{ leadId }}/collect-documents |
| **Bindings** | leadId (Text) |
| **Body (JSON)** | `{"note": "{{ note }}", "performedBy": "USR-STAFF-01"}` |

### Query 7: Reject Lead (Workflow)

| Field | Value |
|---|---|
| **Name** | rejectLead |
| **Method** | POST |
| **Path** | /api/workflow/lead/{{ leadId }}/reject |
| **Bindings** | leadId (Text) |
| **Body (JSON)** | `{"note": "{{ note }}", "reason": "{{ reason }}", "performedBy": "USR-STAFF-01"}` |

---

## Bước 4: Test Connection

1. Mở query **getAllLeads**
2. Click **"Send"** (hoặc Run)
3. Verify response trả về danh sách leads
4. Nếu lỗi:
   - Kiểm tra ngrok đang chạy
   - Kiểm tra Domain Service đang chạy (port 8090)
   - Kiểm tra Base URL đúng
   - Kiểm tra headers đã thêm đúng

---

## Troubleshooting

| Vấn đề | Giải pháp |
|---|---|
| 502 Bad Gateway | Domain Service chưa chạy → `docker start crm-domain-service` |
| ngrok tunnel expired | Restart ngrok: `ngrok http 8090`, cập nhật Base URL |
| CORS error | Đã handle ở Domain Service (CrossOrigin *) |
| HTML response thay vì JSON | Thiếu header `ngrok-skip-browser-warning: true` |
| Connection refused | Kiểm tra port 8090 đang listen |

---

## So sánh với Appsmith

| Tiêu chí | Appsmith | Budibase |
|---|---|---|
| Datasource location | Queries & JS sidebar | Data section > REST API |
| Query creation | Per-query with datasource ref | Grouped under connection |
| Bindings syntax | `{{variable}}` (JS) | `{{ variable }}` (Handlebars) |
| Headers config | Per-datasource | Per-connection (default headers) |
| Test query | Run button in query editor | Send button in API editor |

