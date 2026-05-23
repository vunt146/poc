# BPMN vs Implementation Gap Analysis

## Ngày phân tích: 2026-05-20

## Tổng quan

File BPMN (`bpmn-processes/processes/lead-lifecycle.bpmn`) mô tả happy path của lead lifecycle. Tuy nhiên, implementation thực tế (Domain Service + Frontend Appsmith) có một số mở rộng so với BPMN. Tài liệu này ghi nhận các khác biệt.

---

## BPMN Flow hiện tại

```
Start (Lead Created)
  → [User Task] Liên hệ khách hàng (formKey: lead-contact-form)
  → [Exclusive Gateway] Kết quả liên hệ?
      ├─ contactResult = "REJECTED" → End (KH từ chối)
      └─ default (Tiếp tục) → [User Task] Xử lý cơ hội (formKey: lead-process-form)
          → [User Task] Thu thập hồ sơ (formKey: lead-document-form)
          → [Service Task] Cập nhật trạng thái (type: lead-status-change)
          → End (Hoàn thành)
```

---

## Domain Service State Machine (thực tế)

```
NEW_LEAD          → CONTACTED, PROCESSING, REJECTED
NEW_IMPORTED_LEAD → CONTACTED, PROCESSING, REJECTED
CONTACTED         → PROCESSING, DOCUMENT_COLLECTED, REJECTED
PROCESSING        → DOCUMENT_COLLECTED, COMPLETED, REJECTED
DOCUMENT_COLLECTED → COMPLETED, REJECTED
COMPLETED         → (terminal - không chuyển tiếp)
REJECTED          → (terminal - không chuyển tiếp)
```

---

## Các khác biệt (Gaps)

### Gap 1: Reject ở mọi bước

| Aspect | BPMN | Implementation |
|---|---|---|
| Reject timing | Chỉ sau Gateway (sau Contact) | Cho phép reject ở BẤT KỲ bước nào (trừ terminal) |
| Mechanism | Condition expression trên sequence flow | `LeadWorkflowController.rejectLead()` - start process với REJECTED nếu chưa có process |
| Impact | BPMN chỉ model 1 reject path | Frontend có button "Từ chối" visible ở mọi trạng thái (trừ COMPLETED/REJECTED) |

**Giải pháp nếu cần cập nhật BPMN**: Thêm boundary error event hoặc event-based subprocess cho reject ở mỗi user task.

### Gap 2: Trạng thái NEW_IMPORTED_LEAD

| Aspect | BPMN | Implementation |
|---|---|---|
| Start event | 1 start event (Lead Created) | 2 trạng thái khởi đầu: NEW_LEAD, NEW_IMPORTED_LEAD |
| Behavior | Không phân biệt | NEW_IMPORTED_LEAD xử lý tương tự NEW_LEAD (cùng allowed transitions) |
| Frontend | N/A | Button "Hành động tiếp theo" check cả 2 trạng thái |

**Giải pháp nếu cần cập nhật BPMN**: Thêm message start event hoặc signal start event cho imported leads.

### Gap 3: Trạng thái DOCUMENT_COLLECTED

| Aspect | BPMN | Implementation |
|---|---|---|
| Status | Implicit (giữa Thu thập hồ sơ và Cập nhật TT) | Explicit status `DOCUMENT_COLLECTED` trong enum |
| Transition | Thu thập hồ sơ → Cập nhật TT (auto) | PROCESSING → DOCUMENT_COLLECTED → COMPLETED |
| Frontend | N/A | Progress bar hiển thị step "Document Collected" |

**Giải pháp nếu cần cập nhật BPMN**: Thêm intermediate throw event hoặc tách service task thành 2 bước.

### Gap 4: Skip steps (flexible transitions)

| Aspect | BPMN | Implementation |
|---|---|---|
| Flow | Strictly linear: Contact → Process → Documents | Cho phép skip: NEW_LEAD → PROCESSING (bỏ qua CONTACTED) |
| Use case | N/A | Khi lead đã được liên hệ ngoài hệ thống |
| Frontend | N/A | Dropdown status cho phép chọn PROCESSING từ NEW_LEAD |

**Giải pháp nếu cần cập nhật BPMN**: Thêm exclusive gateway sau start event cho phép skip Contact.

---

## Đánh giá tổng thể

| Tiêu chí | Đánh giá |
|---|---|
| BPMN đủ cho POC demo? | ✅ Đủ - happy path hoạt động |
| BPMN phản ánh đúng business logic? | ⚠️ Một phần - thiếu edge cases |
| Cần cập nhật ngay? | ❌ Không - POC scope, domain service xử lý đúng |
| Nên cập nhật khi nào? | Khi chuyển sang production hoặc khi cần audit trail chính xác trên Camunda Operate |

---

## Khuyến nghị

1. **Giữ nguyên BPMN hiện tại** cho POC - đủ để demo Camunda integration
2. **Domain Service là source of truth** cho state machine logic
3. **Frontend tuân theo Domain Service** (không tuân theo BPMN trực tiếp)
4. **Khi scale lên production**: Cập nhật BPMN để Camunda Operate hiển thị đúng tất cả paths, bao gồm:
   - Boundary events cho reject
   - Multiple start events
   - Explicit DOCUMENT_COLLECTED status
