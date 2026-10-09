<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>${tenChucNang} - Victor Sport</title>
    <link rel="stylesheet" href="/css/victorsport.css">
</head>
<body>
<div class="app-shell">
    <jsp:include page="menu.jsp"/>
    <div class="main-area">
        <header class="topbar">
            <div>
                <h1>${tenChucNang}</h1>
                <div class="muted">Victor Sport</div>
            </div>
            <div class="topbar-user">
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
