# 🔄 CHECKPOINT - Hướng dẫn tiếp tục sau restart

## Trạng thái hiện tại
- ✅ Code đã generate xong (35+ files)
- ✅ Build thành công (Maven compile OK)
- ✅ PBT Tests PASS (10/10)
- ❌ Docker stack chưa chạy (cần Docker Desktop)

---

## Sau khi restart máy, thực hiện theo thứ tự:

### Bước 1: Mở Docker Desktop
- Mở Docker Desktop từ Start Menu
- Chờ icon Docker ở system tray chuyển sang trạng thái "Running" (~30-60s)

### Bước 2: Verify Docker
Mở PowerShell/CMD:
```powershell
& "C:\Program Files\Docker\Docker\resources\bin\docker.exe" version
```

### Bước 3: Build JAR
```powershell
& "C:\Program Files\NetBeans-23\netbeans\java\maven\bin\mvn.cmd" clean package -DskipTests
```
Chạy trong thư mục: `d:\PROJECT\POC\workspace\CRM\domain-service`

### Bước 4: Start Docker Compose Stack
```powershell
& "C:\Program Files\Docker\Docker\resources\bin\docker.exe" compose up -d
```
Chạy trong thư mục: `d:\PROJECT\POC\workspace\CRM`

### Bước 5: Chờ services healthy (~60-90s)
```powershell
& "C:\Program Files\Docker\Docker\resources\bin\docker.exe" compose ps
```

### Bước 6: Verify APIs
```powershell
curl http://localhost:8090/api/leads
curl http://localhost:8090/api/forms/lead-contact
curl http://localhost:8090/actuator/health
```

### Bước 7: Truy cập UIs
| Service | URL | Mô tả |
|---|---|---|
| Domain Service | http://localhost:8090/api/leads | REST API |
| Camunda Operate | http://localhost:8083 | Monitoring workflows |
| Camunda Tasklist | http://localhost:8084 | User task management |

---

## Nếu gặp lỗi

| Lỗi | Giải pháp |
|---|---|
| "port already in use" | `docker compose down` rồi `docker compose up -d` lại |
| Elasticsearch OOM | Tăng Docker Desktop memory (Settings → Resources → 4GB+) |
| Zeebe connection refused | Chờ thêm 30s, Zeebe cần thời gian khởi động |
| Domain Service fail | Check log: `docker compose logs domain-service` |

---

## Tiếp tục với AI-DLC
Khi quay lại Kiro, chỉ cần nói: "Tôi đã restart xong, Docker đã chạy, hãy giúp tôi start stack"
