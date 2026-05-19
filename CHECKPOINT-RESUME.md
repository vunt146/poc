# 🔄 CHECKPOINT - Trạng thái hiện tại

## Trạng thái
- ✅ Code đã generate xong (35+ files)
- ✅ Build thành công (Maven compile + package OK)
- ✅ PBT Tests PASS (10/10)
- ✅ Docker stack đang chạy (5 containers)
- ✅ BPMN deployed (lead-lifecycle v2)
- ✅ Process instance đã tạo (đang chờ ở task "Liên hệ khách hàng")

---

## Môi trường hiện tại: macOS
- **Maven**: `/Users/working/Setup/apache-maven-3.9.6/bin/mvn`
- **Java**: 23.0.1 (target Java 17)
- **Docker**: 27.4.0
- **zbctl**: installed via npm (global)

---

## Services đang chạy

| Service | Container | Port | URL | Credentials |
|---|---|---|---|---|
| Elasticsearch | crm-elasticsearch | 9200 | http://localhost:9200 | - |
| Zeebe | crm-zeebe | 26500, 8088 | gRPC: localhost:26500 | - |
| Operate | crm-operate | 8083 | http://localhost:8083 | demo/demo |
| Tasklist | crm-tasklist | 8084 | http://localhost:8084 | demo/demo |
| Domain Service | crm-domain-service | 8090 | http://localhost:8090/api/leads | - |

---

## Lệnh thường dùng

### Start/Stop stack
```bash
# Start
docker compose up -d

# Stop
docker compose down

# Xem logs
docker compose logs -f domain-service
docker compose logs -f zeebe
```

### Build lại Domain Service
```bash
cd domain-service
mvn clean package -DskipTests
cd ..
docker compose up -d --build domain-service
```

### Deploy BPMN
```bash
zbctl deploy resource bpmn-processes/processes/lead-lifecycle.bpmn --address localhost:26500 --insecure
```

### Tạo process instance
```bash
zbctl create instance lead-lifecycle --variables '{"leadId":"LEAD-001","ownerId":"USR-STAFF-01","contactResult":""}' --address localhost:26500 --insecure
```

### Test APIs
```bash
curl http://localhost:8090/api/leads
curl http://localhost:8090/api/forms/lead-contact
curl http://localhost:8090/api/users
```

### Chạy PBT tests
```bash
cd domain-service
mvn test
```

---

## Lưu ý kỹ thuật

- Zeebe health check hiển thị "unhealthy" do image không có `curl`, nhưng service hoạt động bình thường
- Nếu Operate/Tasklist không start tự động, chạy: `docker start crm-operate crm-tasklist crm-domain-service`
- Elasticsearch cần ~60s để healthy, Zeebe cần thêm ~60s sau đó

---

## Tiếp theo có thể làm

1. Cấu hình Appsmith/Budibase frontend (manual, theo README)
2. Thêm thêm BPMN processes phức tạp hơn
3. Tích hợp workflow với Domain Service API
4. Demo end-to-end: tạo lead → start workflow → complete tasks → verify trạng thái
