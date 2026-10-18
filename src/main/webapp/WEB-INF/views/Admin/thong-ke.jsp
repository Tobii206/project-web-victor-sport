<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<fmt:setLocale value="vi_VN"/>
<!DOCTYPE html><html lang="vi"><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1">
<title>Thống kê — VictorSport</title><link rel="icon" href="/logo/favicon-victorsport.svg">
<link rel="stylesheet" href="/css/victorsport.css?v=admin-20261014"><link rel="stylesheet" href="/css/tong-quan.css?v=20261018"><link rel="stylesheet" href="/css/thong-ke.css?v=1">
<link rel="stylesheet" href="/css/giao-dien-chung.css?v=20261018-2"></head><body><div class="app-shell"><jsp:include page="/WEB-INF/views/Admin/menu.jsp"/><div class="main-area">
<%@ include file="dau-trang-tong-quan.jsp" %>
<main class="content-layout overview-page stats-page">
<section class="overview-intro"><div><p class="overview-eyebrow">KẾT QUẢ KINH DOANH</p><h2>Tổng quan bán hàng</h2><p class="muted">Theo dõi doanh thu, đơn hàng và sản phẩm bán chạy.</p></div><a class="button secondary stats-invoice-link" href="/quan-ly/hoa-don">Xem hóa đơn <span aria-hidden="true">↗</span></a></section>
<form class="overview-filter panel" method="get">
<div class="field"><label for="tuNgay">Từ ngày</label><input class="input" type="date" id="tuNgay" name="tuNgay" value="${tuNgay}" required></div>
<div class="field"><label for="denNgay">Đến ngày</label><input class="input" type="date" id="denNgay" name="denNgay" value="${denNgay}" required></div>
<div class="field"><label for="kenh">Kênh bán hàng</label><select class="select" id="kenh" name="kenh"><option value="tat-ca" ${kenh == 'tat-ca' ? 'selected' : ''}>Tất cả kênh bán</option><option value="online" ${kenh == 'online' ? 'selected' : ''}>Online</option><option value="quay" ${kenh == 'quay' ? 'selected' : ''}>Tại quầy</option></select></div>
<button class="button stats-filter-button"><svg viewBox="0 0 24 24" aria-hidden="true"><path d="M4 7h16M7 12h10M10 17h4"/></svg> Xem thống kê</button>
</form>
<c:if test="${not empty error}"><div class="alert error" role="alert" data-vs-flash="error"><c:out value="${error}"/></div></c:if>
<c:if test="${not empty tongQuan}">
<div class="overview-cards">
<article class="overview-card featured"><div class="stats-card-top"><span>Doanh thu hoàn tất</span><i class="stats-icon"><svg viewBox="0 0 24 24" aria-hidden="true"><rect x="3" y="5" width="18" height="15" rx="3"/><path d="M3 9h18M16 14h2M6 5V3h11"/></svg></i></div><strong><fmt:formatNumber value="${tongQuan.doanh_thu}" maxFractionDigits="0"/> <em>đ</em></strong><small>Sau ưu đãi · gồm phí giao hàng</small></article>
<article class="overview-card"><div class="stats-card-top"><span>Đã hoàn tất</span><i class="stats-icon complete"><svg viewBox="0 0 24 24" aria-hidden="true"><circle cx="12" cy="12" r="9"/><path d="m8 12 3 3 5-6"/></svg></i></div><strong>${tongQuan.hoan_tat}<em>đơn</em></strong><small>Đã hoàn tất hoặc khách đã nhận hàng</small></article>
<article class="overview-card"><div class="stats-card-top"><span>Đang xử lý</span><i class="stats-icon pending"><svg viewBox="0 0 24 24" aria-hidden="true"><circle cx="12" cy="12" r="9"/><path d="M12 7v5l3 2"/></svg></i></div><strong>${tongQuan.dang_xu_ly}<em>đơn</em></strong><small>Chờ xác nhận đến đang vận chuyển</small></article>
<article class="overview-card"><div class="stats-card-top"><span>Đã hủy</span><i class="stats-icon cancelled"><svg viewBox="0 0 24 24" aria-hidden="true"><rect x="5" y="3" width="14" height="18" rx="2"/><path d="m9 10 6 6M15 10l-6 6"/></svg></i></div><strong>${tongQuan.da_huy}<em>đơn</em></strong><small>Không tính vào doanh thu</small></article>
</div>
<div class="stats-middle">
<section class="panel stats-revenue"><div class="panel-header"><div><h2>Doanh thu theo ngày</h2><p class="muted">Chỉ tính đơn hoàn tất · lọc theo ngày tạo đơn</p></div><span class="stats-legend"><i></i> Doanh thu</span></div><div class="panel-body">
<c:set var="dinhDoanhThu" value="0"/><c:forEach items="${bieuDo}" var="d"><c:if test="${d.doanh_thu > dinhDoanhThu}"><c:set var="dinhDoanhThu" value="${d.doanh_thu}"/></c:if></c:forEach>
<c:set var="donViBieuDo" value="${dinhDoanhThu >= 1000000 ? 1000000 : 1000}"/>
<c:choose><c:when test="${tongQuan.hoan_tat > 0}">
<p class="stats-chart-unit">Đơn vị: ${dinhDoanhThu >= 1000000 ? 'triệu đồng' : 'nghìn đồng'}</p>
<div class="stats-chart-layout"><div class="stats-chart-axis" aria-hidden="true"><span><fmt:formatNumber value="${dinhDoanhThu / donViBieuDo}" maxFractionDigits="1"/></span><span><fmt:formatNumber value="${dinhDoanhThu * 0.75 / donViBieuDo}" maxFractionDigits="1"/></span><span><fmt:formatNumber value="${dinhDoanhThu * 0.5 / donViBieuDo}" maxFractionDigits="1"/></span><span><fmt:formatNumber value="${dinhDoanhThu * 0.25 / donViBieuDo}" maxFractionDigits="1"/></span><span>0</span></div>
<div class="revenue-chart" role="list" aria-label="Doanh thu từng ngày"><c:forEach items="${bieuDo}" var="d"><div class="revenue-day" role="listitem" title="${d.ngay}: ${d.doanh_thu} đ · ${d.so_don} đơn"><div class="revenue-track"><span style="height:${d.cao}%"></span></div><small>${d.nhan}</small></div></c:forEach></div></div>
</c:when><c:otherwise><div class="stats-empty"><svg viewBox="0 0 24 24" aria-hidden="true"><path d="M4 3v18h17M8 16v-3M13 16V9M18 16V6"/></svg><h3>Chưa có doanh thu trong kỳ</h3><p>Đơn hoàn tất sẽ được tổng hợp tại đây.</p></div></c:otherwise></c:choose>
<details class="overview-details"><summary>Xem số liệu từng ngày <span aria-hidden="true">↓</span></summary><div class="overview-table"><table class="table"><thead><tr><th>Ngày</th><th>Đơn hoàn tất</th><th class="money">Doanh thu</th></tr></thead><tbody><c:forEach items="${bieuDo}" var="d"><tr><td>${d.ngay}</td><td>${d.so_don}</td><td class="money"><fmt:formatNumber value="${d.doanh_thu}"/> đ</td></tr></c:forEach></tbody></table></div></details>
</div></section>
<section class="panel stats-orders"><div class="panel-header"><div><h2>Tình trạng đơn</h2><p class="muted">${tongQuan.so_don} đơn trong kỳ đã chọn</p></div></div><div class="panel-body">
<div class="stats-order-total"><strong>${tongQuan.so_don}</strong><span>Tổng đơn hàng</span></div>
<div class="stats-order-status complete"><div><span><i></i>Đã hoàn tất</span><strong>${tongQuan.hoan_tat}</strong></div><div class="stats-progress"><span style="width:${tongQuan.so_don > 0 ? tongQuan.hoan_tat * 100 / tongQuan.so_don : 0}%"></span></div></div>
<div class="stats-order-status pending"><div><span><i></i>Đang xử lý</span><strong>${tongQuan.dang_xu_ly}</strong></div><div class="stats-progress"><span style="width:${tongQuan.so_don > 0 ? tongQuan.dang_xu_ly * 100 / tongQuan.so_don : 0}%"></span></div></div>
<div class="stats-order-status cancelled"><div><span><i></i>Đã hủy</span><strong>${tongQuan.da_huy}</strong></div><div class="stats-progress"><span style="width:${tongQuan.so_don > 0 ? tongQuan.da_huy * 100 / tongQuan.so_don : 0}%"></span></div></div>
<p class="stats-completion"><strong><fmt:formatNumber value="${tongQuan.so_don > 0 ? tongQuan.hoan_tat * 100 / tongQuan.so_don : 0}" maxFractionDigits="0"/>%</strong> đơn hàng đã hoàn tất</p>
</div></section>
</div>
<section class="panel stats-bestsellers"><div class="panel-header"><div><h2>Sản phẩm bán chạy</h2><p class="muted">Xếp hạng theo số đôi bán ra từ đơn hoàn tất</p></div><span class="overview-pill">Top 10 sản phẩm</span></div><div class="panel-body"><div class="overview-table"><table class="table"><thead><tr><th>#</th><th>Sản phẩm</th><th>Số đôi đã bán</th><th class="money">Tiền hàng</th></tr></thead><tbody>
<c:forEach items="${banChay}" var="sp" varStatus="st"><tr><td><span class="overview-rank rank-${st.count}">${st.count}</span></td><td><a href="/quan-ly/san-pham/${sp.id}"><c:out value="${sp.ten_san_pham}"/></a><div class="muted"><c:out value="${sp.ma_san_pham}"/></div></td><td><div class="stats-sold"><strong>${sp.so_luong}</strong><div class="stats-sold-bar"><span style="width:${banChay[0].so_luong > 0 ? sp.so_luong * 100 / banChay[0].so_luong : 0}%"></span></div></div></td><td class="money"><fmt:formatNumber value="${sp.tien_hang}"/> đ</td></tr></c:forEach>
<c:if test="${empty banChay}"><tr><td colspan="4" class="overview-empty">Chưa có sản phẩm bán ra trong các đơn hoàn tất.</td></tr></c:if>
</tbody></table></div><p class="overview-note">Tiền hàng theo giá trên hóa đơn, trước ưu đãi cấp hóa đơn và phí vận chuyển.</p></div></section>
</c:if></main></div></div></body></html>
