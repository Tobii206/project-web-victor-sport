window.VictorKhoiTaoPhieuOnline = function () {
var chonPhieuOnline = document.getElementById('phieuGiamGiaOnline');
if (chonPhieuOnline) {
    function capNhatPhieuOnline() {
        var choPhi = chonPhieuOnline.dataset.choPhi === 'true';
        var tongGoc = Number(chonPhieuOnline.dataset.tongGoc);
        var giamDot = Number(chonPhieuOnline.dataset.giamDot);
        var phieu = chonPhieuOnline.options[chonPhieuOnline.selectedIndex];
        var giamPhieu = Number(phieu.dataset.giam || 0);
        var dungPhieuTien = chonPhieuOnline.value && phieu.dataset.freeship !== 'true';
        var giam = Math.min(tongGoc, dungPhieuTien ? giamPhieu : giamDot);
        var phiGoc = choPhi ? 0 : Number(chonPhieuOnline.dataset.phiGoc || 0);
        var giamPhi = Math.min(phiGoc, Number(phieu.dataset.giamPhi || 0));
        var phi = phiGoc - giamPhi;
        var tong = Math.round((tongGoc - giam + phi) * 100) / 100;
        document.getElementById('phiGiaoHangOnline').textContent = choPhi ? 'Chờ lấy phí' : phi.toLocaleString('vi-VN') + ' đ';
        document.getElementById('tienGiamOnline').textContent = giam.toLocaleString('vi-VN') + ' đ';
        document.getElementById('tongThanhToanOnline').textContent = tong.toLocaleString('vi-VN') + ' đ';
        document.getElementById('tongTienDuKien').value = choPhi ? '' : tong.toFixed(2);
        var nhanTong = document.getElementById('nhanTongThanhToanOnline');
        if (nhanTong) nhanTong.textContent = choPhi ? 'Tạm tính, chưa gồm phí giao hàng' : 'Tổng thanh toán';
        document.getElementById('formThanhToanOnline').dataset.tongTien = tong;
        var thongBao = giamDot > 0
            ? 'Chọn phiếu giảm tiền hàng sẽ áp dụng mức giảm của phiếu thay cho đợt giảm giá.'
            : 'Chọn phiếu để được giảm giá đơn hàng.';
        if (chonPhieuOnline.value) {
            thongBao = phieu.dataset.freeship === 'true'
                ? (giamPhi > 0 ? 'Đã áp dụng phiếu giảm phí vận chuyển.' : 'Đơn đang miễn phí vận chuyển. Không trừ lượt phiếu giảm giá vận chuyển.')
                : 'Đã áp dụng phiếu giảm giá bạn chọn.';
        }
        if (choPhi) thongBao = 'Phí GHN sẽ tự động cập nhật khi đủ địa chỉ. Phiếu giảm giá vận chuyển được tính sau khi có phí giao hàng.';
        document.getElementById('thongBaoPhieu').textContent = thongBao;
    }
    chonPhieuOnline.onchange = function (event) {
        if (event) chonPhieuOnline.dataset.tuChon = 'true';
        capNhatPhieuOnline();
    };
    capNhatPhieuOnline();
}

};
window.VictorKhoiTaoPhieuOnline();
