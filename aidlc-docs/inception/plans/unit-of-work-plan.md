# Kế hoạch Phân tách Units of Work - CRM Lead Management POC

## Tổng quan

Dựa trên Application Design đã phê duyệt, hệ thống được phân tách thành 4 units of work chính. Tài liệu này xác nhận chiến lược phân tách và tổ chức code.

---

## Kế hoạch thực hiện

- [x] Xác nhận chiến lược phân tách units
- [x] Tạo unit-of-work.md với định nghĩa chi tiết từng unit
- [x] Tạo unit-of-work-dependency.md với dependency matrix
- [x] Tạo unit-of-work-story-map.md mapping requirements → units
- [x] Xác định code organization strategy (greenfield)
- [x] Validate unit boundaries và dependencies

---

## Câu hỏi xác nhận

### Câu hỏi về Code Organization

## Question 1
Cấu trúc thư mục dự án nên tổ chức như thế nào?

A) Mono-repo - Tất cả trong 1 repository, mỗi unit là 1 thư mục con
B) Multi-repo - Mỗi unit là 1 repository riêng biệt
C) Mono-repo với workspace - Dùng tool quản lý (Maven multi-module cho Java, npm workspaces cho FE)
X) Other (please describe after [Answer]: tag below)

[Answer]: X
Chưa xác định, hãy giúp tôi chọn lựa mô hình phù hợp và triển khai nhanh nhất nhưng cũng hiệu quả nhất

---

## Question 2
Spring Boot Domain Service nên dùng build tool nào?

A) Maven (pom.xml) - Phổ biến, ổn định, nhiều tài liệu
B) Gradle (build.gradle) - Linh hoạt hơn, build nhanh hơn
C) Maven multi-module - Mỗi module (lead, form, workflow, user) là 1 Maven module riêng
X) Other (please describe after [Answer]: tag below)

[Answer]: A

---

## Question 3
Java version cho Spring Boot?

A) Java 17 (LTS, phổ biến nhất hiện tại)
B) Java 21 (LTS mới nhất, virtual threads)
C) Java 11 (LTS cũ, tương thích rộng)
X) Other (please describe after [Answer]: tag below)

[Answer]: X
Chọn java tương thích vào camunda 8

---

## Question 4
BPMN process definitions nên nằm ở đâu trong project?

A) Trong Spring Boot resources (src/main/resources/bpmn/) - deploy cùng application
B) Thư mục riêng ngoài Spring Boot (bpmn/) - deploy độc lập lên Camunda
C) Cả hai - copy trong resources để auto-deploy khi dev, thư mục riêng cho production deploy
X) Other (please describe after [Answer]: tag below)

[Answer]: B

---

## Question 5
Thứ tự phát triển các units nên như thế nào?

A) Tuần tự: Domain Service → BPMN → Appsmith → Budibase
B) Song song một phần: Domain Service + BPMN song song → Appsmith + Budibase song song
C) Domain Service trước (foundation), sau đó BPMN + Appsmith + Budibase song song
X) Other (please describe after [Answer]: tag below)

[Answer]: B

---
