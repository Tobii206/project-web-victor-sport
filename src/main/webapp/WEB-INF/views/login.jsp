<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Đăng nhập - Victor Sport</title>
    <link rel="stylesheet" href="/css/victorsport.css">
</head>
<body>
<main class="login-page">
    <section class="login-card">
        <div class="brand">
            <h1 class="brand-title">Victor Sport</h1>
            <p class="brand-subtitle">Đăng nhập hệ thống bán hàng</p>
        </div>

        <% if (request.getAttribute("error") != null) { %>
            <div class="alert error"><%= request.getAttribute("error") %></div>
        <% } %>

        <form method="post" action="/login" autocomplete="off">
            <div class="field">
                <label for="emailDangNhap">Email</label>
                <input class="input" id="emailDangNhap" name="emailDangNhap" type="email" value="" autocomplete="off" autocapitalize="none" spellcheck="false" required>
            </div>
            <div class="field">
                <label for="matKhauDangNhap">Mật khẩu</label>
                <input class="input" id="matKhauDangNhap" name="matKhauDangNhap" type="password" value="" autocomplete="new-password" required>
            </div>
            <button class="button full" type="submit">Đăng nhập</button>
        </form>
    </section>
</main>
</body>
</html>
