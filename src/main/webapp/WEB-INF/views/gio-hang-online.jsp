<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<fmt:setLocale value="vi_VN"/>
<!DOCTYPE html><html lang="vi"><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1.0"><title>Giỏ hàng — VictorSport</title><link rel="stylesheet" href="/css/trang-chu.css?v=gio-hang-1"></head>
<body><%@ include file="dau-trang-online.jsp" %><main class="store-container online-container">
<h1>Giỏ hàng của bạn</h1>
<c:if test="${not empty error}"><div class="account-error" role="alert"><c:out value="${error}"/></div></c:if>
<c:if test="${not empty success}"><div class="online-success" role="status"><c:out value="${success}"/></div></c:if>
<c:forEach items="${danhSach}" var="dong">
    <div class="account-card online-cart-row">
        <div><a href="/san-pham/${dong.id_san_pham}"><strong><c:out value="${dong.ten_san_pham}"/></strong></a><p class="subtle">Size <c:out value="${dong.ten_kich_thuoc}"/> · <c:out value="${dong.ten_mau_sac}"/> · <c:out value="${dong.ten_form_chan}"/></p><p><fmt:formatNumber value="${dong.gia_ban}"/> đ / đôi</p></div>
        <form method="post" action="/gio-hang/sua"><input type="hidden" name="idPhienBan" value="${dong.id}"><input class="online-cart-quantity" type="number" name="soLuong" value="${dong.so_luong_mua}" min="0" aria-label="Số lượng"><button class="store-button" type="submit">Cập nhật</button></form>
        <strong><fmt:formatNumber value="${dong.thanh_tien}"/> đ</strong>
        <form method="post" action="/gio-hang/sua"><input type="hidden" name="idPhienBan" value="${dong.id}"><input type="hidden" name="soLuong" value="0"><button class="account-logout" type="submit">Xóa</button></form>
    </div>
</c:forEach>
<c:if test="${empty danhSach and empty error}"><div class="empty-state"><p>Giỏ hàng đang trống.</p></div></c:if>
<c:if test="${not empty error}"><c:forEach items="${gioDaChon}" var="muc"><form method="post" action="/gio-hang/sua"><input type="hidden" name="idPhienBan" value="${muc.key}"><input type="hidden" name="soLuong" value="0"><button class="account-logout">Xóa mẫu giày #${muc.key} khỏi giỏ</button></form></c:forEach></c:if>
<div class="online-cart-bottom"><a href="/#san-pham">← Tiếp tục mua sắm</a><c:if test="${not empty danhSach}"><strong>Tổng tiền: <fmt:formatNumber value="${tongTien}"/> đ</strong><a class="store-button" href="/thanh-toan">Đặt hàng</a></c:if></div>
</main></body></html>
