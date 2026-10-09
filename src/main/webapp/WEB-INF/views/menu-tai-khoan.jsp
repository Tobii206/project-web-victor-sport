<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<details class="account-menu">
    <summary class="account-link" aria-label="Menu tài khoản" title="Menu tài khoản">
        <img src="/images/nguoi-dung.svg" alt="" width="24" height="24">
    </summary>
    <div class="account-dropdown">
        <a href="/tai-khoan">Thông tin chi tiết</a>
        <c:if test="${not empty sessionScope.nguoiDungDangNhap}">
            <a href="/pos">Bán hàng tại quầy</a>
        </c:if>
        <c:choose>
            <c:when test="${not empty sessionScope.nguoiDungDangNhap or not empty sessionScope.khachHangOnlineDangNhap}">
                <form method="post" action="/tai-khoan/dang-xuat">
                    <button type="submit">Đăng xuất</button>
                </form>
            </c:when>
            <c:otherwise><a href="/tai-khoan">Đăng nhập</a></c:otherwise>
        </c:choose>
    </div>
</details>
