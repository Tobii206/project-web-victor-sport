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
                    <h2>Danh sách hóa đơn</h2>
                    <span class="muted">Đơn mới nhất ở trên</span>
                </div>
                <div class="panel-body">
                    <c:if test="${not empty error}">
                        <div class="alert error"><c:out value="${error}"/></div>
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
                                    <td><a class="button secondary" href="/quan-ly/hoa-don/${hd.id}">Xem chi tiết</a></td>
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
</body>
</html>
