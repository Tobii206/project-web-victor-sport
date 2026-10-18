(() => {
    const form = document.getElementById('formTinhPhiGhn');
    if (!form) return;
    const tinh = document.getElementById('ghnTinh');
    const huyen = document.getElementById('ghnHuyen');
    const xa = document.getElementById('ghnXa');
    const ketQua = document.getElementById('ketQuaPhiGhn');
    const nutTinh = document.getElementById('nutTinhPhiGhn');
    let phienDiaChi = 0;
    let phienTinhPhi = 0;
    function thongBao(noiDung, loi = false) {
        ketQua.textContent = noiDung;
        ketQua.classList.toggle('error', loi);
        ketQua.hidden = false;
    }
    function xoa(select, nhan) {
        select.replaceChildren(new Option(nhan, ''));
        select.disabled = true;
    }
    async function docPhanHoi(response) {
        if (response.redirected || !(response.headers.get('content-type') || '').includes('application/json'))
            throw new Error('Phiên đăng nhập đã thay đổi. Vui lòng tải lại trang.');
        const data = await response.json();
        if (!response.ok) throw new Error(data.error || 'Chưa lấy được dữ liệu. Vui lòng thử lại.');
        return data;
    }
    async function taiDiaChi(select, cap, ma, nhan, phien) {
        try {
            const response = await fetch('/quan-ly/van-chuyen/ghn/dia-chi?' + new URLSearchParams({cap, ma}), {cache: 'no-store'});
            const ds = await docPhanHoi(response);
            if (phien !== phienDiaChi) return;
            if (!Array.isArray(ds)) throw new Error('Danh mục địa chỉ chưa hợp lệ.');
            xoa(select, nhan);
            ds.forEach(d => select.add(new Option(d.ten, d.id)));
            select.disabled = false;
        } catch (e) {
            if (phien !== phienDiaChi) return;
            xoa(select, 'Chưa tải được địa chỉ');
            thongBao(e.message, true);
        }
    }
    function doiDuLieu() {
        phienTinhPhi++;
        ketQua.hidden = true;
    }
    function taiTinh() {
        doiDuLieu();
        xoa(tinh, 'Đang tải địa chỉ…'); xoa(huyen, 'Chọn quận/huyện'); xoa(xa, 'Chọn phường/xã');
        taiDiaChi(tinh, 'tinh', 0, 'Chọn tỉnh/thành phố', ++phienDiaChi);
    }
    tinh.addEventListener('change', () => {
        doiDuLieu();
        xoa(huyen, 'Chọn quận/huyện'); xoa(xa, 'Chọn phường/xã');
        const phien = ++phienDiaChi;
        if (tinh.value) taiDiaChi(huyen, 'huyen', tinh.value, 'Chọn quận/huyện', phien);
    });
    huyen.addEventListener('change', () => {
        doiDuLieu(); xoa(xa, 'Chọn phường/xã');
        const phien = ++phienDiaChi;
        if (huyen.value) taiDiaChi(xa, 'xa', huyen.value, 'Chọn phường/xã', phien);
    });
    form.addEventListener('input', doiDuLieu);
    document.getElementById('taiLaiDiaChiGhn').addEventListener('click', taiTinh);
    form.addEventListener('submit', async e => {
        e.preventDefault();
        if (!form.reportValidity()) return;
        if (!tinh.value || !huyen.value || !xa.value) {
            thongBao('Vui lòng chọn đủ địa chỉ GHN.', true); return;
        }
        const phien = ++phienTinhPhi;
        nutTinh.disabled = true; nutTinh.textContent = 'Đang tính phí…'; ketQua.hidden = true;
        try {
            const response = await fetch(form.action, {method: 'POST', body: new URLSearchParams(new FormData(form))});
            const data = await docPhanHoi(response);
            if (phien !== phienTinhPhi) return;
            const phi = Number(data.phi);
            if (!Number.isFinite(phi) || phi < 0) throw new Error('GHN chưa trả phí hợp lệ.');
            thongBao('Phí giao hàng dự kiến: ' + phi.toLocaleString('vi-VN') + ' đ');
        } catch (e) { if (phien === phienTinhPhi) thongBao(e.message, true); }
        finally { nutTinh.disabled = false; nutTinh.textContent = 'Tính phí thử nghiệm'; }
    });
    taiTinh();
})();
