# Business Logic Model - Unit 1: Domain Service

## 1. Thuật toán Phân bổ Lead (Lead Allocation Algorithm)

### Pseudocode

```
function allocateLeads(leadIds: List<String>, targetUserIds: List<String>): AllocationResult
  
  // 1. Validation
  validate(leadIds is not empty)
  validate(targetUserIds.size >= 1 AND <= 10)
  validate(all leads exist and are in allocatable status)
  
  // 2. Sort users by username (ascending, case-insensitive)
  sortedUsers = sort(targetUserIds, by: user.username, order: ASC, case-insensitive)
  
  // 3. Shuffle leads randomly (để phân bổ random)
  shuffledLeads = shuffle(leadIds)
  
  // 4. Calculate distribution
  N = shuffledLeads.size
  M = sortedUsers.size
  quotient = N / M  (integer division)
  remainder = N % M
  
  // 5. Allocate
  allocations = new Map<String, List<String>>()
  leadIndex = 0
  
  for i = 0 to M-1:
    count = quotient + (1 if i < remainder else 0)
    userLeads = shuffledLeads[leadIndex .. leadIndex + count]
    allocations[sortedUsers[i]] = userLeads
    leadIndex += count
  
  // 6. Update Lead owners
  for each (userId, leadList) in allocations:
    for each leadId in leadList:
      lead = findLead(leadId)
      lead.ownerId = userId
      lead.updatedAt = now()
      addHistoryEntry(lead, "ALLOCATED", "Phân bổ bởi manager")
  
  // 7. Return result
  return AllocationResult(
    success: true,
    allocations: allocations,
    totalLeadsAllocated: N
  )
```

### Invariants (cho PBT)
- **Size preservation**: Tổng số leads phân bổ = tổng số leads đầu vào
- **No lead lost**: Mỗi lead xuất hiện đúng 1 lần trong kết quả
- **Fair distribution**: Chênh lệch giữa user nhận nhiều nhất và ít nhất <= 1
- **Alphabet priority**: Users đầu alphabet nhận phần dư

---

## 2. Lead State Machine

### State Transition Diagram

```
                    +---> REJECTED (terminal)
                    |         ^    ^    ^
                    |         |    |    |
NEW_LEAD -----+----+--> CONTACTED --+--> PROCESSING --+--> DOCUMENT_COLLECTED ---> COMPLETED
              |                     |                  |                            (terminal)
              |                     |                  |
              +---------------------+------------------+
              (cho phép nhảy tới trạng thái phía trước)
              
NEW_IMPORTED_LEAD (same transitions as NEW_LEAD)
```

### Transition Logic

```
function canTransition(currentStatus: LeadStatus, targetStatus: LeadStatus, history: List<LeadHistoryEntry>): boolean
  
  // Terminal states cannot transition
  if currentStatus in [COMPLETED, REJECTED]:
    return false
  
  // Cannot go back to a status already visited
  visitedStatuses = history.map(h -> h.newStatus)
  if targetStatus in visitedStatuses:
    return false
  
  // Check allowed transitions table
  allowedTargets = getAllowedTransitions(currentStatus)
  return targetStatus in allowedTargets

function getAllowedTransitions(status: LeadStatus): Set<LeadStatus>
  switch status:
    NEW_LEAD, NEW_IMPORTED_LEAD:
      return {CONTACTED, PROCESSING, REJECTED}
    CONTACTED:
      return {PROCESSING, DOCUMENT_COLLECTED, REJECTED}
    PROCESSING:
      return {DOCUMENT_COLLECTED, COMPLETED, REJECTED}
    DOCUMENT_COLLECTED:
      return {COMPLETED, REJECTED}
    COMPLETED, REJECTED:
      return {} // terminal
```

### Transition Execution

```
function updateLeadStatus(leadId: String, request: StatusUpdateRequest): Lead
  
  lead = findLead(leadId)
  
  // Validate transition
  if not canTransition(lead.status, request.newStatus, lead.history):
    throw InvalidTransitionException(lead.status, request.newStatus)
  
  // Special validation for CALL action
  if lead.status == NEW_LEAD and request.newStatus == CONTACTED:
    if request.note is empty:
      throw ValidationException("Vui lòng nhập kết quả cuộc gọi")
  
  // Create history entry
  historyEntry = new LeadHistoryEntry(
    previousStatus: lead.status,
    newStatus: request.newStatus,
    changedBy: request.updatedBy,
    changedAt: now(),
    note: request.note,
    reason: request.reason
  )
  
  // Update lead
  lead.status = request.newStatus
  lead.updatedAt = now()
  lead.history.add(historyEntry)
  
  return save(lead)
```

