<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<fmt:setLocale value="vi_VN"/>
<!DOCTYPE html><html lang="vi"><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1"><title>Thông tin sản phẩm — VictorSport</title>
<link rel="icon" href="/images/favicon-victorsport.svg"><link rel="stylesheet" href="/css/victorsport.css?v=muot-1"><link rel="stylesheet" href="/css/quan-ly-san-pham.css?v=1"></head><body>
<div class="app-shell"><jsp:include page="/WEB-INF/views/Admin/menu.jsp"/><div class="main-area"><%@ include file="/WEB-INF/views/Admin/dau-trang-san-pham.jsp" %><main class="content-layout">
<div class="product-admin-actions"><a class="button secondary" href="/quan-ly/san-pham">← Danh sách sản phẩm</a><c:if test="${not empty idSanPham}"><strong><c:out value="${maSanPham}"/></strong></c:if></div>
<c:if test="${not empty error}"><div class="alert error" role="alert"><c:out value="${error}"/></div></c:if><c:if test="${not empty success}"><div class="alert success" role="status"><c:out value="${success}"/></div></c:if>
<c:if test="${not currentUser.suaSanPham}"><h2 class="product-admin-section"><c:out value="${sanPham.ten_san_pham}"/></h2></c:if>
<c:if test="${currentUser.suaSanPham}">
<section class="panel product-admin-section"><div class="panel-header"><h2>${empty idSanPham ? 'Thêm sản phẩm' : 'Thông tin sản phẩm'}</h2><span class="muted">Mã sản phẩm được tạo tự động</span></div><div class="panel-body">
<form class="product-admin-form" method="post" action="/quan-ly/san-pham${empty idSanPham ? '' : '/'.concat(idSanPham)}/luu">
    <div class="field product-admin-wide"><label for="tenSanPham">Tên sản phẩm *</label><input class="input" id="tenSanPham" name="ten_san_pham" maxlength="255" value="<c:out value='${sanPham.ten_san_pham}'/>" required></div>
    <c:forEach items="${danhMuc}" var="dm" end="5"><div class="field"><label for="${dm.truong}"><c:out value="${dm.nhan}"/>${dm.truong == 'id_thuong_hieu' ? ' *' : ''}</label><select class="select" id="${dm.truong}" name="${dm.truong}" ${dm.truong == 'id_thuong_hieu' ? 'required' : ''}><option value="">Chọn thông tin</option><c:forEach items="${dm.luaChon}" var="muc"><option value="${muc.id}" <c:if test="${sanPham[dm.truong] == muc.id}">selected</c:if>><c:out value="${muc.ten}"/></option></c:forEach></select></div></c:forEach>
    <div class="field"><label for="trangThaiBan">Trạng thái</label><select class="select" id="trangThaiBan" name="trang_thai_kinh_doanh"><option value="1" ${sanPham.trang_thai_kinh_doanh ? 'selected' : ''}>Đang bán</option><option value="0" ${not sanPham.trang_thai_kinh_doanh ? 'selected' : ''}>Ngừng bán</option></select></div>
    <div class="field product-admin-wide"><label for="moTaNgan">Mô tả ngắn</label><textarea class="textarea" id="moTaNgan" name="mo_ta_ngan" maxlength="500"><c:out value="${sanPham.mo_ta_ngan}"/></textarea></div>
    <div class="field product-admin-wide"><label for="moTaChiTiet">Mô tả chi tiết</label><textarea class="textarea" id="moTaChiTiet" name="mo_ta_chi_tiet" maxlength="20000" rows="5"><c:out value="${sanPham.mo_ta_chi_tiet}"/></textarea></div>
    <c:if test="${currentUser.suaSanPham}"><div class="product-admin-wide"><button class="button">Lưu sản phẩm</button></div></c:if>
</form></div></section>
</c:if>
<c:if test="${not empty idSanPham}">
<section id="mau-giay" class="panel product-admin-section"><div class="panel-header"><h2>Mẫu giày và tồn kho</h2><span class="muted">Mỗi mẫu gồm size, màu và form chân</span></div><div class="panel-body">
<c:if test="${currentUser.suaSanPham}">
<h3>${empty mauForm.id ? 'Thêm mẫu giày' : 'Sửa mẫu giày'}</h3>
<form method="post" action="/quan-ly/san-pham/${idSanPham}/mau/luu" class="product-admin-form">
    <input type="hidden" name="idMau" value="<c:out value='${mauForm.id}'/>"><input type="hidden" name="so_luong_cu" value="<c:out value='${empty mauForm.so_luong_cu ? mauForm.so_luong : mauForm.so_luong_cu}'/>">
    <c:forEach items="${danhMuc}" var="dm" begin="6"><div class="field"><label for="${dm.truong}"><c:out value="${dm.nhan}"/> *</label><select class="select" id="${dm.truong}" name="${dm.truong}" required><option value="">Chọn thông tin</option><c:forEach items="${dm.luaChon}" var="muc"><option value="${muc.id}" <c:if test="${mauForm[dm.truong] == muc.id}">selected</c:if>><c:out value="${muc.ten}"/></option></c:forEach></select></div></c:forEach>
    <div class="field"><label for="giaBan">Giá (đ) *</label><input class="input" type="number" id="giaBan" name="gia_ban" min="0" step="0.01" value="<c:out value='${mauForm.gia_ban}'/>" required></div>
    <div class="field"><label for="soLuongTon">Số lượng tồn kho *</label><input class="input" type="number" id="soLuongTon" name="so_luong" min="0" step="1" value="<c:out value='${mauForm.so_luong}'/>" required></div>
    <div class="field"><label for="trangThaiMau">Trạng thái mẫu</label><select class="select" id="trangThaiMau" name="trang_thai"><option value="1" ${mauForm.trang_thai ? 'selected' : ''}>Đang bán</option><option value="0" ${not mauForm.trang_thai ? 'selected' : ''}>Ngừng bán</option></select></div>
    <div class="field"><label for="ghiChu">Ghi chú</label><input class="input" id="ghiChu" name="ghi_chu" maxlength="255" value="<c:out value='${mauForm.ghi_chu}'/>"></div>
    <div class="product-admin-actions"><button class="button">Lưu mẫu giày</button><a class="button secondary" href="/quan-ly/san-pham/${idSanPham}#mau-giay">Nhập mẫu mới</a></div>
