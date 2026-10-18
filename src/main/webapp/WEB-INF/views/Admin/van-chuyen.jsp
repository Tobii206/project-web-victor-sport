<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<fmt:setLocale value="vi_VN"/>
<!DOCTYPE html><html lang="vi"><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1">
<title>Quản lý vận chuyển — VictorSport</title><link rel="icon" href="/images/favicon-victorsport.svg">
<link rel="stylesheet" href="/css/victorsport.css?v=admin-20261014"><link rel="stylesheet" href="/css/tong-quan.css?v=20261018">
</head><body><div class="app-shell"><jsp:include page="/WEB-INF/views/Admin/menu.jsp"/><div class="main-area">
<%@ include file="dau-trang-tong-quan.jsp" %>
<main class="content-layout overview-page">
<section class="overview-intro"><div><p class="overview-eyebrow">ĐƠN HÀNG ONLINE</p><h2>Theo dõi vận chuyển</h2><p class="muted">Xem địa chỉ nhận hàng, phí giao hàng và tiến trình xử lý đơn.</p></div></section>
<form method="get" class="overview-filter panel">
<div class="field shipping-search"><label for="tuKhoa">Tìm đơn hàng</label><input class="input" name="tuKhoa" id="tuKhoa" maxlength="100" value="<c:out value='${tuKhoa}'/>" placeholder="Mã đơn, khách hàng hoặc số điện thoại"></div>
<div class="field"><label for="trangThai">Trạng thái</label><select class="select" name="trangThai" id="trangThai"><option value="">Tất cả</option><option value="1" ${trangThai == 1 ? 'selected' : ''}>Chờ xác nhận</option><option value="2" ${trangThai == 2 ? 'selected' : ''}>Đã xác nhận đơn</option><option value="3" ${trangThai == 3 ? 'selected' : ''}>Đang chuẩn bị hàng</option><option value="4" ${trangThai == 4 ? 'selected' : ''}>Đang vận chuyển</option><option value="5" ${trangThai == 5 ? 'selected' : ''}>Đã nhận hàng</option><option value="6" ${trangThai == 6 ? 'selected' : ''}>Đã hủy</option></select></div><button class="button">Tìm kiếm</button></form>
<c:if test="${not empty error}"><div class="alert error" role="alert"><c:out value="${error}"/></div></c:if>
<c:if test="${not empty tongQuan}"><div class="overview-cards">
<article class="overview-card"><span>Chờ giao</span><strong>${tongQuan.cho_giao}</strong><small>Chờ xác nhận và chuẩn bị hàng</small></article>
<article class="overview-card featured"><span>Đang vận chuyển</span><strong>${tongQuan.dang_giao}</strong><small>Đã xác nhận thanh toán</small></article>
<article class="overview-card"><span>Đã nhận hàng</span><strong>${tongQuan.da_nhan}</strong><small>Khách đã xác nhận nhận hàng</small></article>
<article class="overview-card"><span>Đã hủy</span><strong>${tongQuan.da_huy}</strong><small>Theo bộ lọc hiện tại</small></article></div>
<section class="panel"><div class="panel-header"><h2>Danh sách đơn giao hàng</h2><span class="overview-pill">${tongQuan.so_don} đơn</span></div><div class="panel-body"><div class="overview-table"><table class="table shipping-table"><thead><tr><th>Đơn hàng</th><th>Người nhận</th><th>Địa chỉ nhận hàng</th><th>Phí giao hàng</th><th>Trạng thái</th><th>Thao tác</th></tr></thead><tbody>
<c:forEach items="${donHang}" var="hd"><tr><td><strong><c:out value="${hd.ma_hoa_don}"/></strong><div class="muted"><fmt:formatDate value="${hd.ngay_tao}" pattern="dd/MM/yyyy HH:mm"/></div></td><td><c:out value="${hd.ten_khach_hang}"/><div class="muted"><c:out value="${hd.so_dien_thoai_khach_hang}"/></div></td><td class="shipping-address"><c:out value="${hd.dia_chi_khach_hang}"/></td><td class="money"><fmt:formatNumber value="${hd.phi_van_chuyen}"/> đ</td><td><span class="shipping-state state-${hd.trang_thai_hien_tai}"><c:out value="${hd.ten_trang_thai}"/></span></td><td><a class="button secondary" href="/quan-ly/hoa-don/${hd.id}">Xem đơn</a></td></tr></c:forEach>
<c:if test="${empty donHang}"><tr><td colspan="6" class="overview-empty">Không có đơn hàng phù hợp.</td></tr></c:if>
</tbody></table></div><div class="overview-pagination">
<c:if test="${trang > 1}"><c:url var="trangTruoc" value="/quan-ly/van-chuyen"><c:param name="tuKhoa" value="${tuKhoa}"/><c:param name="trangThai" value="${trangThai}"/><c:param name="trang" value="${trang - 1}"/></c:url><a class="button secondary" href="<c:out value='${trangTruoc}'/>">← Trang trước</a></c:if>
<span>Trang ${trang} / ${soTrang}</span>
<c:if test="${trang < soTrang}"><c:url var="trangSau" value="/quan-ly/van-chuyen"><c:param name="tuKhoa" value="${tuKhoa}"/><c:param name="trangThai" value="${trangThai}"/><c:param name="trang" value="${trang + 1}"/></c:url><a class="button secondary" href="<c:out value='${trangSau}'/>">Trang sau →</a></c:if>
</div><p class="overview-note">Xác nhận thanh toán và hủy đơn trong chi tiết hóa đơn. Đơn đã nhận hàng được khóa trạng thái.</p></div></section></c:if>
</main></div></div></body></html>
