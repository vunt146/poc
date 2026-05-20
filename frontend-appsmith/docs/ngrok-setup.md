# Ngrok Setup Guide - CRM Lead Management POC

## Tổng quan

Ngrok tạo tunnel từ localhost ra internet, cho phép Appsmith Cloud kết nối tới Domain Service đang chạy trên máy local.

```
Appsmith Cloud (internet) → ngrok public URL → localhost:8090 (Domain Service)
```

---

## 1. Cài đặt ngrok

### macOS (Homebrew)
```bash
brew install ngrok
```

### macOS (Download trực tiếp)
```bash
# Download từ https://ngrok.com/download
# Giải nén và di chuyển vào PATH
unzip ngrok-v3-stable-darwin-amd64.zip
sudo mv ngrok /usr/local/bin/
```

### Verify installation
```bash
ngrok version
```

---

## 2. Đăng ký tài khoản ngrok (bắt buộc)

1. Truy cập https://dashboard.ngrok.com/signup
2. Đăng ký tài khoản miễn phí
3. Lấy authtoken từ https://dashboard.ngrok.com/get-started/your-authtoken
4. Cấu hình authtoken:

```bash
ngrok config add-authtoken YOUR_AUTH_TOKEN
```

---

## 3. Khởi động tunnel

### Đảm bảo Domain Service đang chạy
```bash
# Verify Domain Service
curl http://localhost:8090/api/leads
```

### Start ngrok tunnel
```bash
ngrok http 8090
```

### Output mẫu
```
Session Status                online
Account                       your-email@example.com (Plan: Free)
Version                       3.x.x
Region                        Asia Pacific (ap)
Forwarding                    https://xxxx-xxx-xxx.ngrok-free.app → http://localhost:8090

Connections                   ttl     opn     rt1     rt5     p50     p90
                              0       0       0.00    0.00    0.00    0.00
```

**Ghi lại URL**: `https://xxxx-xxx-xxx.ngrok-free.app` - đây là URL sẽ dùng cho Appsmith datasource.

---

## 4. Test tunnel

```bash
# Từ terminal khác, test qua ngrok URL
curl https://xxxx-xxx-xxx.ngrok-free.app/api/leads
```

Nếu nhận được JSON response với danh sách leads → tunnel hoạt động.

---

## 5. Lưu ý quan trọng

### Free tier limitations
| Giới hạn | Giá trị |
|---|---|
| Session duration | Không giới hạn (đã thay đổi từ 2024) |
| Bandwidth | 1GB/month |
| Connections | 40/minute |
| URL | Thay đổi mỗi lần restart ngrok |

### Khi URL thay đổi
Mỗi lần restart ngrok, URL mới sẽ được tạo. Bạn cần:
1. Copy URL mới từ ngrok terminal
2. Cập nhật Datasource URL trên Appsmith Cloud

### ngrok-skip-browser-warning
Appsmith Cloud gọi API qua server-side, nên KHÔNG bị ảnh hưởng bởi ngrok browser warning page. Tuy nhiên, nếu gặp vấn đề, thêm header:
```
ngrok-skip-browser-warning: true
```

---

## 6. Tips cho development ổn định

### Sử dụng ngrok config file (optional)
```bash
# Xem config file location
ngrok config check
```

Thêm vào config file (`~/.config/ngrok/ngrok.yml`):
```yaml
version: "2"
authtoken: YOUR_AUTH_TOKEN
tunnels:
  crm-domain-service:
    addr: 8090
    proto: http
```

Sau đó start bằng:
```bash
ngrok start crm-domain-service
```

### Giữ ngrok chạy liên tục
```bash
# Chạy trong background (nohup)
nohup ngrok http 8090 &

# Hoặc dùng screen/tmux
tmux new -s ngrok
ngrok http 8090
# Ctrl+B, D để detach
```

---

## 7. Troubleshooting

| Vấn đề | Giải pháp |
|---|---|
| "ERR_NGROK_108" | Authtoken chưa cấu hình → `ngrok config add-authtoken` |
| Connection refused | Domain Service chưa chạy → start Spring Boot trước |
| 502 Bad Gateway | Domain Service đang khởi động → đợi 10-15s |
| Bandwidth exceeded | Free tier 1GB/month → restart tunnel hoặc upgrade |
| URL changed | Cập nhật Datasource URL trên Appsmith Cloud |
