USE DATN_SevenStrike;
GO

SET NOCOUNT ON;
GO

INSERT INTO dbo.xuat_xu (ten_xuat_xu) VALUES
(N'Mỹ'),
(N'Đức'),
(N'Nhật Bản'),
(N'Việt Nam'),
(N'Indonesia');

INSERT INTO dbo.thuong_hieu (ten_thuong_hieu) VALUES
(N'Nike'),
(N'Adidas'),
(N'Puma'),
(N'Mizuno'),
(N'Asics'),
(N'New Balance');

INSERT INTO dbo.mau_sac (ten_mau_sac, ma_mau_hex) VALUES
(N'Trắng', '#FFFFFF'),
(N'Đen', '#000000'),
(N'Đỏ', '#D71920'),
(N'Xanh dương', '#1D4ED8'),
(N'Xám', '#808080'),
(N'Vàng neon', '#D9F99D');

INSERT INTO dbo.kich_thuoc (ten_kich_thuoc, gia_tri_kich_thuoc) VALUES
(N'Size 38', 38.0),
(N'Size 39', 39.0),
(N'Size 40', 40.0),
(N'Size 41', 41.0),
(N'Size 42', 42.0),
(N'Size 43', 43.0),
(N'Size 44', 44.0);

INSERT INTO dbo.vi_tri_thi_dau (ten_vi_tri) VALUES
(N'Tiền đạo'),
(N'Tiền vệ'),
(N'Hậu vệ'),
(N'Thủ môn'),
(N'Chạy bộ'),
(N'Tập luyện đa năng');

INSERT INTO dbo.phong_cach_choi (ten_phong_cach) VALUES
(N'Tốc độ'),
(N'Kiểm soát bóng'),
(N'Sút bóng mạnh'),
(N'Êm chân đường dài'),
(N'Linh hoạt'),
(N'Thời trang thể thao');

INSERT INTO dbo.co_giay (ten_co_giay) VALUES
(N'Cổ thấp'),
(N'Cổ lửng'),
(N'Cổ cao');

INSERT INTO dbo.form_chan (ten_form_chan) VALUES
(N'Ôm chân'),
(N'Tiêu chuẩn'),
(N'Rộng chân');

INSERT INTO dbo.chat_lieu (ten_chat_lieu) VALUES
(N'Da tổng hợp'),
(N'Vải knit'),
(N'Vải mesh'),
(N'Da kangaroo'),
(N'Canvas'),
(N'Primeknit');

INSERT INTO dbo.quyen_han (ten_quyen_han) VALUES
(N'Quản trị viên'),
(N'Nhân viên bán hàng'),
(N'Nhân viên kho');

INSERT INTO dbo.nhan_vien (
    id_quyen_han, ten_nhan_vien, ten_tai_khoan, mat_khau, email, so_dien_thoai,
    ngay_sinh, thanh_pho, quan, phuong, dia_chi_cu_the
) VALUES
(1, N'Nguyễn Minh Quân', 'admin', '123456', 'admin@sevenstrike.vn', '0900000001', '1998-02-12', N'Hà Nội', N'Cầu Giấy', N'Dịch Vọng', N'12 Xuân Thủy'),
(2, N'Lê Thanh Huyền', 'banhang01', '123456', 'huyen.lt@sevenstrike.vn', '0900000002', '2000-07-21', N'Hà Nội', N'Nam Từ Liêm', N'Mỹ Đình 1', N'25 Lê Đức Thọ'),
(3, N'Trần Đức Anh', 'kho01', '123456', 'anh.td@sevenstrike.vn', '0900000003', '1999-11-04', N'Hà Nội', N'Thanh Xuân', N'Nhân Chính', N'88 Nguyễn Trãi');

