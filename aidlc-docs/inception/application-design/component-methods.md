# Component Methods - CRM Lead Management POC

## Ghi chú
- Tài liệu này định nghĩa method signatures ở mức high-level
- Business rules chi tiết sẽ được thiết kế trong Functional Design (Construction Phase)
- Input/Output types là logical types, implementation cụ thể sẽ được quyết định khi code generation

---

## 1. Lead Module

### LeadController

| Method | HTTP | Path | Input | Output | Mô tả |
|---|---|---|---|---|---|
| `getLeads()` | GET | /api/leads | query params: ownerId, fromDate, toDate | List\<LeadSummaryDTO\> | Lấy danh sách Lead |
| `getLeadDetail()` | GET | /api/leads/{id} | path: leadId | LeadDetailDTO | Lấy chi tiết Lead |
| `updateLeadStatus()` | PUT | /api/leads/{id}/status | path: leadId, body: StatusUpdateRequest | LeadDetailDTO | Cập nhật trạng thái |
| `allocateLeads()` | POST | /api/leads/allocate | body: AllocationRequest | AllocationResultDTO | Phân bổ Lead |

### LeadService

| Method | Input | Output | Mô tả |
|---|---|---|---|
| `findLeads(criteria)` | LeadSearchCriteria | List\<Lead\> | Tìm Lead theo điều kiện |
| `findLeadById(id)` | String leadId | Lead | Lấy Lead theo ID |
| `updateStatus(id, status, note)` | String, LeadStatus, String | Lead | Cập nhật trạng thái |
| `getLeadHistory(id)` | String leadId | List\<LeadHistoryEntry\> | Lấy lịch sử trạng thái |

### LeadAllocationService

| Method | Input | Output | Mô tả |
|---|---|---|---|
| `allocate(leadIds, userIds)` | List\<String\>, List\<String\> | AllocationResult | Thực hiện phân bổ |
| `validateAllocation(leadIds, userIds)` | List\<String\>, List\<String\> | ValidationResult | Kiểm tra điều kiện phân bổ |
| `calculateDistribution(totalLeads, totalUsers)` | int, int | Map\<String, List\<String\>\> | Tính toán phân bổ đều |

### LeadEventListener

| Method | Input | Output | Mô tả |
|---|---|---|---|
| `onLeadStatusChangeRequested(event)` | LeadStatusChangeEvent | void | Xử lý yêu cầu đổi trạng thái từ workflow |
| `onLeadAllocationRequested(event)` | LeadAllocationEvent | void | Xử lý yêu cầu phân bổ từ workflow |
| `onLeadTaskCompleted(event)` | LeadTaskCompletedEvent | void | Xử lý khi task hoàn thành |

---

## 2. Form Module

### FormController

| Method | HTTP | Path | Input | Output | Mô tả |
|---|---|---|---|---|---|
| `getFormSchema()` | GET | /api/forms/{taskType} | path: taskType | FormSchemaDTO | Lấy form schema theo task type |
| `listFormSchemas()` | GET | /api/forms | - | List\<FormSchemaSummaryDTO\> | Liệt kê tất cả form schemas |

### FormSchemaService

| Method | Input | Output | Mô tả |
|---|---|---|---|
| `getSchemaByTaskType(taskType)` | String taskType | FormSchema | Lookup form schema |
| `getSchemaByFormKey(formKey)` | String formKey | FormSchema | Lookup theo Camunda form key |
| `getAllSchemas()` | - | List\<FormSchema\> | Lấy tất cả schemas |

### FormSchemaRepository

| Method | Input | Output | Mô tả |
|---|---|---|---|
| `findByTaskType(taskType)` | String | Optional\<FormSchema\> | Tìm theo task type |
| `findByFormKey(formKey)` | String | Optional\<FormSchema\> | Tìm theo form key |
| `findAll()` | - | List\<FormSchema\> | Lấy tất cả |

---

## 3. Workflow Module

### WorkflowController

| Method | HTTP | Path | Input | Output | Mô tả |
|---|---|---|---|---|---|
| `startProcess()` | POST | /api/workflow/start | body: StartProcessRequest | ProcessInstanceDTO | Khởi tạo process instance |
| `completeTask()` | POST | /api/workflow/tasks/{taskId}/complete | path: taskId, body: TaskCompletionRequest | TaskResultDTO | Complete user task |
| `getActiveTasks()` | GET | /api/workflow/tasks | query: assignee, processId | List\<TaskDTO\> | Lấy danh sách active tasks |

### WorkflowService

| Method | Input | Output | Mô tả |
|---|---|---|---|
| `startProcessInstance(processId, variables)` | String, Map | ProcessInstance | Start BPMN process |
| `completeUserTask(taskKey, variables)` | long, Map | void | Complete user task trên Zeebe |
| `queryActiveTasks(assignee)` | String | List\<Task\> | Query active tasks |

