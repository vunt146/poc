# Unit of Work - Requirements Mapping - CRM Lead Management POC

## Ghi chú
Vì POC này không có User Stories stage (đã skip), tài liệu này mapping **Functional Requirements** → **Units of Work**.

---

## 1. Requirements → Unit Mapping

### FR-01: Lead Listing (Danh sách Lead)

| Requirement | Unit chịu trách nhiệm | Vai trò |
|---|---|---|
| FR-01.1 Hiển thị danh sách Lead | Domain Service + Appsmith + Budibase | BE: API, FE: UI render |
| FR-01.2 Sắp xếp theo thời gian | Domain Service | BE: Sort logic |
| FR-01.3 Dynamic fields theo sản phẩm | Domain Service + Form Module | BE: productDetails field |
| FR-01.4 Trạng thái danh sách rỗng | Appsmith + Budibase | FE: Empty state UI |

### FR-02: Lead Detail (Chi tiết Lead)

| Requirement | Unit chịu trách nhiệm | Vai trò |
|---|---|---|
| FR-02.1 Hiển thị chi tiết Lead | Domain Service + Appsmith + Budibase | BE: API, FE: Detail page |
| FR-02.2 Thanh tiến trình trạng thái | Appsmith + Budibase | FE: Progress bar UI |
| FR-02.3 Cập nhật trạng thái (dropdown) | Domain Service + Appsmith + Budibase | BE: API + validation, FE: Form |
| FR-02.4 New Lead → Call | Domain Service + BPMN | BE: State transition, WF: Task flow |
| FR-02.5 Document collected → Upload | Domain Service + Appsmith + Budibase | BE: Form schema, FE: Upload UI |

### FR-03: Lead Allocation (Phân bổ Lead)

| Requirement | Unit chịu trách nhiệm | Vai trò |
|---|---|---|
| FR-03.1 Phân bổ cho cán bộ (max 10) | Domain Service + Appsmith + Budibase | BE: Allocation logic, FE: Selection UI |
| FR-03.2 Filter trạng thái phân bổ | Domain Service | BE: Filter logic |
| FR-03.3 Thuật toán chia đều | Domain Service | BE: Allocation algorithm |
| FR-03.4 Cảnh báo >= 2 cán bộ | Appsmith + Budibase | FE: Warning message |
| FR-03.5 Chọn tất cả / Bỏ chọn | Appsmith + Budibase | FE: UI controls |

### FR-04: Dynamic Form Rendering

| Requirement | Unit chịu trách nhiệm | Vai trò |
|---|---|---|
| FR-04.1 Camunda quyết định form | BPMN Processes | WF: Form key in user task |
| FR-04.2 Domain Service cung cấp schema | Domain Service (Form Module) | BE: Form schema API |
| FR-04.3 Frontend render dynamic form | Appsmith + Budibase | FE: JSON → UI rendering |
| FR-04.4 Thay đổi workflow → form mới | BPMN + Domain Service | WF: Hot deploy, BE: Schema update |

### FR-05: Camunda 8 Workflow Orchestration

| Requirement | Unit chịu trách nhiệm | Vai trò |
|---|---|---|
| FR-05.1 Orchestrate Lead lifecycle | BPMN Processes | WF: Process definition |
| FR-05.2 User task + form definition | BPMN Processes | WF: Task + form key |
| FR-05.3 Workflow thay đổi không redeploy | BPMN Processes + Domain Service | WF: Hot deploy, BE: Dynamic lookup |
| FR-05.4 Domain Service = job worker | Domain Service (Workflow Module) | BE: Job worker registration |

### FR-06: Domain Service APIs

| Requirement | Unit chịu trách nhiệm | Vai trò |
|---|---|---|
| FR-06.1 GET /leads | Domain Service (Lead Module) | BE: Lead list API |
| FR-06.2 GET /leads/{id} | Domain Service (Lead Module) | BE: Lead detail API |
| FR-06.3 PUT /leads/{id}/status | Domain Service (Lead Module) | BE: Status update API |
| FR-06.4 POST /leads/allocate | Domain Service (Lead Module) | BE: Allocation API |
| FR-06.5 GET /forms/{taskType} | Domain Service (Form Module) | BE: Form schema API |
| FR-06.6 GET /users/subordinates | Domain Service (User Module) | BE: User list API |

### FR-07: Frontend Platform Comparison

| Requirement | Unit chịu trách nhiệm | Vai trò |
|---|---|---|
| FR-07.1 Cùng chức năng, cùng backend | Appsmith + Budibase | FE: Same features |
| FR-07.2 Đánh giá tiêu chí | Documentation | Docs: Comparison report |
| FR-07.3 Render dynamic form từ schema | Appsmith + Budibase | FE: JSON schema rendering |
| FR-07.4 So sánh tích hợp REST API | Appsmith + Budibase + Documentation | FE + Docs |

---

## 2. NFR → Unit Mapping

| NFR | Unit chịu trách nhiệm | Ghi chú |
|---|---|---|
| NFR-01 Data Management (JSON files) | Domain Service (Data Layer) | Load on startup |
| NFR-02 No Auth | Domain Service | Bỏ qua security config |
| NFR-03 Docker Deployment | Infrastructure (docker-compose) | Tất cả units |
| NFR-04 Property-Based Testing | Domain Service (Tests) | jqwik cho allocation, form, state |

---

## 3. Coverage Summary

### Theo Unit

| Unit | Số FR covered | FR chính |
|---|---|---|
| Domain Service | 22/25 (88%) | FR-01 → FR-06 (tất cả backend logic) |
| BPMN Processes | 6/25 (24%) | FR-04, FR-05 (workflow orchestration) |
| Appsmith | 14/25 (56%) | FR-01 → FR-04, FR-07 (UI rendering) |
| Budibase | 14/25 (56%) | FR-01 → FR-04, FR-07 (UI rendering) |
| Infrastructure | 1/25 (4%) | NFR-03 (Docker deployment) |

### Theo Wave

| Wave | Units | FR Coverage |
|---|---|---|
| Wave 1 | Domain Service + BPMN + Infrastructure | Backend + Workflow (foundation) |
| Wave 2 | Appsmith + Budibase | Frontend (UI layer) |

---

## 4. PBT Requirements Mapping

| PBT Target | Unit | Module | Test Type |
|---|---|---|---|
| Thuật toán phân bổ Lead | Domain Service | Lead Module | Invariant (size preservation), Oracle (brute-force) |
| Form Schema serialization | Domain Service | Form Module | Round-trip (serialize/deserialize) |
| Lead State Transitions | Domain Service | Lead Module | Stateful (state machine), Invariant (valid transitions) |
| Allocation distribution | Domain Service | Lead Module | Commutativity (order independence), Invariant (total = input) |
