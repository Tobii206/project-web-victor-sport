window.VictorKhoiTaoGhn = function () {
    var box = document.getElementById('ghnCheckout');
    if (!box) return;
    var tinh = document.getElementById('tinhGhn'), xa = document.getElementById('xaGhn');
    var loi = document.getElementById('ghnDiaChiLoi'), datHang = document.getElementById('nutDatHangOnline');
    var taiLai = document.getElementById('taiLaiDiaChiGhn');
    var diaChiDaLuu = document.getElementById('diaChiDaLuu');
    var nutLayPhi = box.querySelector('[formaction]');
    var lan = 0, daDoi = false;
    function huyBaoGia() {
        daDoi = true;datHang.disabled = true;
        var text = document.getElementById('ghnBaoGiaHienTai');
        if (text) text.textContent = 'Thông tin đã thay đổi. Vui lòng lấy lại phí giao hàng.';
        var phieu = document.getElementById('phieuGiamGiaOnline');
        if (phieu) {
            phieu.dataset.choPhi = 'true';
            Array.from(phieu.options).forEach(function (option) {
                if (option.dataset.noiThanh === 'true') {
                    if (option.selected) phieu.value = '';
                    option.hidden = true;option.disabled = true;
                }
            });
            if (phieu.onchange) phieu.onchange();
        }
    }
    window.VictorHuyBaoGiaGhn = huyBaoGia;
    function baoLoi(error) {
        loi.textContent = error.message || 'Chưa tải được địa chỉ giao hàng. Vui lòng thử lại.';
        taiLai.hidden = false;datHang.disabled = true;
    }
    async function tai(url, select, nhan, chon) {
        var response, data;
        // Thử lại một lần với lỗi kết nối hoặc dịch vụ tạm thời.
        for (var i = 0; i < 2; i++) {
            try {
                response = await fetch(url, {cache: 'no-store', headers: {'Accept': 'application/json'}});
                data = await response.json();
                if (response.ok) break;
                if (response.status < 500 || i === 1) throw new Error(data.error || 'Chưa tải được địa chỉ giao hàng.');
            } catch (e) {
                if (i === 1 || (response && response.status < 500)) throw new Error(data && data.error || 'Không tải được địa chỉ GHN. Bấm Tải lại địa chỉ để thử lại.');
            }
        }
        if (!response || !response.ok || !Array.isArray(data)) throw new Error(data && data.error || 'Dữ liệu địa chỉ chưa sẵn sàng.');
        var options = [new Option(nhan, '')];
        data.forEach(function (r) { options.push(new Option(r.ten, r.id)); });
        select.replaceChildren.apply(select, options);select.value = chon || '';
    }
    function chuanHoa(ten) {
        return (ten || '').normalize('NFD').replace(/[\u0300-\u036f]/g, '').replace(/đ/g, 'd').replace(/Đ/g, 'D')
            .toLowerCase().trim().replace(/\s+/g, ' ').replace(/^(thanh pho|tinh|tp\.?|phuong|xa|dac khu)\s+/, '');
    }
    function chonTheoTen(select, ten) {
        if (!ten) return;
        var khop = Array.from(select.options).filter(function (o) { return o.value && chuanHoa(o.textContent) === chuanHoa(ten); });
        select.value = khop.length === 1 ? khop[0].value : '';
    }
    async function taiDiaChi(tinhCu, xaCu, tenTinh, tenXa) {
        var hienTai = ++lan;
        taiLai.disabled = true;tinh.disabled = true;xa.disabled = true;datHang.disabled = true;nutLayPhi.disabled = true;
        try {
            var tinhTam = document.createElement('select'), xaTam = document.createElement('select');
            await tai('/thanh-toan/ghn/dia-chi?cap=tinh', tinhTam, 'Chọn tỉnh / thành phố', tinhCu);
            if (!tinhTam.value) chonTheoTen(tinhTam, tenTinh);
            xaTam.appendChild(new Option('Chọn phường / xã', ''));
            if (tinhTam.value) {
                await tai('/thanh-toan/ghn/dia-chi?cap=xa&tinh=' + encodeURIComponent(tinhTam.value), xaTam, 'Chọn phường / xã', xaCu);
                if (!xaTam.value) chonTheoTen(xaTam, tenXa);
            }
            if (hienTai !== lan) return;
            var tinhChon = tinhTam.value, xaChon = xaTam.value;
            tinh.replaceChildren.apply(tinh, Array.from(tinhTam.options));tinh.value = tinhChon;
            xa.replaceChildren.apply(xa, Array.from(xaTam.options));xa.value = xaChon;
            loi.textContent = tenTinh && !tinh.value || tenXa && !xa.value
                ? 'Địa chỉ đã lưu chưa khớp danh sách GHN. Vui lòng chọn lại tỉnh/thành và phường/xã nhận hàng.' : '';
            taiLai.hidden = true;
            datHang.disabled = daDoi || !document.getElementById('ghnBaoGiaHienTai') || !tinh.value || !xa.value;
        } catch (e) { if (hienTai === lan) baoLoi(e); }
        finally {
            if (hienTai === lan) { taiLai.disabled = false;tinh.disabled = false;xa.disabled = !tinh.value;nutLayPhi.disabled = false; }
        }
    }
    tinh.addEventListener('change', function () {
        huyBaoGia();taiDiaChi(tinh.value, '', '', '');
    });
    xa.addEventListener('change', function () { huyBaoGia();loi.textContent = ''; });
    ['ten', 'sdt', 'diaChi', 'paymentMethodId'].forEach(function (id) {
        var el = document.getElementById(id);
        if (el) { el.addEventListener('change', huyBaoGia); el.addEventListener('input', huyBaoGia); }
    });
    function diaChiDaChon() {
        return diaChiDaLuu && diaChiDaLuu.value ? diaChiDaLuu.selectedOptions[0].dataset : {};
    }
    if (diaChiDaLuu) diaChiDaLuu.addEventListener('change', function () {
        huyBaoGia();var dc = diaChiDaChon();taiDiaChi('', '', dc.tinhTen, dc.xaTen);
    });
    taiLai.addEventListener('click', function () {
        var dc = diaChiDaChon();taiDiaChi(tinh.value, xa.value, dc.tinhTen, dc.xaTen);
    });
    var dc = diaChiDaChon();
    taiDiaChi(box.dataset.tinh, box.dataset.xa, dc.tinhTen, dc.xaTen);
};
window.VictorKhoiTaoGhn();
