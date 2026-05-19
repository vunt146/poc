# Domain Entities - Unit 1: Domain Service

## 1. Lead Entity

### Thuộc tính

| Field | Type | Bắt buộc | Mô tả |
|---|---|---|---|
| id | String | ✅ | Mã cơ hội (unique identifier) |
| customerName | String | ✅ | Tên khách hàng |
| status | LeadStatus | ✅ | Trạng thái hiện tại |
| ownerId | String | ✅ | ID của Lead Owner hiện tại |
| productType | ProductType | ✅ | Loại sản phẩm |
| productDetails | Map<String, Object> | ❌ | Thông tin riêng theo sản phẩm (dynamic) |
| createdAt | LocalDateTime | ✅ | Thời gian tạo |
| updatedAt | LocalDateTime | ✅ | Thời gian cập nhật cuối |
| history | List<LeadHistoryEntry> | ✅ | Lịch sử thay đổi trạng thái |

### LeadStatus (Enum)

| Value | Code | Mô tả | Cho phép phân bổ |
|---|---|---|---|
| NEW_LEAD | 1007 | Lead mới | ✅ |
| NEW_IMPORTED_LEAD | 106141 | Lead import mới | ✅ |
| CONTACTED | 2001 | Đã liên hệ | ❌ |
| PROCESSING | 3001 | Đang xử lý | ❌ |
| DOCUMENT_COLLECTED | 4001 | Đã thu thập hồ sơ | ❌ |
| COMPLETED | 5001 | Hoàn thành | ❌ |
| REJECTED | 9001 | KH từ chối | ❌ |

### ProductType (Enum)

| Value | Mô tả | Dynamic Fields |
|---|---|---|
| PAYMENT_ACCOUNT | Tài khoản thanh toán | bundledProduct (Sản phẩm đi kèm) |
| CREDIT_CARD | Thẻ tín dụng | creditLimit (Hạn mức đề nghị) |
| LOAN | Khoản vay | loanType (Loại khoản vay), amount (Số tiền) |

---

## 2. LeadHistoryEntry Entity

### Thuộc tính

| Field | Type | Bắt buộc | Mô tả |
|---|---|---|---|
| id | String | ✅ | History entry ID (auto-generated) |
| leadId | String | ✅ | ID Lead liên quan |
| previousStatus | LeadStatus | ✅ | Trạng thái trước |
| newStatus | LeadStatus | ✅ | Trạng thái sau |
| changedBy | String | ✅ | ID người thay đổi |
| changedAt | LocalDateTime | ✅ | Thời điểm thay đổi |
| note | String | ❌ | Ghi chú |
| reason | String | ❌ | Lý do thay đổi |

---

## 3. User Entity

### Thuộc tính

| Field | Type | Bắt buộc | Mô tả |
|---|---|---|---|
| id | String | ✅ | User ID |
| name | String | ✅ | Tên đầy đủ |
| username | String | ✅ | Username (dùng cho alphabet sorting) |
| miscode | String | ✅ | Mã miscode |
| role | UserRole | ✅ | Vai trò |
| managerId | String | ❌ | ID quản lý trực tiếp (null nếu là top-level) |

### UserRole (Enum)

| Value | Mô tả | Quyền phân bổ |
|---|---|---|
| CBBH | Cán bộ bán hàng | ❌ |
| TEAM_LEAD | Trưởng nhóm | ✅ |
| BRANCH_MANAGER | Quản lý Chi nhánh/Phòng ban | ✅ |
| DIVISION_DIRECTOR | Giám đốc Khối | ✅ |

---

## 4. FormSchema Entity

### Thuộc tính

| Field | Type | Bắt buộc | Mô tả |
|---|---|---|---|
| formId | String | ✅ | Form ID (unique) |
| formKey | String | ✅ | Camunda form key (mapping) |
| taskType | String | ✅ | Loại task tương ứng |
| title | String | ✅ | Tiêu đề form |
| description | String | ❌ | Mô tả form |
| fields | List<FormField> | ✅ | Danh sách fields |

