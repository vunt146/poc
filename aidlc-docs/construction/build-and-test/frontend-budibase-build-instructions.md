# Build Instructions - Frontend Budibase

## Prerequisites

| Requirement | Details |
|---|---|
| **Budibase Cloud Account** | budibase.app (free tier) |
| **Domain Service** | Running on localhost:8090 |
| **ngrok** | Running tunnel to localhost:8090 |
| **Docker** | All containers running (Zeebe, Operate, Tasklist, ES, Domain Service) |
| **Browser** | Chrome/Firefox (modern) |

## Build Steps

### 1. Verify Backend Stack

```bash
# Verify Domain Service is running
curl -H "ngrok-skip-browser-warning: true" http://localhost:8090/api/leads

# Verify ngrok is running (check public URL)
curl http://localhost:4040/api/tunnels
```

Expected: JSON response with leads data.

### 2. Login to Budibase Cloud

1. Mở browser → [budibase.app](https://budibase.app)
2. Đăng nhập với tài khoản đã có
3. Verify dashboard hiển thị

### 3. Create New App

1. Click **"+ Create app"**
2. Name: `CRM Lead Management`
3. Click **Create**

### 4. Setup REST API Connection

Theo hướng dẫn chi tiết tại `frontend-budibase/docs/datasource-setup.md`:

1. **Data** → **+ Add source** → **REST API**
2. Name: `CRM Domain Service`
3. Base URL: `https://<ngrok-url>.ngrok-free.app`
4. Default Headers:
   - `ngrok-skip-browser-warning`: `true`
   - `Content-Type`: `application/json`
5. Save

### 5. Create REST Queries

Tạo 7 queries theo `datasource-setup.md`:
- getAllLeads (GET /api/leads)
- getLeadById (GET /api/leads/{{ leadId }})
- updateLeadStatus (PUT /api/leads/{{ leadId }}/status)
- contactLead (POST /api/workflow/lead/{{ leadId }}/contact)
- processLead (POST /api/workflow/lead/{{ leadId }}/process)
- collectDocuments (POST /api/workflow/lead/{{ leadId }}/collect-documents)
- rejectLead (POST /api/workflow/lead/{{ leadId }}/reject)

### 6. Build Screens

Theo hướng dẫn chi tiết tại `frontend-budibase/docs/budibase-build-guide.md`:

1. **Screen 1**: Lead List (`/leads`) - Table + navigation
2. **Screen 2**: Lead Detail (`/lead/:leadId`) - Info + progress + history + status update + modals

### 7. Configure Navigation

1. Set `/leads` as home screen
2. Configure sidebar navigation
3. App title: `CRM Lead Management`

### 8. Publish App

1. Click **Publish** (top right)
2. Verify app accessible via published URL

## Build Verification

| Check | Expected |
|---|---|
| App created | ✅ Visible in Budibase dashboard |
| REST connection | ✅ Test query returns data |
| Lead List screen | ✅ Table shows leads |
| Lead Detail screen | ✅ Shows lead info when navigated |
| Navigation | ✅ Can switch between screens |
| Published | ✅ App accessible via public URL |

## Troubleshooting

| Issue | Solution |
|---|---|
| REST connection fails | Check ngrok URL, verify Domain Service running |
| No data in table | Check query bindings, verify getAllLeads returns data |
| Navigation broken | Check URL routes match (`/leads`, `/lead/:leadId`) |
| Modals don't open | Check button actions configured correctly |
| Status update fails | Check updateLeadStatus query body format |

## Estimated Build Time

| Phase | Time |
|---|---|
| Setup connection + queries | 15-20 phút |
| Build Lead List screen | 10-15 phút |
| Build Lead Detail screen | 30-45 phút |
| Navigation + styling | 5-10 phút |
| Testing + fixes | 15-20 phút |
| **Total** | **75-110 phút** |

