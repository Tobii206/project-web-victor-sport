<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<fmt:setLocale value="vi_VN"/>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="referrer" content="no-referrer">
    <title>Thanh toán VNPAY Sandbox — VictorSport</title>
    <link rel="icon" href="/logo/favicon-victorsport.svg">
    <link href="https://fonts.googleapis.com/css2?family=Be+Vietnam+Pro:wght@400;500;600;700;800;900&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="/css/trang-chu.css?v=20261107-balanced">
    <link rel="stylesheet" href="/css/thanh-toan-qr.css?v=20261107-balanced">
    <link rel="stylesheet" href="/css/giao-dien-chung.css?v=20261107-readable">
</head>
<body>
<%@ include file="/WEB-INF/view/client/layout/header.jsp" %>
<main class="store-container online-container">
    <section class="account-card qr-card">
        <h1>Thanh toán VNPAY Sandbox</h1>
        <p class="subtle">Môi trường thử nghiệm — không thu tiền thật.</p>
        <c:if test="${not empty loiVnpay}"><p role="alert"><c:out value="${loiVnpay}"/></p></c:if>
        <c:if test="${not empty giaoDich}">
            <p>Đơn hàng <strong><c:out value="${giaoDich.ma_hoa_don}"/></strong></p>
            <p class="online-price"><fmt:formatNumber value="${giaoDich.so_tien}" maxFractionDigits="0"/> đ</p>
            <div role="status" aria-live="polite" id="trangThaiVnpay">
                <c:choose>
                    <c:when test="${giaoDich.trang_thai == 'thanh_cong'}">
                        <p><strong>Thanh toán thử nghiệm thành công.</strong></p>
                        <p>${giaoDich.trang_thai_hien_tai == 6 ? 'Đơn đã hủy. Cửa hàng cần đối soát giao dịch này.' : 'Cửa hàng sẽ tiếp tục xác nhận và chuẩn bị đơn hàng của bạn.'}</p>
                    </c:when>
                    <c:when test="${giaoDich.trang_thai_hien_tai == 6}"><p>Đơn hàng đã hủy.</p></c:when>
                    <c:when test="${giaoDich.trang_thai == 'da_huy'}"><p>Bạn đã hủy thanh toán. Đơn hàng chưa được thanh toán.</p></c:when>
                    <c:when test="${giaoDich.trang_thai == 'that_bai'}"><p>Thanh toán chưa thành công. Bạn có thể hủy đơn và đặt lại.</p></c:when>
                    <c:otherwise>
                        <p><c:out value="${empty thongBao ? 'Đơn đang chờ thanh toán VNPAY.' : thongBao}"/></p>
                        <c:choose>
                            <c:when test="${giaoDich.het_han == 1}"><p>Liên kết thanh toán đã hết hạn. Nếu chưa thanh toán, bạn có thể hủy đơn và đặt lại.</p></c:when>
                            <c:otherwise><p><a class="store-button" rel="noreferrer" href="<c:out value='${giaoDich.duong_dan_thanh_toan}'/>">Tiếp tục đến VNPAY Sandbox</a></p></c:otherwise>
                        </c:choose>
                        <p class="subtle">Trạng thái tự cập nhật khi hệ thống nhận được thông báo từ VNPAY.</p>
                    </c:otherwise>
                </c:choose>
            </div>
            <p><a class="store-button" href="${empty giaoDich.id_khach_hang ? '/don-vang-lai/' : '/don-hang/'}${giaoDich.id}">Xem đơn hàng</a></p>
            <c:if test="${giaoDich.trang_thai == 'cho_thanh_toan' and giaoDich.trang_thai_hien_tai != 6}">
                <script>
                (function () {
                    var lan = 0, timer;
                    async function capNhat() {
                        if (++lan > 60) return;
                        try {
                            var r = await fetch('/thanh-toan/vnpay/${giaoDich.id}/trang-thai', {cache:'no-store', credentials:'same-origin'});
                            if (!r.ok) return;
                            var gd = await r.json();
                            if (gd.trangThai !== 'cho_thanh_toan') {
                                location.replace('/thanh-toan/vnpay/${giaoDich.id}'); return;
                            }
                        } catch (e) { /* Thử lại trong lượt kế tiếp. */ }
                        timer = setTimeout(capNhat, 5000);
                    }
                    timer = setTimeout(capNhat, 5000);
                    window.addEventListener('pagehide', function () { clearTimeout(timer); }, {once:true});
                }());
                </script>
            </c:if>
        </c:if>
        <p><a href="/">Về trang chủ</a></p>
    </section>
</main>
<footer class="store-footer"><div class="store-container"><p>VictorSport · Hà Nội · 0325327602</p></div></footer>
</body>
</html>
