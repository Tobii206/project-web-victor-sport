<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <link rel="icon" type="image/svg+xml" href="/logo/favicon-victorsport.svg?v=1">
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>${tenChucNang} - Victor Sport</title>
    <link rel="stylesheet" href="/css/victorsport.css?v=admin-20261014">
<link rel="stylesheet" href="/css/giao-dien-chung.css?v=20261023-complete-1"></head>
<body>
<div class="app-shell">
    <jsp:include page="/WEB-INF/view/admin/layout/sidebar.jsp"/>
    <div class="main-area">
        <header class="topbar">
            <div class="topbar-heading">
                <jsp:include page="/WEB-INF/view/admin/layout/menu-toggle.jsp"/>
                <div>
                    <h1>${tenChucNang}</h1>
                    <div class="muted">Victor Sport</div>
                </div>
            </div>
            <div class="topbar-user">
                <jsp:include page="/WEB-INF/view/admin/layout/store-link.jsp"/>
                <span>${currentUser.hoTen} - ${currentUser.tenQuyen}</span>
                <form method="post" action="/logout">
                    <button class="button secondary" type="submit">Đăng xuất</button>
                </form>
            </div>
        </header>
        <main class="content-layout">
            <section class="panel">
                <div class="panel-header">
                    <h2>${tenChucNang}</h2>
                </div>
                <div class="panel-body empty-state">
                    <p class="muted">Khu vực quản lý VictorSport</p>
                    <h2>Chức năng đang được cập nhật</h2>
                    <p>Bạn có thể tiếp tục sử dụng các chức năng trong thanh điều hướng.</p>
                    <a class="button secondary" href="${currentUser.trangLamViec}">Về trang làm việc</a>
                </div>
            </section>
        </main>
    </div>
</div>
</body>
</html>
