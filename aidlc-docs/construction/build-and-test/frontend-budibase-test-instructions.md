# Test Instructions - Frontend Budibase

## Test Strategy

Budibase là lowcode platform → testing chủ yếu là **manual testing** qua UI.
Không có unit tests hay automated tests (khác với Domain Service).

---

## Test Environment Setup

### Prerequisites
1. Budibase app đã build và publish
2. Domain Service running (localhost:8090)
3. ngrok tunnel active
4. Có ít nhất 3-5 leads trong hệ thống (sample data)
5. Có ít nhất 1 lead ở mỗi status để test

### Verify Backend
```bash
# Check leads exist
curl -s -H "ngrok-skip-browser-warning: true" http://localhost:8090/api/leads | python3 -m json.tool | head -20

# Check specific lead
curl -s -H "ngrok-skip-browser-warning: true" http://localhost:8090/api/leads/LEAD-001 | python3 -m json.tool
```

---

## Test Cases

### TC-01: Lead List - Basic Display

| # | Step | Expected Result | Pass/Fail |
|---|---|---|---|
| 1 | Mở app (home screen) | Lead List hiển thị | |
| 2 | Verify table có data | Hiển thị danh sách leads | |
| 3 | Verify columns | Status, Lead ID, Customer Name, Product Type, Process Key, Created At | |
| 4 | Verify hidden columns | ownerId, productDetails, updatedAt, history KHÔNG hiển thị | |
| 5 | Verify sorting | Leads mới nhất ở trên | |

### TC-02: Lead List - Status Badge Colors

| # | Step | Expected Result | Pass/Fail |
|---|---|---|---|
| 1 | Tìm lead status NEW_LEAD | Badge màu xanh dương (blue) | |
| 2 | Tìm lead status CONTACTED | Badge màu vàng (yellow) | |
| 3 | Tìm lead status PROCESSING | Badge màu cam (orange) | |
| 4 | Tìm lead status COMPLETED | Badge màu xanh lá (green) | |
| 5 | Tìm lead status REJECTED | Badge màu đỏ (red) | |

### TC-03: Lead List - Navigation

| # | Step | Expected Result | Pass/Fail |
|---|---|---|---|
| 1 | Click vào 1 row trong table | Navigate tới Lead Detail | |
| 2 | Verify URL | URL chứa leadId (e.g., /lead/LEAD-001) | |
| 3 | Verify data | Lead Detail hiển thị đúng lead đã click | |

### TC-04: Lead Detail - Basic Info

| # | Step | Expected Result | Pass/Fail |
|---|---|---|---|
| 1 | Mở Lead Detail (click từ list) | Screen hiển thị | |
| 2 | Verify customer name | Hiển thị đúng tên khách hàng | |
| 3 | Verify product type | Hiển thị đúng loại sản phẩm | |
| 4 | Verify owner | Hiển thị đúng ownerId | |
| 5 | Verify product details | Hiển thị JSON productDetails | |

### TC-05: Lead Detail - Progress Steps

| # | Step | Expected Result | Pass/Fail |
|---|---|---|---|
| 1 | Lead status = NEW_LEAD | Chỉ step "New" highlighted | |
| 2 | Lead status = CONTACTED | Steps "New" + "Contacted" highlighted | |
| 3 | Lead status = PROCESSING | Steps "New" + "Contacted" + "Processing" highlighted | |
| 4 | Lead status = COMPLETED | Tất cả steps highlighted | |

### TC-06: Lead Detail - History Table

| # | Step | Expected Result | Pass/Fail |
|---|---|---|---|
| 1 | Verify history table hiển thị | Table có data | |
| 2 | Verify columns | timestamp, fromStatus, toStatus, changedBy, note | |
| 3 | Verify order | Entry mới nhất ở trên (hoặc dưới) | |

### TC-07: Lead Detail - Back Button

| # | Step | Expected Result | Pass/Fail |
|---|---|---|---|
| 1 | Click "← Quay lại" | Navigate về Lead List | |
| 2 | Verify Lead List | Hiển thị đúng danh sách | |

### TC-08: Status Update - Valid Transitions

| # | Step | Expected Result | Pass/Fail |
|---|---|---|---|
| 1 | Lead status = NEW_LEAD → dropdown | Options: CONTACTED | |
| 2 | Lead status = CONTACTED → dropdown | Options: PROCESSING | |
| 3 | Lead status = PROCESSING → dropdown | Options: DOCUMENT_COLLECTED | |
| 4 | Lead status = DOCUMENT_COLLECTED → dropdown | Options: COMPLETED | |
| 5 | Lead status = COMPLETED → section | Status update section ẩn | |
| 6 | Lead status = REJECTED → section | Status update section ẩn | |

