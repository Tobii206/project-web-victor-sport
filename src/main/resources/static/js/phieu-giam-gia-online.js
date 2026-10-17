var chonPhieuOnline = document.getElementById('phieuGiamGiaOnline');
if (chonPhieuOnline) {
    function capNhatPhieuOnline() {
        var tongGoc = Number(chonPhieuOnline.dataset.tongGoc);
        var giamDot = Number(chonPhieuOnline.dataset.giamDot);
        var phieu = chonPhieuOnline.options[chonPhieuOnline.selectedIndex];
        var giamPhieu = Number(phieu.dataset.giam || 0);
        var giam = Math.min(tongGoc, Math.max(giamDot, giamPhieu));
        var tong = Math.round((tongGoc - giam) * 100) / 100;
        document.getElementById('tienGiamOnline').textContent = giam.toLocaleString('vi-VN') + ' đ';
        document.getElementById('tongThanhToanOnline').textContent = tong.toLocaleString('vi-VN') + ' đ';
        document.getElementById('tongTienDuKien').value = tong.toFixed(2);
        document.getElementById('formThanhToanOnline').dataset.tongTien = tong;
        var thongBao = giamDot > 0
            ? 'Đơn có sản phẩm đang giảm giá chỉ được dùng voucher freeship.'
            : 'Chọn phiếu để được giảm giá đơn hàng.';
        if (chonPhieuOnline.value) {
            thongBao = phieu.dataset.freeship === 'true'
                ? 'Đơn đang miễn phí vận chuyển. Không trừ lượt phiếu freeship.'
                : 'Đã chọn phiếu giảm giá.';
        }
        document.getElementById('thongBaoPhieu').textContent = thongBao;
    }
    chonPhieuOnline.onchange = capNhatPhieuOnline;
    capNhatPhieuOnline();
}
