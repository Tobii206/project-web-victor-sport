<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<fmt:setLocale value="vi_VN"/>
<!DOCTYPE html><html lang="vi"><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1">
<title>Đơn hàng của tôi — VictorSport</title><link rel="icon" href="/images/favicon-victorsport.svg">
<link rel="stylesheet" href="/css/trang-chu.css?v=gia-giam-1"><link rel="stylesheet" href="/css/don-hang.css?v=2"></head>
<body class="store-page"><%@ include file="/WEB-INF/views/KhachHang/dau-trang-online.jsp" %>
<main class="store-container online-container">
<c:if test="${not empty error}"><div class="account-error" role="alert"><c:out value="${error}"/></div></c:if>
<c:if test="${not empty success}"><div class="online-success" role="status"><c:out value="${success}"/></div></c:if>
<div class="don-hang-heading"><div><p class="eyebrow">VICTORSPORT</p><h1>Đơn hàng của tôi</h1><p class="subtle">Theo dõi đơn hàng và xác nhận khi bạn đã nhận được hàng.</p></div><a class="account-back" href="/#san-pham">Tiếp tục mua sắm →</a></div>
<c:forEach items="${donHang}" var="don">
<section class="account-card don-hang-card">
<div class="don-hang-row"><div><h2><a href="/don-hang/${don.id}"><c:out value="${don.ma_hoa_don}"/></a></h2><p class="subtle">Ngày đặt: <fmt:formatDate value="${don.ngay_tao}" pattern="dd/MM/yyyy HH:mm"/></p></div>
<span class="don-hang-status ${don.trang_thai_hien_tai == 5 ? 'da-nhan' : ''} ${don.trang_thai_hien_tai == 6 ? 'da-huy' : ''}"><c:out value="${don.ten_trang_thai}"/></span></div>
<div class="don-hang-row"><p class="online-price"><fmt:formatNumber value="${don.tong_tien_sau_giam}" maxFractionDigits="0"/> đ</p>
<div class="don-hang-actions"><a class="store-button online-buy-now" href="/don-hang/${don.id}">Xem chi tiết</a>
<c:if test="${don.trang_thai_hien_tai == 1}"><form method="post" action="/don-hang/${don.id}/huy" onsubmit="return confirm('Bạn có chắc muốn hủy đơn hàng này?');">
<input type="hidden" name="quayLai" value="danh-sach"><button class="store-button don-hang-cancel" type="submit">Hủy đơn</button></form></c:if>
<c:if test="${don.trang_thai_hien_tai == 4}"><form method="post" action="/don-hang/${don.id}/da-nhan-hang" onsubmit="return confirm('Bạn xác nhận đã nhận được hàng?');">
<input type="hidden" name="quayLai" value="danh-sach"><button class="store-button" type="submit">Đã nhận được hàng</button></form></c:if>
</div></div></section>
</c:forEach>
<c:if test="${empty donHang and empty error}"><section class="account-card empty-state"><h2>Bạn chưa có đơn hàng online</h2><p>Đơn đặt bằng tài khoản này sẽ hiển thị tại đây.</p><a class="store-button" href="/#san-pham">Mua sắm ngay</a></section></c:if>
</main></body></html>
