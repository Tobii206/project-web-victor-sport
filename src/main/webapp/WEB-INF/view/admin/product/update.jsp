<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<fmt:setLocale value="vi_VN"/>
<!DOCTYPE html><html lang="vi"><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1"><title>Thông tin sản phẩm — VictorSport</title>
<link rel="icon" href="/logo/favicon-victorsport.svg"><link rel="stylesheet" href="/css/victorsport.css?v=20261107-balanced"><link rel="stylesheet" href="/css/quan-ly-san-pham.css?v=20261108-auto-upload"><link rel="stylesheet" href="/css/giao-dien-chung.css?v=20261107-readable"></head><body>
<div class="app-shell"><jsp:include page="/WEB-INF/view/admin/layout/sidebar.jsp"/><div class="main-area"><%@ include file="/WEB-INF/view/admin/layout/product-header.jsp" %><main class="content-layout">
<div class="product-admin-actions"><a class="button secondary" href="/quan-ly/san-pham">← Danh sách sản phẩm</a><c:if test="${not empty idSanPham}"><strong><c:out value="${maSanPham}"/></strong></c:if></div>
<c:if test="${not empty error}"><div class="alert error" role="alert" data-vs-flash="error"><c:out value="${error}"/></div></c:if><c:if test="${not empty success}"><div class="alert success" role="status" data-vs-flash="success"><c:out value="${success}"/></div></c:if>
<c:if test="${not currentUser.suaSanPham}"><h2 class="product-admin-section"><c:out value="${sanPham.ten_san_pham}"/></h2></c:if>
<c:if test="${currentUser.suaSanPham}">
<section class="panel product-admin-section"><div class="panel-header"><h2>${empty idSanPham ? 'Thêm sản phẩm' : 'Thông tin sản phẩm'}</h2><span class="muted">Mã sản phẩm được tạo tự động</span></div><div class="panel-body">
<form class="product-admin-form" method="post" action="/quan-ly/san-pham${empty idSanPham ? '' : '/'.concat(idSanPham)}/luu">
    <div class="field product-admin-wide"><label for="tenSanPham">Tên sản phẩm *</label><input class="input" id="tenSanPham" name="ten_san_pham" maxlength="255" value="<c:out value='${sanPham.ten_san_pham}'/>" required></div>
    <div class="field"><label for="doiTuongSp">Giới tính</label><select class="select" id="doiTuongSp" name="doi_tuong">
        <option value="">Chưa phân loại</option>
        <c:forEach items="${cacDoiTuong}" var="loai"><option value="${loai.ma}" ${sanPham.doi_tuong == loai.ma ? 'selected' : ''}><c:out value="${loai.ten}"/></option></c:forEach>
    </select></div>
    <div class="field"><label for="giaBanSanPham">Giá bán (đ) *</label><input class="input" type="number" id="giaBanSanPham" name="gia_ban" min="0" step="0.01" value="<c:out value='${sanPham.gia_ban}'/>" required><small class="muted">Áp dụng cho tất cả mẫu size, màu và form của sản phẩm khi lưu.</small></div>
    <c:forEach items="${danhMuc}" var="dm" end="5"><div class="field"><label for="${dm.truong}"><c:out value="${dm.nhan}"/>${dm.truong == 'id_thuong_hieu' ? ' *' : ''}</label><select class="select" id="${dm.truong}" name="${dm.truong}" ${dm.truong == 'id_thuong_hieu' ? 'required' : ''}><option value="">Chọn thông tin</option><c:forEach items="${dm.luaChon}" var="muc"><option value="${muc.id}" <c:if test="${sanPham[dm.truong] == muc.id}">selected</c:if>><c:out value="${muc.ten}"/></option></c:forEach></select></div></c:forEach>
    <div class="field"><label for="trangThaiBan">Trạng thái</label><select class="select" id="trangThaiBan" name="trang_thai_kinh_doanh"><option value="1" ${sanPham.trang_thai_kinh_doanh ? 'selected' : ''}>Đang bán</option><option value="0" ${not sanPham.trang_thai_kinh_doanh ? 'selected' : ''}>Ngừng bán</option></select></div>
    <div class="field product-admin-wide"><label for="moTaNgan">Mô tả ngắn</label><textarea class="textarea" id="moTaNgan" name="mo_ta_ngan" maxlength="500"><c:out value="${sanPham.mo_ta_ngan}"/></textarea></div>
    <div class="field product-admin-wide"><label for="moTaChiTiet">Mô tả chi tiết</label><textarea class="textarea" id="moTaChiTiet" name="mo_ta_chi_tiet" maxlength="20000" rows="5"><c:out value="${sanPham.mo_ta_chi_tiet}"/></textarea></div>
    <c:if test="${currentUser.suaSanPham}"><div class="product-admin-wide"><button class="button">Lưu sản phẩm</button></div></c:if>
