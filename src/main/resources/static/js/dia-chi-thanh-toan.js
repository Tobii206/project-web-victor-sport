window.VictorKhoiTaoDiaChiThanhToan = function () {
var diaChiDaLuu = document.getElementById('diaChiDaLuu');
var oDiaChiNhan = document.getElementById('diaChi');
if (diaChiDaLuu && oDiaChiNhan) {
    diaChiDaLuu.onchange = function () {
        var luaChon = this.options[this.selectedIndex];
        oDiaChiNhan.value = luaChon.dataset.diaChi || '';
        oDiaChiNhan.readOnly = this.value !== '';
        if (!this.value) oDiaChiNhan.focus();
    };
}

};
window.VictorKhoiTaoDiaChiThanhToan();
