<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <link rel="icon" type="image/svg+xml" href="/logo/favicon-victorsport.svg?v=1">
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Quản lý hóa đơn - Victor Sport</title>
    <link rel="stylesheet" href="/css/victorsport.css?v=20261107-balanced">
    <link rel="stylesheet" href="/css/chi-tiet-hoa-don.css?v=20261109-customer-dialog">
<link rel="stylesheet" href="/css/in-hoa-don.css?v=4"><link rel="stylesheet" href="/css/giao-dien-chung.css?v=20261107-readable"><link rel="stylesheet" href="/css/admin-theme.css?v=20261112-jsp-3"></head>
<body class="admin-page">
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
        <main class="content-layout invoice-detail" data-hoa-don-da-thanh-toan="${hoaDon.da_thanh_toan}" data-ma-hoa-don="${hoaDon.id}"><div class="invoice-print-brand"><img class="invoice-print-logo" src="/logo/logo-nobackgroud.png" alt="VictorSport" width="210" height="64"><span>Cầu Giấy, Hà Nội · 0325327602</span></div>
            <section class="panel">
                <div class="panel-header">
                    <div><span class="invoice-caption">CHI TIẾT HÓA ĐƠN</span><h2><c:out value="${hoaDon.ma_hoa_don}"/></h2></div>
                    <a class="button secondary" href="/quan-ly/hoa-don">Quay lại danh sách</a>
                </div>
                <div class="panel-body">
                    <c:if test="${not empty error}"><div class="alert error" role="alert" data-vs-flash="error"><c:out value="${error}"/></div></c:if>
                    <c:if test="${not empty success}"><div class="alert success" role="status" data-vs-flash="success"><c:out value="${success}"/></div></c:if>
                    <div class="invoice-toolbar">
                        <span class="invoice-status invoice-status-${hoaDon.trang_thai_hien_tai}"><span aria-hidden="true">●</span> <c:out value="${hoaDon.ten_trang_thai}"/></span>
                        <div class="invoice-actions"><c:if test="${hoaDon.da_thanh_toan}"><button type="button" class="button secondary" onclick="window.print()">In / Lưu PDF</button></c:if>
