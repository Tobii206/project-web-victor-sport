<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<fmt:setLocale value="vi_VN"/>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>VictorSport — Hệ thống giày thể thao & bóng đá chính hãng</title>
    <meta name="description" content="VictorSport chuyên cung cấp giày đá bóng sân cỏ nhân tạo, giày chạy bộ và sneaker thể thao chính hãng từ Nike, Adidas, Puma, Mizuno, Asics.">
    <link rel="icon" type="image/svg+xml" href="/logo/favicon-victorsport.svg?v=1">
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Outfit:wght@500;600;700;800;900&family=Plus+Jakarta+Sans:wght@400;500;600;700;800&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="/css/trang-chu.css?v=20261023-ui-1">
    <link rel="stylesheet" href="/css/giao-dien-chung.css?v=20261023-complete-1">
</head>
<body class="store-page">

<!-- 1. Top Promo Ticker -->
<aside class="announcement" aria-label="Thông báo ưu đãi">
    <div class="announcement-inner">
        <div class="announcement-ticker">
            <span>⚡ <strong>MIỄN PHÍ VẬN CHUYỂN</strong> cho đơn nội thành</span>
            <span>•</span>
            <span class="highlight">🔥 CAM KẾT CHÍNH HÃNG 100%</span>
            <span>•</span>
            <span>🔄 ĐỔI SIZE TẬN NƠI TRONG 7 NGÀY</span>
        </div>
        <div class="announcement-links">
            <a href="tel:0325327602">Hotline: 0325.327.602</a>
            <span>|</span>
            <a href="#lien-he">Hệ thống Showroom</a>
        </div>
    </div>
</aside>

<!-- 2. Glassmorphic Sticky Header -->
<header class="store-header">
    <a class="store-logo" href="/" aria-label="Trang chủ VictorSport">
        <img src="/logo/logo-nobackgroud.png" alt="VictorSport" width="210" height="67">
    </a>
    <nav aria-label="Điều hướng chính">
        <a href="#trang-chu" class="active">Trang chủ</a>
        <a href="#san-pham">Sản phẩm</a>
        <a href="#nhu-cau">Nhu cầu sử dụng</a>
        <a href="#flash-sale">Flash Sale 🔥</a>
        <a href="#lien-he">Showroom</a>
    </nav>
    <%@ include file="/WEB-INF/view/client/layout/header-actions.jsp" %>
</header>

