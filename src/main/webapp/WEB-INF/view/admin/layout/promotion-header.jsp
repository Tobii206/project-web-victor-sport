<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<header class="topbar">
<div class="topbar-heading"><jsp:include page="/WEB-INF/view/admin/layout/menu-toggle.jsp"/><div><h1><c:out value="${tenChucNang}"/></h1><div class="muted">VictorSport</div></div></div>
<div class="topbar-user"><jsp:include page="/WEB-INF/view/admin/layout/store-link.jsp"/><span><c:out value="${currentUser.hoTen}"/></span>
<form method="post" action="/logout"><button class="button secondary">Đăng xuất</button></form></div></header>