</form></div></section>
</c:if>
<c:if test="${not empty idSanPham}">
<section id="mau-giay" class="panel product-admin-section"><div class="panel-header"><h2>Mẫu giày và tồn kho</h2><span class="muted">Các size cùng màu dùng chung bộ ảnh</span></div><div class="panel-body">
<c:set var="ghiChuMacDinhMau" value=""/><c:forEach items="${cacMau}" var="mauMacDinh"><c:set var="ghiChuMacDinhMau" value="${mauMacDinh.ghi_chu}"/></c:forEach>
<c:if test="${currentUser.suaSanPham}">
<h3>${empty mauForm.id ? 'Thêm mẫu giày' : 'Sửa mẫu giày'}</h3>
<p class="muted product-admin-wide">Giá bán và mô tả dùng chung cho tất cả mẫu, chỉnh và lưu ở phần Thông tin sản phẩm phía trên.</p>
<form method="post" action="/quan-ly/san-pham/${idSanPham}/mau/luu" class="product-admin-form">
    <input type="hidden" name="idMau" value="<c:out value='${mauForm.id}'/>"><input type="hidden" name="so_luong_cu" value="<c:out value='${empty mauForm.so_luong_cu ? mauForm.so_luong : mauForm.so_luong_cu}'/>">
    <c:forEach items="${danhMuc}" var="dm" begin="6"><div class="field"><label for="${dm.truong}"><c:out value="${dm.nhan}"/> *</label><select class="select" id="${dm.truong}" name="${dm.truong}" required><option value="">Chọn thông tin</option><c:forEach items="${dm.luaChon}" var="muc"><option value="${muc.id}" <c:if test="${mauForm[dm.truong] == muc.id}">selected</c:if>><c:out value="${muc.ten}"/></option></c:forEach></select></div></c:forEach>
    <div class="field"><label for="soLuongTon">Số lượng tồn kho *</label><input class="input" type="number" id="soLuongTon" name="so_luong" min="0" step="1" value="<c:out value='${mauForm.so_luong}'/>" required></div>
    <div class="field"><label for="trangThaiMau">Trạng thái mẫu</label><select class="select" id="trangThaiMau" name="trang_thai"><option value="1" ${mauForm.trang_thai ? 'selected' : ''}>Đang bán</option><option value="0" ${not mauForm.trang_thai ? 'selected' : ''}>Ngừng bán</option></select></div>
    <input type="hidden" name="ghi_chu" value="<c:out value='${empty mauForm.ghi_chu and empty mauForm.id ? ghiChuMacDinhMau : mauForm.ghi_chu}'/>" />
    <div class="product-admin-actions"><button class="button">Lưu mẫu giày</button><a class="button secondary" href="/quan-ly/san-pham/${idSanPham}#mau-giay">Nhập mẫu mới</a></div>
</form></c:if>
<c:if test="${currentUser.suaSanPham}">
<details class="product-quick-variants">
    <summary>Thêm nhanh nhiều size</summary>
    <form method="post" action="/quan-ly/san-pham/${idSanPham}/mau/them-nhanh" class="product-admin-form">
        <input type="hidden" name="trang_thai" value="1">
        <c:forEach items="${danhMuc}" var="dm" begin="6">
            <c:choose>
                <c:when test="${dm.truong == 'id_kich_thuoc'}">
                    <fieldset class="quick-variant-sizes product-admin-wide">
                        <legend>Chọn size</legend>
                        <c:forEach items="${dm.luaChon}" var="muc">
                            <label><input type="checkbox" name="sizes" value="${muc.id}"> <c:out value="${muc.ten}"/></label>
                        </c:forEach>
                    </fieldset>
                </c:when>
                <c:otherwise>
                    <div class="field"><label for="nhanh-${dm.truong}"><c:out value="${dm.nhan}"/></label>
                        <select class="select" id="nhanh-${dm.truong}" name="${dm.truong}" required>
                            <option value="">Chọn thông tin</option>
                            <c:forEach items="${dm.luaChon}" var="muc"><option value="${muc.id}"><c:out value="${muc.ten}"/></option></c:forEach>
                        </select>
                    </div>
                </c:otherwise>
            </c:choose>
        </c:forEach>
        <div class="field"><label for="tonNhapNhanh">Số lượng nhập mỗi size</label><input class="input" id="tonNhapNhanh" name="so_luong" type="number" min="0" max="2147483647" step="1" value="0" required></div>
        <div class="product-admin-wide product-admin-actions"><button class="button" type="submit">Lưu các size đã chọn</button><span class="muted">Mẫu trùng màu, size và form chân sẽ được cộng tồn kho.</span></div>
    </form>
