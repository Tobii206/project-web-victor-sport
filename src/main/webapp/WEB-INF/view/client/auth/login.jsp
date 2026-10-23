<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <link rel="icon" type="image/svg+xml" href="/logo/favicon-victorsport.svg?v=1">
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Đăng nhập — VictorSport</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Outfit:wght@500;600;700;800;900&family=Plus+Jakarta+Sans:wght@400;500;600;700;800&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="/css/trang-chu.css?v=20261023-ui-1">
    <link rel="stylesheet" href="/css/giao-dien-chung.css?v=20261023-complete-1">
</head>
<body>
<%@ include file="/WEB-INF/view/client/layout/header.jsp" %>
<main class="account-container store-container">
    <a class="account-back" href="/">← Về trang chủ</a>
    <div class="account-heading"><p class="eyebrow">TÀI KHOẢN VICTORSPORT</p><h1>Đăng nhập</h1></div>
    <c:if test="${not empty error}"><div class="account-error" role="alert" data-vs-flash="error"><c:out value="${error}"/></div></c:if>
    <c:if test="${not empty success}"><p role="status" data-vs-flash="success"><c:out value="${success}"/></p></c:if>
    <div class="account-layout">
        <section class="account-welcome">
            <div class="account-avatar"><img src="/images/nguoi-dung.svg" alt="" width="40" height="40"></div>
            <h2>Chào mừng bạn<br>đến VictorSport.</h2>
            <p>Website bán giày thể thao</p>
        </section>
        <section class="account-card">
            <h2>Đăng nhập tài khoản</h2>
            <p class="subtle">Vui lòng đăng nhập bằng email</p>
            <form class="account-form" method="post" action="/login" autocomplete="off">
                <input type="hidden" name="quayLai" value="${quayLai}">
                <label for="taiKhoan">Tên tài khoản hoặc email</label>
                <input id="taiKhoan" name="taiKhoan" type="text" value="" autocomplete="off" autocapitalize="none" spellcheck="false" maxlength="100" pattern="([a-zA-Z0-9._\-]+|[a-zA-Z0-9]+([._%+\-][a-zA-Z0-9]+)*@[gG][mM][aA][iI][lL]\.[cC][oO][mM])" title="Nhập tên tài khoản hoặc email có dạng ten@gmail.com" required>
                <label for="matKhau">Mật khẩu</label>
                <input id="matKhau" name="matKhau" type="password" value="" autocomplete="new-password" minlength="6" title="Mật khẩu phải có ít nhất 6 ký tự" required>
                <button class="store-button" type="submit">Đăng nhập</button>
            </form>
            <p><a href="/dang-ky">Tạo tài khoản</a> · <a href="/quen-mat-khau">Quên mật khẩu?</a></p>
        </section>
    </div>
</main>
</body>
</html>
