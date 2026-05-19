--------------------------------------------------------------------------------
ĐẶC TẢ YÊU CẦU: TÍNH NĂNG XEM VÀ PHÂN BỔ LEAD (LEADRB)
Dự án: SaleApp_Phase 7_Sprint 2
 Nền tảng: Mobile SaleApp

--------------------------------------------------------------------------------
1. TÍNH NĂNG XEM LEAD
Tính năng này cho phép người dùng (User) xem danh sách và chi tiết các cơ hội bán (Lead) dựa trên phân quyền dữ liệu.
1.1. Phân quyền dữ liệu (Data Visibility)
Cán bộ bán hàng (CBBH): Chỉ được phép xem dữ liệu Lead của cá nhân
.
Quản lý cấp Trưởng nhóm (Teamlead): Xem dữ liệu của chính mình và các user cấp dưới quyền
.
Quản lý Chi nhánh/Phòng ban: Xem dữ liệu của toàn bộ Chi nhánh/Phòng ban và từng cá nhân thuộc sự quản lý
.
Quản lý cấp Khối: Xem dữ liệu toàn Khối, toàn Chi nhánh/Phòng ban và từng cá nhân trong Khối
.
1.2. Màn hình Danh sách Lead
Điều kiện lấy dữ liệu: Lấy các bản ghi mà User đăng nhập là Lead Owner và được tạo trong khoảng thời gian [T-30; T] (30 ngày gần nhất)
.
Sắp xếp: Mặc định sắp xếp theo "Thời gian tạo: Mới nhất" (đồng bộ ETL từ CRM). Nếu trùng data thì hiển thị random
.
Thông tin hiển thị trên mỗi Card Lead:
Các trường chung: Trạng thái, Mã cơ hội, Tên khách hàng
.
Thông tin riêng theo từng sản phẩm (VD: Tài khoản thanh toán có "Sản phẩm đi kèm", Thẻ tín dụng có "Hạn mức đề nghị", Khoản vay có "Loại khoản vay", v.v.)
.
Trạng thái danh sách rỗng: Nếu User chưa gắn với lead nào, hiển thị thông báo và nút "Thêm cơ hội" luôn nổi ở góc dưới phải màn hình
.
1.3. Màn hình Chi tiết Lead
Logic hiển thị: FE hiển thị thông tin bằng cách gọi API sang CRM lấy dữ liệu ghi chú, điện thoại, email, lịch đã hẹn, thông tin cá nhân/quản lý
.
Các thành phần chính:
Lịch sử Lead: Hiển thị trạng thái Lead (cột trái) và người cập nhật (cột phải), sắp xếp từ gần nhất đến xa nhất
.
Thanh tiến trình trạng thái: Gồm Chưa liên hệ -> Đã liên hệ -> Đang xử lý -> Hoàn thành (hoặc KH từ chối ở bất kỳ giai đoạn nào)
.
Cập nhật trạng thái:
Nhấn drop-down để chọn trạng thái (trừ Hoàn thành và Từ chối). Hệ thống sẽ loại trừ các trạng thái đã có trước đó
.
Trạng thái "New lead": Nhấn "Gọi điện" mặc định chuyển thành "Call", bắt buộc nhập kết quả và ghi chú cuộc gọi
.
Trạng thái "Document collected": Gợi ý User upload hồ sơ và tự động mapping đẩy thông tin (Tên KH, Số GTTT, LeadID, Sản phẩm, Số tiền) sang màn hình Upload
.
Quyền thao tác:
Nếu user KHÔNG phải là Lead Owner: Disable nút thay đổi Trạng thái
.
Nếu trạng thái không nằm trong danh sách được phép cập nhật trên Saleapp: Hiển thị thông báo yêu cầu truy cập hệ thống CRM để sửa
.
Nếu User chỉ có quyền xem (không phải CBBH): Không nhấn được vào chỉnh sửa
.

--------------------------------------------------------------------------------
2. TÍNH NĂNG PHÂN BỔ LEAD
Cho phép cấp Quản lý phân bổ đều các Lead đang sở hữu cho các CBBH dưới quyền.
2.1. Điều kiện và Quy tắc (Business Rules)
Đối tượng thực hiện: Cấp Trưởng nhóm trở lên (TN, CN/PB, GĐM, GĐK..)
.
Điều kiện Lead được phép phân bổ:
Lead Owner hiện tại phải chính là User đang đăng nhập
.
Lead bắt buộc phải đang ở 1 trong 2 trạng thái: New Lead (1007) hoặc New Imported Lead (106141)
.
Giới hạn phân bổ: Tối đa chọn 10 người (Cán bộ phụ trách) trong một lần phân bổ
. Nếu chọn nhiều hơn 10 người, hiển thị popup/tooltip thông báo lỗi và disable các lựa chọn còn lại
.
2.2. Luồng thực hiện (Workflow) & Giao diện
Kích hoạt: Tại màn hình danh sách Lead, nhấn vào Icon "Bút phân bổ" (góc trên bên phải)
. Sau khi nhấn, Icon bút sẽ biến mất và hiện ra giao diện chọn Lead
.
Chọn Lead:
Hệ thống lọc lại danh sách, chỉ hiển thị các Lead thỏa mãn điều kiện phân bổ
.
Nếu không có Lead, báo: "Bạn hiện không có cơ hội nào để phân bổ"
.
User tick chọn các Lead cần phân bổ (có nút "Chọn tất cả" / "Bỏ chọn tất cả")
.
Mở danh sách Cán bộ:
Nút "Phân bổ" (góc dưới phải) sáng lên khi chọn ít nhất 1 Lead
.
Nhấn "Phân bổ" -> Bật Popup danh sách "Cán bộ phụ trách" (Gồm Tên, Username, Số miscode)
.
Chọn Cán bộ & Xác nhận:
User chọn từ 1 đến tối đa 10 CBPT
.
Nếu chọn >= 2 cán bộ, hiển thị dòng cảnh báo: "Bạn đang chọn nhiều hơn 01 cán bộ, tất cả cơ hội sẽ được chia đều cho những cán bộ đã chọn"
.
Nhấn nút "Phân bổ cơ hội" để hoàn tất
. Hệ thống gọi API cập nhật LeadOwner trên CRM
.
2.3. Thuật toán Phân bổ (Allocation Logic)
Chia hết: Nếu số lượng Lead chia đều được cho số lượng User đã chọn -> Hệ thống thực hiện phân bổ đều và random cho tất cả các User
.
Không chia hết (Có số dư): Hệ thống thực hiện chia đều phần nguyên. Phần dư sẽ được phân bổ lần lượt cho các User từ trên xuống dưới theo thứ tự bảng chữ cái (Alphabet)
.