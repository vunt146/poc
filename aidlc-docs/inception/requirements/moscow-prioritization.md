# Phân loại tính năng theo MoSCoW - CRM Lead Management POC

## Giới thiệu

Tài liệu này phân loại các tính năng của POC theo phương pháp MoSCoW:
- **Must Have (M)**: Bắt buộc phải có - POC không thể demo được nếu thiếu
- **Should Have (S)**: Nên có - Tăng giá trị POC đáng kể nhưng có thể tạm bỏ qua
- **Could Have (C)**: Có thể có - Tốt nếu có nhưng không ảnh hưởng mục tiêu chính
- **Won't Have (W)**: Không làm trong phạm vi POC này

---

## MUST HAVE (Bắt buộc)

Các tính năng cốt lõi để POC đạt được mục tiêu chính: chứng minh workflow flexibility, dynamic form rendering, và so sánh frontend platform.

| # | Tính năng | Mô tả | Ref |
|---|---|---|---|
| M1 | **Camunda 8 Workflow Engine Setup** | Cài đặt và cấu hình Camunda 8 self-managed chạy trên Docker | NFR-03.1 |
| M2 | **BPMN Lead Lifecycle Process** | Định nghĩa BPMN process orchestrate luồng Lead: Tạo → Phân bổ → Liên hệ → Xử lý → Hoàn thành | FR-05.1 |
| M3 | **Workflow Flexibility (Hot Deploy)** | Thay đổi BPMN process mà không cần redeploy backend/frontend - đây là mục tiêu chính của POC | FR-05.3, FR-04.4 |
| M4 | **Spring Boot Domain Service** | Backend service cung cấp REST API và đóng vai trò Camunda job worker | FR-05.4, FR-06.x |
| M5 | **API lấy danh sách Lead** | GET /leads - trả về danh sách Lead với thông tin cơ bản | FR-06.1 |
| M6 | **API lấy form schema** | GET /forms/{taskType} - trả về JSON form definition cho từng task type | FR-06.5 |
| M7 | **Dynamic Form Rendering** | Frontend render form động dựa trên JSON schema từ backend | FR-04.3 |
| M8 | **Camunda quyết định form** | Camunda BPMN user task definition xác định task nào cần form gì | FR-04.1 |
| M9 | **Appsmith Frontend App** | Build ứng dụng trên Appsmith với dynamic form rendering | FR-07.1 |
| M10 | **Budibase Frontend App** | Build ứng dụng trên Budibase với cùng chức năng như Appsmith | FR-07.1 |
| M11 | **Sample Data (JSON/YAML)** | File dữ liệu mẫu load khi khởi động, đủ đa dạng để demo | NFR-01.1, NFR-01.3 |
| M12 | **Docker Compose Stack** | Toàn bộ hệ thống khởi động bằng docker-compose | NFR-03.4 |
| M13 | **API phân bổ Lead** | POST /leads/allocate - phân bổ Lead cho danh sách cán bộ | FR-06.4 |
| M14 | **Thuật toán phân bổ đều** | Chia đều Lead cho các cán bộ (nguyên + dư theo alphabet) | FR-03.3 |

---

## SHOULD HAVE (Nên có)

Các tính năng tăng giá trị demo và hoàn thiện trải nghiệm, nhưng POC vẫn có thể chạy được nếu thiếu.

| # | Tính năng | Mô tả | Ref |
|---|---|---|---|
| S1 | **Danh sách Lead - Sắp xếp** | Sắp xếp theo thời gian tạo mới nhất, filter 30 ngày | FR-01.2 |
| S2 | **Lead Card - Dynamic Fields** | Hiển thị thông tin riêng theo sản phẩm trên mỗi card | FR-01.3 |
| S3 | **Chi tiết Lead** | Màn hình chi tiết với lịch sử trạng thái, thông tin KH | FR-02.1 |
| S4 | **Thanh tiến trình trạng thái** | Progress bar: New → Contacted → Processing → Done/Rejected | FR-02.2 |
| S5 | **Cập nhật trạng thái Lead** | Dropdown chọn trạng thái mới (loại trừ trạng thái đã qua) | FR-02.3, FR-06.3 |
| S6 | **API lấy chi tiết Lead** | GET /leads/{id} - trả về full detail bao gồm lịch sử | FR-06.2 |
| S7 | **API lấy danh sách cán bộ** | GET /users/subordinates - danh sách CBPT dưới quyền | FR-06.6 |
| S8 | **Giới hạn 10 cán bộ/lần phân bổ** | Validate tối đa 10 người, hiển thị lỗi nếu vượt | FR-03.1 |
| S9 | **Cảnh báo chia đều** | Hiển thị cảnh báo khi chọn >= 2 cán bộ | FR-03.4 |
| S10 | **Property-Based Testing** | PBT cho thuật toán phân bổ, form schema serialization, state transitions | NFR-04.1-04.4 |
| S11 | **Platform Comparison Document** | Tài liệu so sánh Appsmith vs Budibase theo tiêu chí đánh giá | FR-07.2 |

