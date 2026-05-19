# Thiết kế Ứng dụng Tổng hợp - CRM Lead Management POC

## 1. Tổng quan Kiến trúc

### Mô hình: Modular Monolith + External Workflow Engine

Hệ thống gồm 4 thành phần chính:
- **Domain Service** (Spring Boot): Modular Monolith với 5 modules nội bộ
- **Camunda 8 Zeebe**: External workflow engine orchestrate Lead lifecycle
- **Appsmith**: Frontend platform #1 (lowcode)
- **Budibase**: Frontend platform #2 (lowcode, so sánh)

### Quyết định thiết kế chính

| Quyết định | Lựa chọn | Lý do |
|---|---|---|
| Tổ chức Domain Service | Modular Monolith | Tách biệt concerns nhưng đơn giản cho POC |
| Form Schema management | Kết hợp (Camunda form key + Spring Boot JSON) | Linh hoạt, dễ thay đổi |
| Internal communication | Event-driven (Spring Events) | Loose coupling, workflow flexibility |
| Form lookup | By task type (không query Camunda realtime) | Đơn giản, nhanh |
| Form submit flow | Complete task → Camunda trigger service task → Business logic | Đúng pattern Camunda orchestration |
| Camunda communication | Hybrid (sync start/complete + async job workers) | Cân bằng giữa control và flexibility |
| Frontend communication | REST API + polling | Đơn giản nhất cho POC |
| Workflow change reflection | Frontend query realtime, tự động nhận form mới | Đạt mục tiêu POC: không cần redeploy |
| Sample data structure | Tách theo entity (leads.json, users.json, form-schemas.json) | Rõ ràng, dễ maintain |

---

## 2. Components

### Domain Service Modules

| Module | Trách nhiệm | Interface chính |
|---|---|---|
| **lead-module** | CRUD Lead, state transitions, allocation | LeadController, LeadService, LeadAllocationService |
| **form-module** | Form schema lookup và cung cấp | FormController, FormSchemaService |
| **workflow-module** | Camunda integration, job workers, events | WorkflowController, WorkflowService, WorkflowEventPublisher |
| **user-module** | User/cán bộ data | UserController, UserService |
| **data-layer** | JSON file loading, in-memory store | DataLoader, InMemoryStore\<T\> |

### External Components

| Component | Vai trò | Giao thức |
|---|---|---|
| Camunda 8 Zeebe | Workflow orchestration | gRPC |
| Appsmith | Frontend #1 | REST/HTTP |
| Budibase | Frontend #2 | REST/HTTP |

---

## 3. Service Orchestration

### Pattern: Event-Driven Internal + Hybrid External

```
Frontend (REST) → Controller → Service → Repository (InMemoryStore)
                                  ↕
                        Spring ApplicationEvent
                                  ↕
Camunda (gRPC) → Job Worker → EventPublisher → Event Bus → Business Listener
```

### Luồng chính

**Lấy Form Schema:**
- Frontend → GET /api/forms/{taskType} → FormSchemaService → lookup in-memory → trả JSON

**Submit Form:**
- Frontend → POST /api/workflow/tasks/{id}/complete → WorkflowService → Zeebe complete
- Camunda advance → dispatch service task → Job Worker → publish event → Business logic xử lý

**Phân bổ Lead:**
- Frontend → POST /api/leads/allocate → LeadAllocationService → validate + calculate + update owners

---

## 4. Dependencies

### Dependency Flow

```
Appsmith/Budibase → Domain Service → Camunda Zeebe
                         ↓
                    JSON/YAML Files
```

### Docker Compose Order
1. Camunda Zeebe (port 26500)
2. Domain Service (port 8090) - depends on Zeebe
3. Appsmith (port 8080) + Budibase (port 8081) - depends on Domain Service

---

## 5. Dynamic Form Mechanism

### Cơ chế hoạt động

1. **BPMN Definition**: Mỗi user task trong BPMN có `formKey` (VD: `lead-status-update`)
2. **Form Schema Store**: Spring Boot lưu form schema JSON files, mapping theo formKey/taskType
3. **Frontend Query**: Frontend gọi API lấy form schema theo task type → render dynamic form
4. **Workflow Change**: Deploy BPMN mới + cập nhật form-schemas.json → frontend tự động nhận form mới (query realtime)

### Đạt mục tiêu POC
- ✅ Thay đổi BPMN: Deploy process mới trên Camunda (hot-deploy)
- ✅ Thay đổi form: Cập nhật form-schemas.json (hoặc restart service)
- ✅ Frontend không cần redeploy: Luôn query form schema realtime

---

## 6. Domain Models

### Core Entities

| Entity | Fields chính | Mô tả |
|---|---|---|
| Lead | id, customerName, status, ownerId, productType, productDetails, createdAt | Cơ hội bán |
| User | id, name, username, miscode, role, managerId | Cán bộ/Quản lý |
| FormSchema | formId, formKey, taskType, title, fields | Định nghĩa form |
| FormField | id, type, label, required, options, validation | Field trong form |

### LeadStatus State Machine

```
NEW_LEAD (1007) ──────────────────────────────────────→ REJECTED (9001)
     │                                                        ↑
     v                                                        │
NEW_IMPORTED_LEAD (106141) ──→ CONTACTED (2001) ──→ PROCESSING (3001) ──→ DOCUMENT_COLLECTED (4001) ──→ COMPLETED (5001)
                                      │                    │                        │
                                      └────────────────────┴────────────────────────┴──→ REJECTED (9001)
```

---

## 7. API Summary

| # | Method | Path | Module | Mô tả |
|---|---|---|---|---|
| 1 | GET | /api/leads | Lead | Danh sách Lead |
| 2 | GET | /api/leads/{id} | Lead | Chi tiết Lead |
| 3 | PUT | /api/leads/{id}/status | Lead | Cập nhật trạng thái |
| 4 | POST | /api/leads/allocate | Lead | Phân bổ Lead |
| 5 | GET | /api/forms/{taskType} | Form | Lấy form schema |
| 6 | GET | /api/forms | Form | Liệt kê form schemas |
| 7 | POST | /api/workflow/start | Workflow | Start process |
| 8 | POST | /api/workflow/tasks/{id}/complete | Workflow | Complete task |
| 9 | GET | /api/workflow/tasks | Workflow | Active tasks |
| 10 | GET | /api/users/subordinates | User | Cán bộ dưới quyền |
| 11 | GET | /api/users/{id} | User | Thông tin user |

---

## 8. Tài liệu chi tiết

- [Components](./components.md) - Định nghĩa chi tiết từng component
- [Component Methods](./component-methods.md) - Method signatures và domain models
- [Services](./services.md) - Service layer design và orchestration patterns
- [Component Dependencies](./component-dependency.md) - Dependency matrix và communication patterns
