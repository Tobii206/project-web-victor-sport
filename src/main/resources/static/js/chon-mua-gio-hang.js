(function () {
    'use strict';
    if (window.VictorKhoiTaoChonMuaGio) return;
    var luaChon = new Map();
    function cacMau() { return Array.from(document.querySelectorAll('[data-chon-mua]')); }
    function capNhat() {
        var rows = cacMau(), all = document.getElementById('chonTatCaGio');
        if (!all) return;
        var chon = rows.filter(function (row) { return row.checked; });
        var tong = chon.reduce(function (sum, row) { return sum + Number(row.dataset.thanhTien); }, 0);
        all.checked = rows.length > 0 && chon.length === rows.length;
        all.indeterminate = chon.length > 0 && chon.length < rows.length;
        document.getElementById('soMauDaChon').textContent = 'Đã chọn ' + chon.length + '/' + rows.length + ' sản phẩm';
        document.getElementById('tongTienDaChon').textContent = Number.isFinite(tong) ? tong.toLocaleString('vi-VN', {maximumFractionDigits: 2}) + ' đ' : 'Chưa tính được tổng tiền';
        var button = document.getElementById('nutMuaDaChon');
        button.disabled = chon.length === 0 || !Number.isFinite(tong);
        button.textContent = 'Mua sản phẩm đã chọn (' + chon.length + ') →';
    }
    window.VictorKhoiTaoChonMuaGio = function () {
        cacMau().forEach(function (row) {
            if (luaChon.has(row.value)) row.checked = luaChon.get(row.value);
            else luaChon.set(row.value, row.checked);
        });
        capNhat();
    };
    document.addEventListener('change', function (event) {
        if (event.target.id === 'chonTatCaGio') {
            cacMau().forEach(function (row) { row.checked = event.target.checked;luaChon.set(row.value, row.checked); });
            capNhat();
        } else if (event.target.matches('[data-chon-mua]')) {
            luaChon.set(event.target.value, event.target.checked);capNhat();
        }
    });
    document.addEventListener('vs:du-lieu-da-cap-nhat', window.VictorKhoiTaoChonMuaGio);
    window.VictorKhoiTaoChonMuaGio();
})();
