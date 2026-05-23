# Frontend Budibase - CRM Lead Management POC

## Tổng quan

Budibase Cloud app cho CRM Lead Management, triển khai cùng chức năng với Appsmith (2 screens) để so sánh hai nền tảng lowcode.

## Architecture

```
Budibase Cloud (budibase.app)
        |
        | HTTPS (ngrok tunnel - dùng chung với Appsmith)
        v
    ngrok → localhost:8090
        |
        v
Spring Boot Domain Service (port 8090)
        |
        v
Camunda Zeebe (Docker, port 26500)
```

## Screens

| # | Screen | Route | Chức năng |
|---|---|---|---|
| 1 | Lead List | /leads | Danh sách leads, click → detail |
| 2 | Lead Detail | /lead/:leadId | Chi tiết + status update + workflow actions |

## Quick Start

### Prerequisites
- Tài khoản Budibase Cloud (budibase.app)
- Domain Service đang chạy (localhost:8090)
- ngrok tunnel đang chạy

### Cách 1: Build từ đầu (Recommended)
1. Đọc `docs/datasource-setup.md` - Setup REST API connection
2. Đọc `docs/budibase-build-guide.md` - Build từng screen step-by-step

### Cách 2: Import JSON
1. Đăng nhập Budibase Cloud
2. Import `budibase-export.json`
3. Reconfigure datasource URL (ngrok URL mới)
4. Test

> **Lưu ý**: File `budibase-export.json` là reference structure. Budibase export format có thể khác tùy version. Khuyến nghị build từ guide.

## API Endpoints (Domain Service)

| Method | Endpoint | Screen |
|---|---|---|
| GET | /api/leads | Lead List |
| GET | /api/leads/{id} | Lead Detail |
| PUT | /api/leads/{id}/status | Lead Detail |
| POST | /api/workflow/lead/{leadId}/contact | Lead Detail |
| POST | /api/workflow/lead/{leadId}/process | Lead Detail |
| POST | /api/workflow/lead/{leadId}/collect-documents | Lead Detail |
| POST | /api/workflow/lead/{leadId}/reject | Lead Detail |

## So sánh với Appsmith

| Tiêu chí | Appsmith | Budibase |
|---|---|---|
| Hosting | Appsmith Cloud | Budibase Cloud |
| Scope | 4 pages | 2 screens (Lead List + Detail) |
| Tunnel | ngrok | ngrok (dùng chung) |
| Datasource | REST API datasource | REST API connection |
| Bindings | JS `{{variable}}` | Handlebars `{{ variable }}` |
| Export | JSON (app-level) | JSON (workspace-level) |

## Docs

- `docs/datasource-setup.md` - Hướng dẫn setup datasource
- `docs/budibase-build-guide.md` - Step-by-step build guide
- `docs/evaluation-notes.md` - Template đánh giá so sánh

## Evaluation

Sau khi build xong, điền đánh giá vào `docs/evaluation-notes.md` để so sánh với Appsmith.

