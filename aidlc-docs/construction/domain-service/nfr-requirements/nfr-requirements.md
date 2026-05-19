# NFR Requirements - Unit 1: Domain Service

## Bối cảnh
Đây là POC (Proof of Concept) - các NFR được thiết kế ở mức tối thiểu phù hợp cho mục đích demo và đánh giá, KHÔNG phải production-grade.

---

## 1. Performance

| Yêu cầu | Giá trị | Ghi chú |
|---|---|---|
| Response time (API) | < 500ms | Chấp nhận được cho POC với in-memory data |
| Concurrent users | 1-5 | POC demo, không cần load testing |
| Data volume | 5-10 Leads, 5 Users | Minimal sample data |
| Startup time | < 30s | Bao gồm load JSON files |

---

## 2. Availability

| Yêu cầu | Giá trị | Ghi chú |
|---|---|---|
| Uptime | Không yêu cầu | POC chạy local/demo |
| Recovery | Restart container | Không cần HA |
| Data persistence | Không (in-memory) | Data reset khi restart |
| Backup | Không cần | Source JSON files là backup |

---

## 3. Security

| Yêu cầu | Giá trị | Ghi chú |
|---|---|---|
| Authentication | Không | POC bỏ qua hoàn toàn |
| Authorization | Không | Mọi user truy cập tất cả |
| HTTPS | Không | HTTP cho đơn giản |
| Input validation | Cơ bản | Validate business rules, không cần XSS/injection protection |
| CORS | Allow all origins | POC - frontend chạy trên port khác |

---

## 4. Scalability

| Yêu cầu | Giá trị | Ghi chú |
|---|---|---|
| Horizontal scaling | Không cần | Single instance |
| Vertical scaling | Không cần | Default JVM settings |
| Data growth | Không cần | Fixed sample data |

---

## 5. Reliability

| Yêu cầu | Giá trị | Ghi chú |
|---|---|---|
| Error handling | Basic try-catch | Trả HTTP error codes phù hợp |
| Retry logic | Không | POC scope |
| Circuit breaker | Không | POC scope |
| Logging | Console logging (SLF4J) | Đủ để debug |
| Monitoring | Spring Actuator (health endpoint) | Minimal |

---

## 6. Testability

| Yêu cầu | Giá trị | Ghi chú |
|---|---|---|
| Unit tests | Có | JUnit 5 cho business logic |
| Property-Based Tests | Có (bắt buộc) | jqwik cho allocation, form schema, state transitions |
| Integration tests | Cơ bản | Test API endpoints |
| E2E tests | Không | Manual demo thay thế |
| Code coverage | Không yêu cầu target | Focus vào PBT quality |

---

## 7. Maintainability

| Yêu cầu | Giá trị | Ghi chú |
|---|---|---|
| Code style | Standard Java conventions | Không cần strict linting |
| Documentation | Javadoc cho public APIs | Minimal |
| Configuration | application.yml | Externalized config |
| Dependency management | Maven (pom.xml) | Pinned versions |

---

## 8. Deployment

| Yêu cầu | Giá trị | Ghi chú |
|---|---|---|
| Container | Docker | Dockerfile cho domain-service |
| Orchestration | Docker Compose | Toàn bộ stack |
| Environment | Local development | Không cần cloud |
| CI/CD | Không cần | POC scope |