<c:if test="${currentUser.quanLy and (hoaDon.loai_don == 1 or hoaDon.loai_don == 2) and hoaDon.phuong_thuc == 'COD' and hoaDon.trang_thai_hien_tai == 1}">
<form method="post" action="/quan-ly/hoa-don/${hoaDon.id}/xac-nhan-don-cod" data-xac-nhan-thanh-toan data-tieu-de="Xác nhận đơn COD" data-nhan-xac-nhan="Xác nhận đơn" data-tong-tien="${hoaDon.tong_tien_sau_giam}" data-nhan-phuong-thuc="Trạng thái" data-phuong-thuc="Đã xác nhận đơn" data-noi-dung="Xác nhận đơn COD để chuyển sang bước chuẩn bị hàng."><input type="hidden" name="quayLai" value="chi-tiet"><button class="button">Xác nhận đơn COD</button></form></c:if>


                    <c:if test="${currentUser.quanLy and (hoaDon.vnpay or hoaDon.qr_demo) and hoaDon.trang_thai_hien_tai == 1}">
                        <c:choose>
                            <c:when test="${hoaDon.da_thanh_toan}"><form method="post" action="/quan-ly/hoa-don/${hoaDon.id}/xac-nhan-thanh-toan" data-xac-nhan-thanh-toan data-tong-tien="${hoaDon.tong_tien_sau_giam}" data-tieu-de="Xác nhận đơn hàng" data-nhan-xac-nhan="Xác nhận đơn" data-phuong-thuc="${hoaDon.qr_demo ? 'QR đã xác nhận thanh toán' : 'VNPAY Sandbox đã thanh toán'}" data-noi-dung="Hệ thống đã ghi nhận thanh toán thử nghiệm. Xác nhận tiếp nhận đơn để chuẩn bị hàng."><input type="hidden" name="quayLai" value="chi-tiet"><button class="button" type="submit">Xác nhận đơn hàng</button></form></c:when>
                            <c:otherwise><p class="muted">Chờ hệ thống xác nhận thanh toán tự động.</p></c:otherwise>
                        </c:choose>
                    </c:if>
                    <c:if test="${currentUser.quanLy and (hoaDon.loai_don == 1 or hoaDon.loai_don == 2) and hoaDon.phuong_thuc != 'COD' and not hoaDon.vnpay and not hoaDon.qr_demo and hoaDon.trang_thai_hien_tai == 1}">
                        <form method="post" action="/quan-ly/hoa-don/${hoaDon.id}/xac-nhan-thanh-toan" class="invoice-actions" data-xac-nhan-thanh-toan data-ma-hoa-don="<c:out value='${hoaDon.ma_hoa_don}'/>" data-tong-tien="${hoaDon.tong_tien_sau_giam}" data-nhan-phuong-thuc="Trạng thái" data-tieu-de="Xác nhận thanh toán" data-phuong-thuc="Đã xác nhận đơn" data-noi-dung="Kiểm tra khoản chuyển khoản để xác nhận thanh toán và đơn hàng. Đơn sẽ chuyển sang chờ chuẩn bị.">
                            <button class="button" type="submit">${hoaDon.vnpay ? 'Xác nhận đơn hàng' : 'Xác nhận thanh toán'}</button>
                        </form>
                    </c:if>
                    <c:if test="${currentUser.quanLy and (hoaDon.loai_don == 1 or hoaDon.loai_don == 2) and hoaDon.trang_thai_hien_tai == 2}">
                        <form method="post" action="/quan-ly/hoa-don/${hoaDon.id}/chuan-bi" class="invoice-actions" data-xac-nhan-thanh-toan data-tong-tien="${hoaDon.tong_tien_sau_giam}" data-tieu-de="Bắt đầu chuẩn bị hàng" data-nhan-phuong-thuc="Trạng thái" data-phuong-thuc="Đang chuẩn bị hàng" data-noi-dung="Xác nhận cửa hàng bắt đầu lấy và đóng gói sản phẩm.">
                            <button class="button" type="submit">Bắt đầu chuẩn bị</button>
                        </form>
                    </c:if>
                    <c:if test="${currentUser.quanLy and (hoaDon.loai_don == 1 or hoaDon.loai_don == 2) and hoaDon.trang_thai_hien_tai == 3}">
                        <form method="post" action="/quan-ly/hoa-don/${hoaDon.id}/gui-hang" data-vs-confirm="Xác nhận bàn giao hàng cho shipper? Hệ thống sẽ tự tạo mã vận đơn GHN cho đơn giao qua GHN." data-vs-title="Xác nhận gửi hàng"><button class="button" type="submit">Gửi hàng</button></form>
                    </c:if>
                    <c:if test="${currentUser.quanLy and (hoaDon.loai_don == 1 or hoaDon.loai_don == 2) and hoaDon.trang_thai_hien_tai == 1}">
                        <form class="invoice-actions" method="post" action="/quan-ly/hoa-don/${hoaDon.id}/huy" data-xac-nhan-thanh-toan data-ma-hoa-don="<c:out value='${hoaDon.ma_hoa_don}'/>" data-tong-tien="${hoaDon.tong_tien_sau_giam}" data-tieu-de="Hủy hóa đơn" data-nhan-xac-nhan="Hủy hóa đơn" data-nhan-phuong-thuc="Trạng thái" data-phuong-thuc="Đã hủy" data-noi-dung="Bạn có chắc muốn hủy hóa đơn này? Số lượng sản phẩm sẽ được hoàn lại tồn kho.">
                            <button class="button danger" type="submit">Hủy hóa đơn</button>
                        </form>
                    </c:if>
                        <c:if test="${currentUser.quanLy and (empty hoaDon.id_khach_hang or hoaDon.loai_don == 2) and (hoaDon.loai_don == 1 or hoaDon.loai_don == 2) and hoaDon.trang_thai_hien_tai == 4}">
                            <form method="post" action="/quan-ly/hoa-don/${hoaDon.id}/giao-khach-vang-lai" data-xac-nhan-thanh-toan data-tong-tien="${hoaDon.tong_tien_sau_giam}" data-tieu-de="Xác nhận khách đã nhận hàng" data-nhan-xac-nhan="Xác nhận đã nhận hàng" data-nhan-phuong-thuc="Trạng thái" data-phuong-thuc="Đã nhận hàng" data-noi-dung="Xác nhận khi khách đã nhận đủ hàng. Với đơn COD online, xác nhận đồng thời ghi nhận đã trả đủ tiền."><button class="button" type="submit">Xác nhận khách đã nhận hàng</button></form>
                        </c:if>
                        </div>
                    </div>
                    <c:if test="${(hoaDon.loai_don == 1 or hoaDon.loai_don == 2) and hoaDon.trang_thai_hien_tai == 5}">
                        <div class="alert success">Đơn đã giao thành công. Trạng thái đơn hàng đã được khóa.</div>
                    </c:if>
                    <c:if test="${currentUser.banHangTaiQuay and hoaDon.trang_thai_hien_tai == 0 and (hoaDon.loai_don == 0 or hoaDon.loai_don == 2) and hoaDon.id_nhan_vien == currentUser.id}">
                        <div class="invoice-actions">
                            <form method="post" action="/pos/don-cho/mo">
                                <input type="hidden" name="idDonCho" value="${hoaDon.id}">
                                <button class="button" type="submit">Tiếp tục bán</button>
                            </form>
                            <form method="post" action="/pos/don-cho/huy">
                                <input type="hidden" name="idDonCho" value="${hoaDon.id}">
                                <input type="hidden" name="quayLai" value="hoa-don">
                                <button class="button danger" type="submit">Hủy đơn chờ</button>
                            </form>
                        </div>
                    </c:if>
                    <p><strong>Phương thức:</strong> <c:out value="${hoaDon.phuong_thuc}"/> · <strong>Thanh toán:</strong> ${hoaDon.da_thanh_toan ? 'Đã thanh toán' : 'Chưa thanh toán'}</p>
