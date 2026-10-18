# GHN thử nghiệm

Mở **Quản lý vận chuyển → Thử tính phí GHN** bằng tài khoản quản trị viên.
Phần này đọc danh mục địa chỉ và gọi API tính phí staging. Chưa tạo vận đơn,
chưa gửi hàng thật, chưa tự cộng phí vào thanh toán online.

## Cấu hình trên máy

Tạo tài khoản GHN thử nghiệm, lấy Token và Shop ID của cùng môi trường thử nghiệm.
Trong cấu hình chạy ứng dụng của IntelliJ, thêm các biến môi trường:

```text
GHN_ENABLED=true
GHN_TOKEN=token-thu-nghiem-cua-ban
GHN_SHOP_ID=shop-id-thu-nghiem-cua-ban
```

Không nhập chữ `shop-id-thu-nghiem-cua-ban`: thay bằng số Shop ID GHN cấp.
Khởi động lại ứng dụng. Không lưu token thật trong mã nguồn hoặc đưa lên Git.
Token/Shop ID production không dùng cho phần thử nghiệm này.

Mặc định địa chỉ gửi lấy theo hồ sơ shop GHN. Nếu cần chỉ định địa chỉ gửi:

```text
GHN_FROM_DISTRICT_ID=ma-huyen-ghn-cua-shop
GHN_FROM_WARD_CODE=ma-xa-ghn-cua-shop
```

Mã địa chỉ GHN phải lấy từ danh mục GHN, không dùng mã từ danh mục địa chỉ nội bộ.
Địa chỉ này phục vụ thử API; không thay địa chỉ khách đã lưu.

## Cách kiểm tra

1. Chọn tỉnh/thành phố, quận/huyện và phường/xã GHN.
2. Nhập khối lượng gram, kích thước cm, giá trị khai báo.
3. Bấm **Tính phí thử nghiệm** để nhận phí từ GHN.
4. Khi lỗi cấu hình hoặc mạng, màn hình báo lỗi; không tự coi phí là 0 đồng.

Chưa có thông tin GHN thì ứng dụng vẫn khởi động và xem được danh sách vận chuyển,
thống kê. Chưa thể xác nhận kết nối GHN thành công cho đến khi điền thông tin và gọi thử.

Endpoint cố định: `https://dev-online-gateway.ghn.vn/shiip/public-api`.
Các API được dùng: `master-data/province`, `master-data/district`,
`master-data/ward`, `v2/shipping-order/fee`.

Tài liệu: https://developer.ghn.vn/vi/docs/order/calculate-fee

## Dữ liệu trên màn hình quản lý

- Danh sách vận chuyển đọc `hoa_don`, chỉ lấy đơn online `loai_don IN (1,2)`.
- Phí đã lưu đọc `phi_van_chuyen`; phí thử GHN không sửa hóa đơn hay mã QR đã tạo.
- Thao tác xác nhận, hủy đơn dùng luồng hóa đơn hiện có.
- Thống kê lọc theo `ngay_tao`, chỉ tính doanh thu trạng thái 5 từ `tong_tien_sau_giam`.
- Bảng sản phẩm bán chạy chỉ lấy chi tiết hóa đơn trạng thái 5, không tính đơn chờ/hủy.
- Hai trang và các API GHN chỉ dành cho quản trị viên, kiểm tra quyền ở máy chủ.
- Không cần thêm bảng hoặc sửa schema database cho các chức năng trên.
