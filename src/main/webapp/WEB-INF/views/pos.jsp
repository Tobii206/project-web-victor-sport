<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Bán hàng tại quầy - Victor Sport</title>
    <link rel="stylesheet" href="/css/victorsport.css">
</head>
<body>
<div class="app-shell">
    <jsp:include page="menu.jsp"/>
    <div class="main-area">
    <header class="topbar">
        <div>
            <h1>Bán hàng tại quầy</h1>
            <div class="muted">Victor Sport</div>
        </div>
        <div class="topbar-user">
            <span>${currentUser.hoTen} - ${currentUser.tenQuyen}</span>
            <form method="post" action="/logout">
                <button class="button secondary" type="submit">Đăng xuất</button>
            </form>
        </div>
    </header>

    <main class="pos-layout">
        <section class="panel">
            <div class="panel-header">
                <h2>Sản phẩm</h2>
            </div>
            <div class="panel-body">
                <c:if test="${not empty error}">
                    <div class="alert error">${error}</div>
                </c:if>
                <c:if test="${not empty success}">
                    <div class="alert success">${success}</div>
                </c:if>

                <form class="search-row" method="get" action="/pos">
                    <input class="input" name="keyword" value="${keyword}" placeholder="Tìm mã, tên sản phẩm, thương hiệu">
                    <button class="button" type="submit">Tìm kiếm</button>
                </form>

                <table class="table">
                    <thead>
                    <tr>
                        <th>Sản phẩm</th>
                        <th>Phân loại</th>
                        <th>Tồn</th>
                        <th>Giá</th>
                        <th></th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:forEach items="${products}" var="product">
                        <tr>
                            <td>
                                <strong>${product.tenSanPham}</strong>
                                <div class="muted">${product.maSanPham} - ${product.tenThuongHieu}</div>
                            </td>
                            <td>${product.tenMauSac}, ${product.tenKichThuoc}</td>
                            <td>${product.soLuongTon}</td>
                            <td class="money"><fmt:formatNumber value="${product.giaBan}" type="number" groupingUsed="true"/> đ</td>
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
                            <td colspan="5" class="muted">Không có sản phẩm phù hợp. Hãy thử từ khóa khác.</td>
                        </tr>
                    </c:if>
                    </tbody>
                </table>
            </div>
        </section>

        <aside class="panel">
            <div class="panel-header">
                <h2>Giỏ bán hàng</h2>
                <form method="post" action="/pos/cart/clear">
                    <button class="button secondary" type="submit">Làm mới</button>
                </form>
            </div>
            <div class="panel-body">
                <div class="cart-list">
                    <c:forEach items="${cart.danhSachSanPham}" var="item">
                        <div class="cart-line">
                            <div>
                                <div class="cart-line-title">${item.tenSanPham}</div>
                                <div class="muted">${item.tenMauSac}, ${item.tenKichThuoc} - tồn ${item.soLuongTon}</div>
                            </div>
                            <div class="cart-actions">
                                <form method="post" action="/pos/cart/update">
                                    <input type="hidden" name="detailId" value="${item.idChiTietSanPham}">
                                    <input class="input qty-input" type="number" name="quantity" min="0" max="${item.soLuongTon}" value="${item.soLuongMua}">
                                </form>
                                <div class="money"><fmt:formatNumber value="${item.thanhTien}" type="number" groupingUsed="true"/> đ</div>
                                <form method="post" action="/pos/cart/remove">
                                    <input type="hidden" name="detailId" value="${item.idChiTietSanPham}">
                                    <button class="button danger" type="submit">Xóa</button>
                                </form>
                            </div>
                        </div>
                    </c:forEach>
                    <c:if test="${cart.rong}">
                        <div class="muted">Chưa có sản phẩm trong giỏ.</div>
                    </c:if>
                </div>

                <div class="summary">
                    <div class="summary-row">
                        <span>Tạm tính</span>
                        <strong><fmt:formatNumber value="${summary.tamTinh}" type="number" groupingUsed="true"/> đ</strong>
                    </div>
                    <div class="summary-row">
                        <span>Ưu đãi</span>
                        <strong><fmt:formatNumber value="${summary.tienGiam}" type="number" groupingUsed="true"/> đ</strong>
                    </div>
                    <div class="muted">${summary.tenUuDai}</div>
                    <div class="summary-row total">
                        <span>Thanh toán</span>
                        <span><fmt:formatNumber value="${summary.tongThanhToan}" type="number" groupingUsed="true"/> đ</span>
                    </div>
                </div>

                <form class="checkout-grid" method="post" action="/pos/phieu-giam-gia/chon">
                    <div class="field">
                        <label for="discountCouponId">Phiếu giảm giá</label>
                        <select class="select" id="discountCouponId" name="discountCouponId" onchange="this.form.submit()">
                            <option value="">
                                <c:choose>
                                    <c:when test="${empty selectedDiscountCouponId and not empty summary.idPhieuGiamGia}">
                                        Tự động: ${summary.tenUuDai}
                                    </c:when>
                                    <c:otherwise>
                                        Tự động: không có phiếu phù hợp
                                    </c:otherwise>
                                </c:choose>
                            </option>
                            <c:forEach items="${discountCoupons}" var="coupon">
                                <option value="${coupon.id}" <c:if test="${selectedDiscountCouponId == coupon.id}">selected</c:if>>
                                    ${coupon.maPhieuGiamGia} - ${coupon.tenPhieuGiamGia}
                                </option>
                            </c:forEach>
                        </select>
                    </div>
                </form>

                <form class="checkout-grid" method="post" action="/pos/checkout">
                    <div class="field">
                        <label for="customerId">Khách quen</label>
                        <select class="select" id="customerId" name="customerId">
                            <option value="">Khách vãng lai</option>
                            <c:forEach items="${customers}" var="customer">
                                <option value="${customer.id}">${customer.maKhachHang} - ${customer.tenKhachHang} - ${customer.soDienThoai}</option>
                            </c:forEach>
                        </select>
                    </div>
                    <div class="field">
                        <label for="customerName">Tên khách vãng lai</label>
                        <input class="input" id="customerName" name="customerName" placeholder="Khách vãng lai">
                    </div>
                    <div class="field">
                        <label for="customerPhone">Số điện thoại</label>
                        <input class="input" id="customerPhone" name="customerPhone" placeholder="Có thể để trống">
                    </div>
                    <div class="field">
                        <label for="customerEmail">Email</label>
                        <input class="input" id="customerEmail" name="customerEmail" placeholder="Có thể để trống">
                    </div>
                    <div class="field">
                        <label for="paymentMethodId">Phương thức thanh toán</label>
                        <select class="select" id="paymentMethodId" name="paymentMethodId">
                            <c:forEach items="${paymentMethods}" var="method">
                                <option value="${method.id}">${method.tenPhuongThuc} - ${method.nhaCungCap}</option>
                            </c:forEach>
                        </select>
                    </div>
                    <div class="field">
                        <label for="note">Ghi chú</label>
                        <textarea class="textarea" id="note" name="note"></textarea>
                    </div>
                    <button class="button full" type="submit" <c:if test="${cart.rong}">disabled</c:if>>Thanh toán và tạo hóa đơn</button>
                </form>
            </div>
        </aside>
    </main>
    </div>
</div>
</body>
</html>