<c:if test="${not empty hoaDon.vanDonGhn.ma_van_don}"><p><strong>Mã vận đơn:</strong> <c:out value="${hoaDon.vanDonGhn.ma_van_don}"/></p></c:if>
<div class="invoice-info">
                        <div class="invoice-info-card">
                            <h3>Thông tin khách hàng</h3>
                            <p><strong>Khách hàng:</strong> <c:out value="${hoaDon.ten_khach_hang}"/></p>
                            <p><strong>Số điện thoại:</strong>
                                <c:choose>
                                    <c:when test="${empty hoaDon.so_dien_thoai_khach_hang or hoaDon.so_dien_thoai_khach_hang == '0000000000'}">Không có</c:when>
                                    <c:otherwise><c:out value="${hoaDon.so_dien_thoai_khach_hang}"/></c:otherwise>
                                </c:choose>
                            </p>
                            <p><strong>Email:</strong>
                                <c:choose>
                                    <c:when test="${empty hoaDon.email_hien_thi or hoaDon.email_hien_thi == 'khachvanglai@gmail.com'}">Không có</c:when>
                                    <c:otherwise><c:out value="${hoaDon.email_hien_thi}"/></c:otherwise>
                                </c:choose>
                            </p>
                            <p><strong>Địa chỉ:</strong> <c:out value="${hoaDon.dia_chi_khach_hang}"/></p>
                            <c:if test="${currentUser.quanLy and (hoaDon.loai_don == 1 or hoaDon.loai_don == 2) and hoaDon.trang_thai_hien_tai ge 1 and hoaDon.trang_thai_hien_tai lt 4 and empty hoaDon.vanDonGhn.ma_van_don}">
                                <div class="invoice-edit-address invoice-no-print">
                                    <button class="button secondary" type="button" id="moSuaNhanHang" aria-haspopup="dialog" aria-controls="suaNhanHangHoaDon">Sửa thông tin nhận hàng</button>
                                </div>
                                <dialog id="suaNhanHangHoaDon" class="invoice-address-dialog invoice-no-print" aria-labelledby="tieuDeSuaNhanHang">
                                    <h2 id="tieuDeSuaNhanHang">Sửa thông tin nhận hàng</h2>
                                    <form method="post" action="/quan-ly/hoa-don/${hoaDon.id}/dia-chi">
                                        <div class="field"><label for="hoaDonTenNhan">Người nhận</label><input class="input" id="hoaDonTenNhan" name="ten" maxlength="100" value="<c:out value='${hoaDon.ten_khach_hang}'/>" required autofocus></div>
                                        <div class="field"><label for="hoaDonSdtNhan">Điện thoại</label><input class="input" id="hoaDonSdtNhan" name="sdt" type="tel" maxlength="10" pattern="0[0-9]{9}" value="<c:out value='${hoaDon.so_dien_thoai_khach_hang}'/>" required></div>
                                        <c:if test="${not empty hoaDon.vanDonGhn}">
                                            <p class="muted">Khu vực hiện tại: <c:out value="${hoaDon.vanDonGhn.ten_xa}"/>, <c:out value="${hoaDon.vanDonGhn.ten_tinh}"/></p>
                                            <div class="field"><label for="hoaDonTinh">Đổi tỉnh / thành phố</label><select class="select" id="hoaDonTinh" name="tinh"><option value="">Giữ khu vực hiện tại</option></select></div>
                                            <div class="field"><label for="hoaDonXa">Phường / xã</label><select class="select" id="hoaDonXa" name="xa" disabled><option value="">Chọn phường / xã</option></select></div>
                                            <p id="hoaDonDiaChiStatus" class="muted" role="status"></p>
                                        </c:if>
                                        <div class="field"><label for="hoaDonDiaChi">Địa chỉ chi tiết</label><input class="input" id="hoaDonDiaChi" name="diaChi" maxlength="255" value="<c:out value='${hoaDon.dia_chi_chi_tiet}'/>" required></div>
                                        <p class="muted">Số tiền đã chốt trên hóa đơn được giữ khi đổi địa chỉ.</p>
                                        <div class="invoice-address-actions">
                                            <button class="button secondary" type="button" data-dong-sua-nhan-hang>Đóng</button>
                                            <button class="button" type="submit">Lưu thông tin</button>
                                        </div>
                                    </form>
                                </dialog>
                            </c:if>
                        </div>
                        <div class="invoice-info-card">
                            <h3>Thông tin hóa đơn</h3>
                            <p><strong>Nhân viên:</strong> <c:out value="${hoaDon.ten_nhan_vien}" default="Chưa có"/></p>
                            <p><strong>Ngày tạo:</strong> <fmt:formatDate value="${hoaDon.ngay_tao}" pattern="dd/MM/yyyy HH:mm"/></p>
                            <p><strong>Ngày thanh toán:</strong>
                                <c:choose>
                                    <c:when test="${empty hoaDon.ngay_thanh_toan}">Chưa thanh toán</c:when>
                                    <c:otherwise><fmt:formatDate value="${hoaDon.ngay_thanh_toan}" pattern="dd/MM/yyyy HH:mm"/></c:otherwise>
                                </c:choose>
                            </p>
                            <p><strong>Trạng thái:</strong> <c:out value="${hoaDon.ten_trang_thai}"/></p>
                        </div>
                    </div>
                    <p class="invoice-note"><strong>Ghi chú:</strong> <c:out value="${hoaDon.ghi_chu}" default="Không có"/></p>
                    <c:if test="${hoaDon.trang_thai_hien_tai == 0 and hoaDon.id_nhan_vien == currentUser.id}">
                        <form method="post" action="/pos/don-cho/mo" class="invoice-no-print">
                            <input type="hidden" name="idDonCho" value="${hoaDon.id}">
                            <button class="button secondary" type="submit">Sửa đơn chờ tại quầy</button>
                        </form>
                    </c:if>
                    <h3 class="invoice-section-title">Sản phẩm trong đơn <span>${danhSachSanPham.size()} sản phẩm</span></h3>
                    <div class="invoice-table">
                        <table class="table">
                            <thead>
                            <tr>
                                <th>Sản phẩm</th>
                                <th>Màu sắc</th>
                                <th>Kích thước</th>
                                <th>Số lượng</th>
                                <th>Đơn giá</th>
                                <th>Thành tiền</th>
                            </tr>
                            </thead>
                            <tbody>
                            <c:forEach items="${danhSachSanPham}" var="sp">
                                <tr>
                                    <td>
                                        <c:out value="${sp.ten_san_pham}" default="Sản phẩm không còn thông tin"/>
                                        <div class="muted"><c:out value="${sp.ma_san_pham}"/></div>
                                    </td>
                                    <td><c:out value="${sp.ten_gioi_tinh}"/> · <c:out value="${sp.ten_mau_sac}"/></td>
                                    <td><c:out value="${sp.ten_kich_thuoc}"/></td>
                                    <td>
                                        <c:out value="${sp.so_luong}"/>
                                        <c:if test="${currentUser.quanLy and hoaDon.loai_don == 1 and hoaDon.phuong_thuc == 'COD'
                                            and not hoaDon.co_thanh_toan and empty hoaDon.id_phieu_giam_gia and hoaDon.tong_tien_giam == 0
                                            and hoaDon.trang_thai_hien_tai ge 1 and hoaDon.trang_thai_hien_tai lt 4 and empty hoaDon.vanDonGhn.ma_van_don}">
                                            <details class="invoice-quantity-edit invoice-no-print">
                                                <summary>Sửa</summary>
                                                <form method="post" action="/quan-ly/hoa-don/${hoaDon.id}/so-luong">
                                                    <input type="hidden" name="idDong" value="${sp.id_dong}">
                                                    <input type="hidden" name="soLuongCu" value="${sp.so_luong}">
                                                    <input class="input" type="number" name="soLuong" min="1" step="1" max="2147483647" value="${sp.so_luong}" required aria-label="Số lượng mới">
                                                    <button class="button secondary" type="submit">Lưu</button>
                                                </form>
                                            </details>
                                        </c:if>
                                    </td>
                                    <td class="money"><fmt:formatNumber value="${sp.don_gia}" groupingUsed="true"/> đ</td>
                                    <td class="money"><fmt:formatNumber value="${sp.thanh_tien}" groupingUsed="true"/> đ</td>
                                </tr>
                            </c:forEach>
                            <c:if test="${empty danhSachSanPham}">
                                <tr><td colspan="6" class="muted">Hóa đơn chưa có sản phẩm.</td></tr>
                            </c:if>
                            </tbody>
                        </table>
                    </div>
                    <c:if test="${hoaDon.loai_don == 1 or hoaDon.loai_don == 2}">
                    <section class="invoice-delivery-steps" aria-label="Tiến trình giao hàng">
                        <h3>Tiến trình giao hàng</h3>
                        <c:choose>
                            <c:when test="${hoaDon.trang_thai_hien_tai == 6}"><p class="delivery-cancelled">Đơn hàng đã hủy.</p></c:when>
                            <c:otherwise><ol class="delivery-steps">
                                <li class="delivery-step ${hoaDon.trang_thai_hien_tai > 1 ? 'is-done' : 'is-current'}"><span class="delivery-step-number">1</span><strong>Khách đặt đơn</strong><small>${hoaDon.trang_thai_hien_tai > 1 ? 'Đã xong' : 'Hiện tại'}</small></li>
                                <li class="delivery-step ${hoaDon.trang_thai_hien_tai > 2 ? 'is-done' : (hoaDon.trang_thai_hien_tai == 2 ? 'is-current' : 'is-waiting')}"><span class="delivery-step-number">2</span><strong>Admin xác nhận đơn</strong><small>${hoaDon.trang_thai_hien_tai > 2 ? 'Đã xong' : (hoaDon.trang_thai_hien_tai == 2 ? 'Hiện tại' : 'Đang chờ')}</small></li>
                                <li class="delivery-step ${hoaDon.trang_thai_hien_tai > 3 ? 'is-done' : (hoaDon.trang_thai_hien_tai == 3 ? 'is-current' : 'is-waiting')}"><span class="delivery-step-number">3</span><strong>Đang chuẩn bị hàng</strong><small>${hoaDon.trang_thai_hien_tai > 3 ? 'Đã xong' : (hoaDon.trang_thai_hien_tai == 3 ? 'Hiện tại' : 'Đang chờ')}</small></li>
                                <li class="delivery-step ${hoaDon.trang_thai_hien_tai >= 4 ? 'is-done' : 'is-waiting'}"><span class="delivery-step-number">4</span><strong>Gửi hàng cho shipper</strong><small>${hoaDon.trang_thai_hien_tai >= 4 ? 'Đã bàn giao' : 'Đang chờ'}</small></li>
                                <li class="delivery-step ${hoaDon.trang_thai_hien_tai > 4 ? 'is-done' : (hoaDon.trang_thai_hien_tai == 4 ? 'is-current' : 'is-waiting')}"><span class="delivery-step-number">5</span><strong>Đang vận chuyển</strong><small>${hoaDon.trang_thai_hien_tai > 4 ? 'Đã xong' : (hoaDon.trang_thai_hien_tai == 4 ? 'Hiện tại' : 'Đang chờ')}</small></li>
                                <li class="delivery-step ${hoaDon.trang_thai_hien_tai == 5 ? 'is-done' : 'is-waiting'}"><span class="delivery-step-number">6</span><strong>Khách xác nhận đã nhận hàng</strong><small>${hoaDon.trang_thai_hien_tai == 5 ? 'Đã xong' : 'Đang chờ'}</small></li>
                            </ol></c:otherwise>
                        </c:choose>
                    </section>
                    </c:if>
                    <div class="invoice-bottom">
                    <section class="invoice-history">
                        <h3>Lịch sử đơn hàng</h3>
                        <ol>
                            <c:forEach items="${lichSu}" var="buoc"><li><strong><fmt:formatDate value="${buoc.thoi_gian}" pattern="dd/MM/yyyy HH:mm"/></strong><p><c:out value="${buoc.ghi_chu}"/></p></li></c:forEach>
                        </ol>
                        <c:if test="${empty lichSu}"><p class="muted">Chưa có lịch sử đơn hàng.</p></c:if>
                    </section>
                    <div class="summary invoice-total">
                        <h3>Thanh toán</h3>
                        <div class="summary-row"><span>Tổng tiền hàng</span><strong><fmt:formatNumber value="${hoaDon.tong_tien}" groupingUsed="true"/> đ</strong></div>
                        <div class="summary-row"><span>Giảm giá</span><strong><fmt:formatNumber value="${hoaDon.tong_tien_giam}" groupingUsed="true"/> đ</strong></div>
                        <div class="summary-row"><span>Phí vận chuyển</span><strong><fmt:formatNumber value="${hoaDon.phi_van_chuyen}" groupingUsed="true"/> đ</strong></div>
                        <div class="summary-row total"><span>Tổng thanh toán</span><strong><fmt:formatNumber value="${hoaDon.tong_tien_sau_giam}" groupingUsed="true"/> đ</strong></div>
                        <div class="summary-row"><span>Đã thu</span><strong><fmt:formatNumber value="${hoaDon.da_thu}" groupingUsed="true"/> đ</strong></div>
                        <c:if test="${not empty hoaDon.ghi_chu_tien_mat}"><p><c:out value="${hoaDon.ghi_chu_tien_mat}"/></p></c:if>
                    </div>
                    </div>
                </div>
            </section>
        </main>
    </div>
</div>
<%@ include file="/WEB-INF/view/client/layout/payment-confirm.jsp" %>
<script src="/js/in-hoa-don.js?v=20261109-customer-dialog"></script>
</body>
</html>
