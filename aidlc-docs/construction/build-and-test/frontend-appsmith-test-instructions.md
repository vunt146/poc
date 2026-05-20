# Test Instructions - Frontend Appsmith

## Testing Strategy

Frontend Appsmith là lowcode platform → testing chủ yếu là **manual UI testing** trên Appsmith Cloud.

Không có automated unit tests (Appsmith không hỗ trợ). Testing focus vào:
1. API connectivity
2. UI rendering
3. Business logic (JS Objects)
4. User workflows (end-to-end)

---

## Test Environment Setup

1. ✅ Domain Service running (localhost:8090)
2. ✅ Camunda 8 running (Docker)
3. ✅ ngrok tunnel active
4. ✅ Appsmith app deployed

---

## Test Suite 1: API Connectivity

| # | Test | Steps | Expected Result | Status |
|---|---|---|---|---|
| 1.1 | GET /api/leads | Run getLeads query | JSON array returned | ☐ |
| 1.2 | GET /api/leads/{id} | Run getLeadDetail with valid ID | Lead object returned | ☐ |
| 1.3 | GET /api/leads/allocatable | Run getAllocatableLeads | Filtered leads returned | ☐ |
| 1.4 | GET /api/users/subordinates | Run getSubordinates | User list returned | ☐ |
| 1.5 | GET /api/workflow/tasks | Run getWorkflowTasks | Task list returned | ☐ |
| 1.6 | GET /api/forms/{taskType} | Run getFormSchema | FormSchema JSON returned | ☐ |

---

## Test Suite 2: Page 1 - Lead List

| # | Test | Steps | Expected Result | Status |
|---|---|---|---|---|
| 2.1 | Page loads | Navigate to Lead List | Table displays with data | ☐ |
| 2.2 | Columns correct | Check table columns | Status, ID, Name, Product, Date visible | ☐ |
| 2.3 | Status colors | Check status column | Color-coded backgrounds per status | ☐ |
| 2.4 | Sort by date | Check default sort | Newest first | ☐ |
| 2.5 | Row click | Click any row | Navigates to Lead Detail with correct ID | ☐ |
| 2.6 | Empty state | Remove all data (or filter empty) | "Không có Lead nào" message | ☐ |

---

## Test Suite 3: Page 2 - Lead Detail

| # | Test | Steps | Expected Result | Status |
|---|---|---|---|---|
| 3.1 | Page loads | Navigate from Lead List | Detail page shows correct lead | ☐ |
| 3.2 | Customer info | Check info section | Name, product, owner displayed | ☐ |
| 3.3 | Progress bar | Check progress widget | Correct percentage for current status | ☐ |
| 3.4 | Progress color | Check for REJECTED lead | Red color | ☐ |
| 3.5 | History table | Check history section | All history entries displayed | ☐ |
| 3.6 | Status dropdown | Check dropdown options | Only valid next statuses shown | ☐ |
| 3.7 | Update status | Select new status + click Update | Status changes, success alert, data refreshes | ☐ |
| 3.8 | Back button | Click "← Quay lại" | Returns to Lead List | ☐ |
| 3.9 | Product details | Check product details section | JSON displayed correctly | ☐ |

---

## Test Suite 4: Page 3 - Lead Allocation

| # | Test | Steps | Expected Result | Status |
|---|---|---|---|---|
| 4.1 | Page loads | Navigate to Allocation | Allocatable leads table displayed | ☐ |
| 4.2 | Multi-select | Click checkboxes on rows | Multiple rows selected | ☐ |
| 4.3 | Select All | Click "Chọn tất cả" | All rows selected | ☐ |
| 4.4 | Deselect All | Click "Bỏ chọn" | All rows deselected | ☐ |
| 4.5 | Allocate disabled | No rows selected | "Phân bổ" button disabled | ☐ |
| 4.6 | Open modal | Select rows + click "Phân bổ" | Modal opens with subordinate list | ☐ |
| 4.7 | Warning message | Select >= 2 cán bộ in modal | Warning "chia đều" appears | ☐ |
| 4.8 | No warning | Select 1 cán bộ | No warning shown | ☐ |
| 4.9 | Allocate success | Select leads + users + click "Phân bổ cơ hội" | Success alert, modal closes, list refreshes | ☐ |
| 4.10 | Allocate button disabled | No users selected in modal | "Phân bổ cơ hội" disabled | ☐ |

---

## Test Suite 5: Page 4 - Workflow Tasks (Dynamic Form)

| # | Test | Steps | Expected Result | Status |
|---|---|---|---|---|
| 5.1 | Page loads | Navigate to Workflow Tasks | Task list displayed | ☐ |
| 5.2 | Select task | Click a task row | Form schema loaded, form rendered | ☐ |
| 5.3 | Form title | Check form header | Title from schema displayed | ☐ |
| 5.4 | Form fields | Check rendered fields | All fields from schema present | ☐ |
| 5.5 | Required fields | Try submit without required | Validation error | ☐ |
| 5.6 | Dropdown options | Check dropdown field | Options from schema displayed | ☐ |
| 5.7 | Visibility condition | Change dependent field value | Conditional field shows/hides | ☐ |
| 5.8 | Submit form | Fill form + submit | Task completed, success alert, list refreshes | ☐ |
| 5.9 | No task selected | Initial state | "Chọn task" message shown | ☐ |

---

## Test Suite 6: Navigation & Layout

| # | Test | Steps | Expected Result | Status |
|---|---|---|---|---|
| 6.1 | Sidebar menu | Check sidebar | 4 pages listed | ☐ |
| 6.2 | Navigate pages | Click each menu item | Correct page loads | ☐ |
| 6.3 | App name | Check header | "CRM Lead Management" displayed | ☐ |
| 6.4 | Responsive | Resize browser | Layout adapts | ☐ |

---

## Test Suite 7: End-to-End Workflow

| # | Test | Steps | Expected Result | Status |
|---|---|---|---|---|
| 7.1 | Full lead lifecycle | Lead List → Detail → Update status → Verify history | Complete flow works | ☐ |
| 7.2 | Allocation flow | Allocation → Select leads → Select users → Allocate → Verify | Leads reassigned | ☐ |
| 7.3 | Task completion | Tasks → Select task → Fill form → Submit → Verify task gone | Workflow advances | ☐ |

---

## Test Results Summary

| Suite | Total | Pass | Fail | Status |
|---|---|---|---|---|
| API Connectivity | 6 | ___ | ___ | ☐ |
| Lead List | 6 | ___ | ___ | ☐ |
| Lead Detail | 9 | ___ | ___ | ☐ |
| Lead Allocation | 10 | ___ | ___ | ☐ |
| Dynamic Form | 9 | ___ | ___ | ☐ |
| Navigation | 4 | ___ | ___ | ☐ |
| End-to-End | 3 | ___ | ___ | ☐ |
| **TOTAL** | **47** | ___ | ___ | ☐ |

---

## Known Limitations (Not Bugs)

| # | Limitation | Reason |
|---|---|---|
| 1 | FILE field type not rendered | Appsmith JSON Form doesn't support file upload natively |
| 2 | ngrok URL changes on restart | Free tier limitation - update datasource manually |
| 3 | No automated tests | Appsmith lowcode platform doesn't support test automation |
