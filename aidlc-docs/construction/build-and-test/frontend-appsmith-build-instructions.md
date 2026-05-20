# Build Instructions - Frontend Appsmith

## Prerequisites

| Requirement | Details |
|---|---|
| **Domain Service** | Running on localhost:8090 |
| **Camunda 8** | Running (Docker containers) |
| **ngrok** | Installed + authtoken configured |
| **Appsmith Cloud** | Account at app.appsmith.com |
| **Browser** | Chrome/Firefox (modern) |

## Build Steps

### 1. Verify Domain Service is Running

```bash
curl http://localhost:8090/api/leads
```

**Expected**: JSON array with lead data. If error → start services:
```bash
docker compose up -d
# Wait ~60s for Elasticsearch
# Then verify again
```

### 2. Start ngrok Tunnel

```bash
ngrok http 8090
```

**Expected output**:
```
Forwarding  https://xxxx-xxx-xxx.ngrok-free.app → http://localhost:8090
```

**Ghi lại ngrok URL** - sẽ dùng ở bước tiếp theo.

### 3. Verify Tunnel Works

```bash
curl https://YOUR-NGROK-URL.ngrok-free.app/api/leads
```

**Expected**: Same JSON response as localhost.

### 4. Import Appsmith App (Option A - Recommended)

1. Login https://app.appsmith.com
2. Click **"Import"** → **"Import from file"**
3. Upload `frontend-appsmith/appsmith-export.json`
4. App sẽ được tạo với tên "CRM Lead Management"

### 5. Configure Datasource

1. Trong app, vào **Datasources** panel
2. Click `CRM_Domain_Service`
3. Cập nhật **URL** = ngrok URL (VD: `https://abc123.ngrok-free.app`)
4. Verify headers:
   - `ngrok-skip-browser-warning: true`
   - `Content-Type: application/json`
5. Click **"Test"** → verify "Connection successful"
6. Click **"Save"**

### 6. Verify All Queries

Test từng query:
1. `getLeads` → Run → verify JSON response
2. `getLeadDetail` → Set leadId param → Run
3. `getAllocatableLeads` → Run
4. `getSubordinates` → Run
5. `getWorkflowTasks` → Run

### 7. Deploy App

1. Click **"Deploy"** (góc trên phải)
2. App published với public URL

---

## Build Option B: Build from Scratch

Nếu import không hoạt động hoặc muốn build thủ công:
1. Theo hướng dẫn trong `frontend-appsmith/docs/appsmith-build-guide.md`
2. Tạo từng page theo step-by-step guide

---

## Build Artifacts

| Artifact | Location |
|---|---|
| Appsmith App | app.appsmith.com (cloud) |
| JSON Export | `frontend-appsmith/appsmith-export.json` |
| Build Guide | `frontend-appsmith/docs/appsmith-build-guide.md` |

---

## Troubleshooting

| Issue | Solution |
|---|---|
| Import fails | Kiểm tra JSON format, thử tạo app mới và import lại |
| Datasource test fails | Verify ngrok đang chạy + URL đúng |
| Queries return empty | Domain Service chưa start hoặc data chưa load |
| Page navigation broken | Verify page names match exactly |
