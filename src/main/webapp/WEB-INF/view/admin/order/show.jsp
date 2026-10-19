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
<link rel="stylesheet" href="/css/giao-dien-chung.css?v=20261018-2"></head>
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
        <main class="content-layout">
            <section class="panel">
                <div class="panel-header">
                    <h2>Danh sách hóa đơn</h2>
                    <span class="muted">Đơn mới nhất ở trên</span>
                </div>
                <div class="panel-body">
                    <c:if test="${not empty error}">
                        <div class="alert error" data-vs-flash="error"><c:out value="${error}"/></div>
                    </c:if>
                    <c:if test="${not empty success}">
                        <div class="alert success" data-vs-flash="success"><c:out value="${success}"/></div>
                    </c:if>
                    <form class="search-row" method="get" action="/quan-ly/hoa-don">
                        <input class="input" name="tuKhoa" value="<c:out value='${tuKhoa}'/>"
                               placeholder="Tìm mã hóa đơn, tên khách hàng, số điện thoại" aria-label="Tìm hóa đơn">
                        <button class="button" type="submit">Tìm kiếm</button>
                    </form>
                    <div class="invoice-table">
                        <table class="table">
                            <thead>
                            <tr>
                                <th>Mã hóa đơn</th>
                                <th>Khách hàng</th>
                                <th>Số điện thoại</th>
                                <th>Ngày tạo</th>
                                <th>Tổng tiền sau giảm</th>
                                <th>Trạng thái</th>
                                <th>Thao tác</th>
                            </tr>
                            </thead>
                            <tbody>
                            <c:forEach items="${danhSachHoaDon}" var="hd">
                                <tr>
                                    <td><c:out value="${hd.ma_hoa_don}"/></td>
                                    <td><c:out value="${hd.ten_khach_hang}"/></td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${empty hd.so_dien_thoai_khach_hang or hd.so_dien_thoai_khach_hang == '0000000000'}">Không có</c:when>
                                            <c:otherwise><c:out value="${hd.so_dien_thoai_khach_hang}"/></c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td><fmt:formatDate value="${hd.ngay_tao}" pattern="dd/MM/yyyy HH:mm"/></td>
                                    <td class="money"><fmt:formatNumber value="${hd.tong_tien_sau_giam}" groupingUsed="true"/> đ</td>
                                    <td><c:out value="${hd.ten_trang_thai}"/></td>
                                    <td><div class="invoice-actions">
                                        <a class="button secondary" href="/quan-ly/hoa-don/${hd.id}">Xem chi tiết</a>
<c:if test="${currentUser.quanLy and (hd.loai_don == 1 or hd.loai_don == 2) and hd.phuong_thuc == 'COD' and hd.trang_thai_hien_tai == 1}">
<form method="post" action="/quan-ly/hoa-don/${hd.id}/xac-nhan-don-cod" data-xac-nhan-thanh-toan data-tieu-de="Xác nhận đơn COD" data-nhan-xac-nhan="Xác nhận đơn" data-tong-tien="${hd.tong_tien_sau_giam}" data-nhan-phuong-thuc="Trạng thái" data-phuong-thuc="Đang vận chuyển" data-noi-dung="Xác nhận giao đơn COD. Chưa ghi nhận thu tiền."><input type="hidden" name="quayLai" value="danh-sach"><button class="button">Xác nhận đơn COD</button></form></c:if>
<c:if test="${currentUser.quanLy and (hd.loai_don == 1 or hd.loai_don == 2) and hd.phuong_thuc == 'COD' and not hd.da_thanh_toan and (hd.trang_thai_hien_tai == 4 or hd.trang_thai_hien_tai == 5)}">
<form method="post" action="/quan-ly/hoa-don/${hd.id}/thu-tien-cod" data-xac-nhan-thanh-toan data-tieu-de="Xác nhận đã thu tiền COD" data-tong-tien="${hd.tong_tien_sau_giam}" data-noi-dung="Chỉ xác nhận sau khi đã nhận và đối soát đủ tiền COD."><input type="hidden" name="quayLai" value="danh-sach"><button class="button">Đã thu tiền COD</button></form></c:if>

                                        <c:if test="${currentUser.quanLy and (hd.loai_don == 1 or hd.loai_don == 2) and hd.phuong_thuc != 'COD' and hd.trang_thai_hien_tai >= 1 and hd.trang_thai_hien_tai <= 3}">
                                            <form method="post" action="/quan-ly/hoa-don/${hd.id}/xac-nhan-thanh-toan" data-xac-nhan-thanh-toan data-ma-hoa-don="<c:out value='${hd.ma_hoa_don}'/>" data-tong-tien="${hd.tong_tien_sau_giam}" data-nhan-phuong-thuc="Trạng thái" data-tieu-de="Xác nhận thanh toán" data-phuong-thuc="Đang vận chuyển" data-noi-dung="Sau khi xác nhận, đơn hàng sẽ chuyển sang trạng thái Đang vận chuyển.">
                                                <input type="hidden" name="quayLai" value="danh-sach"><button class="button" type="submit">Xác nhận thanh toán</button>
                                            </form>
                                        </c:if>
                                        <c:if test="${currentUser.quanLy and (hd.loai_don == 1 or hd.loai_don == 2) and hd.trang_thai_hien_tai == 1}">
                                            <form method="post" action="/quan-ly/hoa-don/${hd.id}/huy" data-xac-nhan-thanh-toan data-ma-hoa-don="<c:out value='${hd.ma_hoa_don}'/>" data-tong-tien="${hd.tong_tien_sau_giam}" data-tieu-de="Hủy hóa đơn" data-nhan-xac-nhan="Hủy hóa đơn" data-nhan-phuong-thuc="Trạng thái" data-phuong-thuc="Đã hủy" data-noi-dung="Bạn có chắc muốn hủy hóa đơn này? Số lượng sản phẩm sẽ được hoàn lại tồn kho.">
                                                <input type="hidden" name="quayLai" value="danh-sach"><button class="button danger" type="submit">Hủy hóa đơn</button>
                                            </form>
                                        </c:if>
                                        <c:if test="${currentUser.banHangTaiQuay and hd.trang_thai_hien_tai == 0 and hd.loai_don == 0 and hd.id_nhan_vien == currentUser.id}">
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
                                <tr><td colspan="7" class="muted">Không có hóa đơn phù hợp.</td></tr>
                            </c:if>
                            </tbody>
                        </table>
                    </div>
                </div>
            </section>
        </main>
    </div>
</div>
<%@ include file="/WEB-INF/view/client/layout/payment-confirm.jsp" %>
</body>
</html>
