<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<fmt:setLocale value="vi_VN"/>
<!DOCTYPE html>
<html lang="vi">
<head>
    <link rel="icon" type="image/svg+xml" href="/logo/favicon-victorsport.svg?v=1">
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Giỏ hàng — VictorSport</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Outfit:wght@500;600;700;800;900&family=Plus+Jakarta+Sans:wght@400;500;600;700;800&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="/css/trang-chu.css?v=20261023-ui-1">
    <link rel="stylesheet" href="/css/giao-dien-chung.css?v=20261023-complete-1">
</head>
<body>
<%@ include file="/WEB-INF/view/client/layout/header.jsp" %>

<main class="store-container online-container">
    <div style="margin-bottom: 24px;">
        <p class="online-breadcrumb"><a href="/">Trang chủ</a> / Giỏ hàng</p>
        <h1 style="font-family: var(--vs-font-heading); font-size: 32px; font-weight: 800; margin: 0;">Giỏ hàng của bạn</h1>
    </div>

    <c:if test="${not empty error}">
        <div class="account-error" role="alert" data-vs-flash="error"><c:out value="${error}"/></div>
    </c:if>
    <c:if test="${not empty success}">
        <div class="online-success" role="status" data-vs-flash="success"><c:out value="${success}"/></div>
    </c:if>

    <c:forEach items="${danhSach}" var="dong">
        <div class="account-card online-cart-row">
            <div>
                <a href="/san-pham/${dong.id_san_pham}" style="font-family: var(--vs-font-heading); font-size: 17px; font-weight: 700; color: #0f172a;">
                    <c:out value="${dong.ten_san_pham}"/>
                </a>
                <p class="subtle" style="margin: 4px 0 8px; font-size: 13px;">
                    <c:out value="${dong.ten_kich_thuoc}"/> · <c:out value="${dong.ten_mau_sac}"/> · <c:out value="${dong.ten_form_chan}"/>
                </p>
                <p style="margin: 0; font-family: var(--vs-font-heading); font-weight: 700; color: #ff5e1e;">
                    <fmt:formatNumber value="${dong.gia_sau_giam}"/> đ / đôi
                    <c:if test="${dong.gia_sau_giam lt dong.gia_ban}">
                        <del class="gia-goc"><fmt:formatNumber value="${dong.gia_ban}"/> đ</del>
                    </c:if>
                </p>
            </div>

            <form method="post" action="/gio-hang/sua">
                <input type="hidden" name="idPhienBan" value="${dong.id}">
                <input class="online-cart-quantity" type="number" name="soLuong" value="${dong.so_luong_mua}" min="0" aria-label="Số lượng" style="border-radius: 10px; font-weight: 700;">
                <button class="store-button secondary" type="submit" style="min-height: 40px; padding: 8px 16px; font-size: 13px;">Cập nhật</button>
            </form>

            <strong style="font-family: var(--vs-font-heading); font-size: 19px; font-weight: 800; color: #0f172a;">
                <fmt:formatNumber value="${dong.thanh_tien}"/> đ
            </strong>

            <form method="post" action="/gio-hang/sua">
                <input type="hidden" name="idPhienBan" value="${dong.id}">
                <input type="hidden" name="soLuong" value="0">
                <button class="account-logout" type="submit" style="cursor: pointer;">Xóa</button>
            </form>
        </div>
    </c:forEach>

    <c:if test="${empty danhSach and empty error}">
        <div class="empty-state">
            <h3>Giỏ hàng đang trống</h3>
            <p>Khám phá các mẫu giày bóng đá & thể thao chính hãng mới nhất.</p>
            <a class="store-button" href="/#san-pham">Mua sắm ngay ⚡</a>
        </div>
    </c:if>

    <c:if test="${not empty error}">
        <c:forEach items="${gioDaChon}" var="muc">
            <form method="post" action="/gio-hang/sua">
                <input type="hidden" name="idPhienBan" value="${muc.key}">
                <input type="hidden" name="soLuong" value="0">
                <button class="account-logout">Xóa mẫu giày #${muc.key} khỏi giỏ</button>
            </form>
        </c:forEach>
    </c:if>

    <c:if test="${not empty danhSach}">
        <div class="online-cart-bottom" style="display: flex; align-items: center; justify-content: space-between; flex-wrap: wrap; gap: 20px;">
            <a href="/#san-pham" style="font-weight: 600; color: #64748b;">← Tiếp tục mua sắm</a>
            <div style="display: flex; align-items: center; gap: 24px;">
                <span style="font-size: 15px; color: #64748b;">Tổng tiền:</span>
                <strong style="font-family: var(--vs-font-heading); font-size: 26px; font-weight: 800; color: #ff5e1e;">
                    <fmt:formatNumber value="${tongTien}"/> đ
                </strong>
                <a class="store-button" href="/thanh-toan">Tiến hành đặt hàng →</a>
            </div>
        </div>
    </c:if>
</main>

<footer class="store-footer">
    <div class="store-container footer-content">
        <div class="footer-brand">
            <a class="store-logo" href="/" aria-label="VictorSport">
                <img src="/logo/logo-nobackgroud.png" alt="VictorSport" width="200" height="64" style="filter: brightness(1.1);">
            </a>
            <p>Hệ thống giày thể thao & bóng đá chính hãng hàng đầu. Đồng hành cùng mỗi bước chạy và niềm đam mê thể thao của bạn.</p>
            <div class="footer-hotline">
                <span>Hotline đặt hàng:</span>
                <a href="tel:0325327602" style="color: inherit;">0325.327.602</a>
            </div>
        </div>
        <div class="footer-column">
            <h4>Sản phẩm</h4>
            <ul>
                <li><a href="/#san-pham">Tất cả giày thể thao</a></li>
                <li><a href="/?nhuCau=1#san-pham">Giày bóng đá tiền đạo</a></li>
                <li><a href="/?nhuCau=5#san-pham">Giày chạy bộ marathon</a></li>
            </ul>
        </div>
        <div class="footer-column">
            <h4>Chính sách & Hỗ trợ</h4>
            <ul>
                <li><a href="/tra-cuu-don-hang">Tra cứu đơn hàng</a></li>
                <li><a href="/#nhu-cau">Hướng dẫn chọn size</a></li>
                <li><a href="#san-pham">Chính sách đổi trả 7 ngày</a></li>
            </ul>
        </div>
        <div class="footer-column">
            <h4>Thanh toán & Kết nối</h4>
            <div class="footer-payment-badges">
                <span class="payment-tag">COD</span>
                <span class="payment-tag">VietQR</span>
                <span class="payment-tag">Napas 247</span>
                <span class="payment-tag">GHN Express</span>
            </div>
        </div>
    </div>
    <div class="store-container footer-bottom">
        <div>© 2026 VictorSport. Bản quyền thuộc về Cửa hàng Thể thao VictorSport.</div>
        <div>Đồng hành cùng mỗi bước chân • Designed with Excellence</div>
    </div>
</footer>
<button type="button" class="back-to-top" id="backToTopBtn" aria-label="Lên đầu trang">↑</button>
<script src="/js/victor-modern.js?v=20261023-countdown"></script>
</body>
</html>
