<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<fmt:setLocale value="vi_VN"/>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Thanh toán QR — VictorSport</title>
    <link rel="icon" href="/images/favicon-victorsport.svg">
    <link rel="stylesheet" href="/css/trang-chu.css?v=gia-giam-1">
    <link rel="stylesheet" href="/css/thanh-toan-qr.css">
</head>
<body>
<%@ include file="/WEB-INF/views/KhachHang/dau-trang-online.jsp" %>
<main class="store-container online-container">
    <section class="account-card qr-card">
        <h1>Thanh toán chuyển khoản QR</h1>
        <p>Đơn hàng <strong><c:out value="${thanhToan.ma_hoa_don}"/></strong></p>
        <c:choose>
            <c:when test="${thanhToan.trang_thai_hien_tai >= 4 && thanhToan.trang_thai_hien_tai <= 5}">
                <p>Cửa hàng đã xác nhận thanh toán đơn hàng này.</p>
            </c:when>
            <c:when test="${thanhToan.trang_thai_hien_tai == 6}">
                <p>Đơn hàng đã hủy. Vui lòng không chuyển khoản cho đơn này.</p>
            </c:when>
            <c:otherwise>
                <p>Mở ứng dụng ngân hàng và quét mã bên dưới để chuyển khoản.</p>
                <img class="qr-image" referrerpolicy="no-referrer" src="<c:out value='${thanhToan.duong_dan_thanh_toan}'/>" alt="Mã QR chuyển khoản cho đơn hàng">
                <p class="online-price"><fmt:formatNumber value="${thanhToan.so_tien}"/> đ</p>
                <p>Nội dung chuyển khoản: <strong><c:out value="${thanhToan.ma_tham_chieu}"/></strong></p>
                <p class="subtle">Kiểm tra người nhận <c:out value="${thanhToan.chu_tai_khoan}"/> trước khi chuyển. Giữ đúng số tiền và nội dung để cửa hàng đối chiếu.</p>
                <p>Sau khi chuyển khoản, vui lòng chờ cửa hàng kiểm tra và xác nhận. Bạn không cần chuyển lại khi tải lại trang.</p>
                <p class="subtle">Tài khoản nhận: <c:out value="${thanhToan.ngan_hang}"/> · <c:out value="${thanhToan.so_tai_khoan}"/> · <c:out value="${thanhToan.chu_tai_khoan}"/>.</p>
            </c:otherwise>
        </c:choose>
        <c:if test="${not empty sessionScope.khachHangOnlineDangNhap}">
            <a class="store-button" href="/don-hang/${idHoaDon}">Xem đơn hàng</a>
        </c:if>
        <p><a href="/">Về trang chủ</a></p>
    </section>
</main>
</body>
</html>
