var chonMauAnh = document.getElementById('mauAnh');
var cacAnhMau = document.querySelectorAll('.product-admin-image[data-mau]');
var thongBaoKhongAnh = document.getElementById('khongCoAnhMau');

function hienAnhMauDangChon() {
    var soAnh = 0;
    for (var i = 0; i < cacAnhMau.length; i++) {
        var dungMau = cacAnhMau[i].dataset.mau === chonMauAnh.value;
        cacAnhMau[i].hidden = !dungMau;
        if (dungMau) { soAnh++; }
    }
    thongBaoKhongAnh.hidden = soAnh > 0;
}

if (chonMauAnh) {
    chonMauAnh.onchange = hienAnhMauDangChon;
    hienAnhMauDangChon();
}
