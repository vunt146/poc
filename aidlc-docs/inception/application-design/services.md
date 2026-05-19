# Thiết kế Service Layer - CRM Lead Management POC

## Tổng quan

Service layer được thiết kế theo mô hình **Event-Driven** bên trong Modular Monolith:
- **Workflow Module** nhận jobs từ Camunda Zeebe → publish internal events
- **Business Modules** (Lead, Form, User) subscribe events và xử lý logic
- **REST Controllers** cung cấp API đồng bộ cho frontend
- **Giao tiếp Camunda**: Hybrid (đồng bộ cho start/complete, bất đồng bộ cho service tasks)

---

## 1. Service Orchestration Pattern

### Pattern: Event-Driven Internal Communication

```
+------------------+       Internal Events        +------------------+
|  Workflow Module  | --------------------------> |   Lead Module    |
|  (Job Workers)   |       (Spring Events)        |   (Listeners)    |
+------------------+                              +------------------+
        ^                                                  |
        |              Complete Task                        |
        +--------------------------------------------------+
```

**Luồng chính:**
1. Camunda Zeebe dispatch service task → Job Worker nhận
2. Job Worker publish Spring ApplicationEvent
3. Business module listener nhận event → xử lý logic
4. Business module gọi WorkflowService.completeTask() khi xong

### Lý do chọn Event-Driven:
- **Loose coupling**: Workflow module không cần biết chi tiết business logic
- **Extensibility**: Dễ thêm listener mới khi workflow thay đổi
- **Testability**: Có thể test business logic độc lập với Camunda
- **Workflow flexibility**: Thay đổi BPMN không ảnh hưởng code business logic

---

## 2. Service Definitions

### 2.1 LeadService

| Thuộc tính | Giá trị |
|---|---|
| **Trách nhiệm** | Quản lý Lead lifecycle, CRUD operations |
| **Dependencies** | LeadRepository, WorkflowService (để complete tasks) |
| **Consumers** | LeadController (REST), LeadEventListener (events) |

**Orchestration:**
- Nhận request từ REST API → xử lý trực tiếp
- Nhận event từ Workflow → xử lý → gọi WorkflowService complete task

---

### 2.2 LeadAllocationService

| Thuộc tính | Giá trị |
|---|---|
| **Trách nhiệm** | Thuật toán phân bổ Lead đều cho cán bộ |
| **Dependencies** | LeadRepository, UserRepository |
| **Consumers** | LeadController (REST), LeadEventListener (events) |

**Orchestration:**
- Validate điều kiện phân bổ (trạng thái Lead, số lượng cán bộ)
- Tính toán phân bổ (chia đều + dư theo alphabet)
- Cập nhật Lead Owner cho từng Lead
- Trả kết quả phân bổ

---

### 2.3 FormSchemaService

| Thuộc tính | Giá trị |
|---|---|
| **Trách nhiệm** | Lookup và cung cấp form schema |
| **Dependencies** | FormSchemaRepository |
| **Consumers** | FormController (REST) |

**Orchestration:**
- Frontend gọi API với task type
- Service lookup form schema từ in-memory store
- Trả JSON form schema cho frontend render

---

### 2.4 WorkflowService

| Thuộc tính | Giá trị |
|---|---|
| **Trách nhiệm** | Tương tác đồng bộ với Camunda Zeebe |
| **Dependencies** | ZeebeClient |
| **Consumers** | WorkflowController (REST), Business modules (complete tasks) |

**Orchestration:**
- Start process instance (đồng bộ)
- Complete user task (đồng bộ)
- Query active tasks (đồng bộ)

---

### 2.5 WorkflowEventPublisher

| Thuộc tính | Giá trị |
|---|---|
| **Trách nhiệm** | Bridge giữa Zeebe Job Workers và internal events |
| **Dependencies** | Spring ApplicationEventPublisher |
| **Consumers** | ZeebeJobWorkerConfig (job workers gọi publisher) |

**Orchestration:**
- Job Worker nhận job từ Zeebe
- Chuyển đổi job variables thành domain event
- Publish event qua Spring ApplicationEventPublisher
- Business listeners tự động nhận và xử lý

---

### 2.6 UserService

