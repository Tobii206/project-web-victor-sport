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
<a class="account-back" href="/don-hang">← Về danh sách đơn hàng</a>
<c:if test="${don.trang_thai_hien_tai >= 1 && don.trang_thai_hien_tai <= 3 && don.ghi_chu == 'Chuyển khoản QR - chờ kiểm tra thanh toán'}">
    <p><a class="store-button" href="/thanh-toan/qr/${don.id}">Xem mã QR thanh toán</a></p>
</c:if>
<div class="don-hang-heading"><div><p class="eyebrow">ĐƠN HÀNG CỦA BẠN</p><h1><c:out value="${don.ma_hoa_don}"/></h1><p class="subtle">Ngày đặt: <fmt:formatDate value="${don.ngay_tao}" pattern="dd/MM/yyyy HH:mm"/></p></div>
<span class="don-hang-status ${don.trang_thai_hien_tai == 5 ? 'da-nhan' : ''} ${don.trang_thai_hien_tai == 6 ? 'da-huy' : ''}"><c:out value="${don.ten_trang_thai}"/></span></div>
<c:if test="${don.trang_thai_hien_tai == 1}"><section class="account-card don-hang-row don-hang-card"><p>Đơn hàng đang chờ xác nhận. Bạn có thể hủy nếu không còn nhu cầu mua.</p>
<form method="post" action="/don-hang/${don.id}/huy" onsubmit="return confirm('Bạn có chắc muốn hủy đơn hàng này?');"><button class="store-button don-hang-cancel" type="submit">Hủy đơn</button></form></section></c:if>
<c:if test="${don.trang_thai_hien_tai == 4}"><section class="account-card don-hang-row don-hang-card"><p>Đơn hàng đang vận chuyển. Hãy xác nhận sau khi bạn đã nhận được hàng.</p>
<form method="post" action="/don-hang/${don.id}/da-nhan-hang" onsubmit="return confirm('Bạn xác nhận đã nhận được hàng?');"><button class="store-button" type="submit">Đã nhận được hàng</button></form></section></c:if>
<c:if test="${don.trang_thai_hien_tai == 5}"><div class="online-success">Đã nhận hàng. Cảm ơn bạn đã mua sắm tại VictorSport!</div></c:if>
<c:if test="${don.trang_thai_hien_tai == 6}"><div class="account-error">Đơn hàng đã được hủy.</div></c:if>
<div class="don-hang-columns"><section class="account-card"><h2>Thông tin nhận hàng</h2>
<p><strong>Người nhận:</strong> <c:out value="${don.ten_khach_hang}"/></p><p><strong>Số điện thoại:</strong> <c:out value="${don.so_dien_thoai_khach_hang}"/></p>
<p><strong>Địa chỉ:</strong> <c:out value="${don.dia_chi_khach_hang}"/></p></section>
<section class="account-card"><h2>Thanh toán</h2><p class="don-hang-row"><span>Tiền hàng</span><strong><fmt:formatNumber value="${don.tong_tien}" maxFractionDigits="0"/> đ</strong></p>
<p class="don-hang-row"><span>Giảm giá</span><strong><fmt:formatNumber value="${don.tong_tien_giam}" maxFractionDigits="0"/> đ</strong></p>
<p class="don-hang-row"><span>Phí vận chuyển</span><strong><fmt:formatNumber value="${don.phi_van_chuyen}" maxFractionDigits="0"/> đ</strong></p>
<p class="don-hang-row"><span>Tổng thanh toán</span><strong class="online-price"><fmt:formatNumber value="${don.tong_tien_sau_giam}" maxFractionDigits="0"/> đ</strong></p></section></div>
<section class="account-card don-hang-card"><h2>Sản phẩm đã đặt</h2>
<c:forEach items="${sanPham}" var="sp"><div class="don-hang-item don-hang-row"><div><strong><c:out value="${sp.ten_san_pham}"/></strong>
<p class="subtle"><c:out value="${sp.ten_mau_sac}"/> · <c:out value="${sp.ten_kich_thuoc}"/> · Số lượng: <c:out value="${sp.so_luong}"/></p>
<p>Đơn giá: <fmt:formatNumber value="${sp.don_gia}" maxFractionDigits="0"/> đ</p></div><strong><fmt:formatNumber value="${sp.thanh_tien}" maxFractionDigits="0"/> đ</strong></div></c:forEach></section>
<section class="account-card don-hang-card"><h2>Lịch sử đơn hàng</h2><ol class="don-hang-timeline">
<c:forEach items="${lichSu}" var="buoc"><li><p><strong><fmt:formatDate value="${buoc.thoi_gian}" pattern="dd/MM/yyyy HH:mm"/></strong></p><p><c:out value="${buoc.ghi_chu}"/></p></li></c:forEach>
</ol><c:if test="${empty lichSu}"><p class="subtle">Chưa có lịch sử đơn hàng.</p></c:if></section>
</main></body></html>
