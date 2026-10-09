<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="soLuongTrongGio" value="0"/>
<c:forEach items="${sessionScope.gioHangOnline}" var="mucGio">
    <c:set var="soLuongTrongGio" value="${soLuongTrongGio + mucGio.value}"/>
</c:forEach>
<div class="header-icons">
    <a class="account-link cart-link" href="/gio-hang" aria-label="Giỏ hàng, ${soLuongTrongGio} sản phẩm" title="Giỏ hàng">
        <img src="/images/gio-hang.svg" alt="" width="24" height="24">
        <c:if test="${soLuongTrongGio > 0}"><span class="cart-count">${soLuongTrongGio}</span></c:if>
    </a>
    <%@ include file="menu-tai-khoan.jsp" %>
</div>