<main>
    <!-- 3. Dynamic Sports Hero Banner -->
    <section id="trang-chu" class="shop-hero store-container">
        <div class="shop-hero-copy">
            <div class="hero-pill-badge">
                <span class="dot"></span> BỘ SƯU TẬP 2026 • CHÍNH HÃNG 100%
            </div>
            <h1>BỨC PHÁ TỐC ĐỘ.<br><span class="hero-gradient-text">CHINH PHỤC ĐỈNH CAO.</span></h1>
            <p>Khám phá các dòng giày đá bóng sân cỏ nhân tạo, chạy bộ marathon và sneaker thể thao chính hãng đỉnh nhất từ Nike, Adidas, Puma, Mizuno, Asics.</p>

            <div class="hero-cta-group">
                <a class="store-button" href="#san-pham">Mua sắm ngay <span aria-hidden="true">⚡</span></a>
                <a class="store-button secondary" href="#flash-sale">Khám phá Flash Sale 🔥</a>
            </div>

            <div class="shop-hero-stats">
                <div class="stat-item">
                    <strong>${sanPham.size()}+</strong>
                    <span>Mẫu giày<br>tuyển chọn</span>
                </div>
                <div class="stat-item">
                    <strong>${thuongHieu.size()}</strong>
                    <span>Thương hiệu<br>toàn cầu</span>
                </div>
                <div class="stat-item">
                    <strong>100%</strong>
                    <span>Đảm bảo<br>chính hãng</span>
                </div>
            </div>
        </div>

        <!-- Hero Featured Shoe Showcase with 3D Spotlight -->
        <div class="shop-featured">
            <span class="shop-featured-label">⚡ BỘ SƯU TẬP NỔI BẬT</span>
            <c:choose>
                <c:when test="${not empty sanPham}">
                    <div class="shop-featured-slides">
                        <c:forEach items="${sanPham}" var="noiBat" varStatus="viTri">
                            <div class="shop-featured-slide ${viTri.first ? 'active' : ''}" aria-hidden="${not viTri.first}" <c:if test="${not viTri.first}">inert</c:if>>
                                <c:set var="anhNoiBat" value="${noiBat.anh}"/>
                                <c:if test="${empty anhNoiBat}"><c:set var="anhNoiBat" value="/images/giay-mac-dinh.svg"/></c:if>
                                <c:if test="${not fn:startsWith(anhNoiBat, '/') and not fn:startsWith(anhNoiBat, 'http')}"><c:set var="anhNoiBat" value="/${anhNoiBat}"/></c:if>

                                <div class="hero-shoe-art">
                                    <div class="hero-shoe-glow"></div>
                                    <a href="/san-pham/${noiBat.id}" aria-label="Xem ${noiBat.ten_san_pham}">
                                        <img src="<c:out value='${anhNoiBat}'/>" alt="<c:out value='${noiBat.ten_san_pham}'/>" width="640" height="420" onerror="this.onerror=null;this.src='/images/giay-mac-dinh.svg';">
                                    </a>
                                </div>

                                <div class="shop-featured-info">
                                    <div>
                                        <p><c:out value="${noiBat.ten_thuong_hieu}"/></p>
                                        <h2><a href="/san-pham/${noiBat.id}"><c:out value="${noiBat.ten_san_pham}"/></a></h2>
                                    </div>
                                    <a class="shop-featured-arrow" href="/san-pham/${noiBat.id}" aria-label="Xem sản phẩm">↗</a>
                                </div>
                            </div>
                        </c:forEach>
                    </div>
                </c:when>
                <c:otherwise>
                    <div class="hero-shoe-art">
                        <div class="hero-shoe-glow"></div>
                        <img src="/images/giay-mac-dinh.svg" alt="Minh họa giày thể thao" width="640" height="420">
                    </div>
                    <p style="color: #94a3b8; text-align: center;">Tìm đôi giày cho mỗi bước chân.</p>
                </c:otherwise>
            </c:choose>
        </div>
    </section>

    <!-- 4. Trust & Benefits Bar (4 Cam kết vàng) -->
    <section class="store-container" aria-label="Cam kết dịch vụ">
        <div class="store-benefits">
            <div>
                <div class="benefit-icon-wrapper">🚚</div>
                <div class="benefit-text">
                    <strong>Miễn phí vận chuyển</strong>
                    <p>Áp dụng cho đơn nội thành</p>
                </div>
            </div>
            <div>
                <div class="benefit-icon-wrapper">🛡️</div>
                <div class="benefit-text">
                    <strong>100% Chính hãng</strong>
                    <p>Sản phẩm có nguồn gốc rõ ràng</p>
                </div>
            </div>
            <div>
                <div class="benefit-icon-wrapper">🔄</div>
                <div class="benefit-text">
                    <strong>Đổi size tận nơi 7 ngày</strong>
                    <p>Hỗ trợ đổi mẫu và size dễ dàng, tiện lợi</p>
                </div>
            </div>
            <div>
                <div class="benefit-icon-wrapper">⚡</div>
                <div class="benefit-text">
                    <strong>Giao hàng hỏa tốc</strong>
                    <p>Kiểm tra hàng trước khi thanh toán</p>
                </div>
            </div>
        </div>
    </section>

    <!-- 5. Flash Sale Live Countdown Banner -->
    <section id="flash-sale" class="store-container">
        <div class="flash-sale-section">
            <div class="flash-sale-title">
                <span class="flash-sale-icon">🔥</span>
                <div class="flash-sale-text">
                    <h3>FLASH SALE HÔM NAY — GIẢM ĐẾN 40%</h3>
                    <p>Cơ hội sở hữu giày thể thao đỉnh cao với giá ưu đãi có hạn!</p>
                </div>
            </div>
            <div class="countdown-timer" id="flashSaleCountdown" data-han="${hanUuDai}" aria-label="Thời gian ưu đãi còn lại">
                <div class="countdown-box"><span class="num" id="fsHours">--</span><span class="label">Giờ</span></div>
                <span class="countdown-colon">:</span>
                <div class="countdown-box"><span class="num" id="fsMinutes">--</span><span class="label">Phút</span></div>
                <span class="countdown-colon">:</span>
                <div class="countdown-box"><span class="num" id="fsSeconds">--</span><span class="label">Giây</span></div>
            </div>
        </div>
    </section>

    <!-- 6. Nhu cầu sử dụng (Sports Categories) -->
    <section id="nhu-cau" class="store-container shop-categories">
        <div class="section-heading">
            <div>
                <p class="eyebrow">TÌM GIÀY PHÙ HỢP VỚI BẠN</p>
                <h2>Chọn theo nhu cầu sử dụng</h2>
            </div>
            <a class="store-button secondary" href="#san-pham" style="color: #0f172a; border-color: #cbd5e1; background: #fff;">Xem tất cả</a>
        </div>
        <div class="shop-category-list">
            <c:forEach items="${boLoc.nhuCau}" var="muc">
                <a href="/?nhuCau=${muc.id}#san-pham" class="shop-category ${nhuCauDangChon == muc.id ? 'selected' : ''}">
                    <div class="category-info">
                        <span class="category-title"><c:out value="${muc.ten}"/></span>
                        <small class="category-desc">Tối ưu form &amp; lực sút</small>
                    </div>
                    <span class="category-arrow" aria-hidden="true">↗</span>
                </a>
            </c:forEach>
        </div>
    </section>

    <!-- 7. Product Catalog & Smart Filters -->
    <section id="san-pham" class="product-section store-container">
        <div class="section-heading">
            <div>
                <p class="eyebrow">BỘ SƯU TẬP VICTORSPORT</p>
                <h2>Giày thể thao dành cho bạn</h2>
            </div>
            <span class="product-count">${sanPham.size()} sản phẩm</span>
        </div>

        <!-- Brand Selector Bar -->
        <div class="shop-brand-list" aria-label="Lọc theo thương hiệu">
            <a href="/#san-pham" class="${empty thuongHieuDangChon ? 'selected' : ''}">Tất cả</a>
            <c:forEach items="${thuongHieu}" var="th">
                <a href="/?thuongHieu=${th.id}#san-pham" class="${thuongHieuDangChon == th.id ? 'selected' : ''}">
                    <c:out value="${th.ten_thuong_hieu}"/>
                </a>
            </c:forEach>
        </div>

        <!-- Smart Filter Form -->
        <form class="product-filters shop-filters" method="get" action="/#san-pham">
            <div class="search-field">
                <label for="tuKhoa">Tìm kiếm nhanh</label>
                <div style="position: relative; width: 100%;">
                    <input id="tuKhoa" name="tuKhoa" value="<c:out value='${tuKhoa}'/>" placeholder="Tìm tên giày, mã sản phẩm, hãng...">
                    <button type="button" id="clearSearchBtn" hidden aria-label="Xóa từ khóa" style="position: absolute; right: 12px; top: 50%; transform: translateY(-50%); background: none; border: none; font-size: 16px; color: #94a3b8;">✕</button>
                </div>
            </div>
            <div>
                <label for="thuongHieu">Thương hiệu</label>
                <select id="thuongHieu" name="thuongHieu">
                    <option value="">Tất cả thương hiệu</option>
                    <c:forEach items="${thuongHieu}" var="th">
                        <option value="${th.id}" <c:if test="${thuongHieuDangChon == th.id}">selected</c:if>><c:out value="${th.ten_thuong_hieu}"/></option>
                    </c:forEach>
                </select>
            </div>
            <div>
                <label for="nhuCau">Vị trí / Nhu cầu</label>
                <select id="nhuCau" name="nhuCau">
                    <option value="">Tất cả nhu cầu</option>
                    <c:forEach items="${boLoc.nhuCau}" var="muc">
                        <option value="${muc.id}" <c:if test="${nhuCauDangChon == muc.id}">selected</c:if>><c:out value="${muc.ten}"/></option>
                    </c:forEach>
                </select>
            </div>
            <div>
                <label for="sapXep">Sắp xếp</label>
                <select id="sapXep" name="sapXep">
                    <option value="moi-nhat">Mới nhất</option>
                    <option value="gia-tang" <c:if test="${sapXep == 'gia-tang'}">selected</c:if>>Giá thấp đến cao</option>
                    <option value="gia-giam" <c:if test="${sapXep == 'gia-giam'}">selected</c:if>>Giá cao đến thấp</option>
                </select>
            </div>

            <!-- Detailed Advanced Filters (Collapsible) -->
            <div>
                <label for="kichThuoc">Kích thước</label>
                <select id="kichThuoc" name="kichThuoc">
                    <option value="">Tất cả kích thước</option>
                    <c:forEach items="${boLoc.kichThuoc}" var="muc">
                        <option value="${muc.id}" <c:if test="${kichThuocDangChon == muc.id}">selected</c:if>><c:out value="${muc.ten}"/></option>
                    </c:forEach>
                </select>
            </div>
            <div>
                <label for="mauSac">Màu sắc</label>
                <select id="mauSac" name="mauSac">
                    <option value="">Tất cả màu sắc</option>
                    <c:forEach items="${boLoc.mauSac}" var="muc">
                        <option value="${muc.id}" <c:if test="${mauSacDangChon == muc.id}">selected</c:if>><c:out value="${muc.ten}"/></option>
                    </c:forEach>
                </select>
            </div>
            <div>
                <label for="chatLieu">Chất liệu</label>
                <select id="chatLieu" name="chatLieu">
                    <option value="">Tất cả chất liệu</option>
                    <c:forEach items="${boLoc.chatLieu}" var="muc">
                        <option value="${muc.id}" <c:if test="${chatLieuDangChon == muc.id}">selected</c:if>><c:out value="${muc.ten}"/></option>
                    </c:forEach>
                </select>
            </div>
            <div>
                <label for="xuatXu">Xuất xứ</label>
                <select id="xuatXu" name="xuatXu">
                    <option value="">Tất cả xuất xứ</option>
                    <c:forEach items="${boLoc.xuatXu}" var="muc">
                        <option value="${muc.id}" <c:if test="${xuatXuDangChon == muc.id}">selected</c:if>><c:out value="${muc.ten}"/></option>
                    </c:forEach>
                </select>
            </div>

            <div class="shop-stock-filter">
                <label>
                    <input type="checkbox" name="conHang" value="true" <c:if test="${conHang}">checked</c:if>>
                    <span>Chỉ hiện mẫu còn hàng</span>
                </label>
            </div>
            <div class="shop-filter-actions">
                <button class="store-button" type="submit">Áp dụng bộ lọc</button>
                <a href="/#san-pham">Xóa bộ lọc</a>
            </div>
        </form>

        <c:if test="${not empty loiDuLieu}">
            <div class="empty-state" role="alert">
                <p><c:out value="${loiDuLieu}"/></p>
                <a class="store-button" href="/#san-pham">Tải lại sản phẩm</a>
            </div>
        </c:if>

        <!-- Product Cards Grid -->
        <div class="product-grid">
            <c:forEach items="${sanPham}" var="sp">
                <article class="product-card">
                    <a class="product-picture" href="/san-pham/${sp.id}" aria-label="Chi tiết ${sp.ten_san_pham}">
                        <!-- Stock Beacon -->
                        <span class="stock-label ${sp.so_luong <= 0 ? 'sold-out' : ''}">
                            <c:choose>
                                <c:when test="${sp.so_luong > 0}">Còn hàng</c:when>
                                <c:otherwise>Tạm hết hàng</c:otherwise>
                            </c:choose>
                        </span>

                        <c:if test="${sp.gia_sau_giam lt sp.gia_goc}">
                            <span class="discount-badge">SALE</span>
                        </c:if>

                        <!-- Image -->
                        <c:set var="imageUrl" value="${sp.anh}"/>
                        <c:if test="${empty imageUrl}"><c:set var="imageUrl" value="/images/giay-mac-dinh.svg"/></c:if>
                        <c:if test="${not fn:startsWith(imageUrl, '/') and not fn:startsWith(imageUrl, 'http')}"><c:set var="imageUrl" value="/${imageUrl}"/></c:if>
                        <img src="<c:out value='${imageUrl}'/>" alt="<c:out value='${sp.ten_san_pham}'/>" loading="lazy" width="640" height="420" onerror="this.onerror=null;this.src='/images/giay-mac-dinh.svg';">
                    </a>

                    <div class="product-content">
                        <p class="product-brand"><c:out value="${sp.ten_thuong_hieu}"/> <span>· <c:out value="${sp.ma_san_pham}"/></span></p>
                        <h3><a href="/san-pham/${sp.id}"><c:out value="${sp.ten_san_pham}"/></a></h3>

                        <div class="shop-product-tags">
                            <c:if test="${not empty sp.ten_vi_tri}"><span><c:out value="${sp.ten_vi_tri}"/></span></c:if>
                            <c:if test="${not empty sp.ten_chat_lieu}"><span><c:out value="${sp.ten_chat_lieu}"/></span></c:if>
                        </div>

                        <p class="shop-product-description"><c:out value="${sp.mo_ta_ngan}"/></p>

                        <p class="product-price">
                            <small>Từ</small>
                            <fmt:formatNumber value="${sp.gia_sau_giam}" maxFractionDigits="0" groupingUsed="true"/> đ
                            <c:if test="${sp.gia_sau_giam lt sp.gia_goc}">
                                <del class="gia-goc"><fmt:formatNumber value="${sp.gia_goc}"/> đ</del>
                            </c:if>
                        </p>

                        <p class="subtle">${sp.so_size} lựa chọn size<c:if test="${not empty sp.ten_xuat_xu}"> · <c:out value="${sp.ten_xuat_xu}"/></c:if></p>
                        <a class="online-detail-link" href="/san-pham/${sp.id}">
                            <span>Xem chi tiết & chọn size</span>
                            <span aria-hidden="true">→</span>
                        </a>
                    </div>
                </article>
            </c:forEach>
        </div>

        <c:if test="${empty sanPham and empty loiDuLieu}">
            <div class="empty-state">
                <h3>Chưa có sản phẩm phù hợp</h3>
                <p>Thử đổi kích thước, màu sắc hoặc xóa bớt điều kiện lọc để tìm đôi giày khác.</p>
                <a class="store-button" href="/#san-pham">Xem tất cả sản phẩm</a>
            </div>
        </c:if>
    </section>

    <!-- 8. Showroom & Store Experience Section -->
    <section id="lien-he" class="store-container">
        <div class="shop-contact">
            <div>
                <p class="eyebrow">HỆ THỐNG CỬA HÀNG VICTORSPORT</p>
                <h2>Thử giày trực tiếp tại Showroom</h2>
                <p>📍 Địa chỉ: Cầu Giấy, Hà Nội • Giờ mở cửa: 08:30 – 22:00 (Tất cả các ngày)</p>
                <p style="margin-top: 6px; font-size: 13.5px; color: #cbd5e1;">Tư vấn đo form chân và chọn size miễn phí từ các chuyên gia thể thao.</p>
            </div>
            <div style="display: flex; gap: 14px; flex-wrap: wrap;">
                <a class="store-button" href="tel:0325327602">📞 Hotline: 0325.327.602</a>
                <a class="store-button secondary" href="https://maps.google.com/?q=Cầu+Giấy+Hà+Nội" target="_blank" rel="noopener">Chỉ đường ↗</a>
            </div>
        </div>
    </section>
