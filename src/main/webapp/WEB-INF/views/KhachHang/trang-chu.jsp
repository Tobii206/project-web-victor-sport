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
    <title>VictorSport — Cửa hàng giày thể thao</title>
    <link rel="icon" type="image/svg+xml" href="/images/favicon-victorsport.svg?v=1">
    <link rel="stylesheet" href="/css/trang-chu.css?v=data-2">
</head>
<body class="store-page">
<header class="store-header">
    <a class="store-logo" href="/" aria-label="Trang chủ VictorSport"><img src="/images/logo-victorsport-trong-suot.png" alt="VictorSport" width="210" height="67"></a>
    <nav aria-label="Điều hướng chính">
        <a href="/" class="active">Trang chủ</a><a href="#san-pham">Sản phẩm</a><a href="#nhu-cau">Nhu cầu sử dụng</a><a href="#lien-he">Liên hệ</a>
    </nav>
    <%@ include file="/WEB-INF/views/KhachHang/icon-thanh-menu.jsp" %>
</header>
<main>
    <section class="shop-hero store-container">
        <div class="shop-hero-copy">
            <p class="eyebrow">CỬA HÀNG GIÀY THỂ THAO VICTORSPORT</p>
            <h1>Chọn đôi giày.<br><span>Bắt đầu hành trình.</span></h1>
            <p>Khám phá giày theo thương hiệu, nhu cầu sử dụng và kích thước phù hợp với bạn.</p>
            <a class="store-button" href="#san-pham">Mua sắm ngay <span aria-hidden="true">↗</span></a>
            <div class="shop-hero-stats"><span><strong>${sanPham.size()}</strong> sản phẩm phù hợp</span><span><strong>${thuongHieu.size()}</strong> thương hiệu</span></div>
        </div>
        <div class="shop-featured">
            <c:choose>
                <c:when test="${not empty sanPham}">
                    <span class="shop-featured-label">KHÁM PHÁ BỘ SƯU TẬP</span>
                    <div class="shop-featured-slides">
                    <c:forEach items="${sanPham}" var="noiBat" varStatus="viTri">
                    <div class="shop-featured-slide ${viTri.first ? 'active' : ''}" aria-hidden="${not viTri.first}" <c:if test="${not viTri.first}">inert</c:if>>
                    <c:set var="anhNoiBat" value="${noiBat.anh}"/>
                    <c:if test="${empty anhNoiBat}"><c:set var="anhNoiBat" value="/images/giay-mac-dinh.svg"/></c:if>
                    <c:if test="${not fn:startsWith(anhNoiBat, '/') and not fn:startsWith(anhNoiBat, 'http')}"><c:set var="anhNoiBat" value="/${anhNoiBat}"/></c:if>
                    <a href="/san-pham/${noiBat.id}"><img src="<c:out value='${anhNoiBat}'/>" alt="<c:out value='${noiBat.ten_san_pham}'/>" width="640" height="420" onerror="this.onerror=null;this.src='/images/giay-mac-dinh.svg';"></a>
                    <div class="shop-featured-info"><div><p><c:out value="${noiBat.ten_thuong_hieu}"/></p><h2><a href="/san-pham/${noiBat.id}"><c:out value="${noiBat.ten_san_pham}"/></a></h2></div><a class="shop-featured-arrow" href="/san-pham/${noiBat.id}" aria-label="Xem sản phẩm">↗</a></div>
                    </div>
                    </c:forEach>
                    </div>
                </c:when>
                <c:otherwise><img src="/images/giay-mac-dinh.svg" alt="Minh họa giày thể thao" width="640" height="420"><p>Tìm đôi giày cho mỗi bước chân.</p></c:otherwise>
            </c:choose>
        </div>
    </section>

    <section id="nhu-cau" class="store-container shop-categories">
        <div class="section-heading"><div><p class="eyebrow">TÌM GIÀY PHÙ HỢP</p><h2>Chọn theo nhu cầu sử dụng</h2></div></div>
        <div class="shop-category-list">
            <c:forEach items="${boLoc.nhuCau}" var="muc">
                <a href="/?nhuCau=${muc.id}#san-pham" class="shop-category ${nhuCauDangChon == muc.id ? 'selected' : ''}"><c:out value="${muc.ten}"/><span aria-hidden="true">↗</span></a>
            </c:forEach>
        </div>
    </section>

    <section id="san-pham" class="product-section store-container">
        <div class="section-heading"><div><p class="eyebrow">BỘ SƯU TẬP VICTORSPORT</p><h2>Giày thể thao dành cho bạn</h2></div><span class="product-count">${sanPham.size()} sản phẩm</span></div>
        <div class="shop-brand-list" aria-label="Lọc theo thương hiệu">
            <a href="/#san-pham" class="${empty thuongHieuDangChon ? 'selected' : ''}">Tất cả</a>
            <c:forEach items="${thuongHieu}" var="th"><a href="/?thuongHieu=${th.id}#san-pham" class="${thuongHieuDangChon == th.id ? 'selected' : ''}"><c:out value="${th.ten_thuong_hieu}"/></a></c:forEach>
        </div>
        <form class="product-filters shop-filters" method="get" action="/#san-pham">
            <div class="search-field"><label for="tuKhoa">Tìm kiếm</label><input id="tuKhoa" name="tuKhoa" value="<c:out value='${tuKhoa}'/>" placeholder="Tên giày, mã sản phẩm hoặc thương hiệu"></div>
            <div><label for="thuongHieu">Thương hiệu</label><select id="thuongHieu" name="thuongHieu"><option value="">Tất cả thương hiệu</option><c:forEach items="${thuongHieu}" var="th"><option value="${th.id}" <c:if test="${thuongHieuDangChon == th.id}">selected</c:if>><c:out value="${th.ten_thuong_hieu}"/></option></c:forEach></select></div>
            <div><label for="nhuCau">Nhu cầu / vị trí thi đấu</label><select id="nhuCau" name="nhuCau"><option value="">Tất cả nhu cầu</option><c:forEach items="${boLoc.nhuCau}" var="muc"><option value="${muc.id}" <c:if test="${nhuCauDangChon == muc.id}">selected</c:if>><c:out value="${muc.ten}"/></option></c:forEach></select></div>
            <div><label for="kichThuoc">Kích thước</label><select id="kichThuoc" name="kichThuoc"><option value="">Tất cả kích thước</option><c:forEach items="${boLoc.kichThuoc}" var="muc"><option value="${muc.id}" <c:if test="${kichThuocDangChon == muc.id}">selected</c:if>><c:out value="${muc.ten}"/></option></c:forEach></select></div>
            <div><label for="mauSac">Màu sắc</label><select id="mauSac" name="mauSac"><option value="">Tất cả màu sắc</option><c:forEach items="${boLoc.mauSac}" var="muc"><option value="${muc.id}" <c:if test="${mauSacDangChon == muc.id}">selected</c:if>><c:out value="${muc.ten}"/></option></c:forEach></select></div>
            <div><label for="chatLieu">Chất liệu</label><select id="chatLieu" name="chatLieu"><option value="">Tất cả chất liệu</option><c:forEach items="${boLoc.chatLieu}" var="muc"><option value="${muc.id}" <c:if test="${chatLieuDangChon == muc.id}">selected</c:if>><c:out value="${muc.ten}"/></option></c:forEach></select></div>
            <div><label for="xuatXu">Xuất xứ</label><select id="xuatXu" name="xuatXu"><option value="">Tất cả xuất xứ</option><c:forEach items="${boLoc.xuatXu}" var="muc"><option value="${muc.id}" <c:if test="${xuatXuDangChon == muc.id}">selected</c:if>><c:out value="${muc.ten}"/></option></c:forEach></select></div>
            <div><label for="sapXep">Sắp xếp</label><select id="sapXep" name="sapXep"><option value="moi-nhat">Mới nhất</option><option value="gia-tang" <c:if test="${sapXep == 'gia-tang'}">selected</c:if>>Giá thấp đến cao</option><option value="gia-giam" <c:if test="${sapXep == 'gia-giam'}">selected</c:if>>Giá cao đến thấp</option></select></div>
            <div class="shop-stock-filter"><label><input type="checkbox" name="conHang" value="true" <c:if test="${conHang}">checked</c:if>> Chỉ hiện mẫu còn hàng</label></div>
            <div class="shop-filter-actions"><button class="store-button" type="submit">Áp dụng bộ lọc</button><a href="/#san-pham">Xóa bộ lọc</a></div>
        </form>
        <c:if test="${not empty loiDuLieu}"><div class="empty-state" role="alert"><p><c:out value="${loiDuLieu}"/></p><a href="/#san-pham">Tải lại sản phẩm</a></div></c:if>
        <div class="product-grid">
            <c:forEach items="${sanPham}" var="sp">
                <article class="product-card">
                    <a class="product-picture shop-product-picture" href="/san-pham/${sp.id}">
                        <span class="stock-label ${sp.so_luong <= 0 ? 'sold-out' : ''}"><c:choose><c:when test="${sp.so_luong > 0}">Còn hàng</c:when><c:otherwise>Hết hàng</c:otherwise></c:choose></span>
                        <c:set var="imageUrl" value="${sp.anh}"/><c:if test="${empty imageUrl}"><c:set var="imageUrl" value="/images/giay-mac-dinh.svg"/></c:if>
                        <c:if test="${not fn:startsWith(imageUrl, '/') and not fn:startsWith(imageUrl, 'http')}"><c:set var="imageUrl" value="/${imageUrl}"/></c:if>
                        <img src="<c:out value='${imageUrl}'/>" alt="<c:out value='${sp.ten_san_pham}'/>" loading="lazy" width="640" height="420" onerror="this.onerror=null;this.src='/images/giay-mac-dinh.svg';">
                    </a>
                    <div class="product-content">
                        <p class="product-brand"><c:out value="${sp.ten_thuong_hieu}"/> <span>· <c:out value="${sp.ma_san_pham}"/></span></p>
                        <h3><a href="/san-pham/${sp.id}"><c:out value="${sp.ten_san_pham}"/></a></h3>
                        <div class="shop-product-tags"><c:if test="${not empty sp.ten_vi_tri}"><span><c:out value="${sp.ten_vi_tri}"/></span></c:if><c:if test="${not empty sp.ten_chat_lieu}"><span><c:out value="${sp.ten_chat_lieu}"/></span></c:if></div>
                        <p class="shop-product-description"><c:out value="${sp.mo_ta_ngan}"/></p>
                        <p class="product-price"><small>Từ</small> <fmt:formatNumber value="${sp.gia_ban}" maxFractionDigits="0" groupingUsed="true"/> đ</p>
                        <p class="subtle">${sp.so_size} lựa chọn size<c:if test="${not empty sp.ten_xuat_xu}"> · <c:out value="${sp.ten_xuat_xu}"/></c:if></p>
                        <a class="online-detail-link" href="/san-pham/${sp.id}">Xem chi tiết và chọn size →</a>
                    </div>
                </article>
            </c:forEach>
        </div>
        <c:if test="${empty sanPham and empty loiDuLieu}"><div class="empty-state"><h3>Chưa có sản phẩm phù hợp</h3><p>Thử đổi size, màu sắc hoặc xóa bớt điều kiện lọc để tìm đôi giày khác.</p><a class="store-button" href="/#san-pham">Xem tất cả sản phẩm</a></div></c:if>
    </section>

    <section id="lien-he" class="store-container shop-contact"><div><p class="eyebrow">GẶP VICTORSPORT</p><h2>Thử giày trực tiếp tại cửa hàng</h2><p>Địa chỉ: Cầu Giấy</p></div><a class="store-button" href="tel:0325327602">Liên hệ: 0325327602</a></section>
</main>
<footer class="store-footer"><div class="store-container footer-content"><div><a class="store-logo" href="/"><img src="/images/logo-victorsport.png" alt="VictorSport" width="210" height="67"></a><p>Đồng hành cùng mỗi bước chân.</p><p>Cầu Giấy · <a href="tel:0325327602">0325327602</a></p></div><div><a href="#san-pham">Sản phẩm</a><a href="#nhu-cau">Nhu cầu sử dụng</a><a href="/tai-khoan">Tài khoản</a></div><small>© VictorSport</small></div></footer>
<script src="/js/san-pham-noi-bat.js?v=1"></script>
</body>
</html>
