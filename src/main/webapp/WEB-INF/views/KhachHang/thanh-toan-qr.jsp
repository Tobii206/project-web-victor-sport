<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<fmt:setLocale value="vi_VN"/>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Thanh toán QR — VictorSport</title>
    <link rel="icon" href="/logo/favicon-victorsport.svg">
    <link rel="stylesheet" href="/css/trang-chu.css?v=gia-giam-1">
    <link rel="stylesheet" href="/css/thanh-toan-qr.css">
<link rel="stylesheet" href="/css/giao-dien-chung.css?v=20261018-2"></head>
<body>
<%@ include file="/WEB-INF/views/KhachHang/dau-trang-online.jsp" %>
<main class="store-container online-container">
    <section class="account-card qr-card">
        <h1>Thanh toán chuyển khoản QR</h1>
        <c:if test="${not empty success}"><p role="status" data-vs-flash="success"><c:out value="${success}"/></p></c:if>
        <c:if test="${not empty error}"><p role="alert" data-vs-flash="error"><c:out value="${error}"/></p></c:if>
        <p>Đơn hàng <strong><c:out value="${thanhToan.ma_hoa_don}"/></strong></p>
        <c:choose>
            <c:when test="${thanhToan.trang_thai_thanh_toan == 'thanh_cong'}">
                <p>Cửa hàng đã xác nhận thanh toán đơn hàng này.</p>
            </c:when>
            <c:when test="${thanhToan.trang_thai_hien_tai == 6}">
                <p>Đơn hàng đã hủy. Vui lòng không chuyển khoản cho đơn này.</p>
            </c:when>
            <c:when test="${thanhToan.trang_thai_thanh_toan == 'da_huy'}"><p>Mã QR này không còn sử dụng. Vui lòng kiểm tra phương thức thanh toán trong đơn hàng.</p></c:when>
            <c:when test="${thanhToan.qua_han}"><p>Mã QR đã hết hạn. Vui lòng không chuyển khoản bằng mã này. Nếu đã chuyển tiền, liên hệ cửa hàng để đối soát.</p></c:when>
            <c:otherwise>
                <p>Mở ứng dụng ngân hàng và quét mã bên dưới để chuyển khoản.</p>
                <img class="qr-image" referrerpolicy="no-referrer" src="<c:out value='${thanhToan.duong_dan_thanh_toan}'/>" alt="Mã QR chuyển khoản cho đơn hàng">
                <c:if test="${not empty thanhToan.thoi_gian_het_han}"><p id="thoiHanQr" data-con-lai="${thanhToan.con_lai_giay}">Mã có hiệu lực đến <fmt:formatDate value="${thanhToan.thoi_gian_het_han}" pattern="HH:mm dd/MM/yyyy"/>.</p></c:if>
                <p class="online-price"><fmt:formatNumber value="${thanhToan.so_tien}"/> đ</p>
                <p>Nội dung chuyển khoản: <strong><c:out value="${thanhToan.ma_tham_chieu}"/></strong></p>
                <p class="subtle">Kiểm tra người nhận <c:out value="${thanhToan.chu_tai_khoan}"/> trước khi chuyển. Giữ đúng số tiền và nội dung để cửa hàng đối chiếu.</p>
                <p>Sau khi chuyển khoản, vui lòng chờ cửa hàng kiểm tra và xác nhận. Bạn không cần chuyển lại khi tải lại trang.</p>
                <p class="subtle">Tài khoản nhận: <c:out value="${thanhToan.ngan_hang}"/> · <c:out value="${thanhToan.so_tai_khoan}"/> · <c:out value="${thanhToan.chu_tai_khoan}"/>.</p>
            </c:otherwise>
        </c:choose>
        <c:if test="${thanhToan.trang_thai_hien_tai == 1 and thanhToan.trang_thai_thanh_toan == 'cho_thanh_toan'}"><p class="subtle">Nếu chưa chuyển tiền, bạn có thể đổi sang COD hoặc hủy đơn để hoàn lại tồn kho. Nếu đã chuyển tiền, hãy liên hệ 0325327602 để đối soát.</p>
        <form method="post" action="/thanh-toan/qr/${idHoaDon}/doi-cod" data-vs-confirm="Chỉ đổi nếu bạn chưa chuyển khoản. Sau khi đổi, bạn thanh toán khi nhận hàng và không sử dụng mã QR cũ." data-vs-title="Đổi sang thanh toán COD" data-vs-button="Đổi sang COD" data-vs-tone="normal"><button class="store-button">Đổi sang COD</button></form>
        <form method="post" action="${empty sessionScope.khachHangOnlineDangNhap ? '/don-vang-lai/' : '/don-hang/'}${idHoaDon}/huy" data-vs-confirm="Bạn muốn hủy đơn này? Số lượng sản phẩm sẽ được hoàn lại tồn kho. Nếu đã chuyển khoản, vui lòng liên hệ cửa hàng trước khi hủy." data-vs-title="Hủy đơn chưa thanh toán" data-vs-button="Hủy đơn hàng" data-vs-tone="danger"><button class="account-logout">Hủy đơn</button></form></c:if>
        <c:if test="${not empty sessionScope.khachHangOnlineDangNhap}">
            <a class="store-button" href="/don-hang/${idHoaDon}">Xem đơn hàng</a>
        </c:if>
        <c:if test="${empty sessionScope.khachHangOnlineDangNhap}"><a class="store-button" href="/don-vang-lai/${idHoaDon}">Xem đơn hàng</a></c:if>
        <p><a href="/">Về trang chủ</a></p>
    </section>
</main>
<script src="/js/thoi-han-qr.js?v=20261018-live" defer></script>
</body>
</html>
