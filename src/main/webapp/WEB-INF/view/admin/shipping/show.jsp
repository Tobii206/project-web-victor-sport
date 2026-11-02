<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<fmt:setLocale value="vi_VN"/>
<!DOCTYPE html><html lang="vi"><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1">
<title>Quản lý vận chuyển — VictorSport</title><link rel="icon" href="/logo/favicon-victorsport.svg">
<link rel="stylesheet" href="/css/victorsport.css?v=admin-20261014"><link rel="stylesheet" href="/css/tong-quan.css?v=20261023-ui-1">
<link rel="stylesheet" href="/css/giao-dien-chung.css?v=20261023-type-2"></head><body><div class="app-shell"><jsp:include page="/WEB-INF/view/admin/layout/sidebar.jsp"/><div class="main-area">
<%@ include file="/WEB-INF/view/admin/layout/dashboard-header.jsp" %>
<main class="content-layout overview-page">
<section class="overview-intro"><div><p class="overview-eyebrow">ĐƠN HÀNG ONLINE</p><h2>Theo dõi vận chuyển</h2><p class="muted">Theo dõi phí giao hàng, mã vận đơn và trạng thái. GHN được xử lý tự động.</p></div></section>
<form method="get" class="overview-filter panel">
<div class="field shipping-search"><label for="tuKhoa">Tìm đơn hàng</label><input class="input" name="tuKhoa" id="tuKhoa" maxlength="100" value="<c:out value='${tuKhoa}'/>" placeholder="Mã đơn, khách hàng hoặc số điện thoại"></div>
<div class="field"><label for="trangThai">Trạng thái</label><select class="select" name="trangThai" id="trangThai"><option value="">Tất cả</option><option value="1" ${trangThai == 1 ? 'selected' : ''}>Chờ xác nhận</option><option value="2" ${trangThai == 2 ? 'selected' : ''}>Đã xác nhận đơn</option><option value="3" ${trangThai == 3 ? 'selected' : ''}>Đang chuẩn bị hàng</option><option value="4" ${trangThai == 4 ? 'selected' : ''}>Đang vận chuyển</option><option value="5" ${trangThai == 5 ? 'selected' : ''}>Đã nhận hàng</option><option value="6" ${trangThai == 6 ? 'selected' : ''}>Đã hủy</option></select></div><button class="button">Tìm kiếm</button></form>
<c:if test="${not empty success}"><div class="alert" role="status" data-vs-flash="success"><c:out value="${success}"/></div></c:if>
<c:if test="${not empty error}"><div class="alert error" role="alert" data-vs-flash="error"><c:out value="${error}"/></div></c:if>
<c:if test="${not empty tongQuan}"><div class="overview-cards shipping-overview-cards">
<article class="overview-card"><span>Chờ xác nhận</span><strong>${tongQuan.cho_xac_nhan}</strong><small>Khách đã đặt đơn</small></article>
<article class="overview-card"><span>Đã xác nhận đơn</span><strong>${tongQuan.da_xac_nhan}</strong><small>Chờ admin bắt đầu chuẩn bị</small></article>
<article class="overview-card"><span>Đang chuẩn bị hàng</span><strong>${tongQuan.dang_chuan_bi}</strong><small>Chờ gửi hàng cho shipper</small></article>
<article class="overview-card featured"><span>Đang vận chuyển</span><strong>${tongQuan.dang_giao}</strong><small>Shipper đã lấy hàng</small></article>
<article class="overview-card"><span>Đã nhận hàng</span><strong>${tongQuan.da_nhan}</strong><small>Đơn đã giao thành công</small></article>
<article class="overview-card"><span>Đã hủy</span><strong>${tongQuan.da_huy}</strong><small>Theo bộ lọc hiện tại</small></article></div>
<section class="panel"><div class="panel-header"><h2>Danh sách đơn giao hàng</h2><span class="overview-pill">${tongQuan.so_don} đơn</span></div><div class="panel-body"><div class="overview-table"><table class="table shipping-table"><thead><tr><th>Đơn hàng</th><th>Người nhận</th><th>Địa chỉ nhận hàng</th><th>Phí khách trả</th><th>Phí GHN</th><th>Trạng thái / Mã vận đơn</th><th>Chi tiết</th></tr></thead><tbody>
<c:forEach items="${donHang}" var="hd"><tr>
<td><strong><c:out value="${hd.ma_hoa_don}"/></strong><div class="muted"><fmt:formatDate value="${hd.ngay_tao}" pattern="dd/MM/yyyy HH:mm"/></div></td>
<td><c:out value="${hd.ten_khach_hang}"/><div class="muted"><c:out value="${hd.so_dien_thoai_khach_hang}"/></div></td>
<td class="shipping-address"><c:out value="${hd.dia_chi_khach_hang}"/></td>
<td class="money"><fmt:formatNumber value="${hd.phi_van_chuyen}" maxFractionDigits="0"/> đ</td>
<td class="money"><c:choose><c:when test="${hd.phi_thuc_te != null}"><fmt:formatNumber value="${hd.phi_thuc_te}" maxFractionDigits="0"/> đ</c:when><c:otherwise><span class="muted">Chưa có vận đơn</span></c:otherwise></c:choose></td>
<td><span class="shipping-state state-${hd.trang_thai_hien_tai}"><c:out value="${hd.ten_trang_thai}"/></span><c:if test="${not empty hd.ma_van_don}"><div class="muted">Mã vận đơn: <c:out value="${hd.ma_van_don}"/></div></c:if></td>
<td><a class="button secondary" href="/quan-ly/hoa-don/${hd.id}">Xem đơn</a></td></tr></c:forEach>
<c:if test="${empty donHang}"><tr><td colspan="7" class="overview-empty">Không có đơn hàng phù hợp.</td></tr></c:if>
</tbody></table></div><div class="overview-pagination">
<c:if test="${trang > 1}"><c:url var="trangTruoc" value="/quan-ly/van-chuyen"><c:param name="tuKhoa" value="${tuKhoa}"/><c:param name="trangThai" value="${trangThai}"/><c:param name="trang" value="${trang - 1}"/></c:url><a class="button secondary" href="<c:out value='${trangTruoc}'/>">← Trang trước</a></c:if>
<span>Trang ${trang} / ${soTrang}</span>
<c:if test="${trang < soTrang}"><c:url var="trangSau" value="/quan-ly/van-chuyen"><c:param name="tuKhoa" value="${tuKhoa}"/><c:param name="trangThai" value="${trangThai}"/><c:param name="trang" value="${trang + 1}"/></c:url><a class="button secondary" href="<c:out value='${trangSau}'/>">Trang sau →</a></c:if>
</div><p class="overview-note">Phí khách trả là phí giao hàng trên hóa đơn sau ưu đãi vận chuyển. Phí GHN là phí hãng trả về khi tạo vận đơn. Tạo và cập nhật GHN được xử lý tự động; xác nhận gửi hàng và giao thành công tại chi tiết đơn.</p></div></section></c:if>
</main></div></div></body></html>
