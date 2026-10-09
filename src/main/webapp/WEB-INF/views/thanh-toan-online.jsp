<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<fmt:setLocale value="vi_VN"/>
<!DOCTYPE html><html lang="vi"><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1.0"><title>Xác nhận đơn hàng — VictorSport</title><link rel="stylesheet" href="/css/trang-chu.css?v=gio-hang-1"></head>
<body><%@ include file="dau-trang-online.jsp" %><main class="store-container online-container"><h1>Xác nhận đơn hàng</h1>
<c:if test="${not empty error}"><div class="account-error" role="alert"><c:out value="${error}"/></div></c:if>
<c:if test="${not empty danhSach}"><div class="online-detail">
<section class="account-card"><h2>Thông tin nhận hàng</h2><form class="account-form" method="post" action="/thanh-toan"><input type="hidden" name="loai" value="${loai}">
<label for="ten">Họ và tên</label><input id="ten" name="ten" maxlength="100" value="<c:out value='${ten}'/>" required>
<label for="sdt">Số điện thoại</label><input id="sdt" name="sdt" type="tel" pattern="0[0-9]{9}" maxlength="10" value="<c:out value='${sdt}'/>" required>
<label for="diaChi">Địa chỉ nhận hàng</label><input id="diaChi" name="diaChi" maxlength="255" value="<c:out value='${diaChi}'/>" required>
<p>Phương thức: Thanh toán khi nhận hàng (COD)</p><button class="store-button">Xác nhận đặt hàng</button></form></section>
<section class="account-card"><h2>Sản phẩm đã chọn</h2><c:forEach items="${danhSach}" var="dong"><div class="online-order-item"><strong><c:out value="${dong.ten_san_pham}"/></strong><p class="subtle">Size <c:out value="${dong.ten_kich_thuoc}"/> · <c:out value="${dong.ten_mau_sac}"/> · <c:out value="${dong.ten_form_chan}"/></p><p>Số lượng: ${dong.so_luong_mua} · <fmt:formatNumber value="${dong.thanh_tien}"/> đ</p></div></c:forEach><p class="online-price">Tổng: <fmt:formatNumber value="${tongTien}"/> đ</p></section>
</div></c:if><p><a href="/gio-hang">← Về giỏ hàng</a></p></main></body></html>
