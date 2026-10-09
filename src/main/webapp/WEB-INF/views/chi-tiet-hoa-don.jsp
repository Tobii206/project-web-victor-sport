<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Quản lý hóa đơn - Victor Sport</title>
    <link rel="stylesheet" href="/css/victorsport.css">
</head>
<body>
<div class="app-shell">
    <jsp:include page="menu.jsp"/>
    <div class="main-area">
        <header class="topbar">
            <div>
                <h1>Quản lý hóa đơn</h1>
                <div class="muted">Victor Sport</div>
            </div>
            <div class="topbar-user">
                <span><c:out value="${currentUser.hoTen}"/> - <c:out value="${currentUser.tenQuyen}"/></span>
                <form method="post" action="/logout">
                    <button class="button secondary" type="submit">Đăng xuất</button>
                </form>
            </div>
        </header>
        <main class="content-layout">
            <section class="panel">
                <div class="panel-header">
                    <h2>Hóa đơn <c:out value="${hoaDon.ma_hoa_don}"/></h2>
                    <a class="button secondary" href="/quan-ly/hoa-don">Quay lại danh sách</a>
                </div>
                <div class="panel-body">
                    <c:if test="${hoaDon.trang_thai_hien_tai == 0 and hoaDon.loai_don == 0 and hoaDon.id_nhan_vien == currentUser.id}">
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
                    <div class="invoice-info">
                        <div>
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
                        <div>
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
                    <p><strong>Ghi chú:</strong> <c:out value="${hoaDon.ghi_chu}" default="Không có"/></p>
                    <h3>Sản phẩm trong đơn</h3>
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
                    <div class="summary">
                        <div class="summary-row"><span>Tổng tiền hàng</span><strong><fmt:formatNumber value="${hoaDon.tong_tien}" groupingUsed="true"/> đ</strong></div>
                        <div class="summary-row"><span>Giảm giá</span><strong><fmt:formatNumber value="${hoaDon.tong_tien_giam}" groupingUsed="true"/> đ</strong></div>
                        <div class="summary-row"><span>Phí vận chuyển</span><strong><fmt:formatNumber value="${hoaDon.phi_van_chuyen}" groupingUsed="true"/> đ</strong></div>
                        <div class="summary-row total"><span>Tổng tiền sau giảm</span><strong><fmt:formatNumber value="${hoaDon.tong_tien_sau_giam}" groupingUsed="true"/> đ</strong></div>
                    </div>
                </div>
            </section>
        </main>
    </div>
</div>
</body>
</html>
