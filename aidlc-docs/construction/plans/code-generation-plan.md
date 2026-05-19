# Kế hoạch Code Generation - CRM Lead Management POC

## Tổng quan

Kế hoạch này mô tả chi tiết các bước generate code cho toàn bộ POC, chia theo units và waves.

**Workspace Root**: `d:\PROJECT\POC\workspace\CRM`
**Project Type**: Greenfield multi-unit (mono-repo)

---

## Wave 1: Foundation

### Unit 1: Domain Service (Spring Boot)
**Thư mục**: `/domain-service/`

#### Step 1: Project Structure Setup
- [ ] Tạo Maven project structure (pom.xml, src/main/java, src/test/java)
- [ ] Cấu hình pom.xml với dependencies (Spring Boot, Zeebe, jqwik, Jackson)
- [ ] Tạo application.yml (server port, Zeebe config, logging)
- [ ] Tạo CrmPocApplication.java (main class)
- [ ] Tạo Dockerfile

#### Step 2: Data Layer
- [ ] Tạo InMemoryStore<T> generic class
- [ ] Tạo DataLoader (load JSON files on startup)
- [ ] Tạo sample data files: leads.json (5-10 leads)
- [ ] Tạo sample data files: users.json (5 users)
- [ ] Tạo sample data files: form-schemas.json (3-4 schemas)

#### Step 3: Domain Models
- [ ] Tạo Lead entity + LeadStatus enum + ProductType enum
- [ ] Tạo LeadHistoryEntry entity
- [ ] Tạo User entity + UserRole enum
- [ ] Tạo FormSchema + FormField + FieldType + VisibilityCondition + ValidationRule
- [ ] Tạo DTOs: LeadSummaryDTO, LeadDetailDTO, AllocationRequest, AllocationResult, StatusUpdateRequest
- [ ] Tạo Event classes: LeadStatusChangeEvent, LeadAllocationEvent, LeadTaskCompletedEvent

#### Step 4: Lead Module - Business Logic
- [ ] Tạo LeadRepository (CRUD trên InMemoryStore)
- [ ] Tạo LeadService (findLeads, findById, updateStatus, getHistory)
- [ ] Tạo LeadAllocationService (allocate, validate, calculateDistribution)
- [ ] Tạo LeadEventListener (lắng nghe workflow events)

#### Step 5: Lead Module - PBT Tests
- [ ] Tạo LeadAllocationPropertyTest (jqwik):
  - Property: size preservation (tổng leads = input)
  - Property: no duplication (mỗi lead xuất hiện 1 lần)
  - Property: fair distribution (chênh lệch <= 1)
  - Property: alphabet priority (dư cho users đầu alphabet)
- [ ] Tạo LeadStateTransitionPropertyTest (jqwik):
  - Property: no backward transition
  - Property: terminal is terminal
  - Property: history grows monotonically
  - Property: REJECTED from any non-terminal
- [ ] Tạo custom generators: LeadArbitrary, UserArbitrary

#### Step 6: Lead Module - Unit Tests (Example-based)
- [ ] Tạo LeadServiceTest (JUnit 5)
- [ ] Tạo LeadAllocationServiceTest (JUnit 5, specific scenarios)
- [ ] Tạo LeadStateTransitionTest (JUnit 5, specific edge cases)

#### Step 7: Lead Module - REST Controller
- [ ] Tạo LeadController (GET /api/leads, GET /api/leads/{id}, PUT /api/leads/{id}/status, POST /api/leads/allocate)
- [ ] Tạo exception handlers (GlobalExceptionHandler)

#### Step 8: Form Module
- [ ] Tạo FormSchemaRepository
- [ ] Tạo FormSchemaService (getByTaskType, getByFormKey, getAll)
- [ ] Tạo FormController (GET /api/forms/{taskType}, GET /api/forms)
- [ ] Tạo FormSchemaRoundTripPropertyTest (jqwik): serialize/deserialize round-trip

#### Step 9: User Module
- [ ] Tạo UserRepository
- [ ] Tạo UserService (findSubordinates, findById, findAll)
- [ ] Tạo UserController (GET /api/users/subordinates, GET /api/users/{id})

#### Step 10: Workflow Module
- [ ] Tạo WorkflowService (startProcess, completeTask, queryTasks)
- [ ] Tạo WorkflowEventPublisher (publish Spring events)
- [ ] Tạo ZeebeJobWorkerConfig (job worker registration: lead-status-change, lead-allocation)
- [ ] Tạo WorkflowController (POST /api/workflow/start, POST /api/workflow/tasks/{id}/complete, GET /api/workflow/tasks)

