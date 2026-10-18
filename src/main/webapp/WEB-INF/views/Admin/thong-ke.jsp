<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<fmt:setLocale value="vi_VN"/>
<!DOCTYPE html><html lang="vi"><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1">
<title>Thống kê — VictorSport</title><link rel="icon" href="/images/favicon-victorsport.svg">
<link rel="stylesheet" href="/css/victorsport.css?v=admin-20261014"><link rel="stylesheet" href="/css/tong-quan.css?v=20261018">
</head><body><div class="app-shell"><jsp:include page="/WEB-INF/views/Admin/menu.jsp"/><div class="main-area">
<%@ include file="dau-trang-tong-quan.jsp" %>
<main class="content-layout overview-page">
<section class="overview-intro"><div><p class="overview-eyebrow">KẾT QUẢ KINH DOANH</p><h2>Tổng quan bán hàng</h2><p class="muted">Theo ngày tạo hóa đơn. Doanh thu chỉ tính các đơn đã hoàn tất hoặc khách đã nhận hàng.</p></div></section>
<form class="overview-filter panel" method="get">
<div class="field"><label for="tuNgay">Từ ngày</label><input class="input" type="date" id="tuNgay" name="tuNgay" value="${tuNgay}" required></div>
<div class="field"><label for="denNgay">Đến ngày</label><input class="input" type="date" id="denNgay" name="denNgay" value="${denNgay}" required></div>
<div class="field"><label for="kenh">Kênh bán hàng</label><select class="select" id="kenh" name="kenh"><option value="tat-ca" ${kenh == 'tat-ca' ? 'selected' : ''}>Tất cả</option><option value="online" ${kenh == 'online' ? 'selected' : ''}>Online</option><option value="quay" ${kenh == 'quay' ? 'selected' : ''}>Tại quầy</option></select></div>
<button class="button">Xem thống kê</button></form>
<c:if test="${not empty error}"><div class="alert error" role="alert"><c:out value="${error}"/></div></c:if>
<c:if test="${not empty tongQuan}">
<div class="overview-cards">
<article class="overview-card featured"><span>Doanh thu đơn hoàn tất</span><strong><fmt:formatNumber value="${tongQuan.doanh_thu}"/> đ</strong><small>Đã áp dụng ưu đãi của đơn hàng</small></article>
<article class="overview-card"><span>Đã hoàn tất</span><strong>${tongQuan.hoan_tat}</strong><small>Hóa đơn</small></article>
<article class="overview-card"><span>Đang xử lý</span><strong>${tongQuan.dang_xu_ly}</strong><small>Chờ xác nhận đến đang giao</small></article>
<article class="overview-card"><span>Đã hủy</span><strong>${tongQuan.da_huy}</strong><small>Không tính vào doanh thu</small></article>
</div>
<section class="panel"><div class="panel-header"><div><h2>Doanh thu theo ngày</h2><p class="muted">Đơn đã hoàn tất · đơn vị: đồng</p></div><span class="overview-pill">${tongQuan.so_don} đơn trong kỳ</span></div><div class="panel-body">
<c:if test="${tongQuan.hoan_tat == 0}"><p class="overview-empty">Chưa có đơn hoàn tất trong khoảng thời gian này.</p></c:if>
<c:if test="${tongQuan.hoan_tat > 0}"><div class="revenue-chart" role="list" aria-label="Doanh thu từng ngày"><c:forEach items="${bieuDo}" var="d"><div class="revenue-day" role="listitem" title="${d.ngay}: ${d.doanh_thu} đ · ${d.so_don} đơn"><div class="revenue-track"><span style="height:${d.cao}%"></span></div><small>${d.nhan}</small></div></c:forEach></div></c:if>
<details class="overview-details"><summary>Xem số liệu từng ngày</summary><div class="overview-table"><table class="table"><thead><tr><th>Ngày</th><th>Đơn hoàn tất</th><th class="money">Doanh thu</th></tr></thead><tbody><c:forEach items="${bieuDo}" var="d"><tr><td>${d.ngay}</td><td>${d.so_don}</td><td class="money"><fmt:formatNumber value="${d.doanh_thu}"/> đ</td></tr></c:forEach></tbody></table></div></details>
</div></section>
<section class="panel"><div class="panel-header"><div><h2>Sản phẩm bán chạy</h2><p class="muted">10 sản phẩm bán nhiều nhất từ các đơn hoàn tất</p></div></div><div class="panel-body"><div class="overview-table"><table class="table"><thead><tr><th>#</th><th>Sản phẩm</th><th>Số đôi</th><th class="money">Tiền hàng</th></tr></thead><tbody>
<c:forEach items="${banChay}" var="sp" varStatus="st"><tr><td><span class="overview-rank">${st.count}</span></td><td><a href="/quan-ly/san-pham/${sp.id}"><c:out value="${sp.ten_san_pham}"/></a><div class="muted"><c:out value="${sp.ma_san_pham}"/></div></td><td>${sp.so_luong}</td><td class="money"><fmt:formatNumber value="${sp.tien_hang}"/> đ</td></tr></c:forEach>
<c:if test="${empty banChay}"><tr><td colspan="4" class="overview-empty">Chưa có sản phẩm bán ra trong các đơn hoàn tất.</td></tr></c:if>
</tbody></table></div><p class="overview-note">Tiền hàng theo giá trên từng dòng hóa đơn, trước ưu đãi cấp hóa đơn và phí vận chuyển.</p></div></section>
</c:if></main></div></div></body></html>
