window.VictorKhoiTaoSanPham = function () {
var anhChinh = document.getElementById('anhChinh');
var huongDanSize = document.getElementById('huongDanSize');
var moHuongDanSize = document.getElementById('moHuongDanSize');
if (moHuongDanSize) {
    moHuongDanSize.onclick = function () { huongDanSize.showModal(); };
    document.getElementById('dongHuongDanSize').onclick = function () { huongDanSize.close(); };
    huongDanSize.onclick = function (event) {
        var khung = huongDanSize.getBoundingClientRect();
        if (event.clientX < khung.left || event.clientX > khung.right || event.clientY < khung.top || event.clientY > khung.bottom) {
            huongDanSize.close();
        }
    };
}
var soLuong = document.getElementById('soLuong');
var cacMau = document.querySelectorAll('input[name="idPhienBan"]');
var cacAnhMau = document.querySelectorAll('[data-anh-mau]');
var mauDangXem;

function hienAnh(src, nutAnh) {
    anhChinh.onerror = function () { this.onerror = null; this.src = '/images/giay-mac-dinh.svg'; };
    anhChinh.src = src;
    cacAnhMau.forEach(function (nut) { nut.setAttribute('aria-pressed', String(nut === nutAnh)); });
}

cacAnhMau.forEach(function (nut) {
    nut.onclick = function () { hienAnh(nut.querySelector('img').src, nut); };
});

function hienThiMau() {
    var dangChon = document.querySelector('input[name="idPhienBan"]:checked:not(:disabled)');
    var mau = dangChon || Array.from(cacMau).find(function (el) { return !el.closest('.online-variant').hidden; });
    document.getElementById('tonKho').textContent = dangChon ? 'Còn ' + dangChon.dataset.ton + ' sản phẩm' : 'Hết hàng';
    document.querySelectorAll('#formChonGiay .online-purchase button, #soLuong').forEach(function (el) {
        el.disabled = !dangChon;
    });
    if (!mau) {
        var anhDau = cacAnhMau[0];
        if (anhDau) {
            cacAnhMau.forEach(function (nut) { nut.hidden = nut.dataset.anhMau !== anhDau.dataset.anhMau; });
            hienAnh(anhDau.querySelector('img').src, anhDau);
        }
        return;
    }
    var gia = Number(mau.dataset.gia);
    document.getElementById('giaBan').textContent = gia.toLocaleString('vi-VN') + ' đ';
    var giaGoc = document.getElementById('giaGoc');
    giaGoc.hidden = Number(mau.dataset.giaGoc) <= gia;
    giaGoc.textContent = Number(mau.dataset.giaGoc).toLocaleString('vi-VN') + ' đ';
    if (dangChon) {
        soLuong.max = dangChon.dataset.ton;
        if (Number(soLuong.value) < 1) soLuong.value = 1;
        if (Number(soLuong.value) > Number(dangChon.dataset.ton)) soLuong.value = dangChon.dataset.ton;
    }
    if (mauDangXem !== mau.dataset.mau) {
        mauDangXem = mau.dataset.mau;
        var anhBia = null;
        cacAnhMau.forEach(function (nut) {
            nut.hidden = nut.dataset.anhMau !== mauDangXem;
            if (!nut.hidden && !anhBia) anhBia = nut;
        });
        hienAnh(mau.dataset.anh, anhBia);
    }
}

for (var i = 0; i < cacMau.length; i++) { cacMau[i].onchange = hienThiMau; }
var cacGioiTinh = document.querySelectorAll('#formChonGiay input[name="doiTuong"]');
function hienThiGioiTinh() {
    var loai = document.querySelector('#formChonGiay input[name="doiTuong"]:checked');
    if (!loai) return;
    cacMau.forEach(function (el) {
        var phuHop = el.dataset.gioiTinh === loai.value;
        el.closest('.online-variant').hidden = !phuHop;
        el.disabled = !phuHop || Number(el.dataset.ton) <= 0;
        if (el.disabled) el.checked = false;
    });
    if (!document.querySelector('input[name="idPhienBan"]:checked:not(:disabled)')) {
        var dauTien = Array.from(cacMau).find(function (el) { return !el.disabled; });
        if (dauTien) dauTien.checked = true;
    }
    hienThiMau();
}
cacGioiTinh.forEach(function (el) { el.onchange = hienThiGioiTinh; });
if (soLuong) {
    document.getElementById('giamSoLuong').onclick = function () { if (Number(soLuong.value) > 1) { soLuong.stepDown(); } };
    document.getElementById('tangSoLuong').onclick = function () { soLuong.stepUp(); };
    if (cacGioiTinh.length) hienThiGioiTinh(); else hienThiMau();
}

var cacNutThongTin = document.querySelectorAll('.online-info-tab');
var cacNoiDung = document.querySelectorAll('.online-info-content');

for (var k = 0; k < cacNutThongTin.length; k++) {
    cacNutThongTin[k].onclick = function () {
        for (var i = 0; i < cacNutThongTin.length; i++) {
            cacNutThongTin[i].classList.remove('active');
            cacNutThongTin[i].setAttribute('aria-pressed', 'false');
        }
        for (var j = 0; j < cacNoiDung.length; j++) {
            cacNoiDung[j].hidden = true;
        }
        this.classList.add('active');
        this.setAttribute('aria-pressed', 'true');
        document.getElementById(this.dataset.noiDung).hidden = false;
    };
}

};
window.VictorKhoiTaoSanPham();
