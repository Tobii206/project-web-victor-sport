<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<fmt:setLocale value="vi_VN"/>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>VictorSport — Giày thể thao cho mỗi bước chân</title>
    <link rel="stylesheet" href="/css/trang-chu.css?v=gio-hang-1">
</head>
<body class="store-page">
<header class="store-header">
    <a class="store-logo" href="/" aria-label="Trang chủ VictorSport"><span class="logo-mark">V</span> VICTOR<span>SPORT</span></a>
    <nav aria-label="Điều hướng chính">
        <a href="/" class="active">Trang chủ</a>
        <a href="#san-pham">Sản phẩm</a>
        <a href="#thuong-hieu">Thương hiệu</a>
        <a href="#gioi-thieu">Về VictorSport</a>
    </nav>
    <%@ include file="icon-thanh-menu.jsp" %>
</header>
<main>
    <section class="hero">
        <div class="hero-copy">
            <p class="eyebrow">VỮNG BƯỚC HÔM NAY · BỨT PHÁ NGÀY MAI</p>
            <h1>Cho mỗi bước chân.<br><span>Cho mọi đam mê.</span></h1>
            <p class="hero-description">Từ sân bóng đến đường chạy, tìm đôi giày thể thao phù hợp để tự tin chinh phục hành trình của bạn.</p>
            <div class="hero-buttons">
                <a class="store-button" href="#san-pham">Khám phá sản phẩm <span aria-hidden="true">↗</span></a>
                <a class="hero-link" href="#gioi-thieu">Về VictorSport <span aria-hidden="true">→</span></a>
            </div>
            <div class="hero-topics"><span>Bóng đá</span><span>Chạy bộ</span><span>Tập luyện</span></div>
        </div>
        <div class="hero-art">
            <span class="art-word" aria-hidden="true">VICTOR</span>
            <span class="art-caption">CHUYỂN ĐỘNG<br>MỖI NGÀY</span>
            <div class="art-circle"></div>
            <img src="/images/giay-mac-dinh.svg" alt="Minh họa giày thể thao VictorSport" width="640" height="420">
            <div class="art-note"><span>VICTORSPORT</span><strong>Sẵn sàng cho bước tiếp theo.</strong></div>
        </div>
    </section>

    <section class="store-benefits" aria-label="Trải nghiệm mua sắm">
        <div><span class="benefit-number">01</span><div><strong>Đa dạng lựa chọn</strong><p>Giày cho nhiều hoạt động thể thao</p></div></div>
        <div><span class="benefit-number">02</span><div><strong>Tìm đôi giày phù hợp</strong><p>Khám phá sản phẩm theo thương hiệu</p></div></div>
        <div><span class="benefit-number">03</span><div><strong>Mua tại cửa hàng</strong><p>Trải nghiệm và chọn giày trực tiếp</p></div></div>
    </section>

    <section id="thuong-hieu" class="brand-section store-container">
        <span class="eyebrow">THƯƠNG HIỆU TRONG CỬA HÀNG</span>
        <div class="brand-list">
            <c:forEach items="${thuongHieu}" var="th">
                <c:url value="/" var="brandUrl"><c:param name="thuongHieu" value="${th.id}"/></c:url>
                <a href="${brandUrl}#san-pham"><c:out value="${th.ten_thuong_hieu}"/></a>
            </c:forEach>
            <c:if test="${empty thuongHieu}"><span class="subtle">Các thương hiệu sẽ được hiển thị khi có dữ liệu.</span></c:if>
        </div>
    </section>

    <section id="san-pham" class="product-section store-container">
        <div class="section-heading">
            <div><p class="eyebrow">BỘ SƯU TẬP VICTORSPORT</p><h2>Tìm đôi giày của bạn</h2></div>
            <span class="product-count">${sanPham.size()} sản phẩm</span>
        </div>
        <form class="product-filters" method="get" action="/#san-pham">
            <div class="search-field"><label for="tuKhoa">Tìm kiếm</label><input id="tuKhoa" name="tuKhoa" value="<c:out value='${tuKhoa}'/>" placeholder="Tên giày, mã sản phẩm, thương hiệu"></div>
            <div><label for="thuongHieu">Thương hiệu</label><select id="thuongHieu" name="thuongHieu">
                <option value="">Tất cả thương hiệu</option>
                <c:forEach items="${thuongHieu}" var="th"><option value="${th.id}" <c:if test="${thuongHieuDangChon == th.id}">selected</c:if>><c:out value="${th.ten_thuong_hieu}"/></option></c:forEach>
            </select></div>
            <div><label for="sapXep">Sắp xếp</label><select id="sapXep" name="sapXep">
                <option value="moi-nhat">Mới nhất</option>
                <option value="gia-tang" <c:if test="${sapXep == 'gia-tang'}">selected</c:if>>Giá thấp đến cao</option>
                <option value="gia-giam" <c:if test="${sapXep == 'gia-giam'}">selected</c:if>>Giá cao đến thấp</option>
            </select></div>
            <button class="store-button" type="submit">Tìm giày</button>
        </form>
        <c:if test="${not empty loiDuLieu}"><div class="empty-state"><p><c:out value="${loiDuLieu}"/></p><a href="/#san-pham">Tải lại sản phẩm</a></div></c:if>
        <div class="product-grid">
            <c:forEach items="${sanPham}" var="sp">
                <article class="product-card">
                    <a class="product-picture" href="/san-pham/${sp.id}">
                        <span class="stock-label <c:if test='${sp.so_luong <= 0}'>sold-out</c:if>"><c:choose><c:when test="${sp.so_luong > 0}">Còn hàng</c:when><c:otherwise>Hết hàng</c:otherwise></c:choose></span>
                        <c:choose><c:when test="${empty sp.anh}"><c:set var="imageUrl" value="/images/giay-mac-dinh.svg"/></c:when><c:otherwise><c:url value="${sp.anh}" var="imageUrl"/></c:otherwise></c:choose>
                        <img src="<c:out value='${imageUrl}'/>" alt="<c:out value='${sp.ten_san_pham}'/>" loading="lazy" width="640" height="420" onerror="this.onerror=null;this.src='/images/giay-mac-dinh.svg';">
                    </a>
                    <div class="product-content">
                        <p class="product-brand"><c:out value="${sp.ten_thuong_hieu}"/> <span>· <c:out value="${sp.ma_san_pham}"/></span></p>
                        <h3><a href="/san-pham/${sp.id}"><c:out value="${sp.ten_san_pham}"/></a></h3>
                        <p class="product-price"><small>Từ</small> <fmt:formatNumber value="${sp.gia_ban}" groupingUsed="true"/> đ</p>
                        <a class="online-detail-link" href="/san-pham/${sp.id}">Chọn size và mua hàng →</a>
                    </div>
                </article>
            </c:forEach>
        </div>
        <c:if test="${empty sanPham and empty loiDuLieu}"><div class="empty-state"><h3>Chưa tìm thấy đôi giày phù hợp</h3><p>Thử một từ khóa khác hoặc xem tất cả sản phẩm của cửa hàng.</p><a class="store-button" href="/#san-pham">Xem tất cả sản phẩm</a></div></c:if>
    </section>

    <section id="gioi-thieu" class="about-section store-container">
        <div><p class="eyebrow">VỀ VICTORSPORT</p><h2>Đam mê thể thao.<br>Bắt đầu từ đôi giày.</h2></div>
        <div><p>VictorSport là cửa hàng giày thể thao dành cho những người yêu chuyển động. Chúng tôi mang đến các lựa chọn cho bóng đá, chạy bộ và tập luyện, để bạn tìm được đôi giày phù hợp với hoạt động của mình.</p><a href="#san-pham">Khám phá bộ sưu tập <span aria-hidden="true">↗</span></a></div>
    </section>
</main>
<footer class="store-footer"><div class="store-container footer-content"><div><a class="store-logo" href="/">VICTOR<span>SPORT</span></a><p>Đồng hành cùng mỗi bước chân.</p></div><div><a href="#san-pham">Sản phẩm</a><a href="#gioi-thieu">Về cửa hàng</a><a href="/login">Đăng nhập nhân viên</a></div><small>© VictorSport</small></div></footer>
</body>
</html>
