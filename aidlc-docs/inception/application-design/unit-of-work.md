# Định nghĩa Units of Work - CRM Lead Management POC

## Chiến lược phân tách

| Thuộc tính | Giá trị |
|---|---|
| **Số lượng Units** | 4 |
| **Cấu trúc Repository** | Mono-repo (tất cả trong 1 repository) |
| **Thứ tự phát triển** | Song song một phần: (Domain Service + BPMN) → (Appsmith + Budibase) |
| **Build Tool** | Maven (pom.xml) cho Java |
| **Java Version** | Java 17 (LTS, tương thích Camunda 8) |
| **BPMN Location** | Thư mục riêng, deploy độc lập lên Camunda |

---

## Unit 1: Domain Service (Spring Boot)

| Thuộc tính | Giá trị |
|---|---|
| **Tên** | domain-service |
| **Công nghệ** | Java 17, Spring Boot 3.x, Maven, Camunda Zeebe Client |
| **Mục đích** | Backend API + Camunda Job Workers + Form Schema + Data Layer |
| **Thư mục** | `/domain-service/` |

### Trách nhiệm
- Cung cấp REST API cho frontend (11 endpoints)
- Tích hợp Camunda Zeebe (job workers, start/complete process)
- Quản lý form schema (lookup by task type)
- Load và quản lý sample data (JSON files)
- Thuật toán phân bổ Lead
- Event-driven internal communication

### Cấu trúc thư mục
```
domain-service/
├── pom.xml
├── src/
│   ├── main/
│   │   ├── java/com/crm/poc/
│   │   │   ├── CrmPocApplication.java
│   │   │   ├── lead/                    # Lead Module
│   │   │   │   ├── controller/
│   │   │   │   ├── service/
│   │   │   │   ├── repository/
│   │   │   │   ├── model/
│   │   │   │   └── event/
│   │   │   ├── form/                    # Form Module
│   │   │   │   ├── controller/
│   │   │   │   ├── service/
│   │   │   │   ├── repository/
│   │   │   │   └── model/
│   │   │   ├── workflow/                # Workflow Module
│   │   │   │   ├── controller/
│   │   │   │   ├── service/
│   │   │   │   ├── worker/
│   │   │   │   └── event/
│   │   │   ├── user/                    # User Module
│   │   │   │   ├── controller/
│   │   │   │   ├── service/
│   │   │   │   ├── repository/
│   │   │   │   └── model/
│   │   │   └── data/                    # Data Layer
│   │   │       ├── DataLoader.java
│   │   │       └── InMemoryStore.java
│   │   └── resources/
│   │       ├── application.yml
│   │       └── data/                    # Sample data
│   │           ├── leads.json
│   │           ├── users.json
│   │           └── form-schemas.json
│   └── test/
│       └── java/com/crm/poc/
│           ├── lead/
│           │   └── LeadAllocationPropertyTest.java  # PBT
│           ├── form/
│           │   └── FormSchemaRoundTripTest.java      # PBT
│           └── workflow/
│               └── LeadStateTransitionTest.java      # PBT
└── Dockerfile
```

### Dependencies chính
- `spring-boot-starter-web` - REST API
- `spring-boot-starter-actuator` - Health checks
- `spring-zeebe-client` - Camunda Zeebe integration
- `jackson-databind` - JSON processing
- `jqwik` - Property-Based Testing
- `spring-boot-starter-test` - Testing

---

## Unit 2: BPMN Processes (Camunda)

| Thuộc tính | Giá trị |
|---|---|
| **Tên** | bpmn-processes |
| **Công nghệ** | BPMN 2.0, Camunda Modeler |
| **Mục đích** | Lead lifecycle workflow definitions |
| **Thư mục** | `/bpmn-processes/` |

### Trách nhiệm
- Định nghĩa Lead lifecycle BPMN process
- Xác định user tasks với form keys
- Xác định service tasks cho job workers
- Quản lý process variables
- Cho phép hot-deploy (thay đổi không cần restart)

### Cấu trúc thư mục
```
bpmn-processes/
├── README.md                           # Hướng dẫn deploy
├── processes/
│   ├── lead-lifecycle.bpmn             # Main process
│   └── lead-allocation.bpmn           # Sub-process phân bổ (nếu cần)
├── forms/
│   └── (Camunda form definitions nếu dùng Camunda Forms)
└── scripts/
    └── deploy.sh                       # Script deploy lên Zeebe
```

