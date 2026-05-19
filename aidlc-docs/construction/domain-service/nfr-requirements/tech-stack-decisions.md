# Tech Stack Decisions - Unit 1: Domain Service

## Tổng quan

| Layer | Công nghệ | Version | Lý do |
|---|---|---|---|
| Language | Java | 17 (LTS) | Tương thích Camunda 8, ổn định |
| Framework | Spring Boot | 3.2.x | Latest stable, Java 17 support |
| Build Tool | Maven | 3.9.x | Phổ biến, ổn định, nhiều tài liệu |
| Workflow Client | Spring Zeebe | 8.5.x | Official Camunda 8 Spring integration |
| JSON Processing | Jackson | (Spring Boot managed) | Default trong Spring Boot |
| Testing | JUnit 5 | (Spring Boot managed) | Standard Java testing |
| PBT Framework | jqwik | 1.8.x | JUnit 5 integration, stateful testing support |
| Logging | SLF4J + Logback | (Spring Boot managed) | Default Spring Boot logging |
| Container | Docker | Latest | Containerization |

---

## Chi tiết quyết định

### 1. Java 17

**Lý do chọn:**
- LTS (Long Term Support) - ổn định
- Camunda 8 Zeebe Client chính thức hỗ trợ
- Spring Boot 3.x yêu cầu tối thiểu Java 17
- Records, sealed classes, text blocks hỗ trợ code sạch hơn

**Alternatives xem xét:**
- Java 21: Mới hơn nhưng chưa cần virtual threads cho POC
- Java 11: Quá cũ, Spring Boot 3.x không hỗ trợ

---

### 2. Spring Boot 3.2.x

**Lý do chọn:**
- Latest stable release
- Native support cho Java 17+
- Spring Zeebe starter tương thích
- Auto-configuration giảm boilerplate

**Dependencies chính:**
```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.2.5</version>
</parent>

<dependencies>
    <!-- Web -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    
    <!-- Actuator -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-actuator</artifactId>
    </dependency>
    
    <!-- Camunda Zeebe -->
    <dependency>
        <groupId>io.camunda.spring</groupId>
        <artifactId>spring-boot-starter-camunda</artifactId>
        <version>8.5.0</version>
    </dependency>
    
    <!-- Testing -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>
    
    <!-- PBT - jqwik -->
    <dependency>
        <groupId>net.jqwik</groupId>
        <artifactId>jqwik</artifactId>
        <version>1.8.4</version>
        <scope>test</scope>
    </dependency>
</dependencies>
```

---

### 3. Maven (pom.xml)

**Lý do chọn:**
- Phổ biến nhất trong Java ecosystem
- Nhiều tài liệu và community support
- Convention over configuration
- Tương thích tốt với IDE (IntelliJ, VS Code)

**Cấu trúc Maven:**
- Single module (không multi-module cho POC)
- Package structure thể hiện modular monolith (packages = modules)

---

### 4. Spring Zeebe (Camunda 8 Client)

**Lý do chọn:**
- Official Spring Boot starter cho Camunda 8
- Auto-configuration cho Zeebe client
- Annotation-based job worker registration (`@JobWorker`)
- Tích hợp tốt với Spring dependency injection

**Configuration:**
```yaml
# application.yml
zeebe:
  client:
    broker:
      gateway-address: localhost:26500
    security:
      plaintext: true  # POC - không cần TLS
```

---

### 5. jqwik (Property-Based Testing)

**Lý do chọn (PBT-09 compliance):**
- JUnit 5 Platform integration (chạy cùng @Test)
- Custom generators/arbitraries cho domain types
- Automatic shrinking
- Seed-based reproducibility
- Stateful testing support (cho state machine tests)
- Active maintenance

**Capabilities:**
- ✅ Custom generators (Arbitraries API)
- ✅ Automatic shrinking
- ✅ Seed-based reproducibility
- ✅ JUnit 5 integration
- ✅ Stateful testing (@Property + ActionSequence)

---

### 6. Docker

**Base image:** `eclipse-temurin:17-jre-alpine`

**Lý do:**
- Lightweight (Alpine)
- Official Eclipse Temurin JRE
- Java 17 support
- Small image size (~100MB)

**Dockerfile pattern:**
```dockerfile
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY target/*.jar app.jar
EXPOSE 8090
ENTRYPOINT ["java", "-jar", "app.jar"]
```

---

## PBT-09 Compliance Summary

| Criteria | Status | Evidence |
|---|---|---|
| Framework selected | ✅ | jqwik 1.8.x |
| Documented in tech stack | ✅ | This document |
| Included in dependencies | ✅ | pom.xml dependency listed |
| Custom generators supported | ✅ | jqwik Arbitraries API |
| Shrinking supported | ✅ | Built-in |
| Seed reproducibility | ✅ | Built-in |
| Test runner integration | ✅ | JUnit 5 Platform |

---

## Camunda 8 Stack

| Component | Version | Port | Mô tả |
|---|---|---|---|
| Zeebe Broker | 8.5.x | 26500 (gRPC) | Process execution engine |
| Zeebe Gateway | 8.5.x | 8088 (REST) | REST API |
| Operate | 8.5.x | 8081 | Process monitoring, incident management |
| Tasklist | 8.5.x | 8082 | User task management UI |
| Elasticsearch | 8.x | 9200 | Required by Operate & Tasklist (data store) |

**Docker images:**
- `camunda/zeebe:8.5.0` - Workflow engine
- `camunda/operate:8.5.0` - Process monitoring & debugging
- `camunda/tasklist:8.5.0` - User task management
- `docker.elastic.co/elasticsearch/elasticsearch:8.13.0` - Search & analytics engine

**Lý do thêm Operate & Tasklist:**
- **Operate**: Chứng minh khả năng monitoring workflow realtime, xem process instances, debug incidents, visualize BPMN execution - đây là điểm mạnh quan trọng của Camunda 8
- **Tasklist**: Chứng minh khả năng quản lý user tasks built-in, có thể dùng song song với custom frontend (Appsmith/Budibase) để so sánh
- **Elasticsearch**: Dependency bắt buộc cho Operate và Tasklist (Zeebe export events → ES → Operate/Tasklist query)

**Ghi chú:**
- Operate và Tasklist giúp demo năng lực platform Camunda 8 đầy đủ hơn
- Elasticsearch cần thêm RAM (~1-2GB) - cần lưu ý khi chạy local
- Không cần Optimize (analytics/reporting) cho POC scope
