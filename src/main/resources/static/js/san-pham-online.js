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
var cacAnh = document.querySelectorAll('.online-thumbnail');

function hienThiMau() {
    var mau = document.querySelector('input[name="idPhienBan"]:checked');
    if (!mau) { return; }
    var gia = Number(mau.dataset.gia);
    document.getElementById('giaBan').textContent = gia.toLocaleString('vi-VN') + ' đ';
    var giaGoc = document.getElementById('giaGoc');
    giaGoc.hidden = Number(mau.dataset.giaGoc) <= gia;
    giaGoc.textContent = Number(mau.dataset.giaGoc).toLocaleString('vi-VN') + ' đ';
    document.getElementById('tonKho').textContent = 'Còn ' + mau.dataset.ton + ' sản phẩm';
    soLuong.max = mau.dataset.ton;
    if (Number(soLuong.value) > Number(mau.dataset.ton)) { soLuong.value = mau.dataset.ton; }
    anhChinh.onerror = function () { this.onerror = null; this.src = '/images/giay-mac-dinh.svg'; };
    anhChinh.src = mau.dataset.anh;
}

for (var i = 0; i < cacMau.length; i++) { cacMau[i].onchange = hienThiMau; }
for (var j = 0; j < cacAnh.length; j++) {
    cacAnh[j].onclick = function () {
        anhChinh.onerror = function () { this.onerror = null; this.src = '/images/giay-mac-dinh.svg'; };
        anhChinh.src = this.querySelector('img').src;
    };
}
if (soLuong) {
    document.getElementById('giamSoLuong').onclick = function () { if (Number(soLuong.value) > 1) { soLuong.stepDown(); } };
    document.getElementById('tangSoLuong').onclick = function () { soLuong.stepUp(); };
    hienThiMau();
    if (!document.querySelector('input[name="idPhienBan"]:checked') && cacAnh.length > 0) { cacAnh[0].click(); }
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