</form></c:if>
<div class="product-admin-table"><table class="table"><thead><tr><th>Mã mẫu</th><th>Size / Màu / Form</th><th>Giá</th><th>Tồn kho</th><th>Trạng thái</th><th>Thao tác</th></tr></thead><tbody>
<c:forEach items="${cacMau}" var="m"><tr><td><c:out value="${m.ma_chi_tiet_san_pham}"/></td><td><c:out value="${m.ten_kich_thuoc}"/> · <c:out value="${m.ten_mau_sac}"/> · <c:out value="${m.ten_form_chan}"/></td><td><fmt:formatNumber value="${m.gia_ban}"/> đ</td><td>${m.so_luong}</td><td>${m.trang_thai ? 'Đang bán' : 'Ngừng bán'}</td><td><c:if test="${currentUser.suaSanPham}"><div class="product-admin-actions"><a class="button secondary" href="/quan-ly/san-pham/${idSanPham}?mau=${m.id}#mau-giay">Sửa</a><form method="post" action="/quan-ly/san-pham/${idSanPham}/mau/${m.id}/xoa" onsubmit="return confirm('Xóa mẫu giày này khỏi danh sách bán?');"><button class="button danger">Xóa</button></form></div></c:if></td></tr></c:forEach>
<c:if test="${empty cacMau}"><tr><td colspan="6">Chưa có mẫu giày. Thêm size, màu và form chân để bán sản phẩm.</td></tr></c:if>
</tbody></table></div></div></section>
<section id="anh-san-pham" class="panel product-admin-section"><div class="panel-header"><h2>Ảnh từng mẫu giày</h2></div><div class="panel-body">
<c:if test="${currentUser.suaSanPham and not empty cacMau}"><form method="post" action="/quan-ly/san-pham/${idSanPham}/anh/them" enctype="multipart/form-data" class="product-admin-form">
<div class="field"><label for="mauAnh">Mẫu giày nhận ảnh</label><select class="select" id="mauAnh" name="idMau" required><c:forEach items="${cacMau}" var="m"><option value="${m.id}" ${mauAnhDangChon == m.id ? 'selected' : ''}><c:out value="${m.ma_chi_tiet_san_pham}"/> · <c:out value="${m.ten_kich_thuoc}"/> · <c:out value="${m.ten_mau_sac}"/> · <c:out value="${m.ten_form_chan}"/></option></c:forEach></select></div>
<div class="field"><label for="tepAnh">Chọn ảnh JPG / PNG (tối đa 5 MB)</label><input class="input" id="tepAnh" type="file" name="tepAnh" accept="image/jpeg,image/png" required></div><div class="product-admin-actions"><button class="button">Thêm ảnh</button></div></form></c:if>
<div class="product-admin-images"><c:forEach items="${cacAnh}" var="a"><div class="product-admin-image" data-mau="${a.id_chi_tiet_san_pham}"><img src="<c:out value='${a.duong_dan_anh}'/>" alt="Ảnh sản phẩm" onerror="this.onerror=null;this.src='/images/giay-mac-dinh.svg';"><p><c:out value="${a.ma_chi_tiet_san_pham}"/></p><c:if test="${a.la_anh_dai_dien}"><span class="product-admin-tag">Ảnh đại diện</span></c:if><c:if test="${currentUser.suaSanPham}"><div class="product-admin-actions"><c:if test="${not a.la_anh_dai_dien}"><form method="post" action="/quan-ly/san-pham/${idSanPham}/anh/${a.id}/dai-dien"><input type="hidden" name="idMau" value="${a.id_chi_tiet_san_pham}"><button class="button secondary">Chọn đại diện</button></form></c:if><form method="post" action="/quan-ly/san-pham/${idSanPham}/anh/${a.id}/xoa" onsubmit="return confirm('Xóa ảnh này khỏi mẫu giày?');"><input type="hidden" name="idMau" value="${a.id_chi_tiet_san_pham}"><button class="button danger">Xóa ảnh</button></form></div></c:if></div></c:forEach></div>
<p class="muted" id="khongCoAnhMau" hidden>Không có ảnh.</p>
</div></section></c:if>
</main></div></div><script src="/js/anh-mau-giay.js?v=1"></script></body></html>
