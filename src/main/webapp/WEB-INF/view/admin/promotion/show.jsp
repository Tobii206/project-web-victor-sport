<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html><html lang="vi"><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1">
<title><c:out value="${tenChucNang}"/> — VictorSport</title><link rel="icon" href="/logo/favicon-victorsport.svg">
<link rel="stylesheet" href="/css/victorsport.css?v=admin-20261014"><link rel="stylesheet" href="/css/quan-ly-san-pham.css?v=admin-20261014">
<link rel="stylesheet" href="/css/giao-dien-chung.css?v=20261023-complete-1"></head><body><div class="app-shell"><jsp:include page="/WEB-INF/view/admin/layout/sidebar.jsp"/><div class="main-area">
<%@ include file="/WEB-INF/view/admin/layout/promotion-header.jsp" %><main class="content-layout">
<section class="panel"><div class="panel-header"><h2>Danh sách đợt giảm giá</h2><a class="button" href="/quan-ly/dot-giam-gia/them">+ Thêm đợt giảm giá</a></div><div class="panel-body">
<c:if test="${not empty error}"><div class="alert error" role="alert" data-vs-flash="error"><c:out value="${error}"/></div></c:if>
<c:if test="${not empty success}"><div class="alert success" role="status" data-vs-flash="success"><c:out value="${success}"/></div></c:if>
<form method="get" class="product-admin-filters">
<div class="field"><label for="tuKhoa">Tìm kiếm</label><input class="input" id="tuKhoa" name="tuKhoa" placeholder="Tên hoặc mã" value='<c:out value="${tuKhoa}"/>'></div>
<div class="field"><label for="trangThai">Trạng thái</label><select class="select" id="trangThai" name="trangThai"><option value="">Tất cả</option><option value="1" ${trangThai == '1' ? 'selected' : ''}>Cho phép áp dụng</option><option value="0" ${trangThai == '0' ? 'selected' : ''}>Ngừng áp dụng</option></select></div>
<div class="product-admin-actions"><button class="button">Tìm kiếm / Lọc</button><a class="button secondary" href="/quan-ly/dot-giam-gia">Xóa bộ lọc</a></div></form>
<div class="product-admin-table"><table class="table"><thead><tr><th>Mã / Tên</th><th>Giá trị giảm</th><th>Thời gian</th><th>Mẫu áp dụng</th><th>Trạng thái</th><th>Thao tác</th></tr></thead><tbody>
<c:forEach items="${danhSach}" var="u"><tr><td><strong><c:out value="${u.ten_dot_giam_gia}"/></strong><div class="muted"><c:out value="${u.ma_dot_giam_gia}"/></div></td>
<td><fmt:formatNumber value="${u.gia_tri_giam_gia}" maxFractionDigits="2"/> ${u.loai_giam_gia ? 'đ' : '%'}</td><td><c:out value="${u.ngay_bat_dau}"/><div class="muted">đến <c:out value="${u.ngay_ket_thuc}"/></div></td>
<td><c:out value="${u.so_mau}"/></td><td><span class="product-admin-tag ${u.trang_thai ? '' : 'off'}">${u.trang_thai ? 'Cho phép áp dụng' : 'Ngừng áp dụng'}</span></td>
<td><div class="product-admin-actions"><a class="button secondary" href="/quan-ly/dot-giam-gia/${u.id}">Chi tiết / Sửa</a>
<c:if test="${u.trang_thai}"><form method="post" action="/quan-ly/dot-giam-gia/${u.id}/ngung" data-vs-confirm="Bạn muốn ngừng áp dụng đợt giảm giá này? Các sản phẩm trong đợt sẽ ngừng nhận ưu đãi này." data-vs-title="Ngừng đợt giảm giá" data-vs-button="Ngừng áp dụng" data-vs-tone="danger"><button class="button danger">Ngừng áp dụng</button></form></c:if></div></td></tr></c:forEach>
<c:if test="${empty danhSach}"><tr><td colspan="6">Không có đợt giảm giá phù hợp.</td></tr></c:if></tbody></table></div>
</div></section></main></div></div></body></html>