---

## 3. Form Schema Resolution

### Resolution Logic

```
function getFormSchema(taskType: String): FormSchema
  
  // Primary lookup: by taskType
  schema = formSchemaRepository.findByTaskType(taskType)
  
  if schema is present:
    return schema
  
  // Secondary lookup: by formKey (Camunda form key)
  schema = formSchemaRepository.findByFormKey(taskType)
  
  if schema is present:
    return schema
  
  throw FormSchemaNotFoundException("Không tìm thấy form cho task type: " + taskType)
```

### Conditional Field Evaluation (Frontend-side, documented here for reference)

```
function isFieldVisible(field: FormField, formData: Map<String, Object>): boolean
  
  if field.visibilityCondition is null:
    return true  // always visible
  
  condition = field.visibilityCondition
  dependsOnValue = formData[condition.dependsOn]
  
  switch condition.operator:
    "equals":
      return dependsOnValue == condition.value
    "notEquals":
      return dependsOnValue != condition.value
    "in":
      return dependsOnValue in condition.value  // value is a list
    "notIn":
      return dependsOnValue not in condition.value
  
  return true  // default visible
```

---

## 4. Lead Listing Logic

### Query Logic

```
function findLeads(criteria: LeadSearchCriteria): List<Lead>
  
  leads = getAllLeads()
  
  // Filter by date range (30 days)
  cutoffDate = now().minusDays(30)
  leads = leads.filter(l -> l.createdAt >= cutoffDate)
  
  // Filter by owner (POC: skip, return all)
  if criteria.ownerId is not null:
    leads = leads.filter(l -> l.ownerId == criteria.ownerId)
  
  // Sort by createdAt descending
  leads = leads.sortBy(l -> l.createdAt, DESC)
  
  return leads
```

### Allocation Filter Logic

```
function findAllocatableLeads(ownerId: String): List<Lead>
  
  leads = getAllLeads()
  
  // Filter: owner = current user AND status is allocatable
  allocatableStatuses = {NEW_LEAD, NEW_IMPORTED_LEAD}
  leads = leads.filter(l -> 
    l.ownerId == ownerId AND 
    l.status in allocatableStatuses
  )
  
  return leads
```

---

## 5. Testable Properties (PBT-01)

### Thuật toán Phân bổ Lead

| Property | Category | Test Shape | Mô tả |
|---|---|---|---|
| Size preservation | Invariant | sum(allocations.values.sizes) == input.size | Tổng leads phân bổ = tổng đầu vào |
| No lead duplication | Invariant | flatten(allocations.values).distinct.size == input.size | Không có lead trùng lặp |
| Fair distribution | Invariant | max(sizes) - min(sizes) <= 1 | Chênh lệch tối đa 1 |
| Alphabet priority | Invariant | users with extra lead come first alphabetically | Phần dư theo alphabet |
| All users assigned | Invariant | allocations.keys == targetUsers | Mọi user đều nhận leads |
| Idempotent count | Idempotence | allocate(same inputs).totalCount == same | Cùng input → cùng tổng số |

### Form Schema Serialization

| Property | Category | Test Shape | Mô tả |
|---|---|---|---|
| Round-trip | Round-trip | deserialize(serialize(schema)) == schema | Serialize/deserialize giữ nguyên |
| Field count preserved | Invariant | serialized.fields.size == original.fields.size | Số fields không đổi |
| Required fields preserved | Invariant | required fields in output == required fields in input | Required không mất |

### Lead State Transitions

| Property | Category | Test Shape | Mô tả |
|---|---|---|---|
| No backward transition | Invariant | newStatus.order > currentStatus.order (except REJECTED) | Không quay lại |
| Terminal is terminal | Invariant | canTransition(COMPLETED, any) == false | Terminal không chuyển tiếp |
| History grows | Invariant | after transition: history.size == old.size + 1 | History luôn tăng |
| Visited exclusion | Stateful | after visiting S, canTransition(any, S) == false | Không quay lại trạng thái đã qua |
| REJECTED from any | Invariant | canTransition(nonTerminal, REJECTED) == true | REJECTED luôn cho phép |
