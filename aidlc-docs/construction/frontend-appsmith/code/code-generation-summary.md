# Code Generation Summary - Frontend Appsmith

## Generated Files

| # | File | Purpose |
|---|---|---|
| 1 | `frontend-appsmith/README.md` | Project overview + quick start |
| 2 | `frontend-appsmith/appsmith-export.json` | Appsmith app JSON export (importable) |
| 3 | `frontend-appsmith/docs/ngrok-setup.md` | ngrok installation + configuration guide |
| 4 | `frontend-appsmith/docs/datasource-setup.md` | Appsmith Cloud datasource setup |
| 5 | `frontend-appsmith/docs/appsmith-build-guide.md` | Comprehensive step-by-step build guide |
| 6 | `frontend-appsmith/docs/evaluation-notes.md` | Platform evaluation template |

---

## Key Decisions

| Decision | Choice | Rationale |
|---|---|---|
| Hosting | Appsmith Cloud | Máy user không đủ mạnh cho Docker |
| Tunnel | ngrok | Miễn phí, dễ setup, đủ cho POC |
| Form rendering | JSON Form widget | Appsmith native, auto-generate từ JSON |
| Visibility conditions | JS in widget property | Flexible, works with JSON Form |
| State machine | JS Object | Client-side logic cho valid transitions |
| Navigation | Appsmith built-in sidebar | Tự động từ page list |

---

## Architecture Decisions

1. **Frontend → Domain Service only**: Mọi interaction với Camunda đều qua Domain Service API. Frontend không gọi trực tiếp Camunda Tasklist/Zeebe.

2. **ngrok tunnel**: Appsmith Cloud (internet) cần public URL để gọi API. ngrok tạo tunnel từ public URL → localhost:8090.

3. **JSON Form widget**: Appsmith có JSON Form widget native, tự động generate form từ JSON object. Phù hợp cho dynamic form rendering.

4. **Client-side state machine**: Valid next statuses được tính toán bằng JS Object trên Appsmith, dựa trên state transition rules.

---

## Limitations & Workarounds

| Limitation | Workaround |
|---|---|
| ngrok URL thay đổi mỗi lần restart | Cập nhật datasource URL manually |
| JSON Form không hỗ trợ visibilityCondition native | Dùng JS logic trong widget Visible property |
| Appsmith Cloud free tier limits | Đủ cho POC (1 app, limited queries/month) |
| FILE field type | JSON Form không hỗ trợ file upload native → skip cho POC |

---

## Requirements Coverage

| Requirement Group | Status | Notes |
|---|---|---|
| FA-01 (Datasource) | ✅ Covered | ngrok + Appsmith Cloud datasource |
| FA-02 (Lead List) | ✅ Covered | Table + color badges + navigation |
| FA-03 (Lead Detail) | ✅ Covered | Info + progress + status update + history |
| FA-04 (Allocation) | ✅ Covered | Multi-select + modal + warning + allocate |
| FA-05 (Dynamic Form) | ✅ Covered | JSON Form + visibility logic |
| FA-06 (Navigation) | ✅ Covered | Appsmith sidebar |
| NFA-01 (Deployment) | ✅ Covered | Appsmith Cloud + ngrok |
| NFA-02 (Deliverables) | ✅ Covered | JSON export + guides |
| NFA-03 (Evaluation) | ✅ Covered | Template created |
