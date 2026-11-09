// Giao hàng tại quầy: địa chỉ, báo giá GHN và tổng thanh toán.
(function () {
    'use strict';
    if (window.VictorGiaoHangTaiQuay) return;
    window.VictorGiaoHangTaiQuay = true;
    var states = new WeakMap(), money = new Intl.NumberFormat('vi-VN'), khachDangHien;
    function form() { return document.querySelector('form[data-pos-giao-hang]'); }
    function field(f, name) { return f.elements.namedItem(name); }
    function shipping(f) { return field(f, 'giaoHang').value === 'true'; }
    function state(f) {
        if (!states.has(f)) states.set(f, {timer: null, expiry: null, signature: '', loading: false});
        return states.get(f);
    }
    function signature(f) {
        return JSON.stringify(['customerName', 'customerPhone', 'customerAddress', 'tinh', 'xa', 'paymentMethodId']
            .map(function (name) { return field(f, name).value.trim(); }).concat([
                f.dataset.tienHang,
                Array.from(document.querySelectorAll('.cart-line')).map(function (row) {
                    return [row.dataset.mau, row.querySelector('[name="quantity"]').value];
                }), (document.getElementById('discountCouponId') || {}).value
            ]));
    }
    function message(f, text, retry) {
        f.querySelector('#posTrangThaiPhi').textContent = text;
        f.querySelector('#posThuLaiPhi').hidden = !retry;
    }
    function controls(f, ready) {
        f.querySelectorAll('button[name="hanhDong"]').forEach(function (b) {
            b.disabled = !ready || b.dataset.khoaGoc === 'true';
        });
    }
    function total(f, fee, amount) {
        var row = document.querySelector('.pos-phi-giao-hang');
        if (row) row.hidden = !shipping(f);
        var feeEl = document.getElementById('posPhiGiaoHang');
        if (feeEl) feeEl.textContent = fee == null ? 'Đang tính phí' : money.format(fee) + ' đ';
        var sum = document.getElementById('posTongThanhToan');
        if (sum) sum.textContent = amount == null ? 'Chờ phí giao hàng' : money.format(amount) + ' đ';
        f.dataset.tongTien = amount == null ? '' : String(amount);
        var paid = field(f, 'paidAmount');
        paid.setCustomValidity('');
        paid.dispatchEvent(new Event('input', {bubbles: true}));
    }
    function invalidate(f) {
        var s = state(f);clearTimeout(s.expiry);s.signature = '';
        field(f, 'maBaoGia').value = '';controls(f, false);total(f, null, null);
    }
    function complete(f) {
        return ['customerName', 'customerPhone', 'customerAddress', 'tinh', 'xa', 'paymentMethodId'].every(function (name) {
            return field(f, name).value.trim() && field(f, name).checkValidity();
        });
    }
    async function quote(f) {
        var s = state(f);
        if (!f.isConnected || !shipping(f) || s.loading || !complete(f)) return;
        s.loading = true;
        var key = signature(f), data = new URLSearchParams({
            ten: field(f, 'customerName').value.trim(), sdt: field(f, 'customerPhone').value.trim(),
            diaChi: field(f, 'customerAddress').value.trim(), tinh: field(f, 'tinh').value,
            xa: field(f, 'xa').value, paymentMethodId: field(f, 'paymentMethodId').value,
            csrfPos: field(f, 'csrfPos').value
        });
        message(f, 'Đang tính phí giao hàng…', false);
        try {
            var response = await fetch('/pos/giao-hang/bao-gia', {method: 'POST', body: data, headers: {'Accept': 'application/json'}});
            var result = await response.json();
            if (!response.ok) throw new Error(result.error || 'Chưa lấy được phí giao hàng.');
            if (!Number.isFinite(Number(result.phi)) || !Number.isFinite(Number(result.tong)) || !result.maBaoGia)
                throw new Error('Phí giao hàng chưa hợp lệ. Vui lòng thử lại.');
            if (f.isConnected && shipping(f) && key === signature(f)) {
                field(f, 'maBaoGia').value = result.maBaoGia;s.signature = key;
                s.fee = Number(result.phi);s.amount = Number(result.tong);
                total(f, Number(result.phi), Number(result.tong));controls(f, true);
                message(f, 'Đã tính phí GHN. Khách thanh toán đủ tiền hàng và phí giao hàng trước khi gửi.', false);
                s.expiry = setTimeout(function () { schedule(f); }, Math.max(1000, Number(result.hetHan) - Date.now() - 5000));
            }
        } catch (e) {
            if (f.isConnected && shipping(f) && key === signature(f)) message(f, e.message || 'Không kết nối được GHN. Vui lòng thử lại.', true);
        } finally {
            s.loading = false;
            if (f.isConnected && shipping(f) && key !== signature(f)) schedule(f);
        }
    }
    function schedule(f) {
        var s = state(f);clearTimeout(s.timer);
        if (!shipping(f)) return;
        invalidate(f);
        message(f, 'Phí giao hàng tự tính khi nhập đủ thông tin nhận hàng.', false);
        if (complete(f)) s.timer = setTimeout(function () { quote(f); }, 600);
    }
    function tenDiaChi(value) {
        return String(value || '').normalize('NFD').replace(/[\u0300-\u036f]/g, '').replace(/[đĐ]/g, 'd').toLowerCase().trim().replace(/\s+/g, ' ');
    }
    function chonDiaChi(select, ten) {
        if (!ten) return;
        var options = Array.from(select.options).filter(function (o) {return o.value;});
        var exact = options.find(function (o) {return tenDiaChi(o.textContent) === tenDiaChi(ten);});
        if (exact) {select.value = exact.value;return;}
        function boTienTo(v) {return tenDiaChi(v).replace(/^(thanh pho|tinh|tp\.?|phuong|xa|thi tran)\s+/, '');}
        var khop = options.filter(function (o) {return boTienTo(o.textContent) === boTienTo(ten);});
        if (khop.length === 1) select.value = khop[0].value;
    }
    async function addresses(select, url, chosen, current) {
        select.disabled = true;
        try {
            var response = await fetch(url, {headers: {'Accept': 'application/json'}}), rows = await response.json();
            if (current && !current()) return;
            if (!response.ok || !Array.isArray(rows)) throw new Error(rows.error || 'Không tải được danh sách địa chỉ.');
            var placeholder = select.options[0].textContent;
            select.replaceChildren(new Option(placeholder, ''));
            rows.forEach(function (r) { select.add(new Option(r.ten, r.id)); });
            chonDiaChi(select, chosen);
        } finally { if (!current || current()) select.disabled = false; }
    }
    async function province(f) {
        var tinh = field(f, 'tinh'), xa = field(f, 'xa');
        try {
            if (tinh.options.length < 2) await addresses(tinh, '/thanh-toan/ghn/dia-chi?cap=tinh', tinh.dataset.tenDaLuu);
            else if (!tinh.value) chonDiaChi(tinh, tinh.dataset.tenDaLuu);
            var requested = tinh.value;
            if (requested) await addresses(xa, '/thanh-toan/ghn/dia-chi?cap=xa&tinh=' + encodeURIComponent(requested), xa.dataset.tenDaLuu,
                function () { return f.isConnected && tinh.value === requested; });
            schedule(f);
            if (tinh.dataset.tenDaLuu && !tinh.value || requested && xa.dataset.tenDaLuu && !xa.value)
                message(f, 'Địa chỉ đã lưu chưa khớp danh sách GHN. Vui lòng chọn lại tỉnh hoặc phường / xã.', false);
        } catch (e) { message(f, e.message || 'Không tải được địa chỉ GHN.', true); }
    }
    function mode(f, clear) {
        var on = shipping(f), box = f.querySelector('.pos-giao-hang');
        box.hidden = !on;box.disabled = !on;
        box.querySelectorAll('input,select').forEach(function (el) { el.required = on; });
        if (on) { invalidate(f);province(f); }
        else {
            var s = state(f);clearTimeout(s.timer);clearTimeout(s.expiry);s.signature = '';
            field(f, 'maBaoGia').value = '';total(f, 0, Number(f.dataset.tienHang));controls(f, true);
            if (clear) fetch('/pos/giao-hang/bo', {method: 'POST', body: new URLSearchParams({csrfPos: field(f, 'csrfPos').value})}).catch(function () {});
        }
    }
    function init() {
        var f = form();if (!f) return;
        var idKhach = f.dataset.khachHangId || '';
        var doiKhach = khachDangHien !== undefined && khachDangHien !== idKhach;
        if (doiKhach || !states.has(f) && f.dataset.khachVuaDoi === 'true') {
            field(f, 'customerName').value = f.dataset.nguoiNhan || '';
            field(f, 'customerPhone').value = f.dataset.sdtNhan || '';
            field(f, 'customerAddress').value = f.dataset.diaChiNhan || '';
            field(f, 'tinh').dataset.tenDaLuu = f.dataset.tinhNhan || '';
            field(f, 'tinh').value = '';
            field(f, 'xa').dataset.tenDaLuu = f.dataset.xaNhan || '';
            field(f, 'xa').replaceChildren(new Option('Chọn phường / xã', ''));
            field(f, 'maBaoGia').value = '';field(f, 'paidAmount').value = '';
        }
        khachDangHien = idKhach;
        if (!states.has(f)) {
            f.querySelectorAll('button[name="hanhDong"]').forEach(function (b) { b.dataset.khoaGoc = String(b.disabled); });
            state(f);mode(f, false);
        } else if (shipping(f)) {
            if (doiKhach) {province(f);return;}
            // Giỏ hoặc ưu đãi thay đổi qua SSE phải có báo giá mới.
            if (state(f).signature !== signature(f)) schedule(f);
            else quoteTotal(f);
        }
    }
    function quoteTotal(f) {
        var s = state(f);
        if (s.amount != null) total(f, s.fee, s.amount);
    }
    document.addEventListener('change', function (e) {
        var f = form();if (!f || !f.contains(e.target)) return;
        if (e.target.name === 'giaoHang') { mode(f, true);return; }
        if (!shipping(f)) return;
        if (e.target.name === 'tinh') {
            var xa = field(f, 'xa');xa.replaceChildren(new Option('Chọn phường / xã', ''));xa.dataset.tenDaLuu = '';
            invalidate(f);if (e.target.value) province(f);return;
        }
        if (['xa', 'paymentMethodId'].includes(e.target.name)) schedule(f);
    });
    document.addEventListener('input', function (e) {
        var f = form();if (f && f.contains(e.target) && shipping(f) && ['customerName', 'customerPhone', 'customerAddress'].includes(e.target.name)) schedule(f);
    });
    document.addEventListener('click', function (e) {
        if (e.target.id !== 'posThuLaiPhi') return;
        var f = form();
        if (!f || !shipping(f)) return;
        if (complete(f)) schedule(f);
        else province(f);
    });
    document.addEventListener('submit', function (e) {
        var f = form();if (e.target !== f || !shipping(f)) return;
        if (!field(f, 'maBaoGia').value || state(f).signature !== signature(f)) {
            e.preventDefault();e.stopImmediatePropagation();schedule(f);
        }
    }, true);
    document.addEventListener('vs:du-lieu-da-cap-nhat', init);
    if (document.readyState === 'loading') document.addEventListener('DOMContentLoaded', init, {once: true});else init();
})();

