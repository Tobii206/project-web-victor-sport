<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<aside class="side-menu" id="menuAdmin">
    <div class="side-brand">
        <a href="/" aria-label="Trang chủ VictorSport"><img class="side-brand-logo" src="/images/logo-victorsport.png" alt="VictorSport" width="202" height="64"></a>
        <div class="side-brand-subtitle"><span class="side-status-dot"></span> Khu vực quản lý</div>
    </div>
    <nav class="menu-list" aria-label="Menu quản lý">
        <c:if test="${currentUser.quanLy}">
        <span class="menu-item" aria-disabled="true" title="Chưa triển khai">
            <svg viewBox="0 0 24 24" aria-hidden="true"><path d="M4 3v18h17M8 17v-5M13 17V8M18 17V5"/></svg><span>Thống kê</span>
        </span>
        </c:if>
        <p class="menu-caption">BÁN HÀNG</p>
        <c:if test="${currentUser.banHangTaiQuay}"><a class="menu-item ${activeMenu == 'ban-hang' ? 'active' : ''}" href="/pos" aria-current="${activeMenu == 'ban-hang' ? 'page' : 'false'}">
            <svg viewBox="0 0 24 24" aria-hidden="true"><rect x="3" y="3" width="18" height="12" rx="2"/><path d="M8 21h8M12 15v6M7 8h4M7 11h7"/></svg><span>Bán hàng tại quầy</span>
        </a>
        </c:if><a class="menu-item" href="/">
            <svg viewBox="0 0 24 24" aria-hidden="true"><path d="M4 8h16v13H4zM8 8V6a4 4 0 0 1 8 0v2"/></svg><span>Trang bán hàng</span>
        </a>
        <c:if test="${currentUser.xemSanPham}">
        <a class="menu-item ${activeMenu == 'san-pham' ? 'active' : ''}" href="/quan-ly/san-pham" aria-current="${activeMenu == 'san-pham' ? 'page' : 'false'}">
            <svg viewBox="0 0 24 24" aria-hidden="true"><path d="m12 3 9 5v8l-9 5-9-5V8zM3 8l9 5 9-5M12 13v8M7 5.8l9 5"/></svg><span>${currentUser.suaSanPham ? 'Quản lý sản phẩm' : 'Xem tồn kho'}</span>
        </a>
        </c:if>
        <c:if test="${currentUser.xemHoaDon}"><a class="menu-item ${activeMenu == 'hoa-don' ? 'active' : ''}" href="/quan-ly/hoa-don" aria-current="${activeMenu == 'hoa-don' ? 'page' : 'false'}">
            <svg viewBox="0 0 24 24" aria-hidden="true"><path d="M5 3h14v18l-3-2-4 2-4-2-3 2zM9 7h6M9 11h6M9 15h3"/></svg><span>Quản lý hóa đơn</span>
        </a>
        </c:if>
        <c:if test="${currentUser.quanLy}">
        <span class="menu-item" aria-disabled="true" title="Chưa triển khai">
            <svg viewBox="0 0 24 24" aria-hidden="true"><path d="M3 6h11v12H3zM14 10h4l3 4v4h-7M14 14h7"/><circle cx="7" cy="18" r="2"/><circle cx="18" cy="18" r="2"/></svg><span>Quản lý vận chuyển</span>
        </span>
        <p class="menu-caption">ƯU ĐÃI</p>
        <a class="menu-item ${activeMenu == 'dot-giam-gia' ? 'active' : ''}" href="/quan-ly/dot-giam-gia" aria-current="${activeMenu == 'dot-giam-gia' ? 'page' : 'false'}">
            <svg viewBox="0 0 24 24" aria-hidden="true"><path d="M3 3h8l10 10-8 8L3 11z"/><circle cx="7.5" cy="7.5" r="1"/><path d="m11 10 5 5"/></svg><span>Quản lý đợt giảm giá</span>
        </a>
        <a class="menu-item ${activeMenu == 'phieu-giam-gia' ? 'active' : ''}" href="/quan-ly/phieu-giam-gia" aria-current="${activeMenu == 'phieu-giam-gia' ? 'page' : 'false'}">
            <svg viewBox="0 0 24 24" aria-hidden="true"><path d="M3 6h18v4a2 2 0 0 0 0 4v4H3v-4a2 2 0 0 0 0-4zM14 6v3M14 11v2M14 15v3"/></svg><span>Quản lý phiếu giảm giá</span>
        </a>
        <p class="menu-caption">CHỨC NĂNG QUẢN LÝ</p>
        <a class="menu-item ${activeMenu == 'nhan-vien' ? 'active' : ''}" href="/quan-ly/nhan-vien" aria-current="${activeMenu == 'nhan-vien' ? 'page' : 'false'}">
            <svg viewBox="0 0 24 24" aria-hidden="true"><circle cx="12" cy="7" r="4"/><path d="M4 21v-2a8 8 0 0 1 16 0v2M10 16l2 2 2-2M12 18v3"/></svg><span>Quản lý nhân viên</span>
        </a>
        </c:if><c:if test="${currentUser.quanLy or currentUser.xemKhachHang}"><a class="menu-item ${activeMenu == 'khach-hang' ? 'active' : ''}" href="/quan-ly/khach-hang" aria-current="${activeMenu == 'khach-hang' ? 'page' : 'false'}">
            <svg viewBox="0 0 24 24" aria-hidden="true"><circle cx="9" cy="7" r="3.5"/><path d="M2 21v-2a7 7 0 0 1 14 0v2M16 4a3.5 3.5 0 0 1 0 7M18 14a6 6 0 0 1 4 5v2"/></svg><span>Quản lý khách hàng</span>
        </a>
        </c:if>
    </nav>
</aside>
