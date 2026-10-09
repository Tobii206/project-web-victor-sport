var anhChinh = document.getElementById('anhChinh');
var soLuong = document.getElementById('soLuong');
var cacMau = document.querySelectorAll('input[name="idPhienBan"]');
var cacAnh = document.querySelectorAll('.online-thumbnail');

function hienThiMau() {
    var mau = document.querySelector('input[name="idPhienBan"]:checked');
    if (!mau) { return; }
    var gia = Number(mau.dataset.gia);
    var niemYet = Number(mau.dataset.niemYet);
    document.getElementById('giaBan').textContent = gia.toLocaleString('vi-VN') + ' đ';
    document.getElementById('giaNiemYet').textContent = niemYet > gia ? niemYet.toLocaleString('vi-VN') + ' đ' : '';
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