### BPMN Process Elements
- **Start Event**: Lead được tạo/import
- **User Tasks**: Liên hệ KH, Thu thập hồ sơ, Xử lý, Phân bổ
- **Service Tasks**: Cập nhật trạng thái, Gửi thông báo, Phân bổ tự động
- **Exclusive Gateways**: Phân nhánh theo trạng thái (Done/Rejected)
- **End Events**: Hoàn thành hoặc Từ chối

---

## Unit 3: Appsmith Frontend

| Thuộc tính | Giá trị |
|---|---|
| **Tên** | frontend-appsmith |
| **Công nghệ** | Appsmith (lowcode platform, Docker) |
| **Mục đích** | Frontend platform #1 - Dynamic form rendering + Lead management |
| **Thư mục** | `/frontend-appsmith/` |

### Trách nhiệm
- Render dynamic form từ JSON schema
- Hiển thị danh sách Lead
- Hiển thị chi tiết Lead
- Giao diện phân bổ Lead
- Gọi REST API từ Domain Service
- Polling để check task/status updates

### Cấu trúc thư mục
```
frontend-appsmith/
├── README.md                           # Hướng dẫn setup
├── app-export/                         # Appsmith app export (JSON)
│   └── crm-lead-app.json
└── docs/
    ├── setup-guide.md                  # Hướng dẫn import app
    └── screenshots/                    # Screenshots cho comparison
```

### Các trang (Pages)
- **Lead List Page**: Danh sách Lead với cards
- **Lead Detail Page**: Chi tiết + lịch sử + cập nhật trạng thái
- **Lead Allocation Page**: Chọn Lead + chọn cán bộ + phân bổ
- **Dynamic Form Page**: Render form từ schema (reusable)

---

## Unit 4: Budibase Frontend

| Thuộc tính | Giá trị |
|---|---|
| **Tên** | frontend-budibase |
| **Công nghệ** | Budibase (lowcode platform, Docker) |
| **Mục đích** | Frontend platform #2 - Cùng chức năng với Appsmith để so sánh |
| **Thư mục** | `/frontend-budibase/` |

### Trách nhiệm
- Cùng chức năng như Unit 3 (Appsmith)
- Render dynamic form từ JSON schema
- Gọi cùng REST API endpoints
- Dùng để so sánh: lowcode capability, deployment ease, platform compatibility

### Cấu trúc thư mục
```
frontend-budibase/
├── README.md                           # Hướng dẫn setup
├── app-export/                         # Budibase app export
│   └── crm-lead-app.tar.gz
└── docs/
    ├── setup-guide.md                  # Hướng dẫn import app
    └── screenshots/                    # Screenshots cho comparison
```

### Các trang (Screens)
- **Lead List Screen**: Danh sách Lead
- **Lead Detail Screen**: Chi tiết + cập nhật
- **Lead Allocation Screen**: Phân bổ
- **Dynamic Form Screen**: Render form từ schema

---

## Shared Infrastructure

| Thuộc tính | Giá trị |
|---|---|
| **Tên** | infrastructure |
| **Mục đích** | Docker compose, documentation, comparison |
| **Thư mục** | Root level files |

### Cấu trúc
```
/ (workspace root)
├── docker-compose.yml                  # Toàn bộ stack
├── .env                                # Environment variables
├── README.md                           # Project overview
├── docs/
│   ├── architecture.md                 # Tài liệu kiến trúc
│   ├── platform-comparison.md          # So sánh Appsmith vs Budibase
│   └── demo-script.md                  # Kịch bản demo
├── domain-service/                     # Unit 1
├── bpmn-processes/                     # Unit 2
├── frontend-appsmith/                  # Unit 3
├── frontend-budibase/                  # Unit 4
└── aidlc-docs/                         # AI-DLC documentation
```

---

## Tóm tắt

| Unit | Thư mục | Công nghệ | Phát triển |
|---|---|---|---|
| Domain Service | `/domain-service/` | Java 17, Spring Boot, Maven | Wave 1 |
| BPMN Processes | `/bpmn-processes/` | BPMN 2.0, Camunda | Wave 1 |
| Appsmith Frontend | `/frontend-appsmith/` | Appsmith (Docker) | Wave 2 |
| Budibase Frontend | `/frontend-budibase/` | Budibase (Docker) | Wave 2 |
| Infrastructure | Root level | Docker Compose | Wave 1 |