### TC-09: Status Update - Execute

| # | Step | Expected Result | Pass/Fail |
|---|---|---|---|
| 1 | Chọn new status từ dropdown | Status selected | |
| 2 | Nhập note | Note filled | |
| 3 | Click "Cập nhật" | API call thành công | |
| 4 | Verify status changed | Lead status cập nhật | |
| 5 | Verify history | Entry mới trong history table | |
| 6 | Verify progress steps | Steps updated | |

### TC-10: Workflow - Contact Lead

| # | Step | Expected Result | Pass/Fail |
|---|---|---|---|
| 1 | Lead status = NEW_LEAD | Button "Liên hệ khách hàng" hiển thị | |
| 2 | Click button | Modal Contact mở | |
| 3 | Chọn action (CALL) | Selected | |
| 4 | Chọn contactResult (INTERESTED) | Selected | |
| 5 | Nhập note | Filled | |
| 6 | Click "Xác nhận" | API call thành công | |
| 7 | Verify modal đóng | Modal closed | |
| 8 | Verify data refresh | Status updated to CONTACTED | |

### TC-11: Workflow - Process Lead

| # | Step | Expected Result | Pass/Fail |
|---|---|---|---|
| 1 | Lead status = CONTACTED | Button "Xử lý cơ hội" hiển thị | |
| 2 | Click button | Modal Process mở | |
| 3 | Nhập note | Filled | |
| 4 | Click "Xác nhận" | API call thành công | |
| 5 | Verify status | Updated to PROCESSING | |

### TC-12: Workflow - Collect Documents

| # | Step | Expected Result | Pass/Fail |
|---|---|---|---|
| 1 | Lead status = PROCESSING | Button "Thu thập hồ sơ" hiển thị | |
| 2 | Click button | Modal Document mở | |
| 3 | Nhập note | Filled | |
| 4 | Click "Xác nhận" | API call thành công | |
| 5 | Verify status | Updated to DOCUMENT_COLLECTED | |

### TC-13: Workflow - Reject Lead

| # | Step | Expected Result | Pass/Fail |
|---|---|---|---|
| 1 | Lead status != COMPLETED/REJECTED | Button "Từ chối" hiển thị | |
| 2 | Click button | Modal Reject mở | |
| 3 | Nhập reason + note | Filled | |
| 4 | Click "Xác nhận từ chối" | API call thành công | |
| 5 | Verify status | Updated to REJECTED | |
| 6 | Verify buttons ẩn | Action buttons + Reject button ẩn | |

### TC-14: Conditional Button Visibility

| # | Step | Expected Result | Pass/Fail |
|---|---|---|---|
| 1 | Status = NEW_LEAD | "Liên hệ khách hàng" + "Từ chối" visible | |
| 2 | Status = CONTACTED | "Xử lý cơ hội" + "Từ chối" visible | |
| 3 | Status = PROCESSING | "Thu thập hồ sơ" + "Từ chối" visible | |
| 4 | Status = DOCUMENT_COLLECTED | Chỉ "Từ chối" visible (no action button) | |
| 5 | Status = COMPLETED | Không có action buttons | |
| 6 | Status = REJECTED | Không có action buttons | |

### TC-15: Empty State

| # | Step | Expected Result | Pass/Fail |
|---|---|---|---|
| 1 | Nếu không có leads | Hiển thị message "Không có lead nào" | |

---

## Test Summary Template

| Category | Total | Passed | Failed | Notes |
|---|---|---|---|---|
| Lead List (TC-01 to TC-03) | 13 | | | |
| Lead Detail Basic (TC-04 to TC-07) | 14 | | | |
| Status Update (TC-08 to TC-09) | 12 | | | |
| Workflow Actions (TC-10 to TC-13) | 19 | | | |
| Conditional Logic (TC-14) | 6 | | | |
| Empty State (TC-15) | 1 | | | |
| **TOTAL** | **65** | | | |

---

## Known Limitations

1. **ngrok URL changes**: Mỗi lần restart ngrok, cần update Base URL trong Budibase connection
2. **Nested array (history)**: Có thể cần workaround nếu Budibase Table không hỗ trợ nested array trực tiếp
3. **Progress Steps**: Không có native component → dùng Tags/Containers workaround
4. **Dynamic button label**: Có thể cần JavaScript binding phức tạp

