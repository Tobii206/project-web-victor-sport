<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<header class="topbar">
    <div class="topbar-heading"><jsp:include page="/WEB-INF/view/admin/layout/menu-toggle.jsp"/><div><h1>${currentUser.suaSanPham ? 'Quản lý sản phẩm' : 'Xem tồn kho'}</h1><div class="muted">VictorSport</div></div></div>
    <div class="topbar-user"><jsp:include page="/WEB-INF/view/admin/layout/store-link.jsp"/><a class="admin-user-chip" href="/tai-khoan" title="Thông tin tài khoản"><span class="admin-user-avatar" aria-hidden="true"><c:out value="${empty currentUser.hoTen ? 'V' : currentUser.hoTen.substring(0,1)}"/></span><span class="admin-user-name"><c:out value="${currentUser.hoTen}"/></span></a><form method="post" action="/logout"><button class="button secondary">Đăng xuất</button></form></div>
</header>
