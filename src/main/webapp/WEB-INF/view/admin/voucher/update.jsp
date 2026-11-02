<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html><html lang="vi"><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1">
<title><c:out value="${tenChucNang}"/> — VictorSport</title><link rel="icon" href="/logo/favicon-victorsport.svg">
<link rel="stylesheet" href="/css/victorsport.css?v=admin-20261014"><link rel="stylesheet" href="/css/quan-ly-san-pham.css?v=admin-20261014">
<link rel="stylesheet" href="/css/giao-dien-chung.css?v=20261023-type-2"></head><body><div class="app-shell"><jsp:include page="/WEB-INF/view/admin/layout/sidebar.jsp"/><div class="main-area">
<%@ include file="/WEB-INF/view/admin/layout/promotion-header.jsp" %><main class="content-layout">
<section class="panel"><div class="panel-header"><h2>Thông tin ${phieuFreeship ? 'phiếu giảm giá vận chuyển' : 'phiếu giảm tiền hàng'} <span class="muted"><c:out value="${maUuDai}"/></span></h2><a class="button secondary" href="${duongDanDanhSach}">Về danh sách</a></div><div class="panel-body"><c:if test="${not empty error}"><div class="alert error" role="alert" data-vs-flash="error"><c:out value="${error}"/></div></c:if>
<c:if test="${not empty success}"><div class="alert success" role="status" data-vs-flash="success"><c:out value="${success}"/></div></c:if>
<form method="post" action="${duongDanLuu}" class="product-admin-form">
<input type="hidden" name="so_luong_cu" value='<c:out value="${empty uuDai.so_luong_cu ? uuDai.so_luong_su_dung : uuDai.so_luong_cu}"/>'>
<div class="field"><label for="ten_phieu_giam_gia">Tên ${phieuFreeship ? 'phiếu giảm giá vận chuyển' : 'phiếu giảm tiền hàng'} *</label><input class="input" id="ten_phieu_giam_gia" name="ten_phieu_giam_gia" type="text" value='<c:out value="${uuDai.ten_phieu_giam_gia}"/>' required  maxlength="255"></div>
<div class="field"><label for="loai_phieu_giam_gia">${phieuFreeship ? 'Cách giảm phí vận chuyển' : 'Cách giảm tiền hàng'} *</label><select class="select" id="loai_phieu_giam_gia" name="loai_phieu_giam_gia"><option value="0" ${not uuDai.loai_phieu_giam_gia ? 'selected' : ''}>Phần trăm (%)</option><option value="1" ${uuDai.loai_phieu_giam_gia ? 'selected' : ''}>Số tiền (đ)</option></select></div>
<input type="hidden" name="ap_dung_van_chuyen" value="${phieuFreeship ? 1 : 0}">
<c:if test="${phieuFreeship}"><div class="field"><label for="chi_ap_dung_ha_noi">Phạm vi vận chuyển</label><select class="select" id="chi_ap_dung_ha_noi" name="chi_ap_dung_ha_noi"><option value="0" ${not uuDai.chi_ap_dung_ha_noi ? 'selected' : ''}>Toàn quốc</option><option value="1" ${uuDai.chi_ap_dung_ha_noi ? 'selected' : ''}>Chỉ Hà Nội</option></select></div></c:if>
<div class="field"><label for="gia_tri_giam_gia">${phieuFreeship ? 'Mức giảm phí vận chuyển' : 'Giá trị giảm'} *</label><input class="input" id="gia_tri_giam_gia" name="gia_tri_giam_gia" type="number" value='<c:out value="${uuDai.gia_tri_giam_gia}"/>' required min="0" step="0.01" ></div>
<div class="field"><label for="ngay_bat_dau">Ngày bắt đầu *</label><input class="input" id="ngay_bat_dau" name="ngay_bat_dau" type="date" value='<c:out value="${uuDai.ngay_bat_dau}"/>' required  ></div>
<div class="field"><label for="ngay_ket_thuc">Ngày kết thúc *</label><input class="input" id="ngay_ket_thuc" name="ngay_ket_thuc" type="date" value='<c:out value="${uuDai.ngay_ket_thuc}"/>' required  ></div>
<div class="field"><label for="hoa_don_toi_thieu">Hóa đơn tối thiểu (đ)</label><input class="input" id="hoa_don_toi_thieu" name="hoa_don_toi_thieu" type="number" value='<c:out value="${uuDai.hoa_don_toi_thieu}"/>'  min="0" step="0.01" ></div>
<div class="field"><label for="so_tien_giam_toi_da">${phieuFreeship ? 'Giảm phí vận chuyển tối đa (đ)' : 'Giảm tối đa (đ)'}</label><input class="input" id="so_tien_giam_toi_da" name="so_tien_giam_toi_da" type="number" value='<c:out value="${uuDai.so_tien_giam_toi_da}"/>'  min="0" step="0.01" ></div>
<div class="field"><label for="so_luong_su_dung">Số lượt sử dụng còn lại *</label><input class="input" id="so_luong_su_dung" name="so_luong_su_dung" type="number" value='<c:out value="${uuDai.so_luong_su_dung}"/>' required min="0" step="1" ></div>
<div class="field"><label for="mo_ta">Mô tả</label><input class="input" id="mo_ta" name="mo_ta" type="text" value='<c:out value="${uuDai.mo_ta}"/>'   maxlength="255"></div>
<div class="field"><label for="trang_thai">Trạng thái</label><select class="select" id="trang_thai" name="trang_thai"><option value="1" ${uuDai.trang_thai ? 'selected' : ''}>Cho phép áp dụng</option><option value="0" ${not uuDai.trang_thai ? 'selected' : ''}>Ngừng áp dụng</option></select></div>
<div class="product-admin-wide muted">Giảm theo phần trăm tối đa 100%. Ưu đãi được áp dụng từ ngày bắt đầu đến hết ngày kết thúc. Để trống hoặc nhập 0 ở ô giảm tối đa nếu không giới hạn.</div>
<div class="product-admin-actions product-admin-wide"><button class="button">Lưu ${phieuFreeship ? 'phiếu giảm giá vận chuyển' : 'phiếu giảm tiền hàng'}</button></div></form></div></section>
</main></div></div></body></html>
