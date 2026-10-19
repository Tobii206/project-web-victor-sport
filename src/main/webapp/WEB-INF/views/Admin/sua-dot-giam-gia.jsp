<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html><html lang="vi"><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1">
<title><c:out value="${tenChucNang}"/> — VictorSport</title><link rel="icon" href="/logo/favicon-victorsport.svg">
<link rel="stylesheet" href="/css/victorsport.css?v=admin-20261014"><link rel="stylesheet" href="/css/quan-ly-san-pham.css?v=admin-20261014">
<link rel="stylesheet" href="/css/giao-dien-chung.css?v=20261018-2"></head><body><div class="app-shell"><jsp:include page="/WEB-INF/views/Admin/menu.jsp"/><div class="main-area">
<%@ include file="/WEB-INF/views/Admin/dau-trang-uu-dai.jsp" %><main class="content-layout">
<section class="panel"><div class="panel-header"><h2>Thông tin đợt giảm giá <span class="muted"><c:out value="${maUuDai}"/></span></h2><a class="button secondary" href="/quan-ly/dot-giam-gia">Về danh sách</a></div><div class="panel-body"><c:if test="${not empty error}"><div class="alert error" role="alert" data-vs-flash="error"><c:out value="${error}"/></div></c:if>
<c:if test="${not empty success}"><div class="alert success" role="status" data-vs-flash="success"><c:out value="${success}"/></div></c:if>
<form method="post" action="${duongDanLuu}" class="product-admin-form">
<div class="field"><label for="ten_dot_giam_gia">Tên đợt giảm giá *</label><input class="input" id="ten_dot_giam_gia" name="ten_dot_giam_gia" type="text" value='<c:out value="${uuDai.ten_dot_giam_gia}"/>' required  maxlength="255"></div>
<div class="field"><label for="loai_giam_gia">Loại giảm giá *</label><select class="select" id="loai_giam_gia" name="loai_giam_gia"><option value="0" ${not uuDai.loai_giam_gia ? 'selected' : ''}>Phần trăm (%)</option><option value="1" ${uuDai.loai_giam_gia ? 'selected' : ''}>Số tiền (đ)</option></select></div>
<div class="field"><label for="gia_tri_giam_gia">Giá trị giảm *</label><input class="input" id="gia_tri_giam_gia" name="gia_tri_giam_gia" type="number" value='<c:out value="${uuDai.gia_tri_giam_gia}"/>' required min="0" step="0.01" ></div>
<div class="field"><label for="so_tien_giam_toi_da">Tối đa được giảm</label><input class="input" id="so_tien_giam_toi_da" name="so_tien_giam_toi_da" type="number" value='<c:out value="${uuDai.so_tien_giam_toi_da}"/>' min="0" step="0.01"><small class="muted">Áp dụng cho mọi mẫu giày trong đợt. Để trống hoặc nhập 0 để giảm theo mức đã chọn.</small></div>
<div class="field"><label for="ngay_bat_dau">Ngày bắt đầu *</label><input class="input" id="ngay_bat_dau" name="ngay_bat_dau" type="date" value='<c:out value="${uuDai.ngay_bat_dau}"/>' required  ></div>
<div class="field"><label for="ngay_ket_thuc">Ngày kết thúc *</label><input class="input" id="ngay_ket_thuc" name="ngay_ket_thuc" type="date" value='<c:out value="${uuDai.ngay_ket_thuc}"/>' required  ></div>
<div class="field"><label for="muc_uu_tien">Mức ưu tiên *</label><select class="select" id="muc_uu_tien" name="muc_uu_tien" required>
<option value="">Chọn mức ưu tiên</option><c:forEach items="${cacMucUuTien}" var="muc"><option value="${muc}" ${uuDai.muc_uu_tien == muc ? 'selected' : ''}>Mức ${muc}</option></c:forEach>
</select><c:if test="${empty cacMucUuTien}"><small class="muted">Chưa có mức ưu tiên trong dữ liệu.</small></c:if></div>
<div class="field"><label for="trang_thai">Trạng thái</label><select class="select" id="trang_thai" name="trang_thai"><option value="1" ${uuDai.trang_thai ? 'selected' : ''}>Cho phép áp dụng</option><option value="0" ${not uuDai.trang_thai ? 'selected' : ''}>Ngừng áp dụng</option></select></div>
<div class="product-admin-wide muted">Giảm theo phần trăm tối đa 100%. Ưu đãi được áp dụng từ ngày bắt đầu đến hết ngày kết thúc.</div>
<c:if test="${empty idUuDai}"><div class="product-admin-wide muted">Tạo đợt mới ở trạng thái “Cho phép áp dụng” sẽ gửi email thông báo đến các khách hàng có tài khoản đang hoạt động. Email được gửi riêng cho từng khách.</div></c:if>
<c:if test="${not empty idUuDai}"><div class="product-admin-wide muted">Bật lại đợt đã ngừng trong thời gian áp dụng sẽ gửi email thông báo đến khách hàng. Lưu khi đợt đang bật không gửi lại email.</div></c:if>
<div class="product-admin-actions product-admin-wide"><button class="button">Lưu đợt giảm giá</button></div></form></div></section>
<c:if test="${not empty idUuDai}"><section class="panel" id="mau-giam-gia"><div class="panel-header"><h2>Mẫu giày áp dụng</h2></div><div class="panel-body">
<form method="post" action="/quan-ly/dot-giam-gia/${idUuDai}/mau/luu" class="product-admin-form">
<div class="field product-admin-wide"><label for="id_chi_tiet_san_pham">Mẫu giày *</label><select class="select" name="id_chi_tiet_san_pham" id="id_chi_tiet_san_pham" required><option value="">Chọn mẫu giày</option>
<c:forEach items="${cacMau}" var="m"><option value="${m.id}" ${mauForm.id_chi_tiet_san_pham == m.id ? 'selected' : ''}><c:out value="${m.ma_chi_tiet_san_pham}"/> - <c:out value="${m.ten_san_pham}"/> - Size <c:out value="${m.ten_kich_thuoc}"/> - <c:out value="${m.ten_mau_sac}"/> - <c:out value="${m.ten_form_chan}"/></option></c:forEach></select></div>
<div class="field"><label for="ghi_chu">Ghi chú</label><input class="input" id="ghi_chu" name="ghi_chu" type="text" value='<c:out value="${mauForm.ghi_chu}"/>'   maxlength="255"></div>