INSERT INTO dbo.khach_hang (
    ten_khach_hang, ten_tai_khoan, mat_khau, email, so_dien_thoai,
    gioi_tinh, ngay_sinh, anh_dai_dien
) VALUES
(N'Nguyễn Văn An', 'khach01', '123456', 'khach01@gmail.com', '0900000004', 1, '2003-05-10', 'images/customers/khach01.jpg'),
(N'Trần Thị Bình', 'khach02', '123456', 'khach02@gmail.com', '0900000005', 0, '2004-08-15', 'images/customers/khach02.jpg'),
(N'Lê Minh Hoàng', 'khach03', '123456', 'khach03@gmail.com', '0900000006', 1, '2002-11-20', 'images/customers/khach03.jpg'),
(N'Phạm Thu Hà', 'khach04', '123456', 'khach04@gmail.com', '0900000007', 0, '2003-03-08', 'images/customers/khach04.jpg'),
(N'Đỗ Quốc Huy', 'khach05', '123456', 'khach05@gmail.com', '0900000008', 1, '2001-12-25', 'images/customers/khach05.jpg');

INSERT INTO dbo.dia_chi_khach_hang (
    id_khach_hang, ten_dia_chi, thanh_pho, quan, phuong, dia_chi_cu_the, mac_dinh
) VALUES
(1, N'Nhà riêng', N'Hà Nội', N'Cầu Giấy', N'Dịch Vọng', N'Số 10 ngõ 68 Trần Thái Tông', 1),
(2, N'Ký túc xá', N'Hà Nội', N'Thanh Xuân', N'Thanh Xuân Bắc', N'Phòng 501 nhà B', 1),
(3, N'Nhà riêng', N'Hải Phòng', N'Lê Chân', N'Dư Hàng Kênh', N'22 Nguyễn Văn Linh', 1),
(4, N'Công ty', N'Đà Nẵng', N'Hải Châu', N'Bình Hiên', N'15 Nguyễn Văn Linh', 1),
(5, N'Căn hộ', N'Hồ Chí Minh', N'Quận 1', N'Bến Nghé', N'68 Lê Lợi', 1);

INSERT INTO dbo.san_pham (
    id_thuong_hieu, id_xuat_xu, id_vi_tri_thi_dau, id_phong_cach_choi,
    id_co_giay, id_chat_lieu, ten_san_pham, mo_ta_ngan, mo_ta_chi_tiet
) VALUES
(1, 1, 1, 1, 1, 1, N'Nike Mercurial Vapor 15 Academy TF', N'Giày đá bóng sân cỏ nhân tạo thiên về tốc độ.', N'Upper tổng hợp mỏng, đế TF bám sân tốt, phù hợp tiền đạo và cầu thủ chạy cánh.'),
(2, 2, 2, 2, 1, 6, N'Adidas Predator Accuracy.3 TF', N'Giày kiểm soát bóng cho sân cỏ nhân tạo.', N'Bề mặt vân nổi hỗ trợ chạm bóng, form ôm chân, phù hợp tiền vệ.'),
(3, 2, 2, 5, 2, 2, N'Puma Future Match TT', N'Giày linh hoạt cho lối chơi biến hóa.', N'Thiết kế cổ lửng, chất liệu knit co giãn, hỗ trợ xoay trở nhanh.'),
(4, 3, 2, 2, 1, 4, N'Mizuno Morelia Neo IV Pro AS', N'Giày da mềm, cảm giác bóng tốt.', N'Da kangaroo cao cấp, trọng lượng nhẹ, phù hợp cầu thủ thích kiểm soát bóng.'),
(5, 3, 5, 4, 1, 3, N'Asics Gel-Kayano 30', N'Giày chạy bộ ổn định cho đường dài.', N'Công nghệ GEL giảm chấn, đệm êm, hỗ trợ người chạy cần độ ổn định cao.'),
(1, 1, 5, 4, 1, 3, N'Nike Air Zoom Pegasus 40', N'Giày chạy bộ hằng ngày nhẹ và bền.', N'Đệm Zoom Air đàn hồi, upper mesh thoáng khí, phù hợp chạy road.'),
(6, 5, 6, 6, 1, 5, N'New Balance 550 White Green', N'Sneaker thể thao phong cách retro.', N'Thiết kế lấy cảm hứng bóng rổ cổ điển, dễ phối đồ, đi học và đi chơi.'),
(2, 2, 6, 6, 1, 1, N'Adidas Samba OG Black White', N'Sneaker thể thao cổ điển.', N'Form thấp, đế gum, phong cách lifestyle nhưng vẫn giữ tinh thần thể thao.');

