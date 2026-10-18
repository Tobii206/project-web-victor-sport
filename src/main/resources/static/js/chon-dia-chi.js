window.VictorKhoiTaoDiaChi = function () {
// Địa chỉ 2 cấp sau sáp nhập: https://provinces.open-api.vn/api/v2/
var chonTinh = document.getElementById('thanhPho');
var quanCu = document.getElementById('quan');
var chonXa = document.getElementById('phuong');
var cacTinh = [];

function tenDiaChi(ten) {
    return (ten || '').trim().toLowerCase().replace(/^(tỉnh|thành phố|phường|xã|đặc khu)\s+/, '');
}

function timDiaChi(danhSach, ten) {
    for (var i = 0; i < danhSach.length; i++) {
        if (tenDiaChi(danhSach[i].name) === tenDiaChi(ten)) { return danhSach[i]; }
    }
    return null;
}

function dienLuaChon(oChon, danhSachTen, tieuDe, giaTriCu) {
    oChon.replaceChildren(new Option(tieuDe, ''));
    for (var i = 0; i < danhSachTen.length; i++) {
        var dungGiaTri = giaTriCu && tenDiaChi(danhSachTen[i]) === tenDiaChi(giaTriCu);
        var luaChon = new Option(danhSachTen[i], dungGiaTri ? giaTriCu : danhSachTen[i]);
        if (dungGiaTri) { luaChon.selected = true; }
        oChon.add(luaChon);
    }
}

function dienXa(giaTriCu) {
    var tinh = timDiaChi(cacTinh, chonTinh.value);
    dienLuaChon(chonXa, tinh ? tinh.wards : [], 'Chọn phường / xã', giaTriCu);
}

if (chonTinh && chonXa) {
    chonTinh.onchange = function () {
        if (quanCu) { quanCu.value = ''; }
        dienXa('');
    };
    chonXa.onchange = function () { if (quanCu) { quanCu.value = ''; } };
    fetch('/data/dia-chi-viet-nam.json?v=sap-nhap-2025')
        .then(function (phanHoi) {
            if (!phanHoi.ok) { throw new Error('Chưa tải được địa chỉ.'); }
            return phanHoi.json();
        })
        .then(function (duLieu) {
            cacTinh = duLieu;
            var tenTinh = [];
            for (var i = 0; i < cacTinh.length; i++) { tenTinh.push(cacTinh[i].name); }
            dienLuaChon(chonTinh, tenTinh, 'Chọn tỉnh / thành phố', chonTinh.dataset.cu);
            dienXa(chonXa.dataset.cu);
        })
        .catch(function () {
            document.getElementById('loiDiaChi').hidden = false;
        });
}

};
window.VictorKhoiTaoDiaChi();
