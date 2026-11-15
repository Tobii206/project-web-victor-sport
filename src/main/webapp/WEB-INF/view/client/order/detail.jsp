<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<fmt:setLocale value="vi_VN"/>
<!DOCTYPE html><html lang="vi"><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1">
<title>Đơn hàng của tôi — VictorSport</title><link rel="icon" href="/logo/favicon-victorsport.svg">
<link rel="stylesheet" href="/css/trang-chu.css?v=20261107-balanced"><link rel="stylesheet" href="/css/don-hang.css?v=20261109-recipient-button"><link rel="stylesheet" href="/css/in-hoa-don.css?v=20261114-balanced-a4"><link rel="stylesheet" href="/css/giao-dien-chung.css?v=20261107-readable"><style>
@media screen {
.order-detail-page{padding-top:28px;padding-bottom:48px}.order-detail-page>.account-back{display:inline-flex;align-items:center;margin:0 0 18px;color:#64748b;font-size:14px;text-decoration:none}.order-detail-page>.account-back:hover{color:#e86620}
.order-summary{background:#fff;border:1px solid #e2e8f0;border-radius:20px;padding:26px 30px;margin-bottom:22px;box-shadow:0 4px 20px #142d5005}.order-summary .don-hang-heading{margin:0;align-items:flex-start}.order-summary .eyebrow{margin:0 0 6px;font-size:12px;font-weight:700;letter-spacing:1.4px;color:#7b8ca3}.order-summary h1{margin:0 0 10px}.order-summary .subtle{margin:0;font-size:14px}.order-summary .don-hang-status{margin-top:8px}
.order-meta{display:flex;align-items:center;flex-wrap:wrap;gap:10px 24px;margin-top:12px;color:#6b7f98;font-size:14px}.order-meta strong{color:#25466a;letter-spacing:.4px}.order-toolbar{display:flex;align-items:center;gap:10px;flex-wrap:wrap;margin-top:22px;padding-top:20px;border-top:1px solid #edf1f6}.order-toolbar .store-button{display:inline-flex;align-items:center;justify-content:center;gap:9px;min-height:42px;padding:11px 17px;border-radius:10px;font-size:14px;line-height:1.4;box-shadow:none;font-family:inherit;cursor:pointer}.order-toolbar svg{width:18px;height:18px;fill:none;stroke:currentColor;stroke-width:1.7;stroke-linecap:round;stroke-linejoin:round;flex-shrink:0}.order-toolbar .order-action-neutral{background:#f5f8fc;color:#28486d;border:1px solid #e1e8f2}.order-toolbar .order-action-neutral:hover{background:#eaf0f8}.order-toolbar .order-action-return{background:#fff7ef;color:#c15b1c;border:1px solid #ffdfc4;margin-left:auto}.order-toolbar .order-action-return:hover{background:#ffeddd}
.order-detail-page>.online-success{margin:0 0 22px;padding:16px 20px;border-radius:12px}.order-detail-page .don-hang-columns{gap:22px;margin:22px 0}.order-detail-page .don-hang-columns>.account-card{margin:0;padding:26px;min-width:0}.order-detail-page .don-hang-columns h2{margin:0 0 20px;font-size:20px}.order-refund-total{padding-top:14px;border-top:1px dashed #dce5ee;color:#17785d!important}.order-detail-page .don-hang-card:hover{transform:none}
@media(max-width:680px){.order-detail-store .announcement{display:none}.order-detail-page{padding-top:20px}.order-summary{padding:20px}.order-summary .don-hang-heading{gap:12px}.order-summary .don-hang-status{margin-top:0;font-size:12px;padding:6px 11px}.order-meta{gap:10px}.order-toolbar{gap:8px;margin-top:18px;padding-top:16px}.order-toolbar .store-button{flex:1 1 145px;padding:11px 12px}.order-toolbar .order-action-return{margin-left:0}.order-detail-page .don-hang-columns{grid-template-columns:1fr}.order-detail-page .don-hang-columns>.account-card{padding:22px}}
}
</style></head>
<body class="store-page order-detail-store"><%@ include file="/WEB-INF/view/client/layout/header.jsp" %>
<main class="store-container online-container order-detail-page">
<c:set var="coTheSuaNhanHang" value="${not khachVangLai and (don.loai_don == 1 or don.loai_don == 2)
    and don.trang_thai_hien_tai >= 1 and don.trang_thai_hien_tai <= 3 and empty don.vanDonGhn.ma_van_don}"/>
<div class="invoice-print-brand"><img class="invoice-print-logo" src="/logo/logo-nobackgroud.png" alt="VictorSport" width="210" height="64"><span>Cầu Giấy, Hà Nội · 0325327602</span></div>
<a class="account-back" href="${khachVangLai ? '/tra-cuu-don-hang' : '/don-hang'}">← Quay lại</a>
<c:if test="${not empty error and not (moSuaNhanHang and coTheSuaNhanHang)}"><div class="account-error" role="alert" data-vs-flash="error"><c:out value="${error}"/></div></c:if>
<c:if test="${not empty success}"><div class="online-success" role="status" data-vs-flash="success"><c:out value="${success}"/></div></c:if>
<section class="order-summary" aria-label="Tổng quan đơn hàng">
<div class="don-hang-heading"><div><p class="eyebrow">ĐƠN HÀNG CỦA BẠN</p><h1><c:out value="${don.ma_hoa_don}"/></h1><p class="subtle">Ngày đặt: <fmt:formatDate value="${don.ngay_tao}" pattern="dd/MM/yyyy HH:mm"/></p></div>
<span class="don-hang-status invoice-no-print ${don.trang_thai_hien_tai == 5 ? 'da-nhan' : ''} ${don.trang_thai_hien_tai == 6 ? 'da-huy' : ''}"><c:out value="${don.ten_trang_thai}"/></span></div>
<c:if test="${not empty don.vanDonGhn.ma_van_don}"><div class="order-meta"><span>Mã vận đơn: <strong><c:out value="${don.vanDonGhn.ma_van_don}"/></strong></span></div></c:if>
<c:if test="${don.da_thanh_toan or don.vnpay or don.qr_demo or don.trang_thai_hien_tai == 5 or (don.trang_thai_hien_tai >= 1 and don.trang_thai_hien_tai <= 3 and don.ghi_chu == 'Chuyển khoản QR - chờ kiểm tra thanh toán')}">
<div class="order-toolbar invoice-no-print" role="group" aria-label="Thao tác đơn hàng">
<c:if test="${don.da_thanh_toan}"><button class="store-button" type="button" onclick="window.print()"><svg viewBox="0 0 24 24" aria-hidden="true"><path d="M7 8V3h10v5M7 17H4V9h16v8h-3M7 14h10v7H7zM17 11h1"/></svg>In / Lưu PDF</button></c:if>
<c:if test="${don.vnpay}"><a class="store-button order-action-neutral" href="/thanh-toan/vnpay/${don.id}">Xem thanh toán VNPAY Sandbox</a></c:if>
<c:if test="${don.qr_demo}"><a class="store-button order-action-neutral" href="/thanh-toan/qr-demo/${don.id}"><svg viewBox="0 0 24 24" aria-hidden="true"><path d="M3 3h6v6H3zM15 3h6v6h-6zM3 15h6v6H3zM15 15h3v3h3v3h-6zM21 12v3M12 3v3M12 12h3M12 18v3"/></svg>Xem thanh toán QR</a></c:if>
<c:if test="${don.trang_thai_hien_tai >= 1 and don.trang_thai_hien_tai <= 3 and don.ghi_chu == 'Chuyển khoản QR - chờ kiểm tra thanh toán'}"><a class="store-button order-action-neutral" href="/thanh-toan/qr/${don.id}">Xem mã QR thanh toán</a></c:if>
<c:if test="${don.trang_thai_hien_tai == 5}"><a class="store-button order-action-return" href="/tra-hang/don/${don.id}"><svg viewBox="0 0 24 24" aria-hidden="true"><path d="M8 4 3 9l5 5M3 9h11a7 7 0 0 1 0 14" transform="translate(0 -2)"/></svg>Trả hàng / hoàn tiền</a></c:if>
</div></c:if></section>
<c:if test="${(don.trang_thai_hien_tai >= 1 and don.trang_thai_hien_tai <= 3) and not don.co_thanh_toan}"><section class="account-card don-hang-row don-hang-card"><p>Bạn có thể hủy đơn trước khi cửa hàng gửi hàng cho shipper.</p>
<form method="post" action="${khachVangLai ? '/don-vang-lai/' : '/don-hang/'}${don.id}/huy" data-vs-confirm="Bạn muốn hủy đơn hàng này? Sản phẩm đã chọn trong đơn sẽ được hoàn lại tồn kho." data-vs-title="Hủy đơn hàng" data-vs-button="Hủy đơn hàng" data-vs-tone="danger"><button class="store-button don-hang-cancel" type="submit">Hủy đơn</button></form></section></c:if>
<c:if test="${don.trang_thai_hien_tai == 4}"><section class="account-card don-hang-row don-hang-card"><p>Đơn hàng đang vận chuyển. Hãy xác nhận sau khi bạn đã nhận được hàng.</p>
<form method="post" action="${khachVangLai ? '/don-vang-lai/' : '/don-hang/'}${don.id}/da-nhan-hang" data-vs-confirm="Hãy kiểm tra đã nhận đủ hàng. Nếu thanh toán COD, chỉ xác nhận sau khi đã trả đủ tiền cho shipper; hệ thống sẽ đồng thời ghi nhận thanh toán." data-vs-title="Xác nhận đã nhận hàng" data-vs-button="Đã nhận hàng" data-vs-tone="normal"><button class="store-button" type="submit">${don.phuong_thuc == 'COD' ? 'Đã nhận hàng và trả tiền COD' : 'Đã nhận được hàng'}</button></form></section></c:if>
<c:if test="${don.trang_thai_hien_tai == 5}"><div class="online-success invoice-no-print">Đã nhận hàng. Cảm ơn bạn đã mua sắm tại VictorSport!</div></c:if>
<c:if test="${don.trang_thai_hien_tai == 6}"><div class="account-error">Đơn hàng đã được hủy.</div></c:if>
<div class="don-hang-columns"><section class="account-card"><h2>Thông tin nhận hàng</h2>
<p><strong>Người nhận:</strong> <c:out value="${don.ten_khach_hang}"/></p><p><strong>Số điện thoại:</strong> <c:out value="${don.so_dien_thoai_khach_hang}"/></p>
<p><strong>Địa chỉ:</strong> <c:out value="${don.dia_chi_khach_hang}"/></p>
<c:if test="${coTheSuaNhanHang}">
    <div class="don-hang-edit-recipient invoice-no-print">
        <button class="store-button don-hang-button-neutral" type="button" id="moSuaNhanHang" aria-haspopup="dialog" aria-controls="suaNhanHangHoaDon">Sửa thông tin nhận hàng</button>
    </div>
    <dialog id="suaNhanHangHoaDon" class="invoice-address-dialog invoice-no-print" aria-labelledby="tieuDeSuaNhanHang" data-mo-khi-tai="${moSuaNhanHang}">
        <h2 id="tieuDeSuaNhanHang">Sửa thông tin nhận hàng</h2>
        <c:if test="${moSuaNhanHang and not empty error}"><div class="account-error" role="alert"><c:out value="${error}"/></div></c:if>
        <form method="post" action="/don-hang/${don.id}/dia-chi" class="account-form">
            <input type="hidden" name="csrf" value="<c:out value='${sessionScope.csrfSuaNhanHang}'/>">
            <div><label for="donTenNhan">Người nhận</label><input id="donTenNhan" name="ten" maxlength="100" value="<c:out value='${don.ten_khach_hang}'/>" required autofocus></div>
            <div><label for="donSdtNhan">Số điện thoại</label><input id="donSdtNhan" name="sdt" type="tel" maxlength="10" pattern="0[0-9]{9}" value="<c:out value='${don.so_dien_thoai_khach_hang}'/>" required></div>
            <c:if test="${not empty don.vanDonGhn}">
                <div><label for="hoaDonTinh">Tỉnh / thành phố</label><select id="hoaDonTinh" name="tinh"><option value="">Giữ khu vực hiện tại</option></select></div>
                <div><label for="hoaDonXa">Phường / xã</label><select id="hoaDonXa" name="xa" disabled><option value="">Chọn phường / xã</option></select></div>
                <p id="hoaDonDiaChiStatus" class="subtle" role="status"></p>
            </c:if>
            <div><label for="donDiaChiNhan">Địa chỉ chi tiết</label><input id="donDiaChiNhan" name="diaChi" maxlength="255" value="<c:out value='${don.dia_chi_chi_tiet}'/>" required></div>
            <div class="invoice-address-actions">
                <button class="store-button don-hang-button-neutral" type="button" data-dong-sua-nhan-hang>Đóng</button>
                <button class="store-button" type="submit">Lưu thông tin</button>
            </div>
        </form>
    </dialog>
</c:if>
</section>
<section class="account-card"><h2>Thanh toán</h2><p>Phương thức: <strong><c:out value="${don.phuong_thuc}"/></strong> · ${don.da_thanh_toan ? 'Đã thanh toán' : 'Chưa thanh toán'}</p><p class="don-hang-row"><span>Tiền hàng</span><strong><fmt:formatNumber value="${don.tong_tien}" maxFractionDigits="0"/> đ</strong></p>
<p class="don-hang-row"><span>Giảm giá</span><strong><fmt:formatNumber value="${don.tong_tien_giam}" maxFractionDigits="0"/> đ</strong></p>
<p class="don-hang-row"><span>Phí vận chuyển</span><strong><fmt:formatNumber value="${don.phi_van_chuyen}" maxFractionDigits="0"/> đ</strong></p>
<p class="don-hang-row"><span>Tổng thanh toán</span><strong class="online-price"><fmt:formatNumber value="${don.tong_tien_sau_giam}" maxFractionDigits="0"/> đ</strong></p><c:if test="${don.tong_tien_da_hoan > 0}"><p class="don-hang-row order-refund-total"><span>Đã hoàn tiền</span><strong><fmt:formatNumber value="${don.tong_tien_da_hoan}" maxFractionDigits="2"/> đ</strong></p></c:if></section></div>
<section class="account-card don-hang-card"><h2>Sản phẩm đã đặt</h2>
<c:forEach items="${sanPham}" var="sp"><div class="don-hang-item don-hang-row"><div><strong><c:out value="${sp.ten_san_pham}"/></strong>
<p class="subtle"><c:out value="${sp.ten_mau_sac}"/> · <c:out value="${sp.ten_gioi_tinh}"/> · <c:out value="${sp.ten_kich_thuoc}"/> · Số lượng: <c:out value="${sp.so_luong}"/></p>
<p>Đơn giá: <fmt:formatNumber value="${sp.don_gia}" maxFractionDigits="0"/> đ</p></div><strong><fmt:formatNumber value="${sp.thanh_tien}" maxFractionDigits="0"/> đ</strong></div></c:forEach></section>
<section class="account-card don-hang-card"><h2>Lịch sử đơn hàng</h2><ol class="don-hang-timeline">
<c:forEach items="${lichSu}" var="buoc"><li><p><strong><fmt:formatDate value="${buoc.thoi_gian}" pattern="dd/MM/yyyy HH:mm"/></strong></p><p><c:out value="${buoc.ghi_chu}"/></p><c:if test="${not empty buoc.id_yeu_cau}"><a href="/tra-hang/${buoc.id_yeu_cau}" class="invoice-no-print">Xem yêu cầu <c:out value="${buoc.ma_yeu_cau}"/> →</a></c:if></li></c:forEach>
</ol><c:if test="${empty lichSu}"><p class="subtle">Chưa có lịch sử đơn hàng.</p></c:if></section>
</main><script src="/js/in-hoa-don.js?v=20261109-customer-dialog"></script></body></html>