#### Step 11: Integration & Configuration
- [ ] Cấu hình CORS (allow all origins)
- [ ] Cấu hình Spring Actuator (health endpoint)
- [ ] Tạo API documentation summary (README cho domain-service)

---

### Unit 2: BPMN Processes
**Thư mục**: `/bpmn-processes/`

#### Step 12: BPMN Process Definitions
- [ ] Tạo lead-lifecycle.bpmn (main process: Start → User Tasks → Service Tasks → End)
- [ ] Định nghĩa User Tasks với form keys (lead-contact, lead-process, lead-document, lead-allocate)
- [ ] Định nghĩa Service Tasks với job types (lead-status-change, lead-allocation, send-notification)
- [ ] Định nghĩa Gateways (exclusive: status routing, REJECTED path)
- [ ] Tạo deploy script (deploy.sh - zbctl deploy)

#### Step 13: BPMN Documentation
- [ ] Tạo README.md cho bpmn-processes (hướng dẫn deploy, process description)
- [ ] Tạo process-variables.md (danh sách variables trong process)

---

### Infrastructure (Shared)

#### Step 14: Docker Compose
- [ ] Tạo docker-compose.yml (Zeebe + Elasticsearch + Operate + Tasklist + Domain Service + Appsmith + Budibase)
- [ ] Tạo .env file (environment variables, ports)
- [ ] Tạo README.md (project overview, setup guide, quick start)

---

## Wave 2: Frontend

### Unit 3: Appsmith Frontend
**Thư mục**: `/frontend-appsmith/`

#### Step 15: Appsmith App Setup
- [ ] Tạo README.md (setup guide, import instructions)
- [ ] Tạo datasource configuration guide (REST API connection to domain-service:8090)
- [ ] Tạo app structure documentation (pages, widgets, queries)

#### Step 16: Appsmith Pages Documentation
- [ ] Document Lead List Page (table widget, API queries, card layout)
- [ ] Document Lead Detail Page (form widget, status progress, history)
- [ ] Document Lead Allocation Page (checkbox list, user selection popup, allocation button)
- [ ] Document Dynamic Form Page (JSON form widget, conditional rendering)

---

### Unit 4: Budibase Frontend
**Thư mục**: `/frontend-budibase/`

#### Step 17: Budibase App Setup
- [ ] Tạo README.md (setup guide, import instructions)
- [ ] Tạo datasource configuration guide (REST API connection to domain-service:8090)
- [ ] Tạo app structure documentation (screens, components, queries)

#### Step 18: Budibase Screens Documentation
- [ ] Document Lead List Screen (table component, API bindings)
- [ ] Document Lead Detail Screen (form component, status display)
- [ ] Document Lead Allocation Screen (multi-select, user picker)
- [ ] Document Dynamic Form Screen (JSON schema form rendering)

---

## Wave 3: Documentation & Comparison

#### Step 19: Platform Comparison
- [ ] Tạo docs/platform-comparison.md (Appsmith vs Budibase: criteria, evaluation, recommendation)
- [ ] Tạo docs/architecture.md (system architecture documentation)
- [ ] Tạo docs/demo-script.md (kịch bản demo end-to-end)

---

## Tóm tắt

| Wave | Steps | Mô tả |
|---|---|---|
| Wave 1 | Steps 1-14 | Domain Service + BPMN + Docker Compose |
| Wave 2 | Steps 15-18 | Appsmith + Budibase (documentation & guides) |
| Wave 3 | Step 19 | Platform comparison + Architecture docs + Demo script |

**Tổng số steps**: 19
**Files ước tính**: ~50-60 files (code + config + docs + tests)

---

## Ghi chú quan trọng

1. **Appsmith/Budibase**: Vì đây là lowcode platforms, code generation sẽ tạo documentation/guides thay vì source code trực tiếp. Actual app sẽ được build trên platform UI.
2. **PBT Tests**: Được tạo cùng với business logic (Step 5) theo PBT-10 (complementary testing).
3. **BPMN**: File .bpmn là XML, sẽ được generate dưới dạng valid BPMN 2.0 XML.
4. **Docker Compose**: Bao gồm Zeebe + Elasticsearch + Operate + Tasklist + Domain Service.
