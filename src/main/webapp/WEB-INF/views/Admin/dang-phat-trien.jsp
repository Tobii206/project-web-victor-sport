<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <link rel="icon" type="image/svg+xml" href="/logo/favicon-victorsport.svg?v=1">
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>${tenChucNang} - Victor Sport</title>
    <link rel="stylesheet" href="/css/victorsport.css?v=admin-20261014">
<link rel="stylesheet" href="/css/giao-dien-chung.css?v=20261018-2"></head>
<body>
<div class="app-shell">
    <jsp:include page="/WEB-INF/views/Admin/menu.jsp"/>
    <div class="main-area">
        <header class="topbar">
            <div class="topbar-heading">
                <jsp:include page="/WEB-INF/views/Admin/nut-menu-admin.jsp"/>
                <div>
                    <h1>${tenChucNang}</h1>
                    <div class="muted">Victor Sport</div>
                </div>
            </div>
            <div class="topbar-user">
                <jsp:include page="/WEB-INF/views/Admin/nut-ve-trang-ban-hang.jsp"/>
                <span>${currentUser.hoTen} - ${currentUser.tenQuyen}</span>
                <form method="post" action="/logout">
                    <button class="button secondary" type="submit">Đăng xuất</button>
                </form>
            </div>
        </header>
        <main class="content-layout">
            <section class="panel">
                <div class="panel-header">
                   
                </div>
                <div class="panel-body placeholder-blank"></div>
            </section>
        </main>
    </div>
</div>
</body>
</html>