| Thuộc tính | Giá trị |
|---|---|
| **Trách nhiệm** | Cung cấp thông tin user/cán bộ |
| **Dependencies** | UserRepository |
| **Consumers** | UserController (REST), LeadAllocationService |

---

## 3. Luồng nghiệp vụ chính

### 3.1 Luồng lấy Form Schema (Frontend → Backend)

```
Frontend                Spring Boot                    In-Memory Store
   |                        |                               |
   |-- GET /api/forms/{taskType} -->|                       |
   |                        |-- lookup(taskType) ---------->|
   |                        |<-- FormSchema ----------------|
   |<-- JSON FormSchema ----|                               |
   |                        |                               |
   | (render dynamic form)  |                               |
```

**Đặc điểm:**
- Không query Camunda realtime → nhanh, đơn giản
- Frontend luôn lấy form schema mới nhất từ backend
- Khi deploy BPMN mới + cập nhật form-schemas.json → frontend tự động nhận form mới

---

### 3.2 Luồng Submit Form (Frontend → Backend → Camunda)

```
Frontend          Spring Boot              Camunda Zeebe         Spring Boot
   |                  |                         |                    |
   |-- POST /api/workflow/tasks/{id}/complete ->|                    |
   |                  |-- completeTask(key, vars) -->|               |
   |                  |<-- ack ------------------|                   |
   |<-- 200 OK ------|                          |                    |
   |                  |                         |                    |
   |                  |    (Camunda advances     |                    |
   |                  |     to next task/        |                    |
   |                  |     service task)        |                    |
   |                  |                         |-- dispatch job ---->|
   |                  |                         |                    |
   |                  |                    (Job Worker nhận)          |
   |                  |                         |    publish event    |
   |                  |                         |    → listener xử lý |
```

**Đặc điểm:**
- Frontend complete task → Camunda tự advance workflow
- Camunda trigger service task → Job Worker nhận → publish event
- Business logic xử lý bất đồng bộ qua event listener
- Frontend polling để check trạng thái mới

---

### 3.3 Luồng Phân bổ Lead

```
Frontend          LeadController     LeadAllocationService    WorkflowService
   |                  |                      |                      |
   |-- POST /api/leads/allocate ----------->|                      |
   |                  |-- allocate(req) ---->|                      |
   |                  |                     |-- validate() -------->|
   |                  |                     |-- calculate() ------->|
   |                  |                     |-- updateOwners() ---->|
   |                  |                     |                       |
   |                  |                     |-- completeTask() ---->|
   |                  |                     |                      |-- complete on Zeebe
   |                  |<-- AllocationResult -|                      |
   |<-- 200 OK ------|                      |                      |
```

---

## 4. Event Definitions

### Internal Events (Spring ApplicationEvent)

| Event | Publisher | Subscriber | Mô tả |
|---|---|---|---|
| `LeadStatusChangeEvent` | WorkflowEventPublisher | LeadEventListener | Yêu cầu đổi trạng thái từ workflow |
| `LeadAllocationEvent` | WorkflowEventPublisher | LeadEventListener | Yêu cầu phân bổ từ workflow |
| `LeadTaskCompletedEvent` | WorkflowEventPublisher | LeadEventListener | Task hoàn thành |
| `FormSchemaUpdatedEvent` | FormSchemaService | (future use) | Form schema được cập nhật |

---

## 5. API Summary

### REST Endpoints

| Module | Method | Path | Mô tả |
|---|---|---|---|
| Lead | GET | /api/leads | Danh sách Lead |
| Lead | GET | /api/leads/{id} | Chi tiết Lead |
| Lead | PUT | /api/leads/{id}/status | Cập nhật trạng thái |
| Lead | POST | /api/leads/allocate | Phân bổ Lead |
| Form | GET | /api/forms/{taskType} | Lấy form schema |
| Form | GET | /api/forms | Liệt kê form schemas |
| Workflow | POST | /api/workflow/start | Start process |
| Workflow | POST | /api/workflow/tasks/{id}/complete | Complete task |
| Workflow | GET | /api/workflow/tasks | Active tasks |
| User | GET | /api/users/subordinates | Cán bộ dưới quyền |
| User | GET | /api/users/{id} | Thông tin user |
