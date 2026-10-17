<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<fmt:setLocale value="vi_VN"/>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Xác nhận đơn hàng — VictorSport</title>
    <link rel="icon" type="image/svg+xml" href="/images/favicon-victorsport.svg?v=1">
    <link rel="stylesheet" href="/css/trang-chu.css?v=gia-giam-1">
    <link rel="stylesheet" href="/css/thanh-toan-online.css?v=20261017-3">
</head>
<body>
<%@ include file="/WEB-INF/views/KhachHang/dau-trang-online.jsp" %>
<main class="store-container checkout-page">
    <a class="checkout-back" href="/gio-hang">← Quay lại giỏ hàng</a>
    <div class="checkout-heading"><div><p class="checkout-eyebrow">HOÀN TẤT MUA HÀNG</p><h1>Xác nhận đơn hàng</h1><p class="subtle">Kiểm tra thông tin nhận hàng trước khi đặt đơn.</p></div><span class="checkout-brand">VICTORSPORT</span></div>
    <c:if test="${not empty error}"><div class="account-error" role="alert"><c:out value="${error}"/></div></c:if>
    <c:choose>
    <c:when test="${not empty danhSach}">
    <div class="checkout-layout">
        <section class="account-card checkout-information">
            <form class="account-form checkout-form" id="formThanhToanOnline" method="post" action="/thanh-toan" data-xac-nhan-thanh-toan data-tong-tien="${tongTien}" data-tieu-de="Xác nhận thanh toán" data-nhan-xac-nhan="Xác nhận đặt hàng" data-nhan-phuong-thuc="Phương thức" data-phuong-thuc="Thanh toán khi nhận hàng (COD)" data-noi-dung="Vui lòng kiểm tra thông tin nhận hàng và tổng tiền. Bạn sẽ thanh toán khi nhận được hàng.">
                <input type="hidden" name="loai" value="${loai}">
                <input type="hidden" name="tongTienDuKien" id="tongTienDuKien" value="${tongTien}">
                <div class="checkout-section-heading"><span>01</span><h2>Thông tin nhận hàng</h2></div>
                <div class="checkout-contact">
                    <div class="checkout-field"><label for="ten">Họ và tên</label><input id="ten" name="ten" autocomplete="name" maxlength="100" value="<c:out value='${ten}'/>" required></div>
                    <div class="checkout-field"><label for="sdt">Số điện thoại</label><input id="sdt" name="sdt" type="tel" autocomplete="tel" pattern="0[0-9]{9}" title="Số điện thoại phải đủ 10 chữ số và bắt đầu bằng 0" maxlength="10" value="<c:out value='${sdt}'/>" required></div>
                </div>
                <c:if test="${not empty sessionScope.khachHangOnlineDangNhap}">
                    <div class="checkout-field">
                        <div class="checkout-label-row"><label for="diaChiDaLuu">Địa chỉ đã lưu</label><a href="/tai-khoan/dia-chi" target="_blank" rel="noopener">+ Thêm / quản lý địa chỉ</a></div>
                        <select id="diaChiDaLuu" name="idDiaChi">
                            <option value="">Nhập địa chỉ khác</option>
                            <c:forEach items="${diaChiDaLuu}" var="dc"><option value="${dc.id}" data-dia-chi="<c:out value='${dc.dia_chi_day_du}'/>" ${idDiaChiDaChon == dc.id ? 'selected' : ''}><c:out value="${dc.ten_dia_chi}"/>${dc.mac_dinh ? ' (Mặc định)' : ''} — <c:out value="${dc.dia_chi_day_du}"/></option></c:forEach>
                        </select>
                    </div>
                </c:if>
                <div class="checkout-field"><label for="diaChi">Địa chỉ nhận hàng</label><input id="diaChi" name="diaChi" autocomplete="street-address" maxlength="255" value="<c:out value='${diaChi}'/>" ${not empty idDiaChiDaChon ? 'readonly' : ''} required></div>
                <div class="checkout-section-heading checkout-payment-heading"><span>02</span><h2>Phương thức thanh toán</h2></div>
                <div class="checkout-field">
                    <label class="checkout-sr-only" for="paymentMethodId">Phương thức thanh toán</label>
                    <select id="paymentMethodId" name="phuongThuc" required>
                        <option value="COD" ${phuongThucDaChon != 'QR' ? 'selected' : ''}>Thanh toán khi nhận hàng (COD)</option>
                        <option value="QR" ${phuongThucDaChon == 'QR' ? 'selected' : ''}>Chuyển khoản QR</option>
                    </select>
                    <p class="checkout-payment-note">Với QR, mã chuyển khoản sẽ hiển thị sau khi xác nhận đặt hàng.</p>
                </div>
                <div class="checkout-submit-row"><button class="store-button" type="submit">Xác nhận đặt hàng <span aria-hidden="true">→</span></button><p>Vui lòng kiểm tra đúng số điện thoại và địa chỉ nhận hàng.</p></div>
            </form>
        </section>
        <aside class="account-card checkout-summary" aria-label="Tóm tắt đơn hàng">
            <div class="checkout-summary-heading"><h2>Đơn hàng của bạn</h2><span>${danhSach.size()} mẫu</span></div>
            <div class="checkout-products">
                <c:forEach items="${danhSach}" var="dong">
                    <article class="checkout-product">
                        <c:choose>
                            <c:when test="${not empty dong.anh}"><img class="checkout-product-image" src="<c:out value='${dong.anh}'/>" alt="<c:out value='${dong.ten_san_pham}'/>" onerror="this.hidden=true; this.nextElementSibling.hidden=false;"><span class="checkout-no-image" hidden>Chưa có ảnh</span></c:when>
                            <c:otherwise><span class="checkout-no-image">Chưa có ảnh</span></c:otherwise>
                        </c:choose>
                        <div class="checkout-product-info"><h3><c:out value="${dong.ten_san_pham}"/></h3><p><c:out value="${dong.ten_kich_thuoc}"/> · <c:out value="${dong.ten_mau_sac}"/> · <c:out value="${dong.ten_form_chan}"/></p><div class="checkout-product-price"><span>SL: ${dong.so_luong_mua}</span><strong><fmt:formatNumber value="${dong.thanh_tien_goc}"/> đ</strong></div></div>
                    </article>
                </c:forEach>
            </div>
            <div class="checkout-voucher">
                <label for="phieuGiamGiaOnline">Phiếu giảm giá</label>
                <select id="phieuGiamGiaOnline" name="idPhieuGiamGia" form="formThanhToanOnline" data-tong-goc="${tongGoc}" data-giam-dot="${giamTheoDot}">
                    <option value="">Không dùng phiếu giảm giá</option>
                    <c:forEach items="${phieuKhaDung}" var="phieu"><option value="${phieu.id}" data-giam="${phieu.tien_giam}" data-freeship="${phieu.freeship}" ${idPhieuDaChon == phieu.id ? 'selected' : ''}><c:out value="${phieu.ma_phieu_giam_gia}"/> — <c:out value="${phieu.ten_phieu_giam_gia}"/><c:if test="${not phieu.freeship}"> (giảm <fmt:formatNumber value="${phieu.tien_giam}"/> đ)</c:if></option></c:forEach>
                </select>
                <c:if test="${empty phieuKhaDung}"><p>Chưa có phiếu phù hợp với đơn hàng này.</p></c:if>
                <c:if test="${not empty loiPhieu}"><p role="alert"><c:out value="${loiPhieu}"/></p></c:if>
                <c:if test="${not empty phieuKhaDung}"><p>Phiếu phù hợp nhất được chọn sẵn. Bạn có thể đổi hoặc chọn không dùng.</p></c:if>
                <p id="thongBaoPhieu">${coDotGiamGia ? 'Đơn có sản phẩm đang giảm giá chỉ được dùng voucher freeship.' : 'Chọn phiếu để được giảm giá đơn hàng.'}</p>
            </div>
            <div class="checkout-discount-row"><span>Tổng tiền hàng</span><strong><fmt:formatNumber value="${tongGoc}"/> đ</strong></div>
            <div class="checkout-discount-row"><span>Giảm giá</span><strong id="tienGiamOnline"><fmt:formatNumber value="${giamTheoDot}"/> đ</strong></div>
            <div class="checkout-grand-total"><span>Tổng thanh toán</span><strong id="tongThanhToanOnline"><fmt:formatNumber value="${tongTien}"/> <small>đ</small></strong></div>
            <a class="checkout-edit-cart" href="/gio-hang">Chỉnh sửa giỏ hàng →</a>
        </aside>
    </div>
    </c:when>
    <c:otherwise><section class="account-card checkout-empty"><h2>Chưa có sản phẩm để thanh toán</h2><a class="store-button" href="/#san-pham">Tiếp tục mua sắm</a></section></c:otherwise>
    </c:choose>
</main>
<script src="/js/dia-chi-thanh-toan.js"></script>
<script src="/js/phieu-giam-gia-online.js?v=2"></script>
<%@ include file="/WEB-INF/views/KhachHang/xac-nhan-thanh-toan.jsp" %>
</body>
</html>
