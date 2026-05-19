# Kế hoạch Functional Design - Unit 1: Domain Service

## Tổng quan

Thiết kế chi tiết business logic cho Domain Service (Spring Boot), bao gồm:
- Domain models và entities
- Business rules và validation
- Thuật toán phân bổ Lead
- State machine cho Lead lifecycle
- Form schema resolution logic

---

## Kế hoạch thực hiện

- [x] Thiết kế domain entities chi tiết (Lead, User, FormSchema, Events)
- [x] Thiết kế business rules và validation logic
- [x] Thiết kế thuật toán phân bổ Lead (allocation algorithm)
- [x] Thiết kế Lead state machine (transitions, guards)
- [x] Thiết kế form schema resolution logic
- [x] Xác định testable properties cho PBT (PBT-01)
- [x] Validate tính đầy đủ và nhất quán

---

## Câu hỏi làm rõ Business Logic

## Question 1
Khi phân bổ Lead, nếu một Lead đã được phân bổ (owner thay đổi) nhưng sau đó cán bộ mới không xử lý, Lead có thể được phân bổ lại không?

A) Không - một khi đã phân bổ, Lead không thể phân bổ lại (chỉ phân bổ Lead ở trạng thái New)
B) Có - quản lý có thể thu hồi và phân bổ lại bất kỳ lúc nào
C) Có điều kiện - chỉ phân bổ lại nếu Lead vẫn ở trạng thái New sau khi phân bổ
X) Other (please describe after [Answer]: tag below)

[Answer]: B

---

## Question 2
Thuật toán phân bổ: khi "chia đều + dư theo alphabet", alphabet dựa trên trường nào của cán bộ?

A) Tên đầy đủ (fullName) - sắp xếp theo tên tiếng Việt
B) Username - sắp xếp theo username (thường là chữ cái Latin)
C) Miscode - sắp xếp theo mã miscode
X) Other (please describe after [Answer]: tag below)

[Answer]: B

---

## Question 3
Lead state transitions: Có cho phép "nhảy" trạng thái không? (VD: từ New Lead nhảy thẳng sang Processing, bỏ qua Contacted)

A) Không - phải đi tuần tự theo thứ tự (New → Contacted → Processing → Document Collected → Completed)
B) Có - cho phép nhảy tới bất kỳ trạng thái nào phía trước (trừ Completed/Rejected)
C) Một phần - chỉ cho phép nhảy trong một số trường hợp cụ thể
X) Other (please describe after [Answer]: tag below)

[Answer]: C

---

## Question 4
Khi cập nhật trạng thái Lead, có cần lưu thêm thông tin gì không?

A) Chỉ cần lưu trạng thái mới + timestamp
B) Lưu trạng thái mới + timestamp + người cập nhật + ghi chú (note)
C) Lưu full history entry: trạng thái cũ → mới + timestamp + người cập nhật + ghi chú + lý do
X) Other (please describe after [Answer]: tag below)

[Answer]: C

---

## Question 5
Form schema có cần hỗ trợ conditional fields không? (VD: field B chỉ hiện khi field A = "X")

A) Không - tất cả fields trong schema đều hiển thị
B) Có đơn giản - hỗ trợ show/hide field dựa trên giá trị field khác
C) Có nâng cao - hỗ trợ conditional visibility + conditional validation + dynamic options
X) Other (please describe after [Answer]: tag below)

[Answer]: B

---

## Question 6
Sample data cần bao nhiêu records để demo đầy đủ?

A) Minimal: 5-10 Leads, 5 Users, 3-4 Form schemas
B) Moderate: 20-30 Leads, 10 Users, 5-6 Form schemas
C) Comprehensive: 50+ Leads, 20+ Users, tất cả form schemas cho mọi task type
X) Other (please describe after [Answer]: tag below)

[Answer]: A

---
