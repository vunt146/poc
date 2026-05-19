# Business Rules - Unit 1: Domain Service

## 1. Quy tắc Phân bổ Lead (Allocation Rules)

### BR-ALLOC-01: Điều kiện Lead được phân bổ
- Lead Owner hiện tại phải là user đang thực hiện phân bổ
- Lead phải ở trạng thái NEW_LEAD (1007) hoặc NEW_IMPORTED_LEAD (106141)
- **Ngoại lệ**: Quản lý có thể thu hồi và phân bổ lại Lead bất kỳ lúc nào (không giới hạn trạng thái khi thu hồi)

### BR-ALLOC-02: Giới hạn số cán bộ
- Tối đa 10 cán bộ phụ trách trong một lần phân bổ
- Tối thiểu 1 cán bộ
- Nếu chọn > 10: reject request với thông báo lỗi

### BR-ALLOC-03: Thuật toán chia đều
**Input**: N leads, M users (sorted by username alphabetically)

**Algorithm**:
1. Sắp xếp danh sách users theo `username` (ascending, case-insensitive)
2. Tính `quotient = N / M` (phần nguyên)
3. Tính `remainder = N % M` (phần dư)
4. Mỗi user nhận `quotient` leads
5. `remainder` users đầu tiên (theo thứ tự alphabet username) nhận thêm 1 lead
6. Leads được phân bổ random cho mỗi user (shuffle trước khi assign)

**Ví dụ**: 7 leads, 3 users (alice, bob, charlie)
- quotient = 2, remainder = 1
- alice: 3 leads (2 + 1 dư)
- bob: 2 leads
- charlie: 2 leads

### BR-ALLOC-04: Cập nhật sau phân bổ
- Cập nhật `ownerId` của mỗi Lead sang user mới
- Thêm history entry cho mỗi Lead (reason: "Phân bổ bởi [manager]")
- Cập nhật `updatedAt` timestamp

---

## 2. Quy tắc State Transitions (Lead Lifecycle)

### BR-STATE-01: Allowed Transitions

| Từ trạng thái | Cho phép chuyển sang |
|---|---|
| NEW_LEAD | CONTACTED, PROCESSING, REJECTED |
| NEW_IMPORTED_LEAD | CONTACTED, PROCESSING, REJECTED |
| CONTACTED | PROCESSING, DOCUMENT_COLLECTED, REJECTED |
| PROCESSING | DOCUMENT_COLLECTED, COMPLETED, REJECTED |
| DOCUMENT_COLLECTED | COMPLETED, REJECTED |
| COMPLETED | (terminal - không chuyển tiếp) |
| REJECTED | (terminal - không chuyển tiếp) |

**Logic "một phần"**: 
- Cho phép nhảy tới trạng thái phía trước (không bắt buộc tuần tự)
- KHÔNG cho phép quay lại trạng thái đã qua
- REJECTED có thể xảy ra từ bất kỳ trạng thái nào (trừ COMPLETED)
- COMPLETED và REJECTED là terminal states

### BR-STATE-02: Transition Guards
- Không cho phép chuyển sang trạng thái đã có trong history
- Không cho phép chuyển từ terminal states (COMPLETED, REJECTED)
- Mỗi transition bắt buộc có `changedBy` (người thay đổi)

### BR-STATE-03: History Tracking
Mỗi lần chuyển trạng thái phải tạo LeadHistoryEntry:
- `previousStatus`: trạng thái trước
- `newStatus`: trạng thái sau
- `changedBy`: ID người thay đổi
- `changedAt`: timestamp hiện tại
- `note`: ghi chú (optional)
- `reason`: lý do (optional)

### BR-STATE-04: Trạng thái đặc biệt
- **NEW_LEAD → CONTACTED (via Call)**: Khi action = "CALL", bắt buộc có `note` (kết quả cuộc gọi)
- **PROCESSING → DOCUMENT_COLLECTED**: Gợi ý upload hồ sơ (thông tin trong form schema)

---

## 3. Quy tắc Form Schema

### BR-FORM-01: Form Resolution
- Mỗi task type mapping 1:1 với một form schema
- Lookup bằng `taskType` field (primary) hoặc `formKey` (secondary)
- Nếu không tìm thấy form schema → trả lỗi 404

### BR-FORM-02: Conditional Fields
- Field có `visibilityCondition` chỉ hiển thị khi điều kiện thỏa mãn
- Operators hỗ trợ: `equals`, `notEquals`, `in`, `notIn`
- Field ẩn không bắt buộc validation (dù `required = true`)
- Frontend chịu trách nhiệm evaluate conditions

### BR-FORM-03: Validation Rules
- `required`: field không được để trống
- `minLength/maxLength`: giới hạn độ dài string
- `min/max`: giới hạn giá trị number
- `pattern`: regex validation
- Backend validate khi nhận form submission

---

## 4. Quy tắc Lead Listing

### BR-LIST-01: Filter mặc định
- Chỉ lấy Lead có `ownerId` = user hiện tại (POC: bỏ qua, lấy tất cả)
- Chỉ lấy Lead có `createdAt` trong 30 ngày gần nhất
- Sắp xếp theo `createdAt` descending (mới nhất trước)

### BR-LIST-02: Filter phân bổ
- Khi ở chế độ phân bổ, chỉ hiển thị Lead có trạng thái NEW_LEAD hoặc NEW_IMPORTED_LEAD
- Và `ownerId` = user đang đăng nhập

---

## 5. Quy tắc User/Cán bộ

### BR-USER-01: Subordinates
- Lấy danh sách users có `managerId` = user hiện tại
- POC: Vì không có auth, trả về tất cả users có role = CBBH

### BR-USER-02: Quyền phân bổ
- Chỉ users có role >= TEAM_LEAD mới được phân bổ
- POC: Bỏ qua check quyền (mọi user đều có thể phân bổ)

---

## 6. Quy tắc Workflow Integration

### BR-WF-01: Start Process
- Khi Lead mới được tạo → start Camunda process instance
- Process variables: leadId, ownerId, status, productType

### BR-WF-02: Complete User Task
- Frontend gọi complete task → Camunda advance workflow
- Task variables: form submission data

### BR-WF-03: Service Task Processing
- Job Worker nhận job → publish internal event
- Business logic xử lý → complete job trên Zeebe
- Nếu xử lý thất bại → throw BpmnError để Camunda handle

---

## 7. Validation Summary

| Rule ID | Validation | Error Message |
|---|---|---|
| VAL-01 | leadIds không được rỗng | "Vui lòng chọn ít nhất 1 Lead" |
| VAL-02 | targetUserIds.size <= 10 | "Tối đa 10 cán bộ phụ trách" |
| VAL-03 | targetUserIds.size >= 1 | "Vui lòng chọn ít nhất 1 cán bộ" |
| VAL-04 | Lead phải ở trạng thái cho phép phân bổ | "Lead không ở trạng thái cho phép phân bổ" |
| VAL-05 | Transition phải hợp lệ | "Không thể chuyển từ {old} sang {new}" |
| VAL-06 | Note bắt buộc khi action = CALL | "Vui lòng nhập kết quả cuộc gọi" |
| VAL-07 | Form schema phải tồn tại | "Không tìm thấy form cho task type: {type}" |
