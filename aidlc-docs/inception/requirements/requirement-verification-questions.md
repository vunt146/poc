# Câu hỏi làm rõ yêu cầu - CRM POC

Vui lòng trả lời các câu hỏi dưới đây bằng cách điền ký tự lựa chọn sau tag [Answer]:

---

## Question 1
Phạm vi POC này tập trung vào tính năng nào trong spec (pdd.spec)?

A) Chỉ tính năng Xem Lead (phần 1)
B) Chỉ tính năng Phân bổ Lead (phần 2)
C) Cả hai tính năng Xem Lead và Phân bổ Lead
D) Một phần nhỏ đại diện (ví dụ: chỉ danh sách Lead + phân bổ cơ bản)
X) Other (please describe after [Answer]: tag below)

[Answer]: C

---

## Question 2
Mục tiêu chính của POC là gì?

A) Chứng minh khả năng dynamic form rendering trên Appsmith/Budibase kết hợp Camunda workflow
B) So sánh hiệu quả giữa Appsmith và Budibase cho use case CRM
C) Chứng minh kiến trúc tổng thể (frontend dynamic form + workflow engine + domain service) hoạt động end-to-end
D) Đánh giá khả năng tích hợp Camunda 8 với Spring Boot cho business process
X) Other (please describe after [Answer]: tag below)

[Answer]: X
Sử dụng camunda 8 để kiểm tra hoạt động workflow áp dụng sẽ như thế nào, trong đó cần thể hiện sự linh động khi có thay đổi workflow mà không cần deploy lại backend java và frontend. POC để đánh giá FE Appsmith và Budibase cái nào tương thích với nền tảng tổng thể hơn, tập trung vào lowcode, dễ triển khai và apply

---

## Question 3
Cơ chế "dynamic form" cần đạt được mức độ nào?

A) Form definition được lưu dưới dạng JSON/YAML, frontend render dựa trên definition đó
B) Form definition được quản lý bởi Camunda (user task form), frontend chỉ render
C) Form definition nằm trong domain service (Spring Boot), frontend gọi API lấy form schema rồi render
D) Kết hợp: Camunda quyết định task nào cần form gì, domain service cung cấp form schema, frontend render
X) Other (please describe after [Answer]: tag below)

[Answer]: D

---

## Question 4
Vai trò của Camunda 8 trong POC này là gì?

A) Orchestrate toàn bộ luồng nghiệp vụ Lead (từ tạo → phân bổ → xử lý → hoàn thành)
B) Chỉ quản lý luồng phân bổ Lead (allocation workflow)
C) Quản lý trạng thái Lead (state machine cho các trạng thái: New → Contacted → Processing → Done)
D) Điều phối user task (hiển thị form phù hợp cho từng bước trong quy trình)
X) Other (please describe after [Answer]: tag below)

[Answer]: A

---

## Question 5
Appsmith và Budibase sẽ được triển khai như thế nào?

A) Chạy song song cùng một chức năng để so sánh (cùng backend)
B) Mỗi platform thực hiện một phần chức năng khác nhau
C) Appsmith là primary, Budibase là backup/alternative
D) Cả hai đều build đầy đủ, sau đó chọn một để tiếp tục phát triển
X) Other (please describe after [Answer]: tag below)

[Answer]: A

---

## Question 6
Domain Service (Spring Boot) cần cung cấp những API chính nào?

A) CRUD Lead + API phân bổ Lead + API lấy form schema
B) Chỉ API phân bổ Lead + API lấy form schema (Lead data hardcode)
C) API quản lý Lead lifecycle (tạo, cập nhật trạng thái, phân bổ) + form schema
D) Minimal: API lấy danh sách Lead + API phân bổ + API trả form definition
X) Other (please describe after [Answer]: tag below)

[Answer]: C

---

## Question 7
Dữ liệu hardcode cần ở mức nào?

A) Hardcode trực tiếp trong code Java (in-memory list/map)
B) File JSON/YAML chứa sample data, load khi khởi động
C) H2 in-memory database với data seed script
D) Không quan trọng, miễn là có đủ data mẫu để demo dynamic form
X) Other (please describe after [Answer]: tag below)

[Answer]: B

---

## Question 8
Về authentication/authorization cho POC:

A) Không cần - bỏ qua hoàn toàn phân quyền, mọi user đều thấy tất cả
B) Mock đơn giản - hardcode vài user với role khác nhau (CBBH, Teamlead, Manager)
C) Cần cơ chế phân quyền cơ bản để demo data visibility theo role
D) Chỉ cần phân biệt 2 role: nhân viên (xem lead mình) và quản lý (xem + phân bổ)
X) Other (please describe after [Answer]: tag below)

[Answer]: A

---

## Question 9
Kết quả mong đợi của POC (deliverables)?

A) Source code chạy được + tài liệu kiến trúc + hướng dẫn demo
B) Source code + so sánh Appsmith vs Budibase + recommendation
C) Source code + BPMN process definition + form schema samples
D) Tất cả: source code, tài liệu, so sánh platform, BPMN, demo script
X) Other (please describe after [Answer]: tag below)

[Answer]: D

---

## Question 10: Property-Based Testing Extension
Có nên áp dụng Property-Based Testing (PBT) cho dự án này không?

A) Có — áp dụng tất cả PBT rules (phù hợp cho dự án có business logic, data transformations)
B) Một phần — chỉ áp dụng PBT cho pure functions và serialization (phù hợp khi logic không quá phức tạp)
C) Không — bỏ qua PBT rules (phù hợp cho POC, prototype, ứng dụng CRUD đơn giản)
X) Other (please describe after [Answer]: tag below)

[Answer]: A

---

## Question 11: Security Extensions
Có nên áp dụng Security extension rules cho dự án này không?

A) Có — áp dụng tất cả SECURITY rules (phù hợp cho ứng dụng production)
B) Không — bỏ qua SECURITY rules (phù hợp cho POC, prototype, dự án thử nghiệm)
X) Other (please describe after [Answer]: tag below)

[Answer]: B
