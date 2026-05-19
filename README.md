# CRM Lead Management POC

## Tổng quan

POC chứng minh kiến trúc CRM sử dụng:
- **Camunda 8** (Zeebe + Operate + Tasklist) - Workflow orchestration
- **Spring Boot** - Domain Service (REST API + Job Workers)
- **Appsmith** - Frontend Platform #1 (lowcode)
- **Budibase** - Frontend Platform #2 (lowcode, so sánh)

## Mục tiêu POC

1. Kiểm chứng Camunda 8 cho phép thay đổi workflow mà KHÔNG cần redeploy backend/frontend
2. So sánh Appsmith vs Budibase về khả năng lowcode, dễ triển khai
3. Chứng minh kiến trúc dynamic form (Camunda → Spring Boot → Frontend) hoạt động end-to-end

## Quick Start

### Yêu cầu
- Docker & Docker Compose
- Java 17 + Maven (để build domain-service)
- zbctl CLI (để deploy BPMN)

### Khởi động

```bash
# 1. Build domain service
cd domain-service
mvn clean package -DskipTests
cd ..

# 2. Start toàn bộ stack
docker-compose up -d

# 3. Chờ services healthy (~30-60s)
docker-compose ps

# 4. Deploy BPMN process
cd bpmn-processes/scripts
chmod +x deploy.sh
./deploy.sh localhost:26500
```

### Truy cập

| Service | URL | Mô tả |
|---|---|---|
| Domain Service API | http://localhost:8090 | REST API |
| Camunda Operate | http://localhost:8083 | Process monitoring |
| Camunda Tasklist | http://localhost:8084 | User task management |
| Appsmith | http://localhost:8080 | Frontend #1 |
| Budibase | http://localhost:8081 | Frontend #2 |

### API Endpoints

| Method | Path | Mô tả |
|---|---|---|
| GET | /api/leads | Danh sách Lead |
| GET | /api/leads/{id} | Chi tiết Lead |
| PUT | /api/leads/{id}/status | Cập nhật trạng thái |
| POST | /api/leads/allocate | Phân bổ Lead |
| GET | /api/leads/allocatable?ownerId=X | Lead có thể phân bổ |
| GET | /api/forms/{taskType} | Lấy form schema |
| GET | /api/forms | Liệt kê form schemas |
| POST | /api/workflow/start | Start process instance |
| POST | /api/workflow/tasks/{jobKey}/complete | Complete task |
| GET | /api/users/subordinates | Danh sách cán bộ |
| GET | /api/users/{id} | Thông tin user |

## Cấu trúc dự án

```
CRM/
├── docker-compose.yml          # Toàn bộ stack
├── README.md                   # File này
├── domain-service/             # Spring Boot Backend
│   ├── pom.xml
│   ├── Dockerfile
│   └── src/
├── bpmn-processes/             # Camunda BPMN
│   ├── processes/
│   └── scripts/
├── frontend-appsmith/          # Appsmith guides
├── frontend-budibase/          # Budibase guides
└── docs/                       # Documentation
```

## Chạy Tests

```bash
cd domain-service
mvn test
```

Bao gồm:
- Unit tests (JUnit 5)
- Property-Based Tests (jqwik) cho allocation algorithm, state transitions