// Thanh toán tại quầy: kiểm tra tiền, tiền thừa và hỏi in hóa đơn.
(function () {
    'use strict';
    if (window.VictorKiemTraTienTaiQuay) return;
    window.VictorKiemTraTienTaiQuay = true;
    function soTien(value) {
        var parts = String(value || '').trim().match(/^([0-9]{1,16})(?:\.([0-9]{1,2}))?$/);
        return parts ? BigInt(parts[1]) * 100n + BigInt((parts[2] || '').padEnd(2, '0')) : null;
    }
    function kiemTra(input) {
        var daThu = soTien(input.value), canThu = soTien(input.form.dataset.tongTien);
        var method = input.form.querySelector('[name="paymentMethodId"]');
        var tienMat = !!(method && method.selectedOptions[0] && method.selectedOptions[0].dataset.tienMat === 'true');
        var dung = daThu !== null && canThu !== null && (tienMat ? daThu >= canThu : daThu === canThu);
        var row = document.getElementById('posDongTienThua'), change = document.getElementById('posTienThua');
        if (row) row.hidden = !tienMat;
        if (change) change.textContent = daThu !== null && canThu !== null ? dinhDang(daThu > canThu ? daThu - canThu : 0n) : '—';
        var missing = document.getElementById('posTienConThieu');
        if (missing) {
            missing.hidden = daThu === null || canThu === null || daThu >= canThu;
            missing.textContent = !missing.hidden ? 'Khách còn thiếu ' + dinhDang(canThu - daThu) : '';
        }
        var label = document.getElementById('paidAmountLabel');
        if (label) label.textContent = tienMat ? 'Tiền khách đưa (đ) *' : 'Tiền khách đã thanh toán (đ) *';
        input.setCustomValidity(dung || !input.value ? '' : (tienMat ? 'Tiền khách đưa chưa đủ tổng tiền cần thu.' : 'Tiền chuyển khoản hoặc ví điện tử phải đúng tổng tiền cần thu.'));
        return dung;
    }
    function dinhDang(cents) {
        var le = cents % 100n;
        return (cents / 100n).toLocaleString('vi-VN') + (le ? ',' + String(le).padStart(2, '0') : '') + ' đ';
    }
    function capNhat() {
        var input = document.getElementById('paidAmount');
        if (input) kiemTra(input);
    }
    document.addEventListener('click', function (event) {
        if (event.target.id !== 'posNhapDuTien') return;
        var input = document.getElementById('paidAmount');
        if (!input || soTien(input.form.dataset.tongTien) === null) return;
        input.value = input.form.dataset.tongTien;
        input.dispatchEvent(new Event('input', {bubbles: true}));
        input.focus();
    });
    document.addEventListener('input', function (event) {
        if (event.target.id === 'paidAmount') kiemTra(event.target);
    });
    document.addEventListener('change', function (event) {
        if (event.target.name === 'paymentMethodId') capNhat();
    });
    document.addEventListener('submit', function (event) {
        var form = event.target;
        if (!form.matches('form[action="/pos/checkout"]')) return;
        if (event.submitter && event.submitter.value === 'luu-don-cho') return;
        var input = form.querySelector('[name="paidAmount"]');
        if (!input || kiemTra(input)) return;
        event.preventDefault();event.stopImmediatePropagation();input.reportValidity();input.focus();
    }, true);
    document.addEventListener('vs:du-lieu-da-cap-nhat', function () {
        capNhat();
        hoiInHoaDon();
    });
    var hoaDonDaHoi = new Set();
    var dangTaiHoaDonIn = false, vungIn = null;
    function baoLoiIn() {
        if (window.VictorThongBao) window.VictorThongBao.thongBao('Chưa tải được nội dung hóa đơn đã thanh toán. Vui lòng kiểm tra kết nối và thử In nhanh hóa đơn lại.', 'Chưa thể in hóa đơn', 'danger');
    }
    function donVungIn() {
        if (vungIn) vungIn.remove();
        vungIn = null;
        document.body.classList.remove('pos-dang-in');
        window.removeEventListener('afterprint', donVungIn);
    }
    async function inNhanhHoaDon(link) {
        var url = new URL(link.href, location.href);
        if (dangTaiHoaDonIn || url.origin !== location.origin || !/^\/quan-ly\/hoa-don\/\d+$/.test(url.pathname)) return;
        dangTaiHoaDonIn = true;link.setAttribute('aria-busy', 'true');
        try {
            var response = await fetch(url.href, {cache: 'no-store', headers: {'Accept': 'text/html'}});
            if (!response.ok || response.redirected) throw new Error('KhongTaiDuocHoaDon');
            var doc = new DOMParser().parseFromString(await response.text(), 'text/html');
            var invoice = doc.querySelector('[data-hoa-don-da-thanh-toan="true"]');
            if (!invoice || invoice.dataset.maHoaDon !== url.pathname.split('/').pop()) throw new Error('HoaDonKhongHopLe');
            // Chỉ lấy nội dung hóa đơn; bỏ điều khiển và mã chạy từ trang chi tiết.
            invoice.querySelectorAll('script,iframe,object,embed,style,link,form,button,.invoice-actions').forEach(function (node) {node.remove();});
            invoice.querySelectorAll('*').forEach(function (node) {
                Array.from(node.attributes).forEach(function (attr) {
                    if (/^on/i.test(attr.name) || /^(href|src|action)$/i.test(attr.name) && /^\s*(javascript|vbscript):/i.test(attr.value)) node.removeAttribute(attr.name);
                });
            });
            donVungIn();
            vungIn = document.createElement('section');vungIn.id = 'posHoaDonIn';vungIn.setAttribute('aria-hidden', 'true');
            vungIn.appendChild(document.importNode(invoice, true));document.body.appendChild(vungIn);
            await Promise.all(Array.from(vungIn.querySelectorAll('img')).map(function (img) {return img.decode ? img.decode().catch(function () {}) : Promise.resolve();}));
            if (document.fonts) await document.fonts.ready;
            document.body.classList.add('pos-dang-in');
            window.addEventListener('afterprint', donVungIn, {once: true});
            window.print();
        } catch (e) {donVungIn();baoLoiIn();}
        finally {dangTaiHoaDonIn = false;link.removeAttribute('aria-busy');}
    }
    document.addEventListener('click', function (event) {
        var link = event.target.closest('[data-in-nhanh-hoa-don]');
        if (!link || event.ctrlKey || event.metaKey || event.shiftKey || event.altKey || event.button > 0) return;
        event.preventDefault();inNhanhHoaDon(link);
    });
    function hoiInHoaDon() {
        var link = document.querySelector('[data-in-hoa-don-vua-tao]');
        if (!link || !window.VictorThongBao || hoaDonDaHoi.has(link.href)) return;
        hoaDonDaHoi.add(link.href);
        window.VictorThongBao.xacNhan({
            tieuDe: 'Thanh toán thành công',
            noiDung: (link.dataset.thongBao || 'Đã thanh toán thành công.') + '\nBạn có muốn in hóa đơn ngay không?',
            nhanNut: 'In ngay',
            nhanHuy: 'Để sau',
            kieu: 'success'
        }).then(function (dongY) {
            if (!dongY) return;
            inNhanhHoaDon(link);
        });
    }
    function khoiTao() {capNhat();hoiInHoaDon();}
    if (document.readyState === 'loading') document.addEventListener('DOMContentLoaded', khoiTao, {once: true});
    else khoiTao();
})();