<div class="product-admin-wide muted">Các mẫu dùng mức giảm và giới hạn chung của đợt. Chọn lại mẫu đã có để cập nhật ghi chú.</div><div class="product-admin-actions product-admin-wide"><button class="button">Lưu mẫu áp dụng</button></div></form>
<div class="product-admin-table"><table class="table"><thead><tr><th>Mẫu giày</th><th>Size / Màu / Dáng</th><th>Mức giảm của đợt</th><th>Tối đa được giảm</th><th>Ghi chú</th><th>Thao tác</th></tr></thead><tbody>
<c:forEach items="${mauDaChon}" var="m"><tr><td><c:out value="${m.ten_san_pham}"/><div class="muted"><c:out value="${m.ma_chi_tiet_san_pham}"/></div></td><td>Size <c:out value="${m.ten_kich_thuoc}"/> · <c:out value="${m.ten_mau_sac}"/> · <c:out value="${m.ten_form_chan}"/></td>
<td><fmt:formatNumber value="${uuDai.gia_tri_giam_gia}" maxFractionDigits="2"/> ${uuDai.loai_giam_gia ? 'đ' : '%'}</td>
<td><c:choose><c:when test="${empty uuDai.so_tien_giam_toi_da or uuDai.so_tien_giam_toi_da == 0}">Không giới hạn</c:when><c:otherwise><fmt:formatNumber value="${uuDai.so_tien_giam_toi_da}" maxFractionDigits="2"/> đ</c:otherwise></c:choose></td><td><c:out value="${m.ghi_chu}"/></td>
<td><form method="post" action="/quan-ly/dot-giam-gia/${idUuDai}/mau/${m.id}/bo" data-vs-confirm="Mẫu giày này sẽ không còn được áp dụng ưu đãi của đợt giảm giá này." data-vs-title="Bỏ áp dụng giảm giá" data-vs-button="Bỏ áp dụng" data-vs-tone="danger"><button class="button danger">Bỏ áp dụng</button></form></td></tr></c:forEach>
<c:if test="${empty mauDaChon}"><tr><td colspan="6">Chưa có mẫu giày áp dụng.</td></tr></c:if></tbody></table></div></div></section></c:if>
</main></div></div></body></html>
