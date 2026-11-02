<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<fmt:setLocale value="vi_VN"/>
<!DOCTYPE html><html lang="vi"><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1">
<title>Đơn hàng của tôi — VictorSport</title><link rel="icon" href="/logo/favicon-victorsport.svg">
<link rel="stylesheet" href="/css/trang-chu.css?v=20261023-ui-1"><link rel="stylesheet" href="/css/don-hang.css?v=20261029-search-1"><link rel="stylesheet" href="/css/giao-dien-chung.css?v=20261023-type-2"></head>
<body class="store-page"><%@ include file="/WEB-INF/view/client/layout/header.jsp" %>
<main class="store-container online-container">
<c:if test="${not empty error}"><div class="account-error" role="alert" data-vs-flash="error"><c:out value="${error}"/></div></c:if>
<c:if test="${not empty success}"><div class="online-success" role="status" data-vs-flash="success"><c:out value="${success}"/></div></c:if>
<div class="don-hang-heading"><div><p class="eyebrow">VICTORSPORT</p><h1>Đơn hàng của tôi</h1><p class="subtle">Theo dõi đơn hàng và xác nhận khi bạn đã nhận được hàng.</p></div><a class="account-back" href="/#san-pham">Tiếp tục mua sắm →</a></div>
<form class="don-hang-search" method="get" action="/don-hang" role="search"><div><label for="timDonHang">Tìm đơn hàng</label><input id="timDonHang" name="tuKhoa" maxlength="50" value="<c:out value='${tuKhoa}'/>" placeholder="Nhập mã đơn hàng hoặc mã vận đơn"></div><button type="submit" class="store-button">Tìm kiếm</button><c:if test="${not empty tuKhoa}"><a class="account-back" href="/don-hang">Xóa bộ lọc</a></c:if></form>
<c:forEach items="${donHang}" var="don">
<section class="account-card don-hang-card">
<div class="don-hang-row"><div><h2><a href="/don-hang/${don.id}"><c:out value="${don.ma_hoa_don}"/></a></h2><p class="subtle">Ngày đặt: <fmt:formatDate value="${don.ngay_tao}" pattern="dd/MM/yyyy HH:mm"/></p></div>
<c:if test="${not empty don.ma_van_don}"><p class="subtle">Mã vận đơn: <strong><c:out value="${don.ma_van_don}"/></strong></p></c:if>
<span class="don-hang-status ${don.trang_thai_hien_tai == 5 ? 'da-nhan' : ''} ${don.trang_thai_hien_tai == 6 ? 'da-huy' : ''}"><c:out value="${don.ten_trang_thai}"/></span></div>
<div class="don-hang-row"><p class="online-price"><fmt:formatNumber value="${don.tong_tien_sau_giam}" maxFractionDigits="0"/> đ</p>
<div class="don-hang-actions"><a class="store-button online-buy-now" href="/don-hang/${don.id}">Xem chi tiết</a>
<c:if test="${(don.trang_thai_hien_tai >= 1 and don.trang_thai_hien_tai <= 3) and not don.co_thanh_toan}"><form method="post" action="/don-hang/${don.id}/huy" data-vs-confirm="Bạn muốn hủy đơn hàng này? Sản phẩm đã chọn trong đơn sẽ được hoàn lại tồn kho." data-vs-title="Hủy đơn hàng" data-vs-button="Hủy đơn hàng" data-vs-tone="danger">
<input type="hidden" name="quayLai" value="danh-sach"><button class="store-button don-hang-cancel" type="submit">Hủy đơn</button></form></c:if>
<c:if test="${don.trang_thai_hien_tai == 4}"><form method="post" action="/don-hang/${don.id}/da-nhan-hang" data-vs-confirm="Hãy kiểm tra đã nhận đủ hàng. Nếu thanh toán COD, chỉ xác nhận sau khi đã trả đủ tiền cho shipper; hệ thống sẽ đồng thời ghi nhận thanh toán." data-vs-title="Xác nhận đã nhận hàng" data-vs-button="Đã nhận hàng" data-vs-tone="normal">
<input type="hidden" name="quayLai" value="danh-sach"><button class="store-button" type="submit">Đã nhận được hàng</button></form></c:if>
</div></div></section>
</c:forEach>
<c:if test="${empty donHang and empty error}"><section class="account-card empty-state"><c:choose><c:when test="${not empty tuKhoa}"><h2>Không tìm thấy đơn hàng</h2><p>Kiểm tra mã đơn hàng hoặc mã vận đơn. Chỉ hiển thị đơn thuộc tài khoản của bạn.</p></c:when><c:otherwise><h2>Bạn chưa có đơn hàng online</h2><p>Đơn đặt bằng tài khoản này sẽ hiển thị tại đây.</p></c:otherwise></c:choose><a class="store-button" href="/#san-pham">Mua sắm ngay</a></section></c:if>
</main></body></html>