---

## COULD HAVE (Có thể có)

Các tính năng bổ sung giúp POC hoàn thiện hơn nhưng không ảnh hưởng đến việc đánh giá mục tiêu chính.

| # | Tính năng | Mô tả | Ref |
|---|---|---|---|
| C1 | **Trạng thái danh sách rỗng** | Hiển thị thông báo khi không có Lead nào | FR-01.4 |
| C2 | **Nút "Chọn tất cả/Bỏ chọn tất cả"** | Tiện ích chọn nhanh khi phân bổ Lead | FR-03.5 |
| C3 | **Trạng thái "New Lead" → Call** | Nhấn "Gọi điện" chuyển trạng thái, bắt buộc nhập kết quả | FR-02.4 |
| C4 | **Trạng thái "Document collected"** | Gợi ý upload hồ sơ, auto-mapping thông tin | FR-02.5 |
| C5 | **Filter Lead theo điều kiện phân bổ** | Chỉ hiển thị Lead ở trạng thái New Lead/New Imported Lead khi phân bổ | FR-03.2 |
| C6 | **Demo Script** | Kịch bản demo end-to-end chi tiết | Deliverable #7 |
| C7 | **So sánh tích hợp REST API** | Đánh giá chi tiết khả năng tích hợp API của 2 platform | FR-07.4 |

---

## WON'T HAVE (Không làm trong POC)

Các tính năng thuộc spec gốc nhưng nằm ngoài phạm vi POC hoặc không phù hợp với mục tiêu đánh giá.

| # | Tính năng | Lý do loại bỏ | Ref gốc |
|---|---|---|---|
| W1 | **Phân quyền dữ liệu (Data Visibility)** | POC không cần auth - mọi user thấy tất cả | PDD 1.1 |
| W2 | **Quyền thao tác theo Lead Owner** | Bỏ qua phân quyền hoàn toàn | PDD 1.3 |
| W3 | **Tích hợp CRM thực** | POC dùng data hardcode, không kết nối CRM | PDD 1.3 |
| W4 | **ETL đồng bộ từ CRM** | Không có CRM thực để đồng bộ | PDD 1.2 |
| W5 | **Upload hồ sơ thực** | Chỉ demo form, không cần file upload thực | PDD 1.3 |
| W6 | **Gọi điện tích hợp** | Không tích hợp hệ thống call thực | PDD 1.3 |
| W7 | **Production-ready error handling** | POC scope - xử lý lỗi cơ bản là đủ | - |
| W8 | **Database persistence** | Dùng JSON/YAML files thay thế | NFR-01.2 |
| W9 | **Security (HTTPS, CORS, etc.)** | Bỏ qua security cho POC | NFR-02.1 |
| W10 | **Multi-tenant / Multi-branch** | Không cần phân chia chi nhánh/phòng ban | PDD 1.1 |
| W11 | **Camunda Cloud (SaaS)** | Chỉ dùng self-managed | Constraint |

---

## Tóm tắt phân bổ

| Mức độ | Số lượng | Tỷ lệ | Ghi chú |
|---|---|---|---|
| **Must Have** | 14 | ~33% | Core POC - workflow + dynamic form + platform comparison |
| **Should Have** | 11 | ~26% | Hoàn thiện trải nghiệm demo |
| **Could Have** | 7 | ~16% | Nice-to-have, làm nếu còn thời gian |
| **Won't Have** | 11 | ~25% | Ngoài scope POC |

---

## Lưu ý triển khai

### Thứ tự ưu tiên triển khai (đề xuất)
1. **Infrastructure**: Docker compose + Camunda 8 + Spring Boot skeleton (M1, M4, M12)
2. **Core Backend**: Sample data + Lead APIs + Form schema API (M5, M6, M11, M13, M14)
3. **Workflow**: BPMN process definition + job worker integration (M2, M3, M8)
4. **Frontend #1**: Appsmith app với dynamic form (M7, M9)
5. **Frontend #2**: Budibase app clone chức năng (M10)
6. **Enhancement**: Should Have features (S1-S11)
7. **Polish**: Could Have features (C1-C7)

### Tiêu chí đánh giá POC thành công
- ✅ Thay đổi BPMN → frontend tự động hiển thị form mới (không redeploy)
- ✅ Cả Appsmith và Budibase đều render dynamic form thành công
- ✅ Luồng Lead lifecycle chạy end-to-end qua Camunda
- ✅ Có đủ cơ sở để so sánh và recommend platform frontend
