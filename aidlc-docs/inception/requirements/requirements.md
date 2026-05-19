# Requirements Document - CRM Lead Management POC

## Intent Analysis

| Attribute | Value |
|---|---|
| **User Request** | POC chức năng CRM (Xem Lead + Phân bổ Lead) sử dụng kiến trúc: Camunda 8 workflow engine + Spring Boot domain service + Dynamic form frontend (Appsmith & Budibase) |
| **Request Type** | New Project (POC) |
| **Scope Estimate** | Multiple Components (Frontend x2 + Backend + Workflow Engine) |
| **Complexity Estimate** | Moderate - Multi-platform POC with dynamic form mechanism and workflow orchestration |

## Project Goals

### Primary Objectives
1. **Workflow Flexibility**: Kiểm chứng Camunda 8 cho phép thay đổi workflow mà KHÔNG cần redeploy backend Java và frontend
2. **Frontend Platform Evaluation**: So sánh Appsmith vs Budibase về khả năng lowcode, dễ triển khai, tương thích với nền tảng tổng thể
3. **End-to-End Architecture Validation**: Chứng minh kiến trúc Camunda 8 + Spring Boot + Dynamic Form Frontend hoạt động liền mạch

### Success Criteria
- Thay đổi BPMN process trên Camunda mà frontend/backend tự động phản ánh thay đổi (không redeploy)
- Cả Appsmith và Budibase đều render được dynamic form từ schema do backend cung cấp
- Luồng nghiệp vụ Lead lifecycle hoạt động end-to-end qua Camunda orchestration

---

## Functional Requirements

### FR-01: Lead Listing (Danh sách Lead)

| ID | Requirement |
|---|---|
| FR-01.1 | Hệ thống hiển thị danh sách Lead với thông tin: Trạng thái, Mã cơ hội, Tên khách hàng |
| FR-01.2 | Danh sách sắp xếp theo thời gian tạo (mới nhất trước), dữ liệu trong 30 ngày gần nhất |
| FR-01.3 | Mỗi Card Lead hiển thị thông tin riêng theo sản phẩm (dynamic fields) |
| FR-01.4 | Khi danh sách rỗng, hiển thị thông báo phù hợp |

### FR-02: Lead Detail (Chi tiết Lead)

| ID | Requirement |
|---|---|
| FR-02.1 | Hiển thị chi tiết Lead bao gồm: lịch sử trạng thái, thông tin khách hàng, ghi chú |
| FR-02.2 | Thanh tiến trình trạng thái: New → Contacted → Processing → Done (hoặc Rejected) |
| FR-02.3 | Cho phép cập nhật trạng thái Lead thông qua dropdown (loại trừ trạng thái đã qua) |
| FR-02.4 | Trạng thái "New Lead" → nhấn "Gọi điện" chuyển thành "Call", bắt buộc nhập kết quả |
| FR-02.5 | Trạng thái "Document collected" → gợi ý upload hồ sơ, auto-mapping thông tin |

### FR-03: Lead Allocation (Phân bổ Lead)

| ID | Requirement |
|---|---|
| FR-03.1 | Cho phép phân bổ Lead cho các cán bộ dưới quyền (tối đa 10 người/lần) |
| FR-03.2 | Chỉ Lead ở trạng thái New Lead (1007) hoặc New Imported Lead (106141) mới được phân bổ |
| FR-03.3 | Thuật toán chia đều: chia nguyên + phần dư phân bổ theo thứ tự alphabet |
| FR-03.4 | Hiển thị cảnh báo khi chọn >= 2 cán bộ: "cơ hội sẽ được chia đều" |
| FR-03.5 | Nút "Chọn tất cả" / "Bỏ chọn tất cả" cho danh sách Lead |

### FR-04: Dynamic Form Rendering

| ID | Requirement |
|---|---|
| FR-04.1 | Camunda 8 quyết định task nào cần form gì (thông qua BPMN user task definition) |
| FR-04.2 | Domain Service (Spring Boot) cung cấp form schema (JSON) cho từng task |
| FR-04.3 | Frontend (Appsmith/Budibase) render form động dựa trên schema nhận được |
| FR-04.4 | Thay đổi workflow trên Camunda phải tự động phản ánh form mới mà không cần redeploy FE/BE |

### FR-05: Camunda 8 Workflow Orchestration

| ID | Requirement |
|---|---|
| FR-05.1 | Camunda 8 orchestrate toàn bộ luồng Lead lifecycle: Tạo → Phân bổ → Liên hệ → Xử lý → Hoàn thành |
| FR-05.2 | Mỗi bước trong workflow tương ứng với một user task có form definition riêng |
| FR-05.3 | Workflow có thể thay đổi (thêm/bớt bước, thay đổi logic) mà không cần redeploy backend/frontend |
| FR-05.4 | Domain Service đóng vai trò job worker xử lý service tasks trong BPMN |

### FR-06: Domain Service APIs

| ID | Requirement |
|---|---|
| FR-06.1 | API lấy danh sách Lead (GET /leads) - trả về danh sách với filter theo owner và thời gian |
| FR-06.2 | API lấy chi tiết Lead (GET /leads/{id}) - trả về full detail bao gồm lịch sử |
| FR-06.3 | API cập nhật trạng thái Lead (PUT /leads/{id}/status) - thay đổi trạng thái |
| FR-06.4 | API phân bổ Lead (POST /leads/allocate) - nhận danh sách lead IDs + danh sách user IDs |
| FR-06.5 | API lấy form schema (GET /forms/{taskType}) - trả về JSON form definition cho task type |
| FR-06.6 | API lấy danh sách cán bộ (GET /users/subordinates) - trả về danh sách CBPT dưới quyền |