INSERT INTO dbo.chi_tiet_san_pham (
    id_san_pham, id_mau_sac, id_kich_thuoc, id_form_chan, so_luong, gia_niem_yet, gia_ban, ghi_chu
) VALUES
(1, 6, 3, 1, 18, 2190000, 1990000, N'Vàng neon size 40'),
(1, 2, 4, 1, 15, 2190000, 1990000, N'Đen size 41'),
(2, 2, 4, 2, 12, 2390000, 2190000, N'Đen size 41'),
(2, 1, 5, 2, 10, 2390000, 2190000, N'Trắng size 42'),
(3, 3, 3, 1, 11, 1990000, 1790000, N'Đỏ size 40'),
(3, 4, 4, 1, 9, 1990000, 1790000, N'Xanh size 41'),
(4, 1, 3, 2, 8, 3290000, 2990000, N'Trắng size 40'),
(4, 2, 5, 2, 6, 3290000, 2990000, N'Đen size 42'),
(5, 5, 4, 3, 14, 4390000, 3990000, N'Xám size 41'),
(6, 4, 4, 2, 20, 3490000, 3190000, N'Xanh size 41'),
(7, 1, 3, 2, 16, 2890000, 2590000, N'Trắng size 40'),
(8, 2, 2, 2, 13, 2790000, 2490000, N'Đen size 39');

INSERT INTO dbo.anh_chi_tiet_san_pham (id_chi_tiet_san_pham, duong_dan_anh, la_anh_dai_dien, mo_ta) VALUES
(1, 'images/products/nike-mercurial-vapor-15-tf-neon.jpg', 1, N'Nike Mercurial Vapor 15 Academy TF vàng neon'),
(2, 'images/products/nike-mercurial-vapor-15-tf-black.jpg', 1, N'Nike Mercurial Vapor 15 Academy TF đen'),
(3, 'images/products/adidas-predator-accuracy-tf-black.jpg', 1, N'Adidas Predator Accuracy.3 TF đen'),
(4, 'images/products/adidas-predator-accuracy-tf-white.jpg', 1, N'Adidas Predator Accuracy.3 TF trắng'),
(5, 'images/products/puma-future-match-tt-red.jpg', 1, N'Puma Future Match TT đỏ'),
(6, 'images/products/puma-future-match-tt-blue.jpg', 1, N'Puma Future Match TT xanh'),
(7, 'images/products/mizuno-morelia-neo-iv-white.jpg', 1, N'Mizuno Morelia Neo IV Pro AS trắng'),
(8, 'images/products/mizuno-morelia-neo-iv-black.jpg', 1, N'Mizuno Morelia Neo IV Pro AS đen'),
(9, 'images/products/asics-gel-kayano-30-grey.jpg', 1, N'Asics Gel-Kayano 30 xám'),
(10, 'images/products/nike-pegasus-40-blue.jpg', 1, N'Nike Air Zoom Pegasus 40 xanh'),
(11, 'images/products/new-balance-550-white-green.jpg', 1, N'New Balance 550 White Green'),
(12, 'images/products/adidas-samba-og-black-white.jpg', 1, N'Adidas Samba OG Black White');

INSERT INTO dbo.gio_hang (id_khach_hang) VALUES
(1),
(2),
(3),
(4),
(5);

INSERT INTO dbo.chi_tiet_gio_hang (id_gio_hang, id_chi_tiet_san_pham, so_luong, don_gia) VALUES
(1, 1, 1, 1990000),
(1, 3, 1, 2190000),
(2, 5, 2, 1790000),
(3, 7, 1, 2990000),
(4, 9, 1, 3990000),
(5, 11, 1, 2590000);

INSERT INTO dbo.dot_giam_gia (
    ten_dot_giam_gia, loai_giam_gia, gia_tri_giam_gia, ngay_bat_dau, ngay_ket_thuc, muc_uu_tien
) VALUES
(N'Sale khai trương SevenStrike', 0, 10, '2026-01-01', '2026-12-31', 1),
(N'Sale giày chạy bộ cuối tuần', 0, 8, '2026-09-01', '2026-10-31', 2);

