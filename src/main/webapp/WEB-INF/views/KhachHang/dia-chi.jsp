<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html><html lang="vi"><head>
    <meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1">
    <title>Địa chỉ của tôi — VictorSport</title>
    <link rel="icon" href="/logo/favicon-victorsport.svg">
    <link rel="stylesheet" href="/css/trang-chu.css?v=gia-giam-1">
    <link rel="stylesheet" href="/css/dia-chi-khach-hang.css">
<link rel="stylesheet" href="/css/giao-dien-chung.css?v=20261018-2"></head><body>
<%@ include file="/WEB-INF/views/KhachHang/dau-trang-online.jsp" %>
<main class="store-container online-container">
    <a href="/tai-khoan">← Về tài khoản</a>
    <h1>Địa chỉ của tôi</h1>
    <p class="subtle">Lưu địa chỉ nhà, công ty để chọn nhanh khi mua hàng.</p>
    <c:if test="${not empty success}"><div class="online-success" role="status" data-vs-flash="success"><c:out value="${success}"/></div></c:if>
    <c:if test="${not empty error}"><div class="account-error" role="alert" data-vs-flash="error"><c:out value="${error}"/></div></c:if>
    <div class="address-layout">
        <section class="address-list" aria-label="Địa chỉ đã lưu">
            <c:forEach items="${diaChiDaLuu}" var="dc">
                <article class="account-card address-card">
                    <div class="address-heading"><h2><c:out value="${dc.ten_dia_chi}"/></h2><c:if test="${dc.mac_dinh}"><span class="address-default">Mặc định</span></c:if></div>
                    <p><c:out value="${dc.dia_chi_day_du}"/></p>
                    <div class="address-actions">
                        <a href="/tai-khoan/dia-chi?sua=${dc.id}#formDiaChi">Sửa</a>
                        <c:if test="${not dc.mac_dinh}"><form method="post" action="/tai-khoan/dia-chi/${dc.id}/mac-dinh"><button type="submit">Đặt mặc định</button></form></c:if>
                        <form method="post" action="/tai-khoan/dia-chi/${dc.id}/xoa" data-vs-confirm="Bạn muốn xóa địa chỉ nhận hàng đã lưu này?" data-vs-title="Xóa địa chỉ" data-vs-button="Xóa địa chỉ" data-vs-tone="danger"><button type="submit" class="address-delete">Xóa</button></form>
                    </div>
                </article>
            </c:forEach>
            <c:if test="${empty diaChiDaLuu}"><div class="account-card"><h2>Chưa có địa chỉ</h2><p>Thêm địa chỉ đầu tiên để dùng cho những lần mua sau.</p></div></c:if>
        </section>
        <section class="account-card" id="formDiaChi">
            <h2>${empty diaChiSua ? 'Thêm địa chỉ mới' : 'Sửa địa chỉ'}</h2>
            <form class="account-form" method="post" action="/tai-khoan/dia-chi/luu">
                <input type="hidden" name="id" value="${diaChiSua.id}">
                <label for="tenDiaChi">Tên địa chỉ</label>
                <input id="tenDiaChi" name="tenDiaChi" maxlength="100" placeholder="Ví dụ: Nhà riêng, Công ty" value="<c:out value='${not empty formCu ? formCu.tenDiaChi : diaChiSua.ten_dia_chi}'/>" required>
                <label for="thanhPho">Tỉnh / Thành phố</label>
                <select id="thanhPho" name="thanhPho" data-cu="<c:out value='${not empty formCu ? formCu.thanhPho : diaChiSua.thanh_pho}'/>" required><option value="">Chọn tỉnh / thành phố</option></select>
                <label for="phuong">Phường / Xã</label>
                <select id="phuong" name="phuong" data-cu="<c:out value='${not empty formCu ? formCu.phuong : diaChiSua.phuong}'/>" required><option value="">Chọn phường / xã</option></select>
                <p id="loiDiaChi" class="account-error" hidden>Chưa tải được danh mục địa chỉ. Vui lòng tải lại trang.</p>
                <label for="diaChiCuThe">Địa chỉ chi tiết</label>
                <input id="diaChiCuThe" name="diaChiCuThe" maxlength="255" placeholder="Số nhà, tên đường, tòa nhà…" value="<c:out value='${not empty formCu ? formCu.diaChiCuThe : diaChiSua.dia_chi_cu_the}'/>" required>
                <label class="address-checkbox"><input type="checkbox" name="macDinh" value="true" ${(not empty formCu ? formCu.macDinh : diaChiSua.mac_dinh) ? 'checked' : ''}> Đặt làm địa chỉ mặc định</label>
                <button class="store-button" type="submit">Lưu địa chỉ</button>
                <c:if test="${not empty diaChiSua}"><a href="/tai-khoan/dia-chi">Hủy chỉnh sửa / Thêm địa chỉ mới</a></c:if>
            </form>
        </section>
    </div>
</main>
<script src="/js/chon-dia-chi.js?v=20261018-muot"></script>
</body></html>
