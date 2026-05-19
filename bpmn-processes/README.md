# BPMN Processes - CRM Lead Management POC

## Tổng quan

Thư mục này chứa các BPMN process definitions cho Camunda 8 Zeebe.

## Processes

### lead-lifecycle.bpmn
Luồng chính quản lý lifecycle của Lead:
- **Start**: Lead được tạo/import
- **User Task - Liên hệ KH**: Cán bộ liên hệ khách hàng (form: lead-contact-form)
- **Gateway**: Phân nhánh theo kết quả liên hệ
- **User Task - Xử lý**: Xử lý cơ hội (form: lead-process-form)
- **User Task - Thu thập hồ sơ**: Thu thập giấy tờ (form: lead-document-form)
- **Service Task - Cập nhật trạng thái**: Job type: lead-status-change
- **End**: Hoàn thành hoặc KH từ chối

## Deploy

### Yêu cầu
- Camunda Zeebe đang chạy (port 26500)
- `zbctl` CLI tool đã cài đặt

### Cách deploy
```bash
cd scripts
chmod +x deploy.sh
./deploy.sh localhost:26500
```

### Hot Deploy (thay đổi workflow)
Để thay đổi workflow mà không cần restart:
1. Sửa file .bpmn
2. Chạy lại `deploy.sh`
3. Process instances mới sẽ dùng version mới
4. Frontend tự động nhận form mới (query realtime)

## Process Variables

| Variable | Type | Mô tả |
|---|---|---|
| leadId | String | ID của Lead |
| ownerId | String | ID Lead Owner |
| status | String | Trạng thái hiện tại |
| productType | String | Loại sản phẩm |
| contactResult | String | Kết quả liên hệ (INTERESTED/REJECTED) |
| newStatus | String | Trạng thái mới (cho service task) |
| changedBy | String | Người thay đổi |
| note | String | Ghi chú |
