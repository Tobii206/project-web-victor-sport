<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<div class="account-menu" id="menuTaiKhoan">
    <button class="account-link" type="button" id="nutMenuTaiKhoan" aria-label="Menu tài khoản" title="Menu tài khoản" aria-controls="cacMucTaiKhoan" aria-expanded="false">
        <img src="/images/nguoi-dung.svg" alt="" width="24" height="24">
    </button>
    <div class="account-dropdown" id="cacMucTaiKhoan" inert>
        <a href="/tai-khoan">Thông tin chi tiết</a>
        <c:choose>
            <c:when test="${sessionScope.nguoiDungDangNhap.quanLy}">
                <a href="/quan-ly/san-pham">Vào trang quản lý</a>
            </c:when>
            <c:when test="${sessionScope.nguoiDungDangNhap.banHangTaiQuay}">
                <a href="/pos">Bán hàng tại quầy</a>
            </c:when>
        </c:choose>
        <c:choose>
            <c:when test="${not empty sessionScope.nguoiDungDangNhap or not empty sessionScope.khachHangOnlineDangNhap}">
                <form method="post" action="/tai-khoan/dang-xuat">
                    <button type="submit">Đăng xuất</button>
                </form>
            </c:when>
            <c:otherwise><a href="/login">Đăng nhập</a></c:otherwise>
        </c:choose>
    </div>
</div>
<script src="/js/menu-tai-khoan.js?v=1" defer></script>
