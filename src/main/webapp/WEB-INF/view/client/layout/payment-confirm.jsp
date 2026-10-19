<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<link rel="stylesheet" href="/css/xac-nhan-thanh-toan.css?v=20261015-5">
<dialog id="xacNhanThanhToan" class="payment-confirm" aria-labelledby="tieuDeXacNhan" aria-describedby="noiDungXacNhan">
    <div class="payment-confirm-icon" aria-hidden="true"><svg width="30" height="30" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="5" width="18" height="14" rx="3"/><path d="M3 10h18M7 15h3M15 15l2 2 4-4"/></svg></div>
    <p class="payment-confirm-eyebrow">VICTORSPORT</p>
    <h2 id="tieuDeXacNhan">${currentUser.quanLy and activeMenu == 'hoa-don' ? 'Xác nhận thanh toán' : 'Xác nhận thanh toán'}</h2>
    <p id="noiDungXacNhan" class="payment-confirm-description">Vui lòng kiểm tra thông tin trước khi xác nhận.</p>
    <div class="payment-confirm-summary">
        <p id="dongMaHoaDon" hidden><span>Hóa đơn</span><strong id="maHoaDonXacNhan"></strong></p>
        <p><span id="nhanPhuongThuc">${currentUser.quanLy and activeMenu == 'hoa-don' ? 'Trạng thái' : 'Phương thức'}: </span> <strong id="phuongThucXacNhan"></strong></p>
        <p class="payment-confirm-total"><span>Tổng thanh toán</span><strong id="tongTienXacNhan"></strong></p>
    </div>
    <div class="payment-confirm-actions">
        <button type="button" id="huyXacNhan" autofocus>Hủy</button>
        <button type="button" id="dongYXacNhan">${currentUser.quanLy and activeMenu == 'hoa-don' ? 'Xác nhận thanh toán' : 'Xác nhận thanh toán'}</button>
    </div>
</dialog>
<script src="/js/xac-nhan-thanh-toan.js?v=20261018-live"></script>
