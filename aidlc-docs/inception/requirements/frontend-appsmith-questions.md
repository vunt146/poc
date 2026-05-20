# Frontend Appsmith - Clarification Questions

Dự án đã hoàn thành Domain Service + Camunda 8. Bây giờ cần xây dựng Frontend Appsmith.
Bạn đã nêu rõ máy không đủ mạnh để chạy Docker cho Appsmith → muốn dùng Appsmith Cloud.

Vui lòng trả lời các câu hỏi sau bằng cách điền chữ cái vào sau tag [Answer]:

---

## Question 1
Domain Service hiện đang chạy trên localhost:8090. Appsmith Cloud cần kết nối tới API này. Bạn muốn expose Domain Service ra internet bằng cách nào?

A) Sử dụng ngrok (tunnel localhost ra internet, miễn phí)
B) Sử dụng Cloudflare Tunnel
C) Deploy Domain Service lên cloud (AWS/GCP/Azure)
D) Sử dụng cách khác (please describe after [Answer]: tag below)

[Answer]: A

## Question 2
Bạn đã có tài khoản Appsmith Cloud (app.appsmith.com) chưa?

A) Đã có tài khoản và đã dùng qua
B) Đã có tài khoản nhưng chưa dùng
C) Chưa có, sẽ tạo mới
D) Other (please describe after [Answer]: tag below)

[Answer]: B

## Question 3
Về scope frontend Appsmith, bạn muốn build đầy đủ tất cả pages theo requirements hay chỉ một số pages chính?

A) Đầy đủ tất cả: Lead List + Lead Detail + Lead Allocation + Dynamic Form
B) Chỉ core: Lead List + Lead Detail + Lead Allocation (bỏ Dynamic Form)
C) Minimal: Lead List + Lead Detail (demo cơ bản)
D) Other (please describe after [Answer]: tag below)

[Answer]: A

## Question 4
Camunda Tasklist hiện đang chạy ở port 8084. Bạn có muốn tích hợp Appsmith với Camunda Tasklist (claim/complete task) hay chỉ tương tác qua Domain Service API?

A) Chỉ qua Domain Service API (đơn giản hơn, đã có sẵn)
B) Tích hợp cả Camunda Tasklist REST API (phức tạp hơn, nhưng đúng kiến trúc)
C) Other (please describe after [Answer]: tag below)

[Answer]: C
Tôi muốn tích hợp qua Domain Service API, mọi hành động gọi vào camunda đều qua Domain Service API, Frontend không tương tác trực tiếp

## Question 5
Về documentation output, bạn muốn AI-DLC tạo guide chi tiết (step-by-step) để bạn tự build trên Appsmith Cloud, hay bạn muốn tôi tạo Appsmith export file (JSON) mà bạn có thể import?

A) Guide chi tiết step-by-step (tôi sẽ tự build theo hướng dẫn)
B) Tạo Appsmith JSON export file để import trực tiếp
C) Cả hai: JSON export + guide giải thích
D) Other (please describe after [Answer]: tag below)

[Answer]: C
