<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html><html lang="vi"><head>
<meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1"><title>${empty idNhanVien ? 'Thêm nhân viên' : 'Thông tin nhân viên'} — VictorSport</title>
<link rel="icon" href="/images/favicon-victorsport.svg"><link rel="stylesheet" href="/css/victorsport.css?v=muot-1"><link rel="stylesheet" href="/css/quan-ly-san-pham.css?v=1">
</head><body><div class="app-shell"><jsp:include page="/WEB-INF/views/Admin/menu.jsp"/><div class="main-area">
<%@ include file="/WEB-INF/views/Admin/dau-trang-nhan-vien.jsp" %>
<main class="content-layout"><section class="panel">
<div class="panel-header"><h2>${empty idNhanVien ? 'Thêm nhân viên' : 'Thông tin nhân viên'} <c:out value="${maNhanVien}"/></h2><a class="button secondary" href="/quan-ly/nhan-vien">Về danh sách</a></div>
<div class="panel-body">
<c:if test="${not empty error}"><div class="alert error" role="alert"><c:out value="${error}"/></div></c:if>
<c:set var="duongDanLuu" value="/quan-ly/nhan-vien/luu"/><c:if test="${not empty idNhanVien}"><c:set var="duongDanLuu" value="/quan-ly/nhan-vien/${idNhanVien}/luu"/></c:if>
<form method="post" action="${duongDanLuu}" class="product-admin-form" autocomplete="off">
    <div class="field"><label for="tenNhanVien">Họ tên *</label><input class="input" id="tenNhanVien" name="ten_nhan_vien" maxlength="100" required value="<c:out value='${nhanVien.ten_nhan_vien}'/>"></div>
    <div class="field"><label for="email">Email đăng nhập *</label><input class="input" type="email" id="email" name="email" maxlength="100" required value="<c:out value='${nhanVien.email}'/>"></div>
    <div class="field"><label for="soDienThoai">Số điện thoại</label><input class="input" type="tel" id="soDienThoai" name="so_dien_thoai" maxlength="10" pattern="0[0-9]{9}" title="10 chữ số, bắt đầu bằng 0" value="<c:out value='${nhanVien.so_dien_thoai}'/>"></div>
    <div class="field"><label for="matKhau">${empty idNhanVien ? 'Mật khẩu *' : 'Mật khẩu mới'}</label><input class="input" type="password" id="matKhau" name="mat_khau" autocomplete="new-password" minlength="6" maxlength="255" ${empty idNhanVien ? 'required' : ''} placeholder="${empty idNhanVien ? 'Tối thiểu 6 ký tự' : 'Để trống nếu không đổi mật khẩu'}"></div>
    <div class="field"><label for="idQuyen">Quyền *</label><select class="select" id="idQuyen" name="id_quyen_han" required><option value="">Chọn quyền</option><c:forEach items="${cacQuyen}" var="q"><option value="${q.id}" <c:if test="${nhanVien.id_quyen_han == q.id}">selected</c:if>><c:out value="${q.ten_quyen_han}"/></option></c:forEach></select></div>
    <div class="field"><label for="ngaySinh">Ngày sinh</label><input class="input" type="date" id="ngaySinh" name="ngay_sinh" value="<c:out value='${nhanVien.ngay_sinh}'/>"></div>
    <div class="field"><label for="thanhPho">Tỉnh / Thành phố</label><input class="input" id="thanhPho" name="thanh_pho" maxlength="100" value="<c:out value='${nhanVien.thanh_pho}'/>"></div>
    <div class="field"><label for="quan">Quận / Huyện</label><input class="input" id="quan" name="quan" maxlength="100" value="<c:out value='${nhanVien.quan}'/>"></div>
    <div class="field"><label for="phuong">Phường / Xã</label><input class="input" id="phuong" name="phuong" maxlength="100" value="<c:out value='${nhanVien.phuong}'/>"></div>
    <div class="field product-admin-wide"><label for="diaChi">Địa chỉ cụ thể</label><input class="input" id="diaChi" name="dia_chi_cu_the" maxlength="255" value="<c:out value='${nhanVien.dia_chi_cu_the}'/>"></div>
    <div class="field"><label for="trangThai">Trạng thái *</label><select class="select" id="trangThai" name="trang_thai" required><option value="1" ${nhanVien.trang_thai ? 'selected' : ''}>Đang làm việc</option><option value="0" ${not nhanVien.trang_thai ? 'selected' : ''}>Nghỉ việc</option></select></div>
    <div class="field product-admin-wide"><label for="ghiChu">Ghi chú</label><textarea class="textarea" id="ghiChu" name="ghi_chu" maxlength="255"><c:out value="${nhanVien.ghi_chu}"/></textarea></div>
    <div class="product-admin-actions product-admin-wide"><button class="button">${empty idNhanVien ? 'Thêm nhân viên' : 'Lưu thay đổi'}</button><a class="button secondary" href="/quan-ly/nhan-vien">Hủy</a></div>
</form></div></section></main></div></div></body></html>