### WorkflowEventPublisher

| Method | Input | Output | Mô tả |
|---|---|---|---|
| `publishLeadStatusChange(leadId, newStatus)` | String, LeadStatus | void | Publish event đổi trạng thái |
| `publishLeadAllocation(leadIds, userIds)` | List\<String\>, List\<String\> | void | Publish event phân bổ |
| `publishTaskCompleted(taskType, result)` | String, Map | void | Publish event task hoàn thành |

### ZeebeJobWorkerConfig (Job Workers)

| Worker Type | Job Type | Mô tả |
|---|---|---|
| `leadStatusChangeWorker` | `lead-status-change` | Xử lý service task đổi trạng thái Lead |
| `leadAllocationWorker` | `lead-allocation` | Xử lý service task phân bổ Lead |
| `notificationWorker` | `send-notification` | Xử lý service task gửi thông báo |

---

## 4. User Module

### UserController

| Method | HTTP | Path | Input | Output | Mô tả |
|---|---|---|---|---|---|
| `getSubordinates()` | GET | /api/users/subordinates | query: managerId | List\<UserDTO\> | Lấy danh sách cán bộ dưới quyền |
| `getUserById()` | GET | /api/users/{id} | path: userId | UserDTO | Lấy thông tin user |

### UserService

| Method | Input | Output | Mô tả |
|---|---|---|---|
| `findSubordinates(managerId)` | String | List\<User\> | Tìm cán bộ dưới quyền |
| `findById(userId)` | String | User | Tìm user theo ID |
| `findAll()` | - | List\<User\> | Lấy tất cả users |

---

## 5. Data Layer

### DataLoader

| Method | Input | Output | Mô tả |
|---|---|---|---|
| `loadOnStartup()` | - | void | Load tất cả JSON files khi startup |
| `loadLeads()` | - | List\<Lead\> | Load leads.json |
| `loadUsers()` | - | List\<User\> | Load users.json |
| `loadFormSchemas()` | - | List\<FormSchema\> | Load form-schemas.json |

### InMemoryStore\<T\>

| Method | Input | Output | Mô tả |
|---|---|---|---|
| `findAll()` | - | List\<T\> | Lấy tất cả records |
| `findById(id)` | String | Optional\<T\> | Tìm theo ID |
| `save(entity)` | T | T | Lưu/cập nhật entity |
| `delete(id)` | String | void | Xóa entity |
| `findBy(predicate)` | Predicate\<T\> | List\<T\> | Tìm theo điều kiện |

---

## 6. Domain Models (Shared)

### Lead

| Field | Type | Mô tả |
|---|---|---|
| id | String | Mã cơ hội (Lead ID) |
| customerName | String | Tên khách hàng |
| status | LeadStatus (enum) | Trạng thái hiện tại |
| ownerId | String | Lead Owner (user ID) |
| productType | String | Loại sản phẩm |
| productDetails | Map\<String, Object\> | Thông tin riêng theo sản phẩm (dynamic) |
| createdAt | LocalDateTime | Thời gian tạo |
| updatedAt | LocalDateTime | Thời gian cập nhật |

### LeadStatus (Enum)

| Value | Code | Mô tả |
|---|---|---|
| NEW_LEAD | 1007 | Lead mới |
| NEW_IMPORTED_LEAD | 106141 | Lead import mới |
| CONTACTED | 2001 | Đã liên hệ |
| PROCESSING | 3001 | Đang xử lý |
| DOCUMENT_COLLECTED | 4001 | Đã thu thập hồ sơ |
| COMPLETED | 5001 | Hoàn thành |
| REJECTED | 9001 | KH từ chối |

### FormSchema

| Field | Type | Mô tả |
|---|---|---|
| formId | String | ID form |
| formKey | String | Camunda form key |
| taskType | String | Loại task tương ứng |
| title | String | Tiêu đề form |
| fields | List\<FormField\> | Danh sách fields |

### FormField

| Field | Type | Mô tả |
|---|---|---|
| id | String | Field ID |
| type | String | Loại field (text, dropdown, textarea, date, number) |
| label | String | Nhãn hiển thị |
| required | boolean | Bắt buộc hay không |
| options | List\<String\> | Danh sách options (cho dropdown) |
| validation | Map\<String, Object\> | Validation rules |

### User

| Field | Type | Mô tả |
|---|---|---|
| id | String | User ID |
| name | String | Tên đầy đủ |
| username | String | Username |
| miscode | String | Mã miscode |
| role | UserRole | Vai trò |
| managerId | String | ID quản lý trực tiếp |
