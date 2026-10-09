<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Thông tin tài khoản — VictorSport</title>
    <link rel="stylesheet" href="/css/trang-chu.css?v=gio-hang-1">
</head>
<body class="store-page">
<header class="store-header">
    <a class="store-logo" href="/"><span class="logo-mark">V</span> VICTOR<span>SPORT</span></a>
    <nav aria-label="Điều hướng chính"><a href="/">Trang chủ</a><a href="/#san-pham">Sản phẩm</a><a href="/#thuong-hieu">Thương hiệu</a><a href="/#gioi-thieu">Về VictorSport</a></nav>
    <%@ include file="icon-thanh-menu.jsp" %>
</header>
<main class="account-container store-container">
    <a class="account-back" href="/">← Về trang chủ</a>
    <div class="account-heading"><p class="eyebrow">TÀI KHOẢN VICTORSPORT</p><h1>Thông tin tài khoản</h1><p>Đồng hành cùng bạn trong mỗi trải nghiệm tại VictorSport.</p></div>
    <c:if test="${not empty error}"><div class="account-error" role="alert"><c:out value="${error}"/></div></c:if>
    <c:choose>
        <c:when test="${not empty thongTin}">
            <div class="account-layout">
                <aside class="account-card account-sidebar">
                    <div class="account-avatar"><img src="/images/nguoi-dung.svg" alt="" width="40" height="40"></div>
                    <h2><c:out value="${thongTin.ho_ten}"/></h2>
                    <span class="account-role"><c:choose><c:when test="${laNhanVien}">Nhân viên</c:when><c:otherwise>Khách hàng</c:otherwise></c:choose></span>
                    <p class="subtle"><c:out value="${thongTin.ma_tai_khoan}"/></p>
                    <c:if test="${laNhanVien}"><a class="store-button" href="/pos">Vào trang bán hàng</a></c:if>
                    <c:if test="${not laNhanVien}"><a class="store-button" href="/#san-pham">Khám phá sản phẩm</a></c:if>
                    <form method="post" action="/tai-khoan/dang-xuat"><button class="account-logout" type="submit">Đăng xuất</button></form>
                </aside>
                <section class="account-card">
                    <h2>Thông tin cá nhân</h2>
                    <p class="subtle">Thông tin đang được lưu trong tài khoản của bạn.</p>
                    <dl class="account-info">
                        <div><dt>Họ và tên</dt><dd><c:out value="${thongTin.ho_ten}"/></dd></div>
                        <div><dt>Mã tài khoản</dt><dd><c:out value="${thongTin.ma_tai_khoan}"/></dd></div>
                        <div><dt>Email</dt><dd><c:choose><c:when test="${empty thongTin.email}">Không có</c:when><c:otherwise><c:out value="${thongTin.email}"/></c:otherwise></c:choose></dd></div>
                        <div><dt>Số điện thoại</dt><dd><c:choose><c:when test="${empty thongTin.so_dien_thoai}">Không có</c:when><c:otherwise><c:out value="${thongTin.so_dien_thoai}"/></c:otherwise></c:choose></dd></div>
                        <div><dt>Ngày sinh</dt><dd><c:choose><c:when test="${empty thongTin.ngay_sinh}">Không có</c:when><c:otherwise><fmt:formatDate value="${thongTin.ngay_sinh}" pattern="dd/MM/yyyy"/></c:otherwise></c:choose></dd></div>
                        <div><dt>Vai trò</dt><dd><c:out value="${thongTin.vai_tro}"/></dd></div>
                        <c:if test="${not laNhanVien}"><div><dt>Tên tài khoản</dt><dd><c:out value="${thongTin.ten_tai_khoan}"/></dd></div></c:if>
                        <div class="account-address"><dt>Địa chỉ</dt><dd>
                            <c:choose>
                                <c:when test="${empty thongTin.dia_chi_cu_the and empty thongTin.phuong and empty thongTin.quan and empty thongTin.thanh_pho}">Không có</c:when>
                                <c:otherwise>
                                    <c:out value="${thongTin.dia_chi_cu_the}"/>
                                    <c:if test="${not empty thongTin.phuong}"><c:if test="${not empty thongTin.dia_chi_cu_the}">, </c:if><c:out value="${thongTin.phuong}"/></c:if>
                                    <c:if test="${not empty thongTin.quan}"><c:if test="${not empty thongTin.dia_chi_cu_the or not empty thongTin.phuong}">, </c:if><c:out value="${thongTin.quan}"/></c:if>
                                    <c:if test="${not empty thongTin.thanh_pho}"><c:if test="${not empty thongTin.dia_chi_cu_the or not empty thongTin.phuong or not empty thongTin.quan}">, </c:if><c:out value="${thongTin.thanh_pho}"/></c:if>
                                </c:otherwise>
                            </c:choose>
                        </dd></div>
                    </dl>
                </section>
            </div>
        </c:when>
        <c:when test="${loiKetNoi}"><div class="account-card"><a class="store-button" href="/tai-khoan">Thử tải lại</a></div></c:when>
        <c:otherwise>
            <div class="account-layout">
                <section class="account-welcome"><div class="account-avatar"><img src="/images/nguoi-dung.svg" alt="" width="40" height="40"></div><h2>Chào mừng bạn<br>đến VictorSport.</h2><p>Đăng nhập để xem thông tin cá nhân của bạn.</p><a href="/login?quayLai=tai-khoan">Bạn là nhân viên? Đăng nhập tại đây →</a></section>
                <section class="account-card">
                    <h2>Đăng nhập khách hàng</h2>
                    <p class="subtle">Sử dụng tài khoản mua sắm của bạn.</p>
                    <form class="account-form" method="post" action="/tai-khoan/dang-nhap" autocomplete="off">
                        <label for="taiKhoan">Tên tài khoản</label><input id="taiKhoan" name="taiKhoan" autocomplete="off" autocapitalize="none" spellcheck="false" maxlength="50" value="" required>
                        <label for="matKhau">Mật khẩu</label><input id="matKhau" name="matKhau" type="password" autocomplete="new-password" value="" required>
                        <button class="store-button" type="submit">Đăng nhập</button>
                    </form>
                </section>
            </div>
        </c:otherwise>
    </c:choose>
</main>
<footer class="store-footer"><div class="store-container footer-content"><a class="store-logo" href="/">VICTOR<span>SPORT</span></a><a href="/#san-pham">Khám phá sản phẩm</a><small>© VictorSport</small></div></footer>
</body>
</html>