### FormField Entity

| Field | Type | Bắt buộc | Mô tả |
|---|---|---|---|
| id | String | ✅ | Field ID |
| type | FieldType | ✅ | Loại field |
| label | String | ✅ | Nhãn hiển thị |
| required | boolean | ✅ | Bắt buộc hay không |
| placeholder | String | ❌ | Placeholder text |
| defaultValue | Object | ❌ | Giá trị mặc định |
| options | List<FieldOption> | ❌ | Options cho dropdown/radio |
| validation | ValidationRule | ❌ | Validation rules |
| visibilityCondition | VisibilityCondition | ❌ | Điều kiện hiển thị (conditional) |

### FieldType (Enum)

| Value | Mô tả |
|---|---|
| TEXT | Input text |
| TEXTAREA | Multi-line text |
| NUMBER | Số |
| DROPDOWN | Dropdown select |
| DATE | Date picker |
| CHECKBOX | Checkbox |
| RADIO | Radio buttons |
| FILE | File upload (placeholder) |

### FieldOption

| Field | Type | Mô tả |
|---|---|---|
| value | String | Giá trị |
| label | String | Nhãn hiển thị |

### VisibilityCondition (Conditional Fields)

| Field | Type | Mô tả |
|---|---|---|
| dependsOn | String | ID field mà điều kiện phụ thuộc |
| operator | String | Toán tử: "equals", "notEquals", "in", "notIn" |
| value | Object | Giá trị so sánh |

**Ví dụ:** Field "callResult" chỉ hiện khi field "action" = "CALL"
```json
{
  "dependsOn": "action",
  "operator": "equals",
  "value": "CALL"
}
```

### ValidationRule

| Field | Type | Mô tả |
|---|---|---|
| minLength | Integer | Độ dài tối thiểu |
| maxLength | Integer | Độ dài tối đa |
| min | Number | Giá trị tối thiểu |
| max | Number | Giá trị tối đa |
| pattern | String | Regex pattern |
| message | String | Thông báo lỗi |

---

## 5. Event Entities

### LeadStatusChangeEvent

| Field | Type | Mô tả |
|---|---|---|
| leadId | String | ID Lead |
| previousStatus | LeadStatus | Trạng thái cũ |
| newStatus | LeadStatus | Trạng thái mới |
| changedBy | String | Người thay đổi |
| note | String | Ghi chú |
| reason | String | Lý do |
| timestamp | LocalDateTime | Thời điểm |

### LeadAllocationEvent

| Field | Type | Mô tả |
|---|---|---|
| leadIds | List<String> | Danh sách Lead IDs |
| targetUserIds | List<String> | Danh sách cán bộ nhận |
| requestedBy | String | Người yêu cầu phân bổ |
| timestamp | LocalDateTime | Thời điểm |

### LeadTaskCompletedEvent

| Field | Type | Mô tả |
|---|---|---|
| taskType | String | Loại task |
| leadId | String | Lead liên quan |
| completedBy | String | Người hoàn thành |
| result | Map<String, Object> | Kết quả task |
| timestamp | LocalDateTime | Thời điểm |

---

## 6. DTO/Request/Response Objects

### AllocationRequest

| Field | Type | Mô tả |
|---|---|---|
| leadIds | List<String> | Danh sách Lead cần phân bổ |
| targetUserIds | List<String> | Danh sách cán bộ nhận |

### AllocationResult

| Field | Type | Mô tả |
|---|---|---|
| success | boolean | Thành công hay không |
| allocations | Map<String, List<String>> | userId → list of leadIds |
| totalLeadsAllocated | int | Tổng số Lead đã phân bổ |
| message | String | Thông báo |

### StatusUpdateRequest

| Field | Type | Mô tả |
|---|---|---|
| newStatus | LeadStatus | Trạng thái mới |
| updatedBy | String | Người cập nhật |
| note | String | Ghi chú |
| reason | String | Lý do |
