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
    <title>Quản lý hóa đơn - Victor Sport</title>
    <link rel="stylesheet" href="/css/victorsport.css?v=20261107-balanced">
    <link rel="stylesheet" href="/css/loc-hoa-don.css?v=20261107-balanced">
<link rel="stylesheet" href="/css/giao-dien-chung.css?v=20261107-readable"><link rel="stylesheet" href="/css/admin-theme.css?v=20261112-jsp-3"><style>
@media screen {
    .admin-page.invoice-list-page .invoice-table {border-radius:14px;border-color:#e4eaf2;}
    .admin-page.invoice-list-page .invoice-table .table {min-width:1160px;table-layout:auto;}
    .admin-page.invoice-list-page .invoice-table .table th {font-size:12px;letter-spacing:.3px;padding:15px 14px;background:#f5f7fb;color:#73849b;white-space:nowrap;}
    .admin-page.invoice-list-page .invoice-table .table td {padding:18px 14px;vertical-align:middle;font-size:14px;border-bottom:1px solid #edf1f6;}
    .admin-page.invoice-list-page .invoice-table .table tbody tr:nth-child(even) {background:#fcfdff;}
    .admin-page.invoice-list-page .invoice-table .table tbody tr:hover {background:#f5f8fd;}
    .admin-page.invoice-list-page .invoice-table .table :is(th,td):first-child {width:42px;min-width:42px;text-align:center;color:#91a0b4;}
    .admin-page.invoice-list-page .invoice-table .table :is(th,td):nth-child(2) {min-width:105px;white-space:nowrap;}
    .admin-page.invoice-list-page .invoice-table .table :is(th,td):nth-child(3) {min-width:170px;white-space:nowrap;}
    .admin-page.invoice-list-page .invoice-table .table :is(th,td):nth-child(4) {min-width:115px;white-space:nowrap;font-variant-numeric:tabular-nums;color:#65788f;}
    .admin-page.invoice-list-page .invoice-table .table :is(th,td):nth-child(5) {min-width:110px;white-space:nowrap;font-variant-numeric:tabular-nums;}
    .admin-page.invoice-list-page .invoice-table .table :is(th,td):nth-child(6) {min-width:160px;text-align:right;white-space:nowrap;}
    .admin-page.invoice-list-page .invoice-table .table :is(th,td):nth-child(7) {min-width:195px;}
    .admin-page.invoice-list-page .invoice-table .table :is(th,td):last-child {width:1%;min-width:260px;text-align:right;}
    .invoice-code {font-weight:700;color:#23476d;text-decoration:none;letter-spacing:.2px;}.invoice-code:hover{color:#d56a2b;}
    .invoice-created-time {display:block;margin-top:5px;color:#91a0b3;font-size:12px;}
    .invoice-list-page .invoice-refund {display:block;font-size:11px;font-weight:500;color:#ad6a2c;margin-top:6px;}
    .invoice-state {display:inline-flex;align-items:center;gap:6px;border-radius:7px;padding:5px 9px;font-size:12px;font-weight:600;white-space:nowrap;background:#f0f3f8;color:#63758b;}
    .invoice-state:before{content:'';width:5px;height:5px;border-radius:50%;background:currentColor;}
    .invoice-state-1 {background:#fff5e6;color:#b37b24;}.invoice-state-2{background:#edf3ff;color:#5275b7;}.invoice-state-3{background:#f1edfc;color:#8063b4;}.invoice-state-4{background:#eaf5fc;color:#3b83ae;}.invoice-state-5{background:#eaf7f0;color:#338365;}.invoice-state-6{background:#fdf0f0;color:#bf6464;}
    .admin-page.invoice-list-page .invoice-table .table .invoice-channel {font-size:12px;color:#8b9ab0;margin-top:7px;line-height:1.5;white-space:nowrap;}
    .admin-page.invoice-list-page .invoice-table .invoice-actions {display:flex;flex-direction:row;flex-wrap:nowrap;align-items:center;justify-content:flex-end;gap:7px;}
    .admin-page.invoice-list-page .invoice-table .invoice-actions form {margin:0;flex:0 0 auto;}
    .admin-page.invoice-list-page .invoice-table .invoice-actions .button {width:auto;min-height:34px;padding:8px 11px;border-radius:8px;font-size:12px;line-height:1.3;white-space:nowrap;flex:0 0 auto;box-shadow:none;}
    .admin-page.invoice-list-page .invoice-table .invoice-actions .button.secondary {background:white;border-color:#dfe7f1;color:#61758e;}
    .admin-page.invoice-list-page .invoice-table .invoice-actions .button.danger {background:#fff7f7;border-color:#f8dddd;color:#c66868;}
}
</style></head>
<body class="admin-page invoice-list-page">
<div class="app-shell">
    <jsp:include page="/WEB-INF/view/admin/layout/sidebar.jsp"/>
    <div class="main-area">
        <header class="topbar">
            <div class="topbar-heading">
                <jsp:include page="/WEB-INF/view/admin/layout/menu-toggle.jsp"/>
                <div>
                    <h1>Quản lý hóa đơn</h1>
                    <div class="muted">Victor Sport</div>
                </div>
            </div>
            <div class="topbar-user">
                <jsp:include page="/WEB-INF/view/admin/layout/store-link.jsp"/>
                <a class="admin-user-chip" href="/tai-khoan" title="Thông tin tài khoản"><span class="admin-user-avatar" aria-hidden="true"><c:out value="${empty currentUser.hoTen ? 'V' : currentUser.hoTen.substring(0,1)}"/></span><span class="admin-user-name"><c:out value="${currentUser.hoTen}"/></span></a>
                <form method="post" action="/logout">
                    <button class="button secondary" type="submit">Đăng xuất</button>
                </form>
            </div>
        </header>
        <main class="content-layout">
            <section class="panel">
                <div class="panel-header">
                    <h2>Danh sách hóa đơn</h2>
                </div>
                <div class="panel-body">
                    <c:if test="${not empty error}">
                        <div class="alert error" data-vs-flash="error"><c:out value="${error}"/></div>
                    </c:if>
                    <c:if test="${not empty success}">
                        <div class="alert success" data-vs-flash="success"><c:out value="${success}"/></div>
                    </c:if>
                    <form class="invoice-filters" method="get" action="/quan-ly/hoa-don">
                        <div class="invoice-filter-search"><label for="timHoaDon">Tìm kiếm</label><input class="input" id="timHoaDon" name="tuKhoa" value="<c:out value='${tuKhoa}'/>" placeholder="Mã hóa đơn, tên khách hàng, số điện thoại"></div>
                        <div><label for="locTrangThaiHoaDon">Trạng thái đơn</label><select class="select" id="locTrangThaiHoaDon" name="trangThai">
                            <option value="">Tất cả trạng thái</option>
                            <option value="0" ${trangThai == 0 and trangThai != null ? 'selected' : ''}>Đơn chờ</option>
                            <option value="1" ${trangThai == 1 ? 'selected' : ''}>Chờ xác nhận</option>
                            <option value="2" ${trangThai == 2 ? 'selected' : ''}>Đã xác nhận đơn</option>
                            <option value="3" ${trangThai == 3 ? 'selected' : ''}>Đang chuẩn bị hàng</option>
                            <option value="4" ${trangThai == 4 ? 'selected' : ''}>Đang vận chuyển</option>
                            <option value="5" ${trangThai == 5 ? 'selected' : ''}>Đã nhận hàng / Hoàn tất</option>
                            <option value="6" ${trangThai == 6 ? 'selected' : ''}>Đã hủy</option>
                        </select></div>
                        <div><label for="locLoaiDon">Loại đơn</label><select class="select" id="locLoaiDon" name="loaiDon"><option value="">Online và tại quầy</option><option value="online" ${loaiDon == 'online' ? 'selected' : ''}>Online</option><option value="tai-quay" ${loaiDon == 'tai-quay' ? 'selected' : ''}>Tại quầy</option></select></div>
                        <div><label for="locThanhToanHoaDon">Thanh toán</label><select class="select" id="locThanhToanHoaDon" name="thanhToan"><option value="">Tất cả</option><option value="da-thanh-toan" ${thanhToan == 'da-thanh-toan' ? 'selected' : ''}>Đã thanh toán</option><option value="chua-thanh-toan" ${thanhToan == 'chua-thanh-toan' ? 'selected' : ''}>Chưa thanh toán đủ</option></select></div>
                        <div><label for="locTuNgayHoaDon">Ngày tạo từ</label><input class="input" id="locTuNgayHoaDon" type="date" name="tuNgay" value="<c:out value='${tuNgay}'/>"></div>
                        <div><label for="locDenNgayHoaDon">Đến ngày</label><input class="input" id="locDenNgayHoaDon" type="date" name="denNgay" value="<c:out value='${denNgay}'/>"></div>
                        <div class="invoice-filter-actions"><button class="button" type="submit">Lọc hóa đơn</button><a class="button secondary" href="/quan-ly/hoa-don">Xóa bộ lọc</a></div>
                    </form>
                    <div class="invoice-table">
                        <table class="table">
                            <thead>
                            <tr><th scope="col" class="table-stt">STT</th>
                                <th>Mã hóa đơn</th>
                                <th>Khách hàng</th>
                                <th>Số điện thoại</th>
                                <th>Ngày tạo</th>
                                <th>Tổng thanh toán</th>
                                <th>Trạng thái</th>
                                <th>Thao tác</th>
                            </tr>
                            </thead>
                            <tbody>
                            <c:forEach items="${danhSachHoaDon}" var="hd" varStatus="st">
                                <tr><td class="table-stt">${tuDong + st.index}</td>
                                    <td><a class="invoice-code" href="/quan-ly/hoa-don/${hd.id}"><c:out value="${hd.ma_hoa_don}"/></a></td>
                                    <td><c:out value="${hd.ten_khach_hang}"/></td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${empty hd.so_dien_thoai_khach_hang or hd.so_dien_thoai_khach_hang == '0000000000'}">Không có</c:when>
                                            <c:otherwise><c:out value="${hd.so_dien_thoai_khach_hang}"/></c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td><fmt:formatDate value="${hd.ngay_tao}" pattern="dd/MM/yyyy"/><span class="invoice-created-time"><fmt:formatDate value="${hd.ngay_tao}" pattern="HH:mm"/></span></td>
                                    <td class="money"><fmt:formatNumber value="${hd.tong_tien_sau_giam}" groupingUsed="true"/> đ<c:if test="${hd.tong_tien_da_hoan > 0}"><div class="invoice-refund">Đã hoàn <fmt:formatNumber value="${hd.tong_tien_da_hoan}" maxFractionDigits="2"/> đ</div></c:if></td>
                                    <td><span class="invoice-state invoice-state-${hd.trang_thai_hien_tai}"><c:out value="${hd.ten_trang_thai}"/></span><div class="invoice-channel">${hd.loai_don == 2 ? 'Tại quầy · Giao hàng' : (hd.loai_don == 0 ? 'Tại quầy' : 'Online')} · ${hd.da_thanh_toan ? 'Đã thanh toán' : 'Chưa thanh toán đủ'}</div></td>
                                    <td><div class="invoice-actions">
                                        <a class="button secondary" href="/quan-ly/hoa-don/${hd.id}" title="Xem chi tiết hóa đơn">Chi tiết</a>
<c:if test="${currentUser.xuLyDonHang and (hd.loai_don == 1 or hd.loai_don == 2) and hd.phuong_thuc == 'COD' and hd.trang_thai_hien_tai == 1}">
<form method="post" action="/quan-ly/hoa-don/${hd.id}/xac-nhan-don-cod" data-xac-nhan-thanh-toan data-tieu-de="Xác nhận đơn COD" data-nhan-xac-nhan="Xác nhận đơn" data-tong-tien="${hd.tong_tien_sau_giam}" data-nhan-phuong-thuc="Trạng thái" data-phuong-thuc="Đã xác nhận đơn" data-noi-dung="Xác nhận đơn COD để chuyển sang bước chuẩn bị hàng."><input type="hidden" name="quayLai" value="danh-sach"><button class="button" title="Xác nhận đơn COD">Xác nhận COD</button></form></c:if>


                                        <c:if test="${currentUser.xuLyDonHang and (hd.vnpay or hd.qr_demo) and hd.da_thanh_toan and hd.trang_thai_hien_tai == 1}">
                                            <form method="post" action="/quan-ly/hoa-don/${hd.id}/xac-nhan-thanh-toan" data-xac-nhan-thanh-toan data-tong-tien="${hd.tong_tien_sau_giam}" data-tieu-de="Xác nhận đơn hàng" data-nhan-xac-nhan="Xác nhận đơn" data-phuong-thuc="${hd.qr_demo ? 'QR đã xác nhận thanh toán' : 'VNPAY Sandbox đã thanh toán'}" data-noi-dung="Hệ thống đã ghi nhận thanh toán thử nghiệm. Xác nhận tiếp nhận đơn để chuẩn bị hàng."><input type="hidden" name="quayLai" value="danh-sach"><button class="button" type="submit" title="Xác nhận đơn hàng">Xác nhận</button></form>
                                        </c:if>
                                        <c:if test="${currentUser.xuLyDonHang and (hd.loai_don == 1 or hd.loai_don == 2) and hd.phuong_thuc != 'COD' and not hd.vnpay and not hd.qr_demo and hd.trang_thai_hien_tai == 1}">
                                            <form method="post" action="/quan-ly/hoa-don/${hd.id}/xac-nhan-thanh-toan" data-xac-nhan-thanh-toan data-ma-hoa-don="<c:out value='${hd.ma_hoa_don}'/>" data-tong-tien="${hd.tong_tien_sau_giam}" data-nhan-phuong-thuc="Trạng thái" data-tieu-de="Xác nhận thanh toán" data-phuong-thuc="Đã xác nhận đơn" data-noi-dung="Kiểm tra khoản chuyển khoản để xác nhận thanh toán và đơn hàng. Đơn sẽ chuyển sang chờ chuẩn bị.">
                                                <input type="hidden" name="quayLai" value="danh-sach"><button class="button" type="submit" title="Xác nhận thanh toán">Xác nhận</button>
                                            </form>
                                        </c:if>
                                        <c:if test="${currentUser.xuLyDonHang and (hd.loai_don == 1 or hd.loai_don == 2) and hd.trang_thai_hien_tai == 2}">
                                            <form method="post" action="/quan-ly/hoa-don/${hd.id}/chuan-bi" data-xac-nhan-thanh-toan data-tong-tien="${hd.tong_tien_sau_giam}" data-tieu-de="Bắt đầu chuẩn bị hàng" data-nhan-phuong-thuc="Trạng thái" data-phuong-thuc="Đang chuẩn bị hàng" data-noi-dung="Xác nhận cửa hàng bắt đầu lấy và đóng gói sản phẩm.">
                                                <input type="hidden" name="quayLai" value="danh-sach"><button class="button" type="submit" title="Bắt đầu chuẩn bị hàng">Chuẩn bị</button>
                                            </form>
                                        </c:if>
                                        <c:if test="${currentUser.xuLyDonHang and (hd.loai_don == 1 or hd.loai_don == 2) and hd.trang_thai_hien_tai == 1}">
                                            <form method="post" action="/quan-ly/hoa-don/${hd.id}/huy" data-xac-nhan-thanh-toan data-ma-hoa-don="<c:out value='${hd.ma_hoa_don}'/>" data-tong-tien="${hd.tong_tien_sau_giam}" data-tieu-de="Hủy hóa đơn" data-nhan-xac-nhan="Hủy hóa đơn" data-nhan-phuong-thuc="Trạng thái" data-phuong-thuc="Đã hủy" data-noi-dung="Bạn có chắc muốn hủy hóa đơn này? Số lượng sản phẩm sẽ được hoàn lại tồn kho.">
                                                <input type="hidden" name="quayLai" value="danh-sach"><button class="button danger" type="submit" title="Hủy hóa đơn">Hủy</button>
                                            </form>
                                        </c:if>
                                        <c:if test="${currentUser.xuLyDonHang and (empty hd.id_khach_hang or hd.loai_don == 2) and (hd.loai_don == 1 or hd.loai_don == 2) and hd.trang_thai_hien_tai == 4}">
                                            <form method="post" action="/quan-ly/hoa-don/${hd.id}/giao-khach-vang-lai" data-xac-nhan-thanh-toan data-tong-tien="${hd.tong_tien_sau_giam}" data-tieu-de="Xác nhận khách đã nhận hàng" data-nhan-xac-nhan="Xác nhận đã nhận hàng" data-nhan-phuong-thuc="Trạng thái" data-phuong-thuc="Đã nhận hàng" data-noi-dung="Xác nhận khi khách đã nhận đủ hàng. Với đơn COD online, xác nhận đồng thời ghi nhận đã trả đủ tiền."><input type="hidden" name="quayLai" value="danh-sach"><button class="button" type="submit" title="Xác nhận khách đã nhận hàng">Đã giao hàng</button></form>
                                        </c:if>
                                        <c:if test="${currentUser.banHangTaiQuay and hd.trang_thai_hien_tai == 0 and (hd.loai_don == 0 or hd.loai_don == 2) and hd.id_nhan_vien == currentUser.id}">
                                            <form method="post" action="/pos/don-cho/mo">
                                                <input type="hidden" name="idDonCho" value="${hd.id}">
                                                <button class="button" type="submit">Tiếp tục bán</button>
                                            </form>
                                            <form method="post" action="/pos/don-cho/huy">
                                                <input type="hidden" name="idDonCho" value="${hd.id}">
                                                <input type="hidden" name="quayLai" value="hoa-don">
                                                <button class="button danger" type="submit">Hủy đơn chờ</button>
                                            </form>
                                        </c:if>
                                    </div></td>
                                </tr>
                            </c:forEach>
                            <c:if test="${empty danhSachHoaDon}">
                                <tr><td colspan="8" class="muted">Không có hóa đơn phù hợp.</td></tr>
                            </c:if>
                            </tbody>
                        </table>
                    </div>
                        <c:url var="urlPhanTrang" value="/quan-ly/hoa-don">
                            <c:param name="tuKhoa" value="${tuKhoa}"/><c:param name="trangThai" value="${trangThai}"/>
                            <c:param name="loaiDon" value="${loaiDon}"/><c:param name="thanhToan" value="${thanhToan}"/>
                            <c:param name="tuNgay" value="${tuNgay}"/><c:param name="denNgay" value="${denNgay}"/>
                        </c:url>
                        <div class="pagination invoice-pagination">
                            <p><c:choose><c:when test="${tongHoaDon gt 0}">Hiển thị <strong>${tuDong}–${denDong}</strong> / ${tongHoaDon} hóa đơn · Trang ${trang}/${soTrang}</c:when><c:otherwise>0 hóa đơn</c:otherwise></c:choose></p>
                            <c:if test="${tongHoaDon gt 0}">
                            <nav class="invoice-page-links" aria-label="Phân trang hóa đơn">
                                <c:choose><c:when test="${trang gt 1}">
                                    <a href="<c:out value='${urlPhanTrang}'/>&amp;trang=1" aria-label="Trang đầu">« Đầu</a>
                                    <a href="<c:out value='${urlPhanTrang}'/>&amp;trang=${trang - 1}" rel="prev">‹ Trước</a>
                                </c:when><c:otherwise><span aria-disabled="true">« Đầu</span><span aria-disabled="true">‹ Trước</span></c:otherwise></c:choose>
                                <c:forEach begin="${trangBatDau}" end="${trangKetThuc}" var="so">
                                    <c:choose><c:when test="${so == trang}"><span aria-current="page" aria-label="Trang ${so}">${so}</span></c:when>
                                        <c:otherwise><a href="<c:out value='${urlPhanTrang}'/>&amp;trang=${so}" aria-label="Trang ${so}">${so}</a></c:otherwise></c:choose>
                                </c:forEach>
                                <c:choose><c:when test="${trang lt soTrang}">
                                    <a href="<c:out value='${urlPhanTrang}'/>&amp;trang=${trang + 1}" rel="next">Tiếp ›</a>
                                    <a href="<c:out value='${urlPhanTrang}'/>&amp;trang=${soTrang}" aria-label="Trang cuối">Cuối »</a>
                                </c:when><c:otherwise><span aria-disabled="true">Tiếp ›</span><span aria-disabled="true">Cuối »</span></c:otherwise></c:choose>
                            </nav>
                            </c:if>
                        </div>
                </div>
            </section>
        </main>
    </div>
</div>
<%@ include file="/WEB-INF/view/client/layout/payment-confirm.jsp" %>
</body>
</html>