### FR-07: Frontend Platform Comparison

| ID | Requirement |
|---|---|
| FR-07.1 | Cả Appsmith và Budibase đều build cùng chức năng, cùng backend |
| FR-07.2 | Đánh giá tiêu chí: khả năng lowcode, dễ triển khai, tương thích nền tảng |
| FR-07.3 | Cả hai platform đều phải render dynamic form từ JSON schema |
| FR-07.4 | So sánh khả năng tích hợp REST API và xử lý workflow task |

---

## Non-Functional Requirements

### NFR-01: Data Management

| ID | Requirement |
|---|---|
| NFR-01.1 | Dữ liệu sample được lưu trong file JSON/YAML, load khi khởi động ứng dụng |
| NFR-01.2 | Không sử dụng database - toàn bộ data là in-memory từ file |
| NFR-01.3 | Sample data phải đủ đa dạng để demo dynamic form (nhiều loại sản phẩm, nhiều trạng thái) |

### NFR-02: Authentication/Authorization

| ID | Requirement |
|---|---|
| NFR-02.1 | POC không yêu cầu authentication/authorization |
| NFR-02.2 | Mọi user đều có thể truy cập tất cả chức năng (bỏ qua phân quyền) |

### NFR-03: Deployment & Runtime

| ID | Requirement |
|---|---|
| NFR-03.1 | Camunda 8 chạy standalone (self-managed hoặc Docker) |
| NFR-03.2 | Spring Boot service chạy độc lập |
| NFR-03.3 | Appsmith và Budibase chạy trên Docker |
| NFR-03.4 | Toàn bộ stack có thể khởi động bằng docker-compose |

### NFR-04: Testing (Property-Based Testing)

| ID | Requirement |
|---|---|
| NFR-04.1 | Áp dụng Property-Based Testing (PBT) đầy đủ cho business logic |
| NFR-04.2 | Framework: jqwik (Java/JUnit 5) cho domain service |
| NFR-04.3 | PBT bắt buộc cho: thuật toán phân bổ Lead, form schema serialization, state transitions |
| NFR-04.4 | PBT bổ sung (không thay thế) example-based tests |

---

## Architecture Overview

```
+-------------------+     +-------------------+     +-------------------+
|   Appsmith        |     |   Budibase        |     |   Camunda 8       |
|   (Frontend #1)   |     |   (Frontend #2)   |     |   (Workflow Engine)|
+--------+----------+     +--------+----------+     +--------+----------+
         |                          |                         |
         |        REST API          |        REST API         |
         +------------+-------------+                         |
                      |                                       |
                      v                                       v
         +-------------------+                    +-----------+----------+
         |   Spring Boot     |<---Job Worker----->|   Camunda Zeebe      |
         |   Domain Service  |                    |   (gRPC)             |
         +--------+----------+                    +----------------------+
                  |
                  v
         +-------------------+
         |   JSON/YAML Files |
         |   (Sample Data)   |
         +-------------------+
```

### Text Alternative
- Frontend Layer: Appsmith + Budibase (both connect to same backend via REST)
- Backend Layer: Spring Boot Domain Service (provides APIs + acts as Camunda job worker)
- Workflow Layer: Camunda 8 Zeebe (orchestrates business process, communicates with Spring Boot via gRPC)
- Data Layer: JSON/YAML files loaded at startup (no database)

---

## Dynamic Form Mechanism

### Flow
1. User triggers a task in the workflow (via frontend)
2. Frontend calls Domain Service API to get form schema for current task
3. Domain Service queries Camunda for current task type/definition
4. Domain Service returns appropriate JSON form schema
5. Frontend renders form dynamically based on schema
6. User submits form → Domain Service processes → completes Camunda task

### Form Schema Structure (Example)
```json
{
  "formId": "lead-status-update",
  "title": "Cập nhật trạng thái Lead",
  "fields": [
    {
      "id": "status",
      "type": "dropdown",
      "label": "Trạng thái mới",
      "options": ["Contacted", "Processing", "Done", "Rejected"],
      "required": true
    },
    {
      "id": "note",
      "type": "textarea",
      "label": "Ghi chú",
      "required": false
    }
  ]
}
```

---

## Deliverables

1. **Source Code**: Spring Boot domain service + BPMN process definitions
2. **Frontend Apps**: Appsmith app + Budibase app (cùng chức năng)
3. **BPMN Process**: Lead lifecycle workflow definition cho Camunda 8
4. **Form Schemas**: JSON form definitions cho từng task type
5. **Documentation**: Tài liệu kiến trúc + hướng dẫn setup/demo
6. **Platform Comparison**: So sánh Appsmith vs Budibase (criteria: lowcode capability, deployment ease, platform compatibility)
7. **Demo Script**: Kịch bản demo end-to-end

---

## Constraints & Assumptions

### Constraints
- Không sử dụng database (data từ JSON/YAML files)
- Không cần authentication/authorization
- POC scope - không cần production-ready quality
- Camunda 8 self-managed (không dùng Camunda Cloud)

### Assumptions
- Camunda 8 có thể chạy trên Docker
- Appsmith và Budibase hỗ trợ dynamic form rendering từ API response
- Spring Boot có thể tích hợp Camunda Zeebe client
- Sample data đủ để demo tất cả các luồng nghiệp vụ
