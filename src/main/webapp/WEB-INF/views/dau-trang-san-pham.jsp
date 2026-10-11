<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<header class="topbar">
    <div class="topbar-heading"><jsp:include page="nut-menu-admin.jsp"/><div><h1>${currentUser.suaSanPham ? 'Quản lý sản phẩm' : 'Xem tồn kho'}</h1><div class="muted">VictorSport</div></div></div>
    <div class="topbar-user"><jsp:include page="nut-ve-trang-ban-hang.jsp"/><span><c:out value="${currentUser.hoTen}"/></span><form method="post" action="/logout"><button class="button secondary">Đăng xuất</button></form></div>
</header>
