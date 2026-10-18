(function () {
    function hienAnhMauDangChon() {
        var chon = document.getElementById('mauAnh');
        var thongBao = document.getElementById('khongCoAnhMau');
        if (!chon || !thongBao) return;
        var soAnh = 0;
        document.querySelectorAll('#anh-san-pham .product-admin-image[data-mau]').forEach(function (anh) {
            var dungMau = anh.dataset.mau === chon.value;
            anh.hidden = !dungMau;
            if (dungMau) soAnh++;
        });
        thongBao.textContent = 'Chưa có ảnh cho mẫu giày này.';
        thongBao.hidden = soAnh > 0;
    }
    document.addEventListener('change', function (e) {
        if (e.target.id === 'mauAnh') hienAnhMauDangChon();
    });
    document.addEventListener('vs:du-lieu-da-cap-nhat', hienAnhMauDangChon);
    hienAnhMauDangChon();
})();