INSERT INTO dbo.chi_tiet_dot_giam_gia (
    id_dot_giam_gia, id_chi_tiet_san_pham, so_luong_ap_dung, gia_tri_giam_rieng, ghi_chu
) VALUES
(1, 1, 50, NULL, N'Áp dụng Mercurial vàng neon'),
(1, 3, 50, NULL, N'Áp dụng Predator đen'),
(1, 5, 40, NULL, N'Áp dụng Puma đỏ'),
(2, 9, 30, NULL, N'Áp dụng Asics Gel-Kayano'),
(2, 10, 30, NULL, N'Áp dụng Nike Pegasus');

INSERT INTO dbo.phieu_giam_gia (
    ten_phieu_giam_gia, loai_phieu_giam_gia, gia_tri_giam_gia, so_tien_giam_toi_da,
    hoa_don_toi_thieu, so_luong_su_dung, ngay_bat_dau, ngay_ket_thuc, mo_ta
) VALUES
(N'Giảm 10% đơn từ 1 triệu', 0, 10, 300000, 1000000, 100, '2026-01-01', '2026-12-31', N'Voucher toàn shop'),
(N'Giảm 200K đơn từ 2 triệu', 1, 200000, 200000, 2000000, 50, '2026-01-01', '2026-12-31', N'Voucher toàn shop'),
(N'Freeship nội thành', 1, 30000, 30000, 500000, 200, '2026-01-01', '2026-12-31', N'Hỗ trợ phí vận chuyển');

INSERT INTO dbo.hoa_don (
    id_khach_hang, id_nhan_vien, id_phieu_giam_gia,
    loai_don, phi_van_chuyen, tong_tien, tong_tien_sau_giam,
    ten_khach_hang, dia_chi_khach_hang, so_dien_thoai_khach_hang, email_khach_hang,
    trang_thai_hien_tai, ngay_thanh_toan, ghi_chu
) VALUES
(1, 2, 2, 2, 30000, 3980000, 3780000, N'Nguyễn Văn An', N'Số 10 ngõ 68 Trần Thái Tông, Cầu Giấy, Hà Nội', '0900000004', 'khach01@gmail.com', 5, '2026-09-20 10:30:00', N'Mua 2 đôi đá bóng'),
(2, 2, 3, 2, 30000, 2190000, 2160000, N'Trần Thị Bình', N'Phòng 501 nhà B, Thanh Xuân Bắc, Hà Nội', '0900000005', 'khach02@gmail.com', 4, '2026-09-21 14:05:00', N'Giao giờ hành chính'),
(3, 2, NULL, 0, 0, 2990000, 2990000, N'Lê Minh Hoàng', N'22 Nguyễn Văn Linh, Lê Chân, Hải Phòng', '0900000006', 'khach03@gmail.com', 5, '2026-09-22 19:10:00', N'Mua tại quầy'),
(4, 2, 2, 2, 30000, 3990000, 3790000, N'Phạm Thu Hà', N'15 Nguyễn Văn Linh, Hải Châu, Đà Nẵng', '0900000007', 'khach04@gmail.com', 2, NULL, N'Chờ xác nhận thanh toán'),
(5, 2, 1, 1, 30000, 2590000, 2331000, N'Đỗ Quốc Huy', N'68 Lê Lợi, Quận 1, Hồ Chí Minh', '0900000008', 'khach05@gmail.com', 3, '2026-09-23 08:20:00', N'Đơn online sneaker');

INSERT INTO dbo.hoa_don_chi_tiet (id_hoa_don, id_chi_tiet_san_pham, so_luong, don_gia, ghi_chu) VALUES
(1, 1, 1, 1990000, N'Nike Mercurial Vapor 15 vàng neon size 40'),
(1, 2, 1, 1990000, N'Nike Mercurial Vapor 15 đen size 41'),
(2, 3, 1, 2190000, N'Adidas Predator Accuracy.3 đen size 41'),
(3, 7, 1, 2990000, N'Mizuno Morelia Neo IV trắng size 40'),
(4, 9, 1, 3990000, N'Asics Gel-Kayano 30 xám size 41'),
(5, 11, 1, 2590000, N'New Balance 550 White Green size 40');

