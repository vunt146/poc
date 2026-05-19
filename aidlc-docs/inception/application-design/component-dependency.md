# Component Dependencies - CRM Lead Management POC

## 1. Dependency Matrix

### Internal Module Dependencies

| Module (hàng) phụ thuộc vào → | Lead | Form | Workflow | User | Data Layer |
|---|---|---|---|---|---|
| **Lead Module** | - | ❌ | ✅ (complete task) | ✅ (lookup user) | ✅ (repository) |
| **Form Module** | ❌ | - | ❌ | ❌ | ✅ (repository) |
| **Workflow Module** | ❌ | ❌ | - | ❌ | ❌ |
| **User Module** | ❌ | ❌ | ❌ | - | ✅ (repository) |
| **Data Layer** | ❌ | ❌ | ❌ | ❌ | - |

**Ghi chú:**
- ✅ = Phụ thuộc trực tiếp
- ❌ = Không phụ thuộc
- Lead Module phụ thuộc Workflow Module để complete tasks
- Lead Module phụ thuộc User Module để validate/lookup cán bộ khi phân bổ
- Tất cả business modules phụ thuộc Data Layer cho data access
- Workflow Module KHÔNG phụ thuộc business modules (giao tiếp qua events)

### External Dependencies

| Component | Phụ thuộc | Giao thức | Mô tả |
|---|---|---|---|
| Workflow Module | Camunda 8 Zeebe | gRPC | Job workers, start/complete process |
| Appsmith | Domain Service | REST/HTTP | Gọi API lấy data + form schema |
| Budibase | Domain Service | REST/HTTP | Gọi API lấy data + form schema |
| Camunda 8 | Domain Service | gRPC (callback) | Dispatch jobs cho workers |

---

## 2. Communication Patterns

### 2.1 Synchronous (REST API)

```
+-------------+     REST/HTTP      +------------------+
|  Appsmith   | -----------------> |  Domain Service  |
+-------------+                    |  (Spring Boot)   |
                                   +------------------+
+-------------+     REST/HTTP      |                  |
|  Budibase   | -----------------> |                  |
+-------------+                    +------------------+
```

**Đặc điểm:**
- Frontend gọi REST API đồng bộ
- Polling cho status updates (không WebSocket)
- JSON request/response

### 2.2 Hybrid (gRPC + Events)

```
+------------------+      gRPC       +------------------+
|  Domain Service  | <-------------> |  Camunda Zeebe   |
|  (Zeebe Client)  |                 |  (Broker)        |
+------------------+                 +------------------+
        |
        | Spring ApplicationEvent
        v
+------------------+
|  Business Logic  |
|  (Event Listeners)|
+------------------+
```

**Đặc điểm:**
- Start process / Complete task: Đồng bộ qua Zeebe Client API
- Service tasks: Bất đồng bộ - Zeebe dispatch job → Worker nhận → publish event
- Internal events: Spring ApplicationEvent (in-process, synchronous delivery)

### 2.3 Event Flow (Internal)

```
Zeebe Job → Job Worker → WorkflowEventPublisher → Spring Event Bus → Business Listener
```

**Đặc điểm:**
- Loose coupling giữa workflow và business logic
- Job Worker chỉ biết cách publish event, không biết ai xử lý
- Business listener xử lý logic rồi gọi WorkflowService.completeTask()

---

## 3. Data Flow

### 3.1 Luồng dữ liệu tổng thể

```
+-------------------+
|  JSON/YAML Files  |
|  (leads.json,     |
|   users.json,     |
|   form-schemas.json)|
+---------+---------+
          |
          | Load on startup
          v
+---------+---------+
|  InMemoryStore    |
|  (Data Layer)     |
+---------+---------+
          |
          | Repository interface
          v
+---------+---------+     +---------+---------+     +---------+---------+
|  Lead Module      |     |  Form Module      |     |  User Module      |
|  (LeadRepository) |     |  (FormRepository) |     |  (UserRepository) |
+---------+---------+     +---------+---------+     +---------+---------+
          |                         |                         |
          | Service layer           | Service layer           | Service layer
          v                         v                         v
+---------+---------+     +---------+---------+     +---------+---------+
|  LeadController   |     |  FormController   |     |  UserController   |
+---------+---------+     +---------+---------+     +---------+---------+
          |                         |                         |
          +------------+------------+-------------------------+
                       |
                       | REST API (JSON)
                       v
          +------------+------------+
          |   Frontend (Appsmith    |
          |   hoặc Budibase)        |
          +-------------------------+
```

### 3.2 Luồng Workflow State

```
+-------------------+                    +-------------------+
|  Frontend         |                    |  Camunda Zeebe    |
|  (User action)    |                    |  (Process state)  |
+---------+---------+                    +---------+---------+
          |                                        |
          | POST /api/workflow/tasks/{id}/complete  |
          v                                        |
+---------+---------+                              |
|  WorkflowService  | -- completeTask(key) ------->|
+---------+---------+                              |
                                                   |
          (Camunda advances to next node)          |
                                                   |
          +----------------------------------------+
          | dispatch service task job
          v
+---------+---------+
|  Job Worker       |
|  (Zeebe Client)   |
+---------+---------+
          |
          | publish event
          v
+---------+---------+
|  Business Logic   |
|  (Event Listener) |
+---------+---------+
          |
          | update data
          v
+---------+---------+
|  InMemoryStore    |
+-------------------+
```

---

## 4. Deployment Dependencies

### Docker Compose Stack

```
+-------------------+     +-------------------+
|  appsmith         |     |  budibase         |
|  (port: 8080)     |     |  (port: 8081)     |
+---------+---------+     +---------+---------+
          |                         |
          | depends_on              | depends_on
          v                         v
+---------+-------------------------+---------+
|              domain-service                  |
|              (port: 8090)                    |
+---------+-----------------------------------+
          |
          | depends_on
          v
+---------+---------+
|  camunda-zeebe    |
|  (port: 26500)    |
+---------+---------+
```

**Thứ tự khởi động:**
1. `camunda-zeebe` (Zeebe broker)
2. `domain-service` (Spring Boot - cần Zeebe ready)
3. `appsmith` + `budibase` (cần domain-service ready)

---

## 5. Coupling Analysis

### Loose Coupling Points
- **Workflow ↔ Business Logic**: Giao tiếp qua events (có thể thay đổi workflow mà không sửa business code)
- **Frontend ↔ Backend**: REST API contract (frontend không biết internal structure)
- **Form Schema ↔ Workflow**: Chỉ liên kết qua form key (thay đổi schema không ảnh hưởng BPMN)

### Tight Coupling Points (Chấp nhận được cho POC)
- **Data Layer ↔ All Modules**: Tất cả modules dùng chung InMemoryStore
- **Lead Module ↔ Workflow Module**: Lead cần gọi WorkflowService để complete tasks
- **Lead Module ↔ User Module**: Lead cần UserService để validate phân bổ

### Mitigation cho Tight Coupling
- Sử dụng interface/abstraction cho dependencies
- Data Layer expose qua Repository pattern
- WorkflowService expose qua interface (có thể mock trong tests)