</details>
</c:if>
<div id="anh-san-pham" class="product-admin-images product-color-groups">
<c:forEach items="${nhomMau}" var="nhom">
<article class="product-color-group" id="nhom-mau-${nhom.id_mau_sac}" aria-labelledby="ten-mau-${nhom.id_mau_sac}">
    <div class="product-color-heading">
        <div class="product-color-identity">
            <c:choose><c:when test="${not empty nhom.anh}"><img src="<c:out value='${nhom.anh.duong_dan_anh}'/>" alt="Ảnh màu <c:out value='${nhom.ten_mau_sac}'/>" onerror="this.onerror=null;this.src='/images/giay-mac-dinh.svg';"></c:when>
                <c:otherwise><img src="/images/giay-mac-dinh.svg" alt="Chưa có ảnh màu này"></c:otherwise></c:choose>
            <div>
                <h3 id="ten-mau-${nhom.id_mau_sac}">Màu <c:out value="${nhom.ten_mau_sac}"/></h3>
                <span class="product-color-count">${fn:length(nhom.cacMau)} mẫu · ${fn:length(nhom.cacAnh)} ảnh</span>
            </div>
        </div>
        <c:if test="${currentUser.suaSanPham}">
        <details class="product-color-photo-editor">
            <summary class="button secondary">${empty nhom.anh ? 'Thêm ảnh' : 'Quản lý ảnh'}</summary>
            <div class="product-color-photo-panel">
                <form method="post" action="/quan-ly/san-pham/${idSanPham}/anh/them" enctype="multipart/form-data" class="product-color-upload" data-tu-luu-anh-mau>
                    <input type="hidden" name="idMau" value="${nhom.idMau}">
                    <label for="tepAnhMau${nhom.id_mau_sac}">Ảnh chung · JPG hoặc PNG</label>
                    <input id="tepAnhMau${nhom.id_mau_sac}" type="file" name="tepAnh" accept="image/jpeg,image/png" required>
                    <span class="product-color-upload-status" data-trang-thai-anh role="status"></span>
                </form>
                <ul class="product-color-photo-list">
                    <c:forEach items="${nhom.cacAnh}" var="anh" varStatus="st">
                        <li>
                            <img src="<c:out value='${anh.duong_dan_anh}'/>" alt="Ảnh ${st.count}" loading="lazy">
                            <c:choose>
                                <c:when test="${st.first}"><span>Ảnh bìa</span></c:when>
                                <c:otherwise><form method="post" action="/quan-ly/san-pham/${idSanPham}/anh/${anh.id}/dai-dien"><button class="button secondary" type="submit">Đặt ảnh bìa</button></form></c:otherwise>
                            </c:choose>
                            <form method="post" action="/quan-ly/san-pham/${idSanPham}/anh/${anh.id}/xoa" data-vs-confirm="Xóa ảnh này khỏi bộ ảnh?" data-vs-title="Xóa ảnh" data-vs-button="Xóa ảnh" data-vs-tone="danger"><button class="button danger" type="submit">Xóa</button></form>
                        </li>
                    </c:forEach>
                </ul>
            </div>
        </details>
        </c:if>
    </div>
    <div class="product-color-sizes">
        <table class="table" aria-labelledby="ten-mau-${nhom.id_mau_sac}">
            <thead><tr><th scope="col">Size / Form</th><th scope="col">Mã mẫu</th><th scope="col" class="product-color-price">Giá bán</th><th scope="col" class="product-color-stock">Tồn kho</th><th scope="col">Trạng thái</th><c:if test="${currentUser.suaSanPham}"><th scope="col" class="product-color-row-actions">Thao tác</th></c:if></tr></thead>
            <tbody><c:forEach items="${nhom.cacMau}" var="m"><tr>
                <td><strong><c:out value="${m.ten_kich_thuoc}"/></strong><span class="product-color-form"> · <c:out value="${m.ten_form_chan}"/></span></td>
                <td class="product-color-code"><c:out value="${m.ma_chi_tiet_san_pham}"/></td>
                <td class="product-color-price"><fmt:formatNumber value="${m.gia_ban}"/> đ</td><td class="product-color-stock">${m.so_luong}</td>
                <td><span class="product-color-status ${m.trang_thai ? 'on' : 'off'}">${m.trang_thai ? 'Đang bán' : 'Ngừng bán'}</span></td>
                <c:if test="${currentUser.suaSanPham}"><td class="product-color-row-actions"><div class="product-color-actions"><a class="button secondary" href="/quan-ly/san-pham/${idSanPham}?mau=${m.id}#mau-giay">Sửa</a><form method="post" action="/quan-ly/san-pham/${idSanPham}/mau/${m.id}/xoa" data-vs-confirm="Bạn muốn xóa mẫu giày này khỏi danh sách bán?" data-vs-title="Xóa mẫu giày" data-vs-button="Xóa mẫu giày" data-vs-tone="danger"><button class="button danger" type="submit">Xóa</button></form></div></td></c:if>
            </tr></c:forEach></tbody>
        </table>
    </div>
</article>
</c:forEach>
<c:if test="${empty nhomMau}"><p class="muted">Chưa có mẫu giày. Thêm màu, size và form chân để bắt đầu.</p></c:if>
</div></div></section></c:if>
</main></div></div></body></html>
