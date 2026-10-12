<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<link rel="stylesheet" href="/css/xac-nhan-thanh-toan.css?v=1">
<dialog id="xacNhanThanhToan" class="payment-confirm" aria-labelledby="tieuDeXacNhan">
    <h2 id="tieuDeXacNhan">Xác nhận thanh toán</h2>
    <p>Bạn có muốn xác nhận đơn hàng này không?</p>
    <p>Phương thức: <strong id="phuongThucXacNhan"></strong></p>
    <p class="payment-confirm-total">Tổng thanh toán: <strong id="tongTienXacNhan"></strong></p>
    <div class="payment-confirm-actions">
        <button type="button" id="huyXacNhan" autofocus>Hủy</button>
        <button type="button" id="dongYXacNhan">Xác nhận thanh toán</button>
    </div>
</dialog>
<script src="/js/xac-nhan-thanh-toan.js?v=1"></script>
