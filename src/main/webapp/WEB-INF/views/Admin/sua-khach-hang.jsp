<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html><html lang="vi"><head>
<meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1"><title>Thông tin khách hàng — VictorSport</title>
<link rel="icon" href="/images/favicon-victorsport.svg"><link rel="stylesheet" href="/css/victorsport.css?v=admin-20261014"><link rel="stylesheet" href="/css/quan-ly-san-pham.css?v=admin-20261014">
</head><body><div class="app-shell"><jsp:include page="/WEB-INF/views/Admin/menu.jsp"/><div class="main-area">
<%@ include file="/WEB-INF/views/Admin/dau-trang-khach-hang.jsp" %>
<main class="content-layout"><section class="panel">
<div class="panel-header"><h2>${empty idKhachHang ? 'Thêm khách hàng' : 'Thông tin khách hàng'} <c:out value="${maKhachHang}"/></h2><a class="button secondary" href="/quan-ly/khach-hang">Về danh sách</a></div><div class="panel-body">
<c:if test="${not empty error}"><div class="alert error" role="alert"><c:out value="${error}"/></div></c:if>
<c:set var="duongDanLuu" value="/quan-ly/khach-hang/luu"/><c:if test="${not empty idKhachHang}"><c:set var="duongDanLuu" value="/quan-ly/khach-hang/${idKhachHang}/luu"/></c:if>
<form method="post" action="${duongDanLuu}" autocomplete="off">
<fieldset class="product-admin-form" style="border:0;padding:0;margin:0;min-width:0" ${not currentUser.quanLy ? 'disabled' : ''}>
    <div class="field"><label for="tenKhachHang">Họ tên *</label><input class="input" id="tenKhachHang" name="ten_khach_hang" maxlength="100" required value="<c:out value='${khachHang.ten_khach_hang}'/>"></div>
    <div class="field"><label for="taiKhoan">Tên tài khoản *</label><input class="input" id="taiKhoan" name="ten_tai_khoan" maxlength="50" pattern="[a-zA-Z0-9._@\-]+" required value="<c:out value='${khachHang.ten_tai_khoan}'/>"></div>
    <c:if test="${currentUser.quanLy}"><div class="field"><label for="matKhau">${empty idKhachHang ? 'Mật khẩu *' : 'Mật khẩu mới'}</label><input class="input" type="password" id="matKhau" name="mat_khau" autocomplete="new-password" minlength="6" maxlength="72" ${empty idKhachHang ? 'required' : ''} placeholder="${empty idKhachHang ? 'Tối thiểu 6 ký tự' : 'Để trống nếu không đổi mật khẩu'}"></div></c:if>
    <div class="field"><label for="email">Email</label><input class="input" type="email" pattern="[a-zA-Z0-9]+([._%+\-][a-zA-Z0-9]+)*@[gG][mM][aA][iI][lL]\.[cC][oO][mM]" title="Email phải có dạng ten@gmail.com" id="email" name="email" maxlength="100" value="<c:out value='${khachHang.email}'/>"></div>
    <div class="field"><label for="soDienThoai">Số điện thoại</label><input class="input" type="tel" id="soDienThoai" name="so_dien_thoai" maxlength="10" pattern="0[0-9]{9}" title="10 chữ số, bắt đầu bằng 0" value="<c:out value='${khachHang.so_dien_thoai}'/>"></div>
    <div class="field"><label for="gioiTinh">Giới tính</label><select class="select" id="gioiTinh" name="gioi_tinh"><option value="">Chưa cung cấp</option><option value="1" ${khachHang.gioi_tinh == true ? 'selected' : ''}>Nam</option><option value="0" ${khachHang.gioi_tinh == false ? 'selected' : ''}>Nữ</option></select></div>
    <div class="field"><label for="ngaySinh">Ngày sinh</label><input class="input" type="date" id="ngaySinh" name="ngay_sinh" value="<c:out value='${khachHang.ngay_sinh}'/>"></div>
    <div class="field"><label for="trangThai">Trạng thái *</label><select class="select" id="trangThai" name="trang_thai" required><option value="1" ${khachHang.trang_thai ? 'selected' : ''}>Đang hoạt động</option><option value="0" ${not khachHang.trang_thai ? 'selected' : ''}>Không hoạt động</option></select></div>
    <div class="product-admin-wide"><h3>Địa chỉ mặc định</h3></div>
    <div class="field product-admin-wide"><label for="tenDiaChi">Tên địa chỉ</label><input class="input" id="tenDiaChi" name="ten_dia_chi" maxlength="100" placeholder="Ví dụ: Nhà riêng" value="<c:out value='${khachHang.ten_dia_chi}'/>"></div>
    <input type="hidden" id="quan" name="quan" value="<c:out value='${khachHang.quan}'/>">
    <div class="field"><label for="thanhPho">Tỉnh / Thành phố</label><select class="select" id="thanhPho" name="thanh_pho" data-cu="<c:out value='${khachHang.thanh_pho}'/>"><option value="">Chọn tỉnh / thành phố</option><c:if test="${not empty khachHang.thanh_pho}"><option selected value="<c:out value='${khachHang.thanh_pho}'/>"><c:out value="${khachHang.thanh_pho}"/></option></c:if></select></div>
    <div class="field"><label for="phuong">Phường / Xã</label><select class="select" id="phuong" name="phuong" data-cu="<c:out value='${khachHang.phuong}'/>"><option value="">Chọn phường / xã</option><c:if test="${not empty khachHang.phuong}"><option selected value="<c:out value='${khachHang.phuong}'/>"><c:out value="${khachHang.phuong}"/></option></c:if></select></div>
    <p id="loiDiaChi" class="muted product-admin-wide" role="alert" hidden>Chưa tải được danh mục địa chỉ. Vui lòng tải lại trang; địa chỉ đã lưu vẫn được giữ.</p>
    <div class="field product-admin-wide"><label for="diaChi">Địa chỉ chi tiết</label><input class="input" id="diaChi" name="dia_chi_cu_the" maxlength="255" value="<c:out value='${khachHang.dia_chi_cu_the}'/>"></div>
    <c:if test="${currentUser.quanLy}"><div class="product-admin-actions product-admin-wide"><button class="button">${empty idKhachHang ? 'Thêm khách hàng' : 'Lưu thay đổi'}</button><a class="button secondary" href="/quan-ly/khach-hang">Hủy</a></div></c:if>
</fieldset></form></div></section></main></div></div><script src="/js/chon-dia-chi.js?v=3"></script></body></html>