INSERT INTO dbo.phuong_thuc_thanh_toan (ten_phuong_thuc_thanh_toan, nha_cung_cap) VALUES
(N'Tiền mặt', N'POS'),
(N'Chuyển khoản ngân hàng', N'VietQR'),
(N'Ví điện tử', N'MoMo'),
(N'Thanh toán khi nhận hàng', N'COD');

INSERT INTO dbo.giao_dich_thanh_toan (
    id_hoa_don, id_phuong_thuc_thanh_toan, so_tien, trang_thai,
    ma_yeu_cau, ma_giao_dich_ngoai, ma_tham_chieu, thoi_gian_tao, nguoi_cap_nhat, ghi_chu
) VALUES
(1, 2, 3780000, N'thanh_cong', 'REQ000001', 'BANK202609200001', 'HD00001', '2026-09-20 10:30:00', 2, N'Đã nhận chuyển khoản'),
(2, 4, 2160000, N'cho_thanh_toan', 'REQ000002', NULL, 'HD00002', '2026-09-21 14:05:00', 2, N'COD'),
(3, 1, 2990000, N'thanh_cong', 'REQ000003', 'CASH202609220001', 'HD00003', '2026-09-22 19:10:00', 2, N'Thanh toán tại quầy'),
(4, 3, 3790000, N'khoi_tao', 'REQ000004', NULL, 'HD00004', '2026-09-22 21:30:00', 2, N'Chờ ví điện tử phản hồi'),
(5, 2, 2331000, N'thanh_cong', 'REQ000005', 'BANK202609230001', 'HD00005', '2026-09-23 08:20:00', 2, N'Đã nhận chuyển khoản');

INSERT INTO dbo.lich_su_hoa_don (
    id_hoa_don, trang_thai, thoi_gian, ghi_chu, nguoi_cap_nhat, nguoi_thuc_hien, loai_nguoi_thuc_hien
) VALUES
(1, 1, '2026-09-20 10:00:00', N'Tạo đơn online', 2, 1, 'KHACH_HANG'),
(1, 5, '2026-09-21 16:40:00', N'Đã giao thành công', 2, 2, 'NHAN_VIEN'),
(2, 1, '2026-09-21 14:05:00', N'Tạo đơn COD', 2, 2, 'NHAN_VIEN'),
(2, 4, '2026-09-22 09:15:00', N'Đang giao hàng', 2, 2, 'NHAN_VIEN'),
(3, 5, '2026-09-22 19:15:00', N'Hoàn tất tại quầy', 2, 2, 'NHAN_VIEN'),
(4, 2, '2026-09-22 21:35:00', N'Đã xác nhận đơn', 2, 2, 'NHAN_VIEN'),
(5, 3, '2026-09-23 09:00:00', N'Đang chuẩn bị hàng', 2, 2, 'NHAN_VIEN');

INSERT INTO dbo.chuc_nang (ma_chuc_nang, ten_chuc_nang, mo_ta) VALUES
('PRODUCT_READ', N'Xem sản phẩm', N'Cho phép xem danh sách và chi tiết sản phẩm'),
('PRODUCT_WRITE', N'Quản lý sản phẩm', N'Thêm, sửa, xóa mềm sản phẩm'),
('ORDER_READ', N'Xem hóa đơn', N'Cho phép xem hóa đơn'),
('ORDER_WRITE', N'Quản lý hóa đơn', N'Cập nhật trạng thái và thanh toán hóa đơn'),
('CUSTOMER_READ', N'Xem khách hàng', N'Cho phép xem thông tin khách hàng'),
('REPORT_READ', N'Xem báo cáo', N'Cho phép xem thống kê doanh thu');

INSERT INTO dbo.quyen_han_chuc_nang (id_quyen_han, id_chuc_nang) VALUES
(1, 1), (1, 2), (1, 3), (1, 4), (1, 5), (1, 6),
(2, 1), (2, 3), (2, 4), (2, 5),
(3, 1), (3, 2), (3, 3);
GO
