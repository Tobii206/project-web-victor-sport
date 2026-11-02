<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<link rel="stylesheet" href="/css/menu-tai-khoan.css?v=20261102-1">
<div class="account-menu" id="menuTaiKhoan">
    <button class="account-link" type="button" id="nutMenuTaiKhoan" aria-label="Menu tài khoản" title="Menu tài khoản" aria-controls="cacMucTaiKhoan" aria-expanded="false">
        <img src="/images/nguoi-dung.svg" alt="" width="24" height="24">
    </button>
    <div class="account-dropdown" id="cacMucTaiKhoan" inert>
        <c:if test="${not empty hoSoMenuTaiKhoan}">
            <div class="account-menu-profile">
                <img data-avatar-tai-khoan src="<c:out value="${not empty hoSoMenuTaiKhoan.anh_dai_dien ? hoSoMenuTaiKhoan.anh_dai_dien : '/images/nguoi-dung.svg'}"/>" alt="Ảnh đại diện" width="52" height="52" onerror="this.onerror=null;this.src='/images/nguoi-dung.svg';">
                <div><strong><c:out value="${hoSoMenuTaiKhoan.ho_ten}"/></strong><small><c:out value="${hoSoMenuTaiKhoan.vai_tro}"/></small></div>
            </div>
        </c:if>
        <a href="/tra-cuu-don-hang">Tra cứu đơn khách vãng lai</a>
        <a href="/tai-khoan">Thông tin chi tiết</a>
        <c:if test="${not empty sessionScope.khachHangOnlineDangNhap}"><a href="/don-hang">Đơn hàng của tôi</a></c:if>
        <c:if test="${not empty sessionScope.khachHangOnlineDangNhap}"><a href="/tai-khoan/dia-chi">Địa chỉ của tôi</a></c:if>
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
            <c:otherwise><a href="/login">Đăng nhập</a><a href="/dang-ky">Tạo tài khoản</a></c:otherwise>
        </c:choose>
    </div>
</div>
<script src="/js/menu-tai-khoan.js?v=1" defer></script>
