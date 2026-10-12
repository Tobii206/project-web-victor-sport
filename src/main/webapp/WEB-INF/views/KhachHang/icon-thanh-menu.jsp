<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:if test="${empty sessionScope.khachHangOnlineDangNhap and empty sessionScope.nguoiDungDangNhap}">
    <script src="/js/gio-hang-khach-vang-lai.js?v=2"></script>
</c:if>
<c:set var="soLuongTrongGio" value="0"/>
<c:forEach items="${sessionScope.gioHangOnline}" var="mucGio">
    <c:set var="soLuongTrongGio" value="${soLuongTrongGio + mucGio.value}"/>
</c:forEach>
<div class="header-icons">
    <a class="account-link cart-link" href="/gio-hang" aria-label="Giỏ hàng, ${soLuongTrongGio} sản phẩm" title="Giỏ hàng">
        <img src="/images/gio-hang.svg" alt="" width="24" height="24">
        <c:if test="${soLuongTrongGio > 0}"><span class="cart-count">${soLuongTrongGio}</span></c:if>
    </a>
    <%@ include file="/WEB-INF/views/KhachHang/menu-tai-khoan.jsp" %>
</div>
