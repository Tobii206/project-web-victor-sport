# Trả hàng và hoàn tiền

Tham khảo luồng trong DATN_SU26, triển khai bằng JSP trong VictorSport.

## Cách sử dụng

- Khách đã đăng nhập: Đơn hàng của tôi → chi tiết đơn đã nhận → Trả hàng / hoàn tiền.
- Khách vãng lai: tra cứu đơn để xác minh phiên truy cập, mở chi tiết đơn rồi tạo yêu cầu.
- Quản trị viên và nhân viên có ORDER_WRITE: menu Trả hàng & hoàn tiền để lọc, tìm kiếm và xử lý; mỗi trang 10 yêu cầu. Có thể tạo hộ từ chi tiết hóa đơn đã hoàn tất, bao gồm đơn tại quầy.
- Đơn mua trực tiếp tại quầy trả tại cửa hàng. Đơn online hoặc tại quầy có giao hàng được chọn gửi hàng về hoặc mang đến cửa hàng.

## Luồng xử lý

Chờ xác nhận → Đã duyệt → Đang gửi hàng về (nếu gửi qua vận chuyển) → Cửa hàng đã nhận hàng → Đang kiểm tra → Đã duyệt hoàn tiền → Đã hoàn tiền.

- Khách được hủy trước khi đã gửi hàng hoặc cửa hàng đã nhận hàng.
- Người xử lý được từ chối khi xét yêu cầu hoặc khi kiểm tra, bắt buộc ghi lý do ít nhất 10 ký tự.
- Khi gửi hàng, khách nhập đơn vị vận chuyển và mã vận đơn; đây là ghi nhận thủ công, chưa tạo vận đơn hoàn trên GHN.
- Khi duyệt tiếp nhận, tick từng sản phẩm được chấp nhận trả (có chọn tất cả). Bỏ chọn sản phẩm phải ghi lý do ít nhất 10 ký tự. Tiền hoàn cập nhật theo các dòng được duyệt; giữ lại dòng bị từ chối để khách xem kết quả.
- Khi kiểm tra, có thể bỏ thêm sản phẩm không đạt điều kiện hoàn tiền, không được duyệt lại dòng đã bị loại lúc tiếp nhận. Chỉ nhập số lượng đạt cho các dòng được tick. Có ô nhập số lượng hàng lỗi; nhập một ô sẽ tự tính ô còn lại. Server bắt buộc số lượng đạt + lỗi bằng số lượng trả của dòng được duyệt. Không được bỏ qua bước nhận hàng và kiểm tra.
- Bước cuối chỉ **ghi nhận** tiền mặt đã trả hoặc giao dịch ngân hàng đã thực hiện. Phải nhập đúng số tiền đã duyệt; chuyển khoản bắt buộc có mã giao dịch. Hệ thống chưa tự chuyển tiền qua ngân hàng/VNPAY.

## Quy tắc tiền và kho

- Tạo yêu cầu trong 15 ngày từ lần đầu nhận hàng (đơn tại quầy dùng ngày thanh toán khi thiếu lịch sử nhận hàng), đơn phải hoàn tất và đã thu đủ.
- Được trả một phần hoặc toàn bộ số lượng còn lại. Mỗi đơn chỉ có một yêu cầu đang xử lý; sau khi hoàn tất có thể tạo yêu cầu mới cho phần còn lại nếu còn hạn.
- Tiền hoàn dựa trên tiền hàng thực trả sau giảm giá; không hoàn phí vận chuyển. Phân bổ theo giá trị dòng, làm tròn lũy kế đến đồng nguyên để nhiều lần trả vẫn khớp tổng ngân sách. Không hoàn lại lượt voucher.
- Chỉ khi ghi nhận hoàn tiền thành công mới cộng số lượng đạt vào kho. Không tự bật bán lại sản phẩm ngừng bán. Hàng lỗi được ghi riêng, không cộng tồn bán.
- Giữ nguyên trạng thái giao hàng và giao dịch thanh toán gốc của hóa đơn; lưu riêng tiền hoàn, số lượng trả và chứng từ. Doanh thu thống kê trừ tiền đã hoàn, số đôi bán trừ số lượng đã trả, theo bộ lọc ngày tạo đơn hiện có.
- Khóa hóa đơn và yêu cầu trong transaction, chặn quá số lượng/quá tiền, mã tạo trùng và chứng từ hoàn trùng. Ghi nhận lặp cùng yêu cầu đã hoàn không cộng kho lần hai.

## Quyền truy cập và dữ liệu

- Kiểm tra chủ đơn hoặc phiên khách vãng lai đã xác minh. Quyền xử lý kiểm tra lại tài khoản, vai trò và quyền ORDER_WRITE đang hoạt động trong database (quản trị viên vẫn được phép).
- Tất cả thao tác ghi có CSRF token. Ảnh minh chứng được kiểm tra định dạng thật JPG/PNG, 1–5 ảnh, tối đa 5 MB/ảnh và 16 triệu điểm ảnh; ảnh được chuyển PNG bỏ metadata, lưu database, tải qua route kiểm tra quyền.
- `src/main/resources/db/tra-hang.sql` thêm bảng/cột nếu chưa có; `TraHangConfig` chạy khi khởi động. Không xóa dữ liệu cũ.
- Đã áp dụng cấu trúc vào database phát triển. Các đơn và thay đổi kho trong kiểm thử SQL Server đều rollback.

## Kiểm thử

`TraHangTests`: phân bổ tiền, quyền, điều kiện, số lượng, ảnh, chuyển trạng thái, kiểm tra kho, chống ghi nhận trùng, đơn tại quầy, CSRF và khách vãng lai.

`TraHangSqlServerTests`: kiểm tra schema lặp được; chạy luồng trả từng phần trên SQL Server, tiền lẻ, hoàn trùng, hàng lỗi và thống kê. Chỉ chạy khi `VICTORSPORT_SQL_TEST=true`; dữ liệu nghiệp vụ thử nghiệm rollback.

`JspViewsTests`: biên dịch JSP và render dữ liệu mẫu cho form, chi tiết khách, chi tiết quản trị và danh sách. Đã xem bố cục trên desktop và điện thoại.

Lịch sử đơn hàng ghép đầy đủ các mốc từ lịch sử trả hàng theo thời gian, kèm người thực hiện và liên kết về yêu cầu. Áp dụng cả dữ liệu cũ; chỉ giữ một mốc hoàn tiền khi cùng sự kiện đã có ở hai nơi.

Tiền hoàn VND được phân bổ theo đồng nguyên, phần chênh lệch làm tròn chuyển sang dòng/lần trả sau để tổng vẫn khớp ngân sách. Yêu cầu cũ chưa hoàn được hiển thị dự kiến theo đồng nguyên và lưu chuẩn hóa trong thao tác xử lý; không viết dữ liệu khi xem và không sửa giao dịch đã hoàn.
