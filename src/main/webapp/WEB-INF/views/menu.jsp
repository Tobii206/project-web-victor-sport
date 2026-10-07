<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<aside class="side-menu">
    <div class="side-brand">
        <div class="side-brand-title">Victor Sport</div>
        <div class="side-brand-subtitle">Quản lý bán hàng</div>
    </div>
    <nav class="menu-list">
        <a class="menu-item ${activeMenu == 'ban-hang' ? 'active' : ''}" href="/pos">Bán hàng tại quầy</a>
        <a class="menu-item ${activeMenu == 'san-pham' ? 'active' : ''}" href="/quan-ly/san-pham">Quản lý sản phẩm</a>
        <a class="menu-item ${activeMenu == 'hoa-don' ? 'active' : ''}" href="/quan-ly/hoa-don">Quản lý hóa đơn</a>
        <a class="menu-item ${activeMenu == 'dot-giam-gia' ? 'active' : ''}" href="/quan-ly/dot-giam-gia">Quản lý đợt giảm giá</a>
        <a class="menu-item ${activeMenu == 'phieu-giam-gia' ? 'active' : ''}" href="/quan-ly/phieu-giam-gia">Quản lý phiếu giảm giá</a>
        <a class="menu-item ${activeMenu == 'nhan-vien' ? 'active' : ''}" href="/quan-ly/nhan-vien">Quản lý nhân viên</a>
        <a class="menu-item ${activeMenu == 'khach-hang' ? 'active' : ''}" href="/quan-ly/khach-hang">Quản lý khách hàng</a>
        <a class="menu-item ${activeMenu == 'gio-hang' ? 'active' : ''}" href="/quan-ly/gio-hang">Quản lý giỏ hàng</a>
    </nav>
</aside>
