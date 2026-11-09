<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <link rel="icon" type="image/svg+xml" href="/logo/favicon-victorsport.svg?v=1">
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Bán hàng tại quầy - Victor Sport</title>
    <link rel="stylesheet" href="/css/victorsport.css?v=20261107-balanced">
    <link rel="stylesheet" href="/css/in-hoa-don.css?v=3" media="print">
    <link rel="stylesheet" href="/css/pos-gio-hang.css?v=20261107-pos-payment">
<link rel="stylesheet" href="/css/giao-dien-chung.css?v=20261107-readable"></head>
<body>
<div class="app-shell">
    <jsp:include page="/WEB-INF/view/admin/layout/sidebar.jsp"/>
    <div class="main-area">
    <header class="topbar">
        <div class="topbar-heading">
            <jsp:include page="/WEB-INF/view/admin/layout/menu-toggle.jsp"/>
            <div>
                <h1>Bán hàng tại quầy</h1>
                <div class="muted">Victor Sport</div>
            </div>
        </div>
        <div class="topbar-user">
            <jsp:include page="/WEB-INF/view/admin/layout/store-link.jsp"/>
            <span>${currentUser.hoTen} - ${currentUser.tenQuyen}</span>
            <form method="post" action="/logout">
                <button class="button secondary" type="submit">Đăng xuất</button>
            </form>
        </div>
    </header>

    <section class="content-layout">
        <div class="panel">
            <div class="panel-header">
                <h2>Đơn chờ của bạn (${soDonCho}/10)</h2>
                <a class="button secondary" href="/quan-ly/hoa-don">Quản lý hóa đơn</a>
            </div>
            <div class="panel-body">
                <div class="muted">Tạo hoặc lưu đơn chờ bằng nút bên dưới thông tin khách hàng. Đơn chờ chưa trừ tồn kho.</div>
                <div class="invoice-table">
                    <table class="table">
                        <thead><tr><th scope="col" class="table-stt">STT</th><th>Mã đơn</th><th>Khách hàng</th><th>Tạm tính sau giảm</th><th>Thao tác</th></tr></thead>
                        <tbody>
                        <c:forEach items="${danhSachDonCho}" var="don" varStatus="st">
                            <tr><td class="table-stt">${st.count}</td>
                                <td><c:out value="${don.ma_hoa_don}"/></td>
                                <td><c:out value="${don.ten_khach_hang}"/></td>
                                <td class="money"><fmt:formatNumber value="${don.tong_tien_sau_giam}" groupingUsed="true"/> đ</td>
                                <td><div class="invoice-actions">
                                    <form method="post" action="/pos/don-cho/mo">
                                        <input type="hidden" name="idDonCho" value="${don.id}">
                                        <button class="button secondary" type="submit">
                                            <c:choose><c:when test="${don.id == donChoDangChon.id}">Đang chọn</c:when><c:otherwise>Tiếp tục bán</c:otherwise></c:choose>
                                        </button>
                                    </form>
                                    <form method="post" action="/pos/don-cho/huy">
                                        <input type="hidden" name="idDonCho" value="${don.id}">
                                        <button class="button danger" type="submit">Hủy đơn chờ</button>
                                    </form>
                                </div></td>
                            </tr>
                        </c:forEach>
                        <c:if test="${empty danhSachDonCho}"><tr><td colspan="5" class="muted">Chưa có đơn chờ.</td></tr></c:if>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    </section>
    <main class="pos-layout">
        <c:if test="${not empty hoaDonVuaTao}">
            <div class="pos-receipt-result">
                <p><strong><c:out value="${success}"/></strong></p>
                <div class="product-admin-actions">
                    <a class="button" data-in-hoa-don-vua-tao data-in-nhanh-hoa-don data-thong-bao="<c:out value='${success}'/>" href="/quan-ly/hoa-don/${hoaDonVuaTao}">In ngay</a>
                    <a class="button secondary" href="/quan-ly/hoa-don/${hoaDonVuaTao}">Xem hóa đơn</a>
                </div>
            </div>
        </c:if>
        <section class="panel">
            <div class="panel-header">
                <h2>Sản phẩm</h2>
            </div>
            <div class="panel-body">
                <c:if test="${not empty error}">
                    <div class="alert error" data-vs-flash="error">${error}</div>
                </c:if>
                <c:if test="${not empty success and empty hoaDonVuaTao}">
                    <div class="alert success" data-vs-flash="success"><c:out value="${success}"/></div>
                </c:if>

                <form class="search-row" method="get" action="/pos">
                    <input class="input" name="keyword" value="<c:out value='${keyword}'/>" placeholder="Tìm tên, mã sản phẩm, mã mẫu, hãng" aria-label="Tìm sản phẩm">
                    <button class="button" type="submit">Tìm kiếm</button>
                </form>

                <table class="table">
                    <thead>
                    <tr>
                        <th scope="col" class="table-stt">STT</th><th>Sản phẩm</th>
                        <th>Phân loại</th>
                        <th>Tồn</th>
                        <th>Giá</th>
                        <th></th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:forEach items="${products}" var="product" varStatus="st">
                        <tr><td class="table-stt">${st.count}</td>
                            <td>
                                <div class="pos-product-info">
                                    <c:set var="anhMauTaiQuay" value="${product.anhDaiDien}"/>
                                    <c:if test="${not empty anhMauTaiQuay and not fn:startsWith(anhMauTaiQuay, '/') and not fn:startsWith(anhMauTaiQuay, 'http')}"><c:set var="anhMauTaiQuay" value="/${anhMauTaiQuay}"/></c:if>
                                    <c:if test="${not empty anhMauTaiQuay}"><img class="pos-product-image" src="<c:out value='${anhMauTaiQuay}'/>" alt="<c:out value='${product.tenSanPham}'/>" loading="lazy" onerror="this.hidden=true;this.nextElementSibling.hidden=false;"></c:if>
                                    <span class="pos-product-no-image" <c:if test="${not empty anhMauTaiQuay}">hidden</c:if>>Không có ảnh</span>
                                    <div><strong><c:out value="${product.tenSanPham}"/></strong><div class="muted"><c:out value="${product.maSanPham}"/> - <c:out value="${product.tenThuongHieu}"/></div></div>
                                </div>
                            </td>
                            <td>${product.tenMauSac}, ${product.tenKichThuoc}</td>
                            <td>${product.soLuongTon}</td>
                            <td class="money"><fmt:formatNumber value="${product.giaSauGiam}" type="number" groupingUsed="true"/> đ <c:if test="${product.giaSauGiam lt product.giaBan}"><del class="gia-goc"><fmt:formatNumber value="${product.giaBan}" groupingUsed="true"/> đ</del></c:if></td>
                            <td>
                                <form method="post" action="/pos/cart/add">
                                    <input type="hidden" name="detailId" value="${product.idChiTietSanPham}">
                                    <input type="hidden" name="quantity" value="1">
                                    <button class="button" type="submit" <c:if test="${product.soLuongTon <= 0}">disabled</c:if>>Thêm</button>
                                </form>
                            </td>
                        </tr>
                    </c:forEach>
                    <c:if test="${empty products}">
                        <tr>
                            <td colspan="6" class="muted">Không có sản phẩm phù hợp. Hãy thử từ khóa khác.</td>
                        </tr>
                    </c:if>
                    </tbody>
                </table>
            </div>
        </section>

        <aside class="panel pos-cart-panel">
            <div class="panel-header pos-cart-header">
                <div class="pos-cart-heading"><span class="pos-cart-symbol" aria-hidden="true"><svg viewBox="0 0 24 24"><path d="M5 7h14l1 14H4L5 7ZM9 8V6a3 3 0 0 1 6 0v2"/></svg></span><div><h2>Giỏ bán hàng</h2><p class="pos-cart-count">${fn:length(cart.danhSachSanPham)} mẫu<c:if test="${not empty donChoDangChon}"> · <c:out value="${donChoDangChon.ma_hoa_don}"/></c:if></p></div></div>
                <form method="post" action="/pos/cart/clear">
                    <button class="button secondary" type="submit">Làm mới</button>
                </form>
            </div>
            <div class="panel-body">
                <div class="cart-list" aria-label="Sản phẩm trong giỏ bán hàng">
                    <c:forEach items="${cart.danhSachSanPham}" var="item">
                        <article class="cart-line" data-mau="${item.idChiTietSanPham}">
                            <div class="pos-cart-item-top">
                                <div class="pos-cart-item-info"><h3 class="cart-line-title"><c:out value="${item.tenSanPham}"/></h3><p class="pos-cart-code"><c:out value="${item.maSanPham}"/></p></div>
                                <form method="post" action="/pos/cart/remove">
                                    <input type="hidden" name="detailId" value="${item.idChiTietSanPham}">
                                    <button class="pos-cart-remove" type="submit" title="Xóa sản phẩm" aria-label="Xóa <c:out value='${item.tenSanPham}'/>"><svg viewBox="0 0 24 24" aria-hidden="true"><path d="M4 7h16M9 7V4h6v3M6 7l1 14h10l1-14M10 11v6M14 11v6"/></svg></button>
                                </form>
                            </div>
                            <div class="pos-cart-meta"><span><c:out value="${item.tenMauSac}"/></span><span><c:out value="${item.tenKichThuoc}"/></span><small>Còn ${item.soLuongTon}</small></div>
                            <div class="pos-cart-unit">Đơn giá <span><fmt:formatNumber value="${item.donGia}" maxFractionDigits="0" groupingUsed="true"/> đ</span></div>
                            <div class="pos-cart-item-bottom">
                                <form class="pos-cart-quantity" method="post" action="/pos/cart/update">
                                    <input type="hidden" name="detailId" value="${item.idChiTietSanPham}">
                                    <label class="pos-cart-sr-only" for="soLuongMau${item.idChiTietSanPham}">Số lượng <c:out value="${item.tenSanPham}"/></label>
                                    <input id="soLuongMau${item.idChiTietSanPham}" class="input qty-input" type="number" name="quantity" min="0" max="${item.soLuongTon}" step="1" value="${item.soLuongMua}" required>
                                    <button class="pos-cart-save" type="submit" title="Cập nhật số lượng">Lưu</button>
                                </form>
                                <div class="pos-cart-line-total"><span>Thành tiền</span><strong><fmt:formatNumber value="${item.thanhTien}" maxFractionDigits="0" groupingUsed="true"/> đ</strong></div>
                            </div>
                        </article>
                    </c:forEach>
                    <c:if test="${cart.rong}"><div class="pos-cart-empty"><svg viewBox="0 0 24 24" aria-hidden="true"><path d="M5 7h14l1 14H4L5 7ZM9 8V6a3 3 0 0 1 6 0v2M9 13h6"/></svg><strong>Giỏ bán hàng đang trống</strong><p>Chọn sản phẩm bên trái để bắt đầu.</p></div></c:if>
                </div>

                <div class="summary">
                    <div class="summary-row">
                        <span>Tạm tính</span>
                        <strong><fmt:formatNumber value="${summary.tamTinh}" type="number" groupingUsed="true"/> đ</strong>
                    </div>
                    <div class="summary-row">
                        <span>Giảm giá</span>
                        <strong><fmt:formatNumber value="${summary.tienGiam}" type="number" groupingUsed="true"/> đ</strong>
                    </div>
                    <div class="pos-cart-discount-note"><c:out value="${summary.tenUuDai}"/></div>
                    <div class="summary-row pos-phi-giao-hang" hidden><span>Phí giao hàng GHN</span><strong id="posPhiGiaoHang">Đang tính phí</strong></div>
                    <div class="summary-row total">
                        <span>Cần thanh toán</span>
                        <span id="posTongThanhToan"><fmt:formatNumber value="${summary.tongThanhToan}" type="number" groupingUsed="true"/> đ</span>
                    </div>
                </div>

                <form class="checkout-grid" method="post" action="/pos/phieu-giam-gia/chon">
                    <div class="field">
                        <label for="discountCouponId">Phiếu giảm giá</label>
                        <select class="select" id="discountCouponId" name="discountCouponId" onchange="this.form.requestSubmit()" ${empty discountCoupons or cart.rong ? 'disabled' : ''}>
                            <option value="">
                                <c:choose>
                                    <c:when test="${empty discountCoupons or cart.rong}">Không có phiếu đủ điều kiện</c:when>
                                    <c:when test="${not empty selectedDiscountCouponId}">Tự động chọn ưu đãi tốt nhất</c:when>
                                    <c:when test="${not empty summary.idPhieuGiamGia}">
                                        Tự động: ${summary.tenUuDai}
                                    </c:when>
                                    <c:when test="${summary.tienGiam gt 0}">Tự động: giữ đợt giảm giá có lợi hơn</c:when>
                                    <c:otherwise>Tự động chọn ưu đãi tốt nhất</c:otherwise>
                                </c:choose>
                            </option>
                            <c:forEach items="${discountCoupons}" var="coupon">
                                <option value="${coupon.id}" <c:if test="${selectedDiscountCouponId == coupon.id}">selected</c:if>>
                                    ${coupon.maPhieuGiamGia} - ${coupon.tenPhieuGiamGia}
                                </option>
                            </c:forEach>
                        </select>
                        <c:if test="${not empty discountCoupons and summary.tienGiam gt 0 and empty summary.idPhieuGiamGia}"><small class="muted">Đợt giảm giá sản phẩm có lợi hơn phiếu. Bạn vẫn có thể chọn phiếu khác để áp dụng mức giảm của phiếu đó.</small></c:if>
                        <c:if test="${empty discountCoupons and not cart.rong}"><small class="muted">Chưa có phiếu còn hiệu lực, còn lượt và đạt ngưỡng đơn hàng. Phiếu giảm giá vận chuyển chỉ dùng cho đơn giao hàng.</small></c:if>
                    </div>
                </form>

                <form class="checkout-grid pos-cart-customer" method="post" action="/pos/khach-hang/chon">
                    <div class="field">
                        <label for="customerId">Khách quen</label>
                        <select class="select" id="customerId" name="customerId" onchange="this.form.requestSubmit()">
                            <option value="">Khách vãng lai</option>
                            <c:if test="${not empty khachHangDangChon}">
                                <option value="${khachHangDangChon.id}" selected><c:out value="${khachHangDangChon.maKhachHang}"/> - <c:out value="${khachHangDangChon.tenKhachHang}"/></option>
                            </c:if>
                            <c:forEach items="${customers}" var="customer">
                                <c:if test="${customer.id != khachHangDangChon.id}">
                                <option value="${customer.id}">${customer.maKhachHang} - ${customer.tenKhachHang} - ${customer.soDienThoai}</option>
                                </c:if>
                            </c:forEach>
                        </select>
                    </div>
                    <button class="button secondary" type="button" onclick="document.getElementById('themKhachHang').showModal()">+ Thêm khách</button>
                </form>
                <form class="checkout-grid" method="post" action="/pos/checkout" data-xac-nhan-thanh-toan data-tong-tien="${summary.tongThanhToan}" data-tien-hang="${summary.tongThanhToan}" data-pos-giao-hang data-khach-hang-id="<c:out value='${khachHangDangChon.id}'/>" data-khach-vua-doi="${khachHangVuaDoi}"
                    data-nguoi-nhan="<c:out value='${thongTinNhanHang.ten}'/>" data-sdt-nhan="<c:out value='${thongTinNhanHang.sdt}'/>"
                    data-dia-chi-nhan="<c:out value='${thongTinNhanHang.diaChi}'/>" data-tinh-nhan="<c:out value='${thongTinNhanHang.tenTinh}'/>" data-xa-nhan="<c:out value='${thongTinNhanHang.tenXa}'/>">
                    <input type="hidden" name="customerId" value="${khachHangDangChon.id}">

                    <input type="hidden" name="csrfPos" value="<c:out value='${sessionScope.csrfGiaoHangTaiQuay}'/>">
                    <input type="hidden" name="maBaoGia" value="">
                    <c:set var="chonGiaoHang" value="${giaoHangDaChon or not empty giaoHangTaiQuay or donChoDangChon.loai_don == 2}"/>
                    <fieldset class="pos-nhan-hang">
                        <legend>Hình thức nhận hàng</legend>
                        <label><input type="radio" name="giaoHang" value="false" ${not chonGiaoHang ? 'checked' : ''}> Nhận tại quầy</label>
                        <label><input type="radio" name="giaoHang" value="true" ${chonGiaoHang ? 'checked' : ''}> Giao hàng tận nơi</label>
                    </fieldset>
                    <fieldset class="pos-giao-hang" hidden>
                        <legend>Thông tin nhận hàng</legend>
                        <div class="field"><label for="posTenNguoiNhan">Tên người nhận *</label><input class="input" id="posTenNguoiNhan" name="customerName" maxlength="100" value="<c:out value='${thongTinNhanHang.ten}'/>"></div>
                        <div class="field"><label for="posSdtNguoiNhan">Số điện thoại *</label><input class="input" id="posSdtNguoiNhan" name="customerPhone" type="tel" pattern="0[0-9]{9}" maxlength="10" value="<c:out value='${thongTinNhanHang.sdt}'/>"></div>
                        <div class="field"><label for="posTinhGhn">Tỉnh / Thành phố *</label><select class="select" id="posTinhGhn" name="tinh" data-ten-da-luu="<c:out value='${thongTinNhanHang.tenTinh}'/>"><option value="">Chọn tỉnh / thành phố</option></select></div>
                        <div class="field"><label for="posXaGhn">Phường / Xã *</label><select class="select" id="posXaGhn" name="xa" data-ten-da-luu="<c:out value='${thongTinNhanHang.tenXa}'/>"><option value="">Chọn phường / xã</option></select></div>
                        <div class="field pos-giao-hang-wide"><label for="posDiaChiGhn">Địa chỉ chi tiết *</label><input class="input" id="posDiaChiGhn" name="customerAddress" maxlength="200" placeholder="Số nhà, ngõ, tên đường" value="<c:out value='${thongTinNhanHang.diaChi}'/>"></div>
                        <p class="muted pos-giao-hang-wide" id="posTrangThaiPhi" role="status" aria-live="polite">Phí giao hàng tự tính khi nhập đủ thông tin nhận hàng.</p>
                        <button class="button secondary pos-giao-hang-wide" type="button" id="posThuLaiPhi" hidden>Thử lại phí giao hàng</button>
                    </fieldset>
                    <div class="field">
                        <label for="paymentMethodId">Phương thức thanh toán</label>
                        <select class="select" id="paymentMethodId" name="paymentMethodId">
                            <c:forEach items="${paymentMethods}" var="method">
                                <c:if test="${method.nhaCungCap != 'COD'}"><option value="${method.id}" data-tien-mat="${method.nhaCungCap == 'POS'}" ${paymentMethodIdDaChon == method.id ? 'selected' : ''}><c:out value="${method.tenPhuongThuc}"/></option></c:if>
                            </c:forEach>
                        </select>
                    </div>
                    <div class="field">
                        <label for="paidAmount" id="paidAmountLabel">Tiền khách đưa (đ) *</label>
                        <div class="pos-paid-entry">
                            <input class="input" type="number" id="paidAmount" name="paidAmount" min="0" max="9999999999999999.99" step="0.01" inputmode="decimal" autocomplete="off" value="<c:out value='${paidAmount}'/>" placeholder="Nhập số tiền đã nhận" aria-describedby="paidAmountHelp" required>
                            <button type="button" class="button secondary" id="posNhapDuTien">Điền đủ</button>
                        </div>
                        <small class="muted" id="paidAmountHelp">Tiền mặt: nhập số tiền khách đưa để tính tiền thừa. Chuyển khoản: nhập đúng số tiền cần thu.</small>
                    </div>
                    <div class="summary-row pos-tien-thua" id="posDongTienThua" role="status" aria-live="polite"><span>Tiền thừa trả khách</span><strong id="posTienThua">—</strong></div>
                    <small class="muted" id="posTienConThieu" role="status" aria-live="polite" hidden></small>
                    <details class="pos-cart-note" ${not empty donChoDangChon.ghi_chu ? 'open' : ''}><summary>Ghi chú đơn hàng</summary><label class="pos-cart-sr-only" for="note">Ghi chú đơn hàng</label><textarea class="textarea" id="note" name="note" rows="2"><c:out value="${donChoDangChon.ghi_chu}"/></textarea></details>
                    <div class="pos-cart-checkout-actions">
                    <button class="button secondary full" type="submit" name="hanhDong" value="luu-don-cho" formnovalidate
                            <c:if test="${donChoDaMat}">disabled</c:if>>
                        <c:choose><c:when test="${not empty donChoDangChon}">Lưu đơn chờ</c:when><c:otherwise>Tạo đơn chờ</c:otherwise></c:choose>
                    </button>
                    <button class="button full" type="submit" name="hanhDong" value="thanh-toan" <c:if test="${cart.rong or donChoDaMat}">disabled</c:if>>Thanh toán <span aria-hidden="true">→</span></button>
                    </div>
                </form>
            </div>
        </aside>
    </main>
    </div>
