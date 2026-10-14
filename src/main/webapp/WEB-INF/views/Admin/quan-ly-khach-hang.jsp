<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html><html lang="vi"><head>
<meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1"><title>Quản lý khách hàng — VictorSport</title>
<link rel="icon" href="/images/favicon-victorsport.svg"><link rel="stylesheet" href="/css/victorsport.css?v=admin-20261014"><link rel="stylesheet" href="/css/quan-ly-san-pham.css?v=admin-20261014">
</head><body><div class="app-shell"><jsp:include page="/WEB-INF/views/Admin/menu.jsp"/><div class="main-area">
<%@ include file="/WEB-INF/views/Admin/dau-trang-khach-hang.jsp" %>
<main class="content-layout"><section class="panel">
<div class="panel-header"><h2>Danh sách khách hàng</h2><c:if test="${currentUser.quanLy}"><a class="button" href="/quan-ly/khach-hang/them">+ Thêm khách hàng</a></c:if></div><div class="panel-body">
<c:if test="${not empty error}"><div class="alert error" role="alert"><c:out value="${error}"/></div></c:if>
<c:if test="${not empty success}"><div class="alert success" role="status"><c:out value="${success}"/></div></c:if>
<form method="get" class="product-admin-filters">
    <div class="field"><label for="tuKhoa">Tìm khách hàng</label><input class="input" id="tuKhoa" name="tuKhoa" value="<c:out value='${loc.tuKhoa}'/>" placeholder="Tên, mã, tài khoản, email hoặc số điện thoại"></div>
    <div class="field"><label for="trangThai">Trạng thái</label><select class="select" id="trangThai" name="trangThai"><option value="">Tất cả</option><option value="1" ${loc.trangThai == '1' ? 'selected' : ''}>Đang hoạt động</option><option value="0" ${loc.trangThai == '0' ? 'selected' : ''}>Không hoạt động</option></select></div>
    <div class="field"><label for="sapXep">Sắp xếp</label><select class="select" id="sapXep" name="sapXep"><option value="moi">Mới nhất</option><option value="ten" ${loc.sapXep == 'ten' ? 'selected' : ''}>Tên A–Z</option></select></div>
    <div class="product-admin-actions"><button class="button">Tìm kiếm / Lọc</button><a class="button secondary" href="/quan-ly/khach-hang">Xóa bộ lọc</a></div>
</form>
<div class="product-admin-table"><table class="table"><thead><tr><th>Mã khách hàng</th><th>Họ tên</th><th>Tài khoản</th><th>Liên hệ</th><th>Trạng thái</th><th>Thao tác</th></tr></thead><tbody>
<c:forEach items="${khachHang}" var="kh"><tr>
    <td><c:out value="${kh.ma_khach_hang}"/></td><td><strong><c:out value="${kh.ten_khach_hang}"/></strong></td><td><c:out value="${kh.ten_tai_khoan}"/></td>
    <td><c:out value="${empty kh.email ? 'Không có email' : kh.email}"/><div class="muted"><c:out value="${empty kh.so_dien_thoai ? 'Không có số điện thoại' : kh.so_dien_thoai}"/></div></td>
    <td><span class="product-admin-tag ${kh.trang_thai ? '' : 'off'}">${kh.trang_thai ? 'Đang hoạt động' : 'Không hoạt động'}</span></td>
    <td><div class="product-admin-actions"><a class="button secondary" href="/quan-ly/khach-hang/${kh.id}">${currentUser.quanLy ? 'Chi tiết / Sửa' : 'Chi tiết'}</a>
        <c:if test="${currentUser.quanLy and kh.trang_thai}"><form method="post" action="/quan-ly/khach-hang/${kh.id}/ngung-hoat-dong" onsubmit="return confirm('Chuyển khách hàng sang không hoạt động?');"><button class="button danger">Ngừng hoạt động</button></form></c:if>
    </div></td>
</tr></c:forEach><c:if test="${empty khachHang}"><tr><td colspan="6">Không có khách hàng phù hợp.</td></tr></c:if>
</tbody></table></div></div></section></main></div></div></body></html>
