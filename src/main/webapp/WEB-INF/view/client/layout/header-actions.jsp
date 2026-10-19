<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="/WEB-INF/view/layout/notification.jsp" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:if test="${empty sessionScope.khachHangOnlineDangNhap and empty sessionScope.nguoiDungDangNhap}">
    <script src="/js/gio-hang-khach-vang-lai.js?v=3"></script>
</c:if>
<c:set var="soLuongTrongGio" value="0"/>
<c:forEach items="${sessionScope.gioHangOnline}" var="mucGio">
    <c:set var="soLuongTrongGio" value="${soLuongTrongGio + mucGio.value}"/>
</c:forEach>
<div class="header-icons">
    <c:choose>
        <c:when test="${not empty sessionScope.khachHangOnlineDangNhap}"><jsp:include page="/WEB-INF/view/layout/notification-bell.jsp"><jsp:param name="kenhChuong" value="khach"/></jsp:include></c:when>
        <c:when test="${sessionScope.nguoiDungDangNhap.quanLy}"><jsp:include page="/WEB-INF/view/layout/notification-bell.jsp"><jsp:param name="kenhChuong" value="admin"/></jsp:include></c:when>
    </c:choose>
    <a class="account-link cart-link" href="/gio-hang" aria-label="Giỏ hàng, ${soLuongTrongGio} sản phẩm" title="Giỏ hàng">
        <img src="/images/gio-hang.svg" alt="" width="24" height="24">
        <c:if test="${soLuongTrongGio > 0}"><span class="cart-count">${soLuongTrongGio}</span></c:if>
    </a>
    <%@ include file="/WEB-INF/view/client/layout/account-menu.jsp" %>
</div>
