<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html><html lang="vi"><head>
<meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1"><title>Quản lý nhân viên — VictorSport</title>
<link rel="icon" href="/images/favicon-victorsport.svg"><link rel="stylesheet" href="/css/victorsport.css?v=muot-1"><link rel="stylesheet" href="/css/quan-ly-san-pham.css?v=1">
</head><body><div class="app-shell"><jsp:include page="menu.jsp"/><div class="main-area">
<%@ include file="dau-trang-nhan-vien.jsp" %>
<main class="content-layout"><section class="panel">
<div class="panel-header"><h2>Danh sách nhân viên</h2><a class="button" href="/quan-ly/nhan-vien/them">+ Thêm nhân viên</a></div>
<div class="panel-body">
<c:if test="${not empty error}"><div class="alert error" role="alert"><c:out value="${error}"/></div></c:if>
<c:if test="${not empty success}"><div class="alert success" role="status"><c:out value="${success}"/></div></c:if>
<form method="get" class="product-admin-filters">
    <div class="field"><label for="tuKhoa">Tìm nhân viên</label><input class="input" id="tuKhoa" name="tuKhoa" value="<c:out value='${loc.tuKhoa}'/>" placeholder="Tên, mã, email hoặc số điện thoại"></div>
    <div class="field"><label for="quyen">Quyền</label><select class="select" id="quyen" name="quyen"><option value="">Tất cả</option><c:forEach items="${cacQuyen}" var="q"><option value="${q.id}" <c:if test="${loc.quyen == q.id}">selected</c:if>><c:out value="${q.ten_quyen_han}"/></option></c:forEach></select></div>
    <div class="field"><label for="trangThai">Trạng thái</label><select class="select" id="trangThai" name="trangThai"><option value="">Tất cả</option><option value="1" ${loc.trangThai == '1' ? 'selected' : ''}>Đang làm việc</option><option value="0" ${loc.trangThai == '0' ? 'selected' : ''}>Nghỉ việc</option></select></div>
    <div class="field"><label for="sapXep">Sắp xếp</label><select class="select" id="sapXep" name="sapXep"><option value="moi">Mới nhất</option><option value="ten" ${loc.sapXep == 'ten' ? 'selected' : ''}>Tên A–Z</option></select></div>
    <div class="product-admin-actions"><button class="button">Tìm kiếm / Lọc</button><a class="button secondary" href="/quan-ly/nhan-vien">Xóa bộ lọc</a></div>
</form>
<div class="product-admin-table"><table class="table"><thead><tr><th>Mã nhân viên</th><th>Họ tên</th><th>Liên hệ</th><th>Quyền</th><th>Trạng thái</th><th>Thao tác</th></tr></thead><tbody>
<c:forEach items="${nhanVien}" var="nv"><tr>
    <td><c:out value="${nv.ma_nhan_vien}"/></td>
    <td><strong><c:out value="${nv.ten_nhan_vien}"/></strong></td>
    <td><c:out value="${nv.email}" default="Không có"/><div class="muted"><c:out value="${empty nv.so_dien_thoai ? 'Không có số điện thoại' : nv.so_dien_thoai}"/></div></td>
    <td><c:out value="${nv.ten_quyen_han}"/></td>
    <td><span class="product-admin-tag ${nv.trang_thai ? '' : 'off'}">${nv.trang_thai ? 'Đang làm việc' : 'Nghỉ việc'}</span></td>
    <td><div class="product-admin-actions"><a class="button secondary" href="/quan-ly/nhan-vien/${nv.id}">Chi tiết / Sửa</a>
        <c:if test="${nv.trang_thai and nv.id != currentUser.id}"><form method="post" action="/quan-ly/nhan-vien/${nv.id}/nghi-viec" onsubmit="return confirm('Chuyển nhân viên sang trạng thái nghỉ việc?');"><button class="button danger">Nghỉ việc</button></form></c:if>
    </div></td>
</tr></c:forEach>
<c:if test="${empty nhanVien}"><tr><td colspan="6">Không có nhân viên phù hợp.</td></tr></c:if>
</tbody></table></div></div></section></main></div></div></body></html>