</div>
<dialog id="themKhachHang" class="customer-dialog">
    <h2>Thêm khách hàng mới</h2>
    <c:if test="${not empty loiThemKhachHang}">
        <div class="alert error"><c:out value="${loiThemKhachHang}"/></div>
    </c:if>
    <form method="post" action="/pos/khach-hang/them">
        <div class="field">
            <label for="tenKhachHangMoi">Tên khách hàng</label>
            <input class="input" id="tenKhachHangMoi" name="tenKhachHangMoi" maxlength="100" required value="<c:out value='${tenMoi}'/>">
        </div>
        <div class="field">
            <label for="soDienThoaiMoi">Số điện thoại</label>
            <input class="input" id="soDienThoaiMoi" name="soDienThoaiMoi" type="tel" maxlength="10" pattern="0[0-9]{9}" title="Số điện thoại phải đủ 10 chữ số và bắt đầu bằng 0" placeholder="Có thể để trống" value="<c:out value='${soDienThoaiMoi}'/>">
        </div>
        <div class="field">
            <label for="emailMoi">Email</label>
            <input class="input" id="emailMoi" name="emailMoi" type="email" pattern="[a-zA-Z0-9]+([._%+\-][a-zA-Z0-9]+)*@[gG][mM][aA][iI][lL]\.[cC][oO][mM]" title="Email phải có dạng ten@gmail.com" maxlength="100" placeholder="Có thể để trống" value="<c:out value='${emailMoi}'/>">
        </div>
        <div class="invoice-actions">
            <button class="button" type="submit">Lưu khách hàng</button>
            <button class="button secondary" type="button" onclick="document.getElementById('themKhachHang').close()">Đóng</button>
        </div>
    </form>
</dialog>
<c:if test="${not empty loiThemKhachHang}">
    <script>document.getElementById('themKhachHang').showModal();</script>
</c:if>
<script src="/js/ban-hang-tai-quay.js?v=20261109-ghn-address"></script>
<%@ include file="/WEB-INF/view/client/layout/payment-confirm.jsp" %>
</body>
</html>