</main>

<!-- 9. Modern E-Commerce Footer -->
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
                <li><a href="/?nhuCau=2#san-pham">Giày kiểm soát tiền vệ</a></li>
                <li><a href="/?nhuCau=5#san-pham">Giày chạy bộ marathon</a></li>
                <li><a href="/?nhuCau=6#san-pham">Sneaker thể thao</a></li>
            </ul>
        </div>

        <div class="footer-column">
            <h4>Chính sách & Hỗ trợ</h4>
            <ul>
                <li><a href="/tai-khoan">Tài khoản cá nhân</a></li>
                <li><a href="/tra-cuu-don-hang">Tra cứu đơn hàng</a></li>
                <li><a href="/#nhu-cau">Hướng dẫn chọn size giày</a></li>
                <li><a href="#san-pham">Chính sách đổi trả 7 ngày</a></li>
                <li><a href="#san-pham">Chính sách bảo hành chính hãng</a></li>
            </ul>
        </div>

        <div class="footer-column">
            <h4>Thanh toán & Kết nối</h4>
            <ul>
                <li><span>Thanh toán khi nhận hàng (COD)</span></li>
                <li><span>Chuyển khoản QR ngân hàng</span></li>
                <li><span>Bảo mật dữ liệu tuyệt đối</span></li>
            </ul>
            <div class="footer-payment-badges" style="margin-top: 16px;">
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

<!-- 10. Floating Back to Top Button -->
<button type="button" class="back-to-top" id="backToTopBtn" aria-label="Lên đầu trang">↑</button>

<script src="/js/victor-modern.js?v=20261023-countdown"></script>
</body>
</html>
