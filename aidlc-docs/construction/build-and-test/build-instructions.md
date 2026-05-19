# Hướng dẫn Build - CRM Lead Management POC

## Yêu cầu hệ thống

| Phần mềm | Version | Mục đích |
|---|---|---|
| Java JDK | 17+ | Build Domain Service |
| Maven | 3.9+ | Build tool |
| Docker | 24+ | Container runtime |
| Docker Compose | 2.x | Orchestrate services |
| zbctl | 8.5.x | Deploy BPMN (optional) |

## Bước 1: Build Domain Service

```bash
cd domain-service

# Build (skip tests for initial build)
mvn clean package -DskipTests

# Verify JAR created
ls target/domain-service-0.0.1-SNAPSHOT.jar
```

**Kết quả mong đợi**: File JAR ~30-50MB trong thư mục `target/`

## Bước 2: Khởi động Infrastructure

```bash
cd ..  # về workspace root

# Start Elasticsearch + Zeebe + Operate + Tasklist
docker-compose up -d elasticsearch zeebe operate tasklist

# Chờ services healthy (~60-90s)
docker-compose ps

# Verify Zeebe ready
curl http://localhost:8088/ready
```

**Kiểm tra:**
- Elasticsearch: http://localhost:9200 → trả JSON cluster info
- Zeebe Gateway: port 26500 (gRPC)
- Operate: http://localhost:8083 → UI login page
- Tasklist: http://localhost:8084 → UI login page

## Bước 3: Start Domain Service

```bash
# Option A: Chạy trực tiếp (development)
cd domain-service
mvn spring-boot:run

# Option B: Chạy từ JAR
java -jar target/domain-service-0.0.1-SNAPSHOT.jar

# Option C: Chạy trong Docker
docker-compose up -d domain-service
```

**Kiểm tra:**
- Health: http://localhost:8090/actuator/health → `{"status":"UP"}`
- API: http://localhost:8090/api/leads → JSON array of leads

## Bước 4: Deploy BPMN Process

```bash
cd bpmn-processes/scripts
chmod +x deploy.sh
./deploy.sh localhost:26500
```

**Hoặc dùng zbctl trực tiếp:**
```bash
zbctl deploy resource ../processes/lead-lifecycle.bpmn --address localhost:26500 --insecure
```

**Kiểm tra:** Mở Operate (http://localhost:8083) → thấy process "Lead Lifecycle Process"

## Bước 5: Verify End-to-End

```bash
# Test API - Lấy danh sách leads
curl http://localhost:8090/api/leads | jq .

# Test API - Lấy form schema
curl http://localhost:8090/api/forms/lead-contact | jq .

# Test API - Lấy users
curl http://localhost:8090/api/users/subordinates | jq .

# Test API - Start workflow
curl -X POST http://localhost:8090/api/workflow/start \
  -H "Content-Type: application/json" \
  -d '{"bpmnProcessId": "lead-lifecycle", "variables": {"leadId": "LEAD-001", "ownerId": "USR-STAFF-01"}}'
```

## Troubleshooting

| Vấn đề | Nguyên nhân | Giải pháp |
|---|---|---|
| Zeebe connection refused | Zeebe chưa ready | Chờ thêm 30s, check `docker-compose logs zeebe` |
| Elasticsearch OOM | Thiếu RAM | Tăng Docker memory limit (>4GB) |
| Port conflict | Port đã dùng | Đổi port trong docker-compose.yml |
| BPMN deploy fail | Zeebe chưa ready | Chờ health check pass, retry |
