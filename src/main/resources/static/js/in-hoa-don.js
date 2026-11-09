(function () {
    'use strict';
    var url = new URL(location.href);
    if (url.searchParams.get('inHoaDon') !== '1' || !document.querySelector('[data-hoa-don-da-thanh-toan="true"]')) return;
    // Xóa yêu cầu in để tải lại trang không tự mở hộp in lần nữa.
    url.searchParams.delete('inHoaDon');
    history.replaceState(history.state, '', url.pathname + url.search + url.hash);
    function inHoaDon() { window.print(); }
    if (document.readyState === 'complete') inHoaDon();
    else window.addEventListener('load', inHoaDon, {once: true});
})();

window.VictorKhoiTaoDiaChiHoaDon = function () {
    var dialog = document.getElementById('suaNhanHangHoaDon');
    if (!dialog || dialog.dataset.khoiTao) return;
    dialog.dataset.khoiTao = 'true';
    var tinh = document.getElementById('hoaDonTinh');
    var xa = document.getElementById('hoaDonXa');
    var status = document.getElementById('hoaDonDiaChiStatus');
    var dangTaiTinh = false;
    async function layDiaChi(url) {
        var response = await fetch(url, {headers: {'Accept': 'application/json'}});
        var data = await response.json();
        if (!response.ok || !Array.isArray(data)) throw new Error();
        return data;
    }
    async function hienDanhSachTinh() {
        if (!tinh || dangTaiTinh || tinh.options.length > 1) return;
        dangTaiTinh = true;
        try {
            var provinces = await layDiaChi('/thanh-toan/ghn/dia-chi?cap=tinh');
            provinces.forEach(function (item) { tinh.add(new Option(item.ten, item.id)); });
            status.textContent = '';
        } catch (e) {
            status.textContent = 'Chưa tải được khu vực GHN. Bạn vẫn có thể sửa địa chỉ trong khu vực hiện tại.';
        } finally {
            dangTaiTinh = false;
        }
    }
    function moCuaSo() {
        dialog.showModal();
        hienDanhSachTinh();
    }
    document.getElementById('moSuaNhanHang').addEventListener('click', moCuaSo);
    dialog.querySelector('[data-dong-sua-nhan-hang]').addEventListener('click', function () {
        dialog.close();
    });
    if (dialog.dataset.moKhiTai === 'true') moCuaSo();
    dialog.addEventListener('change', async function (event) {
        if (event.target !== tinh) return;
        var idTinh = tinh.value;
        xa.replaceChildren(new Option('Chọn phường / xã', ''));
        xa.disabled = !idTinh;
        xa.required = !!idTinh;
        status.textContent = '';
        if (!idTinh) return;
        try {
            var wards = await layDiaChi('/thanh-toan/ghn/dia-chi?cap=xa&tinh=' + encodeURIComponent(idTinh));
            if (tinh.value !== idTinh) return;
            wards.forEach(function (item) { xa.add(new Option(item.ten, item.id)); });
        } catch (e) {
            if (tinh.value === idTinh) status.textContent = 'Chưa tải được phường / xã. Vui lòng chọn lại tỉnh.';
        }
    });
};
window.VictorKhoiTaoDiaChiHoaDon();
document.addEventListener('vs:du-lieu-da-cap-nhat', window.VictorKhoiTaoDiaChiHoaDon);
