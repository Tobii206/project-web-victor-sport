var cacFormThanhToan = document.querySelectorAll('[data-xac-nhan-thanh-toan]');
var formThanhToan;
var bangXacNhan = document.getElementById('xacNhanThanhToan');
var nutThanhToan;
var daXacNhanThanhToan = false;

if (bangXacNhan) {
        document.addEventListener('submit', function (suKien) {
            if (!suKien.target.matches('[data-xac-nhan-thanh-toan]')) return;
            // Tạo hoặc lưu đơn chờ vẫn thực hiện như bình thường.
            if (suKien.submitter && (suKien.submitter.value === 'luu-don-cho' || suKien.submitter.hasAttribute('data-bo-qua-xac-nhan'))) {
                return;
            }
            if (daXacNhanThanhToan) {
                daXacNhanThanhToan = false;
                return;
            }
            suKien.preventDefault();
            formThanhToan = suKien.target;
            nutThanhToan = suKien.submitter;
            var tieuDe = formThanhToan.dataset.tieuDe || 'Xác nhận thanh toán';
            document.getElementById('tieuDeXacNhan').textContent = tieuDe;
            document.getElementById('dongYXacNhan').textContent = formThanhToan.dataset.nhanXacNhan || tieuDe;
            var phuongThuc = document.getElementById('paymentMethodId');
            document.getElementById('nhanPhuongThuc').textContent = (formThanhToan.dataset.nhanPhuongThuc || 'Phương thức') + ': ';
            document.getElementById('phuongThucXacNhan').textContent = phuongThuc
                ? phuongThuc.options[phuongThuc.selectedIndex].text
                : formThanhToan.dataset.phuongThuc;
            document.getElementById('noiDungXacNhan').textContent = formThanhToan.dataset.noiDung
                || 'Vui lòng kiểm tra thông tin trước khi xác nhận thanh toán.';
            if (phuongThuc && phuongThuc.name === 'phuongThuc' && phuongThuc.value === 'QR') {
                document.getElementById('noiDungXacNhan').textContent = 'Sau khi đặt hàng, quét mã QR để chuyển khoản đúng số tiền và nội dung. Cửa hàng sẽ kiểm tra và xác nhận thanh toán.';
            }
            var maHoaDon = formThanhToan.dataset.maHoaDon;
            document.getElementById('dongMaHoaDon').hidden = !maHoaDon;
            document.getElementById('maHoaDonXacNhan').textContent = maHoaDon || '';
            document.getElementById('tongTienXacNhan').textContent = Number(formThanhToan.dataset.tongTien).toLocaleString('vi-VN') + ' đ';
            bangXacNhan.showModal();
        });

    document.getElementById('huyXacNhan').onclick = function () {
        bangXacNhan.close();
    };
    document.getElementById('dongYXacNhan').onclick = function () {
        bangXacNhan.close();
        if (!formThanhToan || !formThanhToan.isConnected) return;
        daXacNhanThanhToan = true;
        // Giữ đúng nút đã bấm để gửi hành động thanh toán tại quầy.
        if (nutThanhToan) {
            formThanhToan.requestSubmit(nutThanhToan);
        } else {
            formThanhToan.requestSubmit();
        }
        daXacNhanThanhToan = false;
    };
}
