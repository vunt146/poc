# Unit of Work Dependencies - CRM Lead Management POC

## 1. Dependency Matrix

| Unit (hàng) phụ thuộc vào → | Domain Service | BPMN Processes | Appsmith | Budibase | Infrastructure |
|---|---|---|---|---|---|
| **Domain Service** | - | ❌ | ❌ | ❌ | ✅ (Camunda Zeebe) |
| **BPMN Processes** | ✅ (job workers) | - | ❌ | ❌ | ✅ (Camunda Zeebe) |
| **Appsmith** | ✅ (REST API) | ❌ | - | ❌ | ✅ (Docker) |
| **Budibase** | ✅ (REST API) | ❌ | ❌ | - | ✅ (Docker) |
| **Infrastructure** | ❌ | ❌ | ❌ | ❌ | - |

**Ghi chú:**
- Domain Service cần Camunda Zeebe running để kết nối (gRPC)
- BPMN Processes cần Domain Service có job workers registered để service tasks hoạt động
- Appsmith/Budibase cần Domain Service running để gọi API
- Infrastructure (Docker Compose) là foundation cho tất cả

---

## 2. Dependency Graph

```
+-------------------+
|  Infrastructure   |
|  (Docker Compose) |
+--------+----------+
         |
         | provides runtime
         v
+--------+----------+
|  Camunda 8 Zeebe  |
|  (Container)      |
+--------+----------+
         |
         | gRPC connection
         v
+--------+----------+         +-------------------+
|  Domain Service   |<--------|  BPMN Processes   |
|  (Spring Boot)    |  needs  |  (deploy to Zeebe)|
+--------+----------+  workers+-------------------+
         |
         | REST API
         +------------------+------------------+
         |                                     |
         v                                     v
+--------+----------+              +-----------+--------+
|  Appsmith         |              |  Budibase          |
|  (Frontend #1)    |              |  (Frontend #2)     |
+-------------------+              +--------------------+
```

---

## 3. Thứ tự phát triển (Development Waves)

### Wave 1: Foundation (Song song)

| Unit | Công việc | Điều kiện tiên quyết |
|---|---|---|
| Infrastructure | Docker compose setup, Camunda Zeebe container | Không |
| Domain Service | Spring Boot app, APIs, job workers, data layer | Camunda Zeebe running |
| BPMN Processes | BPMN design, process definitions | Camunda Zeebe running |

**Ghi chú Wave 1:**
- Infrastructure setup trước (docker-compose với Camunda)
- Domain Service và BPMN Processes phát triển song song
- Domain Service cần Camunda running để test job workers
- BPMN cần Domain Service job workers để test service tasks end-to-end

### Wave 2: Frontend (Song song)

| Unit | Công việc | Điều kiện tiên quyết |
|---|---|---|
| Appsmith | Build UI, connect APIs, dynamic form | Domain Service APIs ready |
| Budibase | Build UI (same features), connect APIs | Domain Service APIs ready |

**Ghi chú Wave 2:**
- Cả hai frontend phát triển song song
- Cần Domain Service APIs hoạt động để test
- Cùng gọi cùng endpoints → dễ so sánh

---

## 4. Integration Points

### 4.1 Domain Service ↔ Camunda Zeebe

| Integration | Giao thức | Mô tả |
|---|---|---|
| Start Process | gRPC (sync) | Domain Service start process instance |
| Complete Task | gRPC (sync) | Domain Service complete user task |
| Job Workers | gRPC (async) | Zeebe dispatch jobs → workers nhận |
| Deploy Process | REST/gRPC | Deploy BPMN lên Zeebe |

### 4.2 Frontend ↔ Domain Service

| Integration | Giao thức | Mô tả |
|---|---|---|
| Lead CRUD | REST/HTTP | GET/PUT/POST endpoints |
| Form Schema | REST/HTTP | GET form schema by task type |
| Workflow Tasks | REST/HTTP | Start process, complete task, list tasks |
| User Data | REST/HTTP | GET subordinates, user info |

### 4.3 BPMN ↔ Domain Service

| Integration | Cơ chế | Mô tả |
|---|---|---|
| Service Tasks | Job type matching | BPMN service task type = job worker type |
| User Tasks | Form key | BPMN user task form key = form schema lookup key |
| Process Variables | JSON | Variables truyền giữa tasks |

---

## 5. Deployment Dependencies (Docker Compose)

### Thứ tự khởi động

```yaml
# docker-compose.yml dependency order:
services:
  zeebe:                    # 1. Start first (no dependencies)
    ...
  
  domain-service:           # 2. Start after zeebe
    depends_on:
      zeebe:
        condition: service_healthy
    ...
  
  appsmith:                 # 3. Start after domain-service
    depends_on:
      domain-service:
        condition: service_healthy
    ...
  
  budibase:                 # 3. Start after domain-service (parallel with appsmith)
    depends_on:
      domain-service:
        condition: service_healthy
    ...
```

### Port Allocation

| Service | Port | Mô tả |
|---|---|---|
| Camunda Zeebe (Gateway) | 26500 | gRPC gateway |
| Camunda Zeebe (REST) | 8088 | REST API |
| Camunda Operate | 8083 | Process monitoring UI |
| Camunda Tasklist | 8084 | User task management UI |
| Elasticsearch | 9200 | Search engine (Operate/Tasklist dependency) |
| Domain Service | 8090 | Spring Boot REST API |
| Appsmith | 8080 | Appsmith UI |
| Budibase | 8081 | Budibase UI |

---

## 6. Rủi ro và Giảm thiểu

| Rủi ro | Mức độ | Giảm thiểu |
|---|---|---|
| Camunda Zeebe startup chậm | Thấp | Health check + retry trong docker-compose |
| Appsmith/Budibase không hỗ trợ dynamic form tốt | Trung bình | Nghiên cứu trước khả năng, có fallback plan |
| BPMN deploy fail | Thấp | Script deploy với error handling |
| API contract mismatch giữa FE và BE | Trung bình | Định nghĩa API contract rõ ràng trước |
