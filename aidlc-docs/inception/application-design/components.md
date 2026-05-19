# Định nghĩa Components - CRM Lead Management POC

## Tổng quan kiến trúc

Hệ thống được tổ chức theo mô hình **Modular Monolith** trong một Spring Boot application, kết hợp với Camunda 8 Zeebe làm workflow engine bên ngoài, và 2 frontend platforms (Appsmith, Budibase) giao tiếp qua REST API.

---

## 1. Domain Service (Spring Boot Application)

### 1.1 Lead Module (`lead-module`)

| Thuộc tính | Giá trị |
|---|---|
| **Mục đích** | Quản lý toàn bộ lifecycle của Lead entity |
| **Công nghệ** | Java Spring Boot |
| **Trách nhiệm** | CRUD Lead, cập nhật trạng thái, thuật toán phân bổ, lịch sử Lead |

**Trách nhiệm chi tiết:**
- Cung cấp API lấy danh sách Lead (filter theo owner, thời gian)
- Cung cấp API lấy chi tiết Lead (bao gồm lịch sử trạng thái)
- Cung cấp API cập nhật trạng thái Lead
- Thực hiện thuật toán phân bổ Lead (chia đều + dư theo alphabet)
- Quản lý Lead state transitions
- Lắng nghe internal events từ Workflow Module để xử lý business logic

**Interface:**
- `LeadController` - REST endpoints
- `LeadService` - Business logic
- `LeadRepository` - Data access (JSON file)
- `LeadAllocationService` - Thuật toán phân bổ
- `LeadEventListener` - Lắng nghe events từ workflow

---

### 1.2 Form Module (`form-module`)

| Thuộc tính | Giá trị |
|---|---|
| **Mục đích** | Quản lý và cung cấp form schema cho dynamic form rendering |
| **Công nghệ** | Java Spring Boot |
| **Trách nhiệm** | Lưu trữ form schema JSON, lookup theo task type, cung cấp API cho frontend |

**Trách nhiệm chi tiết:**
- Lưu trữ form schema definitions (JSON files)
- Lookup form schema theo task type (mapping từ Camunda form key)
- Cung cấp REST API trả form schema cho frontend
- Hỗ trợ dynamic field definitions (dropdown options, validation rules)

**Interface:**
- `FormController` - REST endpoints
- `FormSchemaService` - Form lookup logic
- `FormSchemaRepository` - Data access (JSON file)

---

### 1.3 Workflow Module (`workflow-module`)

| Thuộc tính | Giá trị |
|---|---|
| **Mục đích** | Tích hợp với Camunda 8 Zeebe, quản lý workflow orchestration |
| **Công nghệ** | Java Spring Boot + Camunda Zeebe Client |
| **Trách nhiệm** | Job workers, publish internal events, start/complete process instances |

**Trách nhiệm chi tiết:**
- Đăng ký và quản lý Zeebe Job Workers
- Publish internal events khi nhận job từ Camunda (event-driven pattern)
- Cung cấp API đồng bộ: start process instance, complete user task
- Quản lý process variables và task variables
- Bridge giữa Camunda workflow và business logic modules

**Interface:**
- `WorkflowController` - REST endpoints (start process, complete task)
- `ZeebeJobWorkerConfig` - Job worker registration
- `WorkflowEventPublisher` - Publish internal events
- `WorkflowService` - Workflow operations (start, complete, query)

---

### 1.4 User Module (`user-module`)

| Thuộc tính | Giá trị |
|---|---|
| **Mục đích** | Quản lý thông tin user/cán bộ (hardcode data) |
| **Công nghệ** | Java Spring Boot |
| **Trách nhiệm** | Cung cấp danh sách cán bộ, thông tin user |

**Trách nhiệm chi tiết:**
- Cung cấp API lấy danh sách cán bộ dưới quyền (subordinates)
- Lưu trữ thông tin user (tên, username, miscode)
- Hỗ trợ lookup user theo ID

**Interface:**
- `UserController` - REST endpoints
- `UserService` - User lookup logic
- `UserRepository` - Data access (JSON file)

---

### 1.5 Data Layer (`data-layer`)

| Thuộc tính | Giá trị |
|---|---|
| **Mục đích** | Quản lý sample data từ JSON/YAML files |
| **Công nghệ** | Java Spring Boot |
| **Trách nhiệm** | Load data khi startup, cung cấp in-memory data store |

**Trách nhiệm chi tiết:**
- Load JSON files khi application startup
- Cung cấp in-memory data store cho các module
- Hỗ trợ CRUD operations trên in-memory data
- Tách biệt theo entity: leads.json, users.json, form-schemas.json

**Interface:**
- `DataLoader` - Load JSON files on startup
- `InMemoryStore<T>` - Generic in-memory data store

---

## 2. Camunda 8 Zeebe (External Workflow Engine)

| Thuộc tính | Giá trị |
|---|---|
| **Mục đích** | Orchestrate toàn bộ Lead lifecycle workflow |
| **Công nghệ** | Camunda 8 Zeebe (Docker) |
| **Trách nhiệm** | BPMN process execution, task assignment, workflow state management |

**Trách nhiệm chi tiết:**
- Thực thi BPMN process definitions
- Quản lý user tasks (assign, complete)
- Dispatch service tasks cho job workers
- Lưu trữ form key/ID trong user task definitions
- Cho phép hot-deploy BPMN mới mà không cần restart

**Artifacts:**
- `lead-lifecycle.bpmn` - Main process definition
- `lead-allocation.bpmn` - Sub-process cho phân bổ (nếu cần)

---

## 3. Appsmith Frontend

| Thuộc tính | Giá trị |
|---|---|
| **Mục đích** | Frontend platform #1 - Dynamic form rendering + Lead management UI |
| **Công nghệ** | Appsmith (lowcode, Docker) |
| **Trách nhiệm** | Render dynamic forms, hiển thị Lead data, gọi REST API |

**Trách nhiệm chi tiết:**
- Gọi REST API từ Domain Service
- Render dynamic form dựa trên JSON schema
- Hiển thị danh sách Lead, chi tiết Lead
- Giao diện phân bổ Lead
- Polling để check trạng thái/task mới

---

## 4. Budibase Frontend

| Thuộc tính | Giá trị |
|---|---|
| **Mục đích** | Frontend platform #2 - Cùng chức năng với Appsmith để so sánh |
| **Công nghệ** | Budibase (lowcode, Docker) |
| **Trách nhiệm** | Render dynamic forms, hiển thị Lead data, gọi REST API |

**Trách nhiệm chi tiết:**
- Cùng chức năng như Appsmith
- Gọi cùng REST API endpoints
- Render cùng JSON form schema
- Dùng để so sánh khả năng lowcode, deployment, platform compatibility

---

## Tóm tắt Components

| Component | Loại | Công nghệ | Vai trò chính |
|---|---|---|---|
| Lead Module | Internal Module | Spring Boot | Business logic Lead |
| Form Module | Internal Module | Spring Boot | Form schema management |
| Workflow Module | Internal Module | Spring Boot + Zeebe Client | Camunda integration |
| User Module | Internal Module | Spring Boot | User data |
| Data Layer | Internal Module | Spring Boot | JSON data loading |
| Camunda 8 Zeebe | External Service | Docker | Workflow orchestration |
| Appsmith | External Frontend | Docker | UI Platform #1 |
| Budibase | External Frontend | Docker | UI Platform #2 |
