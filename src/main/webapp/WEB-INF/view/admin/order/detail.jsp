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
    <link rel="stylesheet" href="/css/victorsport.css?v=admin-20261014">
    <link rel="stylesheet" href="/css/chi-tiet-hoa-don.css?v=20261017">
<link rel="stylesheet" href="/css/in-hoa-don.css?v=2"><link rel="stylesheet" href="/css/giao-dien-chung.css?v=20261018-2"></head>
<body>
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
                <span><c:out value="${currentUser.hoTen}"/> - <c:out value="${currentUser.tenQuyen}"/></span>
                <form method="post" action="/logout">
                    <button class="button secondary" type="submit">Đăng xuất</button>
                </form>
            </div>
        </header>
        <main class="content-layout invoice-detail"><div class="invoice-print-brand"><img class="invoice-print-logo" src="/logo/logo-nobackgroud.png" alt="VictorSport" width="210" height="64"><span>Cầu Giấy, Hà Nội · 0325327602</span></div>
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
                        <div class="invoice-actions"><button type="button" class="button secondary" onclick="window.print()">In / Lưu PDF</button>
<c:if test="${currentUser.quanLy and (hoaDon.loai_don == 1 or hoaDon.loai_don == 2) and hoaDon.phuong_thuc == 'COD' and hoaDon.trang_thai_hien_tai == 1}">
<form method="post" action="/quan-ly/hoa-don/${hoaDon.id}/xac-nhan-don-cod" data-xac-nhan-thanh-toan data-tieu-de="Xác nhận đơn COD" data-nhan-xac-nhan="Xác nhận đơn" data-tong-tien="${hoaDon.tong_tien_sau_giam}" data-nhan-phuong-thuc="Trạng thái" data-phuong-thuc="Đang vận chuyển" data-noi-dung="Xác nhận giao đơn COD. Chưa ghi nhận thu tiền."><input type="hidden" name="quayLai" value="chi-tiet"><button class="button">Xác nhận đơn COD</button></form></c:if>
<c:if test="${currentUser.quanLy and (hoaDon.loai_don == 1 or hoaDon.loai_don == 2) and hoaDon.phuong_thuc == 'COD' and not hoaDon.da_thanh_toan and (hoaDon.trang_thai_hien_tai == 4 or hoaDon.trang_thai_hien_tai == 5)}">
<form method="post" action="/quan-ly/hoa-don/${hoaDon.id}/thu-tien-cod" data-xac-nhan-thanh-toan data-tieu-de="Xác nhận đã thu tiền COD" data-tong-tien="${hoaDon.tong_tien_sau_giam}" data-noi-dung="Chỉ xác nhận sau khi đã nhận và đối soát đủ tiền COD."><input type="hidden" name="quayLai" value="chi-tiet"><button class="button">Đã thu tiền COD</button></form></c:if>

                    <c:if test="${currentUser.quanLy and (hoaDon.loai_don == 1 or hoaDon.loai_don == 2) and hoaDon.phuong_thuc != 'COD' and hoaDon.trang_thai_hien_tai >= 1 and hoaDon.trang_thai_hien_tai <= 3}">
                        <form method="post" action="/quan-ly/hoa-don/${hoaDon.id}/xac-nhan-thanh-toan" class="invoice-actions" data-xac-nhan-thanh-toan data-ma-hoa-don="<c:out value='${hoaDon.ma_hoa_don}'/>" data-tong-tien="${hoaDon.tong_tien_sau_giam}" data-nhan-phuong-thuc="Trạng thái" data-tieu-de="Xác nhận thanh toán" data-phuong-thuc="Đang vận chuyển" data-noi-dung="Sau khi xác nhận, đơn hàng sẽ chuyển sang trạng thái Đang vận chuyển.">
                            <button class="button" type="submit">Xác nhận thanh toán</button>
                        </form>
                    </c:if>
                    <c:if test="${currentUser.quanLy and (hoaDon.loai_don == 1 or hoaDon.loai_don == 2) and hoaDon.trang_thai_hien_tai == 1}">
                        <form class="invoice-actions" method="post" action="/quan-ly/hoa-don/${hoaDon.id}/huy" data-xac-nhan-thanh-toan data-ma-hoa-don="<c:out value='${hoaDon.ma_hoa_don}'/>" data-tong-tien="${hoaDon.tong_tien_sau_giam}" data-tieu-de="Hủy hóa đơn" data-nhan-xac-nhan="Hủy hóa đơn" data-nhan-phuong-thuc="Trạng thái" data-phuong-thuc="Đã hủy" data-noi-dung="Bạn có chắc muốn hủy hóa đơn này? Số lượng sản phẩm sẽ được hoàn lại tồn kho.">
                            <button class="button danger" type="submit">Hủy hóa đơn</button>
                        </form>
                    </c:if>
                        </div>
                    </div>
                    <c:if test="${(hoaDon.loai_don == 1 or hoaDon.loai_don == 2) and hoaDon.trang_thai_hien_tai == 5}">
                        <div class="alert success">Khách đã nhận hàng. Trạng thái đơn hàng đã được khóa.</div>
                    </c:if>
                    <c:if test="${currentUser.banHangTaiQuay and hoaDon.trang_thai_hien_tai == 0 and hoaDon.loai_don == 0 and hoaDon.id_nhan_vien == currentUser.id}">
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
<c:if test="${currentUser.quanLy and empty hoaDon.id_khach_hang and (hoaDon.loai_don == 1 or hoaDon.loai_don == 2) and hoaDon.trang_thai_hien_tai == 4}"><form method="post" action="/quan-ly/hoa-don/${hoaDon.id}/giao-khach-vang-lai" data-xac-nhan-thanh-toan data-tieu-de="Xác nhận giao thành công" data-tong-tien="${hoaDon.tong_tien_sau_giam}" data-noi-dung="Xác nhận đã giao hàng cho khách vãng lai. Trạng thái đơn sẽ được khóa."><button class="button secondary">Xác nhận đã giao hàng</button></form></c:if>
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
                                    <c:when test="${empty hoaDon.email_khach_hang or hoaDon.email_khach_hang == 'khachvanglai@gmail.com'}">Không có</c:when>
                                    <c:otherwise><c:out value="${hoaDon.email_khach_hang}"/></c:otherwise>
                                </c:choose>
                            </p>
                            <p><strong>Địa chỉ:</strong> <c:out value="${hoaDon.dia_chi_khach_hang}"/></p>
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
                                    <td><c:out value="${sp.ten_mau_sac}"/></td>
                                    <td><c:out value="${sp.ten_kich_thuoc}"/></td>
                                    <td><c:out value="${sp.so_luong}"/></td>
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
                    </div>
                    </div>
                </div>
            </section>
        </main>
    </div>
</div>
<%@ include file="/WEB-INF/view/client/layout/payment-confirm.jsp" %>
</body>
</html>
