<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<fmt:setLocale value="vi_VN"/>
<!DOCTYPE html>
<html lang="vi"><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1.0">
<title><c:out value="${sanPham.ten_san_pham}" default="Chi tiết sản phẩm"/> — VictorSport</title>
<link rel="stylesheet" href="/css/trang-chu.css?v=gio-hang-1"></head>
<body><%@ include file="dau-trang-online.jsp" %>
<main class="store-container online-container">
    <p class="online-breadcrumb"><a href="/">Trang chủ</a> / <a href="/#san-pham">Sản phẩm</a> / <c:out value="${sanPham.ten_san_pham}"/></p>
    <c:if test="${not empty error}"><div class="account-error" role="alert"><c:out value="${error}"/></div></c:if>
    <c:if test="${not empty success}"><div class="online-success" role="status"><c:out value="${success}"/> <a href="/gio-hang">Xem giỏ hàng →</a></div></c:if>
    <c:if test="${not empty sanPham}">
    <div class="online-detail">
        <section class="online-gallery account-card">
            <img id="anhChinh" class="online-main-image" src="/images/giay-mac-dinh.svg" alt="<c:out value='${sanPham.ten_san_pham}'/>" onerror="this.onerror=null;this.src='/images/giay-mac-dinh.svg';">
            <div class="online-thumbnails">
                <c:forEach items="${danhSachAnh}" var="anh">
                    <c:set var="duongDan" value="${anh.duong_dan_anh}"/>
                    <c:if test="${not fn:startsWith(duongDan, '/') and not fn:startsWith(duongDan, 'http')}"><c:set var="duongDan" value="/${duongDan}"/></c:if>
                    <button type="button" class="online-thumbnail" aria-label="Xem ảnh sản phẩm"><img src="<c:out value='${duongDan}'/>" alt="Ảnh sản phẩm" onerror="this.onerror=null;this.src='/images/giay-mac-dinh.svg';"></button>
                </c:forEach>
            </div>
        </section>
        <section class="account-card online-product-info">
            <p class="eyebrow"><c:out value="${sanPham.ten_thuong_hieu}"/></p>
            <h1><c:out value="${sanPham.ten_san_pham}"/></h1>
            <p class="subtle">Mã sản phẩm: <c:out value="${sanPham.ma_san_pham}"/></p>
            <p class="online-price"><span id="giaBan">Chọn mẫu giày</span> <del id="giaNiemYet"></del></p>
            <p id="tonKho" class="subtle">Chọn kích thước và màu để xem tồn kho.</p>
            <form method="post" action="/san-pham/${sanPham.id}/chon" id="formChonGiay">
                <fieldset class="online-variants"><legend>Kích thước · Màu sắc · Dáng giày</legend>
                    <c:set var="daChon" value="false"/>
                    <c:forEach items="${phienBan}" var="pb">
                        <c:set var="anhMau" value="${pb.anh}"/>
                        <c:if test="${empty anhMau}"><c:set var="anhMau" value="/images/giay-mac-dinh.svg"/></c:if>
                        <c:if test="${not fn:startsWith(anhMau, '/') and not fn:startsWith(anhMau, 'http')}"><c:set var="anhMau" value="/${anhMau}"/></c:if>
                        <label class="online-variant">
                            <input type="radio" name="idPhienBan" value="${pb.id}" data-gia="${pb.gia_ban}" data-niem-yet="${pb.gia_niem_yet}" data-ton="${pb.so_luong}" data-anh="<c:out value='${anhMau}'/>" required
                                <c:if test="${pb.so_luong <= 0}">disabled</c:if>
                                <c:if test="${pb.so_luong > 0 and not daChon}">checked<c:set var="daChon" value="true"/></c:if>>
                            <span><strong><c:out value="${pb.ten_kich_thuoc}"/></strong><small><c:out value="${pb.ten_mau_sac}"/> · <c:out value="${pb.ten_form_chan}"/></small><c:if test="${pb.so_luong <= 0}"><small>Hết hàng</small></c:if></span>
                        </label>
                    </c:forEach>
                </fieldset>
                <c:if test="${empty phienBan}"><p>Sản phẩm hiện chưa có mẫu để chọn.</p></c:if>
                <div class="online-purchase">
                    <div class="online-quantity"><button type="button" id="giamSoLuong" aria-label="Giảm số lượng">−</button><input type="number" id="soLuong" name="soLuong" value="1" min="1" aria-label="Số lượng" required><button type="button" id="tangSoLuong" aria-label="Tăng số lượng">+</button></div>
                    <button class="store-button" name="thaoTac" value="them" <c:if test="${not daChon}">disabled</c:if>>Thêm vào giỏ</button>
                    <button class="store-button online-buy-now" name="thaoTac" value="mua" <c:if test="${not daChon}">disabled</c:if>>Mua ngay</button>
                </div>
            </form>
            <div class="online-notes"><p>Chọn đúng size và màu trước khi đặt hàng.</p><p>Tồn kho được kiểm tra lại khi xác nhận đơn.</p></div>
        </section>
    </div>
    <section class="account-card online-description"><h2>Thông tin sản phẩm</h2><p><c:out value="${sanPham.mo_ta_chi_tiet}" default="${sanPham.mo_ta_ngan}"/></p></section>
    </c:if>
</main><script src="/js/san-pham-online.js?v=1"></script></body></html>
