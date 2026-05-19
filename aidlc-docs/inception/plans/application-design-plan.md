# Kế hoạch Thiết kế Ứng dụng - CRM Lead Management POC

## Tổng quan

Tài liệu này mô tả kế hoạch thiết kế ứng dụng ở mức high-level: xác định các component chính, interface, service layer, và mối quan hệ phụ thuộc giữa chúng.

---

## Kế hoạch thực hiện

### Phần 1: Xác định Components
- [x] Xác định các component chính của hệ thống
- [x] Định nghĩa trách nhiệm (responsibility) cho từng component
- [x] Xác định interface/contract giữa các component

### Phần 2: Thiết kế Component Methods
- [x] Định nghĩa method signatures cho Domain Service
- [x] Định nghĩa method signatures cho Workflow Integration
- [x] Định nghĩa method signatures cho Form Schema Service
- [x] Định nghĩa method signatures cho Data Layer

### Phần 3: Thiết kế Service Layer
- [x] Xác định service orchestration patterns
- [x] Định nghĩa service boundaries
- [x] Thiết kế luồng giao tiếp giữa các service

### Phần 4: Component Dependencies
- [x] Tạo dependency matrix
- [x] Xác định communication patterns
- [x] Thiết kế data flow

### Phần 5: Validation
- [x] Kiểm tra tính đầy đủ của thiết kế
- [x] Kiểm tra tính nhất quán giữa các artifact

---

## Câu hỏi thiết kế

Vui lòng trả lời các câu hỏi dưới đây để hỗ trợ quyết định thiết kế.

### Câu hỏi về Component Boundaries

## Question 1
Domain Service nên được tổ chức theo cách nào?

A) Monolithic - Một Spring Boot application chứa tất cả logic (Lead, Form, User, Workflow integration)
B) Modular Monolith - Một application nhưng chia thành các module rõ ràng (lead-module, form-module, workflow-module)
C) Layered - Tổ chức theo layer truyền thống (Controller → Service → Repository)
X) Other (please describe after [Answer]: tag below)

[Answer]: B

---

## Question 2
Form Schema nên được quản lý ở đâu?

A) Trong Spring Boot - Form schema là JSON files nằm trong resources, load khi startup
B) Trong Camunda - Form schema gắn trực tiếp vào BPMN user task definition (form key)
C) Kết hợp - Camunda chỉ chứa form key/ID, Spring Boot chứa actual form schema JSON
D) External - Form schema nằm trong file riêng (ngoài cả Spring Boot và Camunda), cả hai đều reference đến
X) Other (please describe after [Answer]: tag below)

[Answer]: C

---

## Question 3
Camunda Zeebe Client trong Spring Boot nên được tổ chức như thế nào?

A) Tích hợp trực tiếp - Job workers nằm trong cùng service class với business logic
B) Tách biệt - Có một layer riêng (WorkflowAdapter) đóng vai trò bridge giữa Camunda và business logic
C) Event-driven - Job workers publish internal events, business logic subscribe và xử lý
X) Other (please describe after [Answer]: tag below)

[Answer]: C

---

### Câu hỏi về Service Layer

## Question 4
Khi frontend cần lấy form cho một task, luồng giao tiếp nên như thế nào?

A) Frontend → Spring Boot API → Query Camunda task → Trả form schema
B) Frontend → Spring Boot API → Lookup form schema by task type (không query Camunda realtime)
C) Frontend → Camunda Tasklist API trực tiếp → Lấy task info → Gọi Spring Boot lấy form schema
X) Other (please describe after [Answer]: tag below)

[Answer]: B

---

## Question 5
Khi user submit form, luồng xử lý nên như thế nào?

A) Frontend → Spring Boot → Xử lý business logic → Complete Camunda task → Trả kết quả
B) Frontend → Spring Boot → Complete Camunda task trước → Camunda trigger service task → Spring Boot xử lý business logic
C) Frontend → Spring Boot → Xử lý business logic song song với complete Camunda task
X) Other (please describe after [Answer]: tag below)

[Answer]: B

---

### Câu hỏi về Communication Patterns

## Question 6
Giao tiếp giữa Spring Boot và Camunda 8 Zeebe nên theo pattern nào?

A) Synchronous - Spring Boot gọi Zeebe API đồng bộ cho mọi thao tác
B) Asynchronous - Job workers lắng nghe events từ Zeebe, xử lý bất đồng bộ
C) Hybrid - Một số thao tác đồng bộ (start process, complete task), job workers bất đồng bộ cho service tasks
X) Other (please describe after [Answer]: tag below)

[Answer]: C

---

## Question 7
Frontend (Appsmith/Budibase) giao tiếp với backend qua cơ chế nào?

A) REST API thuần - Frontend gọi REST endpoints, polling để check trạng thái
B) REST + WebSocket - REST cho CRUD, WebSocket cho real-time updates (task assignments, status changes)
C) REST only - Đơn giản nhất cho POC, không cần real-time
X) Other (please describe after [Answer]: tag below)

[Answer]: A

---

### Câu hỏi về Data Flow

## Question 8
Khi workflow thay đổi (deploy BPMN mới), frontend cần làm gì để phản ánh thay đổi?

A) Không cần làm gì - Frontend luôn query form schema realtime từ backend, tự động nhận form mới
B) Frontend cần refresh/reload page để lấy form schema mới
C) Backend push notification cho frontend biết có workflow mới
X) Other (please describe after [Answer]: tag below)

[Answer]: A

---

## Question 9
Sample data (JSON/YAML) nên được cấu trúc như thế nào?

A) Một file duy nhất chứa tất cả (leads, users, form-schemas)
B) Tách theo entity - leads.json, users.json, form-schemas.json
C) Tách theo domain - lead-data/ (leads + lead-history), user-data/, form-data/
X) Other (please describe after [Answer]: tag below)

[Answer]: B

---
