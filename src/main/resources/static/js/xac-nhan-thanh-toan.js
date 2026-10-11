var formThanhToan = document.querySelector('[data-xac-nhan-thanh-toan]');
var bangXacNhan = document.getElementById('xacNhanThanhToan');
var nutThanhToan;
var daXacNhanThanhToan = false;

if (formThanhToan) {
    formThanhToan.onsubmit = function (suKien) {
        // Tạo hoặc lưu đơn chờ vẫn thực hiện như bình thường.
        if (suKien.submitter && suKien.submitter.value === 'luu-don-cho') {
            return;
        }
        if (daXacNhanThanhToan) {
            daXacNhanThanhToan = false;
            return;
        }
        suKien.preventDefault();
        nutThanhToan = suKien.submitter;
        var phuongThuc = document.getElementById('paymentMethodId');
        document.getElementById('phuongThucXacNhan').textContent = phuongThuc
            ? phuongThuc.options[phuongThuc.selectedIndex].text
            : formThanhToan.dataset.phuongThuc;
        document.getElementById('tongTienXacNhan').textContent = Number(formThanhToan.dataset.tongTien).toLocaleString('vi-VN') + ' đ';
        bangXacNhan.showModal();
    };

    document.getElementById('huyXacNhan').onclick = function () {
        bangXacNhan.close();
    };
    document.getElementById('dongYXacNhan').onclick = function () {
        bangXacNhan.close();
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
