<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<fmt:setLocale value="vi_VN"/>
<!DOCTYPE html><html lang="vi"><head>
<meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1"><title>${currentUser.suaSanPham ? 'Quản lý sản phẩm' : 'Xem tồn kho'} — VictorSport</title>
<link rel="icon" href="/logo/favicon-victorsport.svg"><link rel="stylesheet" href="/css/victorsport.css?v=admin-20261014"><link rel="stylesheet" href="/css/quan-ly-san-pham.css?v=admin-20261014">
<link rel="stylesheet" href="/css/giao-dien-chung.css?v=20261018-2"></head><body><div class="app-shell"><jsp:include page="/WEB-INF/view/admin/layout/sidebar.jsp"/><div class="main-area">
<%@ include file="/WEB-INF/view/admin/layout/product-header.jsp" %>
<main class="content-layout"><section class="panel"><div class="panel-header"><h2>Danh sách sản phẩm</h2><c:if test="${currentUser.suaSanPham}"><a class="button" href="/quan-ly/san-pham/them">+ Thêm sản phẩm</a></c:if></div><div class="panel-body">
<c:if test="${not empty error}"><div class="alert error" role="alert" data-vs-flash="error"><c:out value="${error}"/></div></c:if>
<c:if test="${not empty success}"><div class="alert success" role="status" data-vs-flash="success"><c:out value="${success}"/></div></c:if>
<form method="get" class="product-admin-filters">
    <div class="field"><label for="tuKhoa">Tên hoặc mã sản phẩm</label><input class="input" id="tuKhoa" name="tuKhoa" value="<c:out value='${loc.tuKhoa}'/>" placeholder="Tìm sản phẩm..."></div>
    <c:forEach items="${danhMuc}" var="dm"><c:if test="${dm.truong == 'id_thuong_hieu' or dm.truong == 'id_chat_lieu' or dm.truong == 'id_vi_tri_thi_dau'}">
        <div class="field"><label for="${dm.truong}"><c:out value="${dm.nhan}"/></label><select class="select" id="${dm.truong}" name="${dm.truong}"><option value="">Tất cả</option><c:forEach items="${dm.luaChon}" var="muc"><option value="${muc.id}" <c:if test="${loc[dm.truong] == muc.id}">selected</c:if>><c:out value="${muc.ten}"/></option></c:forEach></select></div>
    </c:if></c:forEach>
    <div class="field"><label for="trangThai">Trạng thái bán</label><select class="select" id="trangThai" name="trang_thai_kinh_doanh"><option value="">Tất cả</option><option value="1" ${loc.trang_thai_kinh_doanh == '1' ? 'selected' : ''}>Đang bán</option><option value="0" ${loc.trang_thai_kinh_doanh == '0' ? 'selected' : ''}>Ngừng bán</option></select></div>
    <div class="field"><label for="tonKho">Tồn kho</label><select class="select" id="tonKho" name="tonKho"><option value="">Tất cả</option><option value="con" ${loc.tonKho == 'con' ? 'selected' : ''}>Còn hàng</option><option value="het" ${loc.tonKho == 'het' ? 'selected' : ''}>Hết hàng</option></select></div>
    <div class="field"><label for="sapXep">Sắp xếp</label><select class="select" id="sapXep" name="sapXep"><option value="moi">Mới nhất</option><option value="ten" ${loc.sapXep == 'ten' ? 'selected' : ''}>Tên A–Z</option><option value="gia-tang" ${loc.sapXep == 'gia-tang' ? 'selected' : ''}>Giá thấp đến cao</option><option value="gia-giam" ${loc.sapXep == 'gia-giam' ? 'selected' : ''}>Giá cao đến thấp</option></select></div>
    <div class="product-admin-actions"><button class="button">Tìm kiếm / Lọc</button><a class="button secondary" href="/quan-ly/san-pham">Xóa bộ lọc</a></div>
</form>
<div class="product-admin-table"><table class="table"><thead><tr><th>Ảnh</th><th>Sản phẩm</th><th>Thương hiệu</th><th>Giá từ</th><th>Tồn kho</th><th>Trạng thái</th><th>Thao tác</th></tr></thead><tbody>
<c:forEach items="${sanPham}" var="sp"><tr>
<td><img class="product-admin-thumbnail" src="<c:out value='${empty sp.anh ? "/images/giay-mac-dinh.svg" : sp.anh}'/>" alt="<c:out value='${sp.ten_san_pham}'/>" onerror="this.onerror=null;this.src='/images/giay-mac-dinh.svg';"></td>
<td><strong><c:out value="${sp.ten_san_pham}"/></strong><div class="muted"><c:out value="${sp.ma_san_pham}"/> · ${sp.so_mau} mẫu</div></td><td><c:out value="${sp.ten_thuong_hieu}"/></td>
<td><c:choose><c:when test="${not empty sp.gia}"><fmt:formatNumber value="${sp.gia}" maxFractionDigits="0"/> đ</c:when><c:otherwise>Chưa có giá</c:otherwise></c:choose></td><td>${sp.ton_kho}</td>
<td><span class="product-admin-tag ${sp.trang_thai_kinh_doanh ? '' : 'off'}">${sp.trang_thai_kinh_doanh ? 'Đang bán' : 'Ngừng bán'}</span></td>
<td><div class="product-admin-actions"><a class="button secondary" href="/quan-ly/san-pham/${sp.id}">${currentUser.suaSanPham ? 'Chi tiết / Sửa' : 'Xem tồn kho'}</a><c:if test="${currentUser.suaSanPham}"><form method="post" action="/quan-ly/san-pham/${sp.id}/xoa" data-vs-confirm="Bạn muốn xóa sản phẩm khỏi danh sách bán? Dữ liệu hóa đơn vẫn được giữ lại." data-vs-title="Xóa sản phẩm" data-vs-button="Xóa sản phẩm" data-vs-tone="danger"><button class="button danger">Xóa</button></form></c:if></div></td>
</tr></c:forEach>
<c:if test="${empty sanPham}"><tr><td colspan="7">Không có sản phẩm phù hợp.</td></tr></c:if>
</tbody></table></div></div></section></main></div></div></body></html>
