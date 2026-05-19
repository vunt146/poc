# Hướng dẫn Unit Test - CRM Lead Management POC

## Chạy toàn bộ tests

```bash
cd domain-service
mvn test
```

## Chạy riêng PBT Tests

```bash
# Chạy tất cả Property-Based Tests
mvn test -Dtest="*PropertyTest"

# Chạy riêng Allocation PBT
mvn test -Dtest="LeadAllocationPropertyTest"

# Chạy riêng State Transition PBT
mvn test -Dtest="LeadStateTransitionPropertyTest"
```

## PBT Configuration

### Seed Logging (PBT-08 compliance)
jqwik tự động log seed khi test fail. Để reproduce:
```bash
# Chạy với seed cụ thể
mvn test -Dtest="LeadAllocationPropertyTest" -Djqwik.seed=12345
```

### Số lần thử (tries)
- Allocation tests: 200 tries (đủ để cover edge cases)
- State transition tests: 100 tries

## Test Coverage

### Property-Based Tests (jqwik)

| Test Class | Properties | Mô tả |
|---|---|---|
| LeadAllocationPropertyTest | 5 properties | Allocation algorithm invariants |
| LeadStateTransitionPropertyTest | 5 properties | State machine invariants |

### Allocation Properties
1. **sizePreservation**: Tổng leads phân bổ = tổng đầu vào
2. **noLeadDuplication**: Không có lead trùng lặp
3. **fairDistribution**: Chênh lệch max-min <= 1
4. **alphabetPriority**: Users đầu alphabet nhận phần dư
5. **allUsersReceiveLeads**: Tất cả users có trong kết quả

### State Transition Properties
1. **terminalStatesCannotTransition**: COMPLETED/REJECTED không chuyển tiếp
2. **rejectedIsAlwaysReachableFromNonTerminal**: REJECTED luôn cho phép
3. **visitedStatusCannotBeRevisited**: Không quay lại trạng thái đã qua
4. **historyGrowsMonotonically**: History luôn tăng sau mỗi transition
5. **allowedTransitionsAreSubsetOfAllStatuses**: Không tự transition về chính mình

## Kết quả mong đợi

```
[INFO] Tests run: 10, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

## PBT-08 Compliance Checklist

- [x] Shrinking enabled (jqwik default)
- [x] Seed logged on failure (jqwik default)
- [x] Reproducible via seed parameter
- [x] Included in standard `mvn test` run
- [x] No PBT excluded from test suite
