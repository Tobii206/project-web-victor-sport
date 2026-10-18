window.VictorKhoiTaoQr = function () {
    if (window.victorDongHoQr) clearInterval(window.victorDongHoQr);
    const hienThi = document.getElementById('thoiHanQr');
    if (!hienThi) return;
    const soGiay = Number(hienThi.dataset.conLai);
    if (!Number.isFinite(soGiay)) return;
    const han = Date.now() + Math.max(0, soGiay) * 1000;
    const capNhat = () => {
        const conLai = Math.max(0, Math.ceil((han - Date.now()) / 1000));
        if (conLai === 0) {
            document.querySelector('.qr-image')?.remove();
            hienThi.textContent = 'Mã QR đã hết hạn. Vui lòng không chuyển khoản. Nếu đã chuyển tiền, liên hệ cửa hàng để đối soát.';
            clearInterval(dongHo);
        } else hienThi.textContent = 'Thời gian còn lại: ' + Math.floor(conLai / 60) + ':' + String(conLai % 60).padStart(2, '0');
    };
    const dongHo = setInterval(capNhat, 1000);window.victorDongHoQr = dongHo;
    capNhat();
};
window.VictorKhoiTaoQr();
