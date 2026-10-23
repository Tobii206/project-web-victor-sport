<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <link rel="icon" type="image/svg+xml" href="/logo/favicon-victorsport.svg?v=1">
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Thông tin tài khoản — VictorSport</title>
    <link rel="stylesheet" href="/css/trang-chu.css?v=20261023-ui-1">
<link rel="stylesheet" href="/css/giao-dien-chung.css?v=20261023-complete-1"></head>
<body class="store-page">
<%@ include file="/WEB-INF/view/client/layout/header.jsp" %>
<main class="account-container store-container">
<c:if test="${not empty success}"><p role="status" data-vs-flash="success"><c:out value="${success}"/></p></c:if>
    <a class="account-back" href="/">← Về trang chủ</a>
    <div class="account-heading"><p class="eyebrow">TÀI KHOẢN VICTORSPORT</p><h1>Thông tin tài khoản</h1><p>Đồng hành cùng bạn trong mỗi trải nghiệm tại VictorSport.</p></div>
    <c:if test="${not empty error}"><div class="account-error" role="alert" data-vs-flash="error"><c:out value="${error}"/></div></c:if>
    <c:choose>
        <c:when test="${not empty thongTin}">
            <div class="account-layout">
                <aside class="account-card account-sidebar">
                    <div class="account-avatar"><img src="/images/nguoi-dung.svg" alt="" width="40" height="40"></div>
                    <h2><c:out value="${thongTin.ho_ten}"/></h2>
                    <span class="account-role"><c:choose><c:when test="${laNhanVien}">Nhân viên</c:when><c:otherwise>Khách hàng</c:otherwise></c:choose></span>
                    <p class="subtle"><c:out value="${thongTin.ma_tai_khoan}"/></p>
                    <c:if test="${laNhanVien}"><a class="store-button" href="${sessionScope.nguoiDungDangNhap.trangLamViec}">Vào trang làm việc</a></c:if>
                    <c:if test="${not laNhanVien}"><a class="store-button" href="/don-hang">Đơn hàng của tôi</a><a class="account-back" href="/#san-pham">Tiếp tục mua sắm</a></c:if>
                    <form method="post" action="/tai-khoan/dang-xuat"><button class="account-logout" type="submit">Đăng xuất</button></form>
                </aside>
                <section class="account-card">
                    <h2>Thông tin cá nhân</h2>
                    <c:if test="${not laNhanVien}"><div class="account-profile-actions"><a href="/tai-khoan/chinh-sua">Chỉnh sửa thông tin</a><a href="/tai-khoan/doi-mat-khau">Đổi mật khẩu</a><a href="/tai-khoan/dia-chi">Địa chỉ của tôi</a></div></c:if>
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
            <section class="account-card"><p>Vui lòng đăng nhập để xem thông tin tài khoản.</p><a class="store-button" href="/login?quayLai=tai-khoan">Đăng nhập</a></section>
        </c:otherwise>
    </c:choose>
    <c:if test="${not laNhanVien and not empty sessionScope.khachHangOnlineDangNhap}">
        <p><a class="store-button" href="/tai-khoan/dia-chi">Địa chỉ của tôi</a></p>
    </c:if>
</main>
<footer class="store-footer"><div class="store-container footer-content"><a class="store-logo" href="/" aria-label="Trang chủ VictorSport"><img src="/logo/logo.png" alt="VictorSport" width="210" height="67"></a><a href="/#san-pham">Khám phá sản phẩm</a><small>© VictorSport</small></div></footer>
</body>
</html>
