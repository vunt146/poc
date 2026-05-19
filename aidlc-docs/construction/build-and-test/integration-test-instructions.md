# Hướng dẫn Integration Test - CRM Lead Management POC

## Tổng quan

Integration tests cho POC tập trung vào:
1. API endpoints hoạt động đúng
2. Workflow integration (start process, complete task)
3. Dynamic form resolution

## Test thủ công (Manual Integration Test)

### Test 1: Lead CRUD Flow

```bash
# 1. Lấy danh sách leads
curl -s http://localhost:8090/api/leads | jq '.[] | {id, customerName, status}'

# 2. Lấy chi tiết lead
curl -s http://localhost:8090/api/leads/LEAD-001 | jq .

# 3. Cập nhật trạng thái (NEW_LEAD → CONTACTED)
curl -s -X PUT http://localhost:8090/api/leads/LEAD-001/status \
  -H "Content-Type: application/json" \
  -d '{
    "newStatus": "CONTACTED",
    "updatedBy": "USR-STAFF-01",
    "note": "Đã gọi điện, KH quan tâm sản phẩm",
    "reason": "Liên hệ lần đầu"
  }' | jq .

# 4. Verify history updated
curl -s http://localhost:8090/api/leads/LEAD-001 | jq '.history'
```

### Test 2: Lead Allocation Flow

```bash
# 1. Lấy leads có thể phân bổ
curl -s "http://localhost:8090/api/leads/allocatable?ownerId=USR-MGR-01" | jq '.[] | {id, status}'

# 2. Lấy danh sách cán bộ
curl -s http://localhost:8090/api/users/subordinates | jq '.[] | {id, username, name}'

# 3. Phân bổ 3 leads cho 2 cán bộ
curl -s -X POST http://localhost:8090/api/leads/allocate \
  -H "Content-Type: application/json" \
  -d '{
    "leadIds": ["LEAD-001", "LEAD-002", "LEAD-006"],
    "targetUserIds": ["USR-STAFF-01", "USR-STAFF-02"],
    "requestedBy": "USR-MGR-01"
  }' | jq .

# 4. Verify owners changed
curl -s http://localhost:8090/api/leads/LEAD-001 | jq '{id, ownerId}'
curl -s http://localhost:8090/api/leads/LEAD-002 | jq '{id, ownerId}'
```

### Test 3: Dynamic Form Flow

```bash
# 1. Lấy tất cả form schemas
curl -s http://localhost:8090/api/forms | jq '.[] | {formId, taskType, title}'

# 2. Lấy form cho task "lead-contact"
curl -s http://localhost:8090/api/forms/lead-contact | jq .

# 3. Verify conditional fields
curl -s http://localhost:8090/api/forms/lead-contact | jq '.fields[] | select(.visibilityCondition != null)'
```

### Test 4: Workflow Integration

```bash
# 1. Start process instance
curl -s -X POST http://localhost:8090/api/workflow/start \
  -H "Content-Type: application/json" \
  -d '{
    "bpmnProcessId": "lead-lifecycle",
    "variables": {
      "leadId": "LEAD-001",
      "ownerId": "USR-STAFF-01",
      "status": "NEW_LEAD",
      "productType": "PAYMENT_ACCOUNT"
    }
  }' | jq .

# 2. Check Operate UI for process instance
# Open: http://localhost:8083

# 3. Check Tasklist for user tasks
# Open: http://localhost:8084
```

### Test 5: Workflow Flexibility (Hot Deploy)

```bash
# 1. Modify BPMN (add/remove a task)
# Edit bpmn-processes/processes/lead-lifecycle.bpmn

# 2. Re-deploy
cd bpmn-processes/scripts
./deploy.sh localhost:26500

# 3. Start new process instance → uses new version
# 4. Frontend queries form schema → gets updated form
# 5. NO restart of domain-service or frontend needed!
```

## Validation Checklist

| Test | Kết quả mong đợi | Pass/Fail |
|---|---|---|
| GET /api/leads | Trả về 7 leads | [ ] |
| GET /api/leads/LEAD-001 | Trả về chi tiết lead | [ ] |
| PUT /api/leads/LEAD-001/status | Status updated, history added | [ ] |
| POST /api/leads/allocate | Leads distributed fairly | [ ] |
| GET /api/forms/lead-contact | Form schema with conditional fields | [ ] |
| POST /api/workflow/start | Process instance created | [ ] |
| Operate UI | Shows process instances | [ ] |
| Tasklist UI | Shows user tasks | [ ] |
| Hot deploy BPMN | New version active without restart | [ ] |
