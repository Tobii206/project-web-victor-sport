<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <link rel="icon" type="image/svg+xml" href="/images/favicon-victorsport.svg?v=1">
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Đăng nhập — VictorSport</title>
    <link rel="stylesheet" href="/css/trang-chu.css?v=muot-1">
</head>
<body>
<%@ include file="dau-trang-online.jsp" %>
<main class="account-container store-container">
    <a class="account-back" href="/">← Về trang chủ</a>
    <div class="account-heading"><p class="eyebrow">TÀI KHOẢN VICTORSPORT</p><h1>Đăng nhập</h1></div>
    <c:if test="${not empty error}"><div class="account-error" role="alert"><c:out value="${error}"/></div></c:if>
    <div class="account-layout">
        <section class="account-welcome">
            <div class="account-avatar"><img src="/images/nguoi-dung.svg" alt="" width="40" height="40"></div>
            <h2>Chào mừng bạn<br>đến VictorSport.</h2>
            <p>Một trang đăng nhập dành cho khách hàng, nhân viên và quản lý.</p>
        </section>
        <section class="account-card">
            <h2>Đăng nhập tài khoản</h2>
            <p class="subtle">Khách hàng dùng tên tài khoản. Nhân viên và quản lý dùng email.</p>
            <form class="account-form" method="post" action="/login" autocomplete="off">
                <input type="hidden" name="quayLai" value="${quayLai}">
                <label for="taiKhoan">Tài khoản hoặc email nhân viên</label>
                <input id="taiKhoan" name="taiKhoan" type="text" value="" autocomplete="off" autocapitalize="none" spellcheck="false" maxlength="100" required>
                <label for="matKhau">Mật khẩu</label>
                <input id="matKhau" name="matKhau" type="password" value="" autocomplete="new-password" required>
                <button class="store-button" type="submit">Đăng nhập</button>
            </form>
        </section>
    </div>
</main>
</body>
</html>