(function () {
    if (window.VictorDuLieuTrucTuyen) return;
    window.VictorDuLieuTrucTuyen = true;
    function batDau() {
        var config = document.getElementById('cauHinhTrucTuyen');
        if (!config) return;
        var kenh = config.dataset.kenh, stream, timer, poll, loading = false, pending = false, topics = new Set();
        var daNhap = new WeakSet(), path = window.location.pathname;
        document.addEventListener('input', ghiNhanNhap);
        document.addEventListener('change', ghiNhanNhap);
        document.addEventListener('vs:giu-ban-nhap', function (e) { if (e.detail) daNhap.add(e.detail); });
        function ghiNhanNhap(e) { if (e.isTrusted && e.target.form) daNhap.add(e.target.form); }
        function laMaBanGhi(ten) { return /^(id|ma|loai)|id$/i.test(ten); }
        function khoaForm(form) {
            return new URL(form.getAttribute('action') || window.location.href, window.location.href).href + '|' + Array.from(form.querySelectorAll('input[type="hidden"]'))
                .filter(function (x) { return laMaBanGhi(x.name); }).map(function (x) { return x.name + '=' + x.value; }).sort().join('&');
        }
        function bao(text) {
            var el = document.getElementById('trangThaiDongBo');
            if (!el) { el = document.createElement('p');el.id = 'trangThaiDongBo';el.className = 'vs-live-status';el.setAttribute('role', 'status');document.body.appendChild(el); }
            el.textContent = text;el.hidden = !text;
        }
        function thayVung(old, fresh) {
            if (!old || !fresh || old.innerHTML === fresh.innerHTML) return;
            if (old.contains(document.activeElement) && document.activeElement.matches('input,select,textarea')) { pending = true;return; }
            var moi = fresh.cloneNode(true), giu = [];
            var chiTiet = moi.querySelectorAll('details');
            old.querySelectorAll('details').forEach(function (d, i) { if (chiTiet[i]) chiTiet[i].open = d.open; });
            old.querySelectorAll('form').forEach(function (form) {
                if (!daNhap.has(form)) return;
                var khop = Array.from(moi.querySelectorAll('form')).filter(function (f) { return khoaForm(f) === khoaForm(form); });
                giu.push([form, khop.length === 1 ? khop[0] : null]);
            });
            if (giu.some(function (p) { return !p[1]; })) return;
            moi.querySelectorAll('script,[data-vs-flash]').forEach(function (n) { n.remove(); });
            giu.forEach(function (p) { p[1].replaceWith(p[0]); });
            old.replaceChildren.apply(old, Array.from(moi.childNodes));
        }
        function thayCacVung(doc, selector) {
            var cu = document.querySelectorAll(selector), moi = doc.querySelectorAll(selector);
            cu.forEach(function (el, i) { thayVung(el, moi[i]); });
        }
        function capNhatFormQuanLy(doc) {
            document.querySelectorAll('main form[method="post"],main form[method="POST"]').forEach(function (form) {
                if (daNhap.has(form) || form.contains(document.activeElement)) return;
                var khop = Array.from(doc.querySelectorAll('form')).filter(function (f) { return khoaForm(f) === khoaForm(form); });
                if (khop.length !== 1) return;
                var moi = khop[0];
                Array.from(form.elements).forEach(function (el) {
                    if (!el.name || /^(file|password|submit|button)$/.test(el.type)) return;
                    // Mã mẫu/sản phẩm gắn với từng nút, không được ghi đè khi đồng bộ.
                    if (el.type === 'hidden' && laMaBanGhi(el.name)) return;
                    var value = moi.elements.namedItem(el.name);
                    if (!value || typeof value.value === 'undefined' || value.length && !value.tagName) return;
                    if (el.tagName === 'SELECT') {
                        var daChon = el.value;
                        el.replaceChildren.apply(el, Array.from(value.options).map(function (o) { return o.cloneNode(true); }));
                        if (el.id === 'mauAnh') {
                            if (Array.from(el.options).some(function (o) { return o.value === daChon; })) el.value = daChon;
                            return;
                        }
                    }
                    el.value = value.value;
                    if (el.type === 'checkbox' || el.type === 'radio') el.checked = value.checked;
                });
            });
        }
        function capNhatSanPham(doc) {
            var form = document.getElementById('formChonGiay'), moi = doc.getElementById('formChonGiay');
            if (!form) return;
            if (!moi) {
                form.querySelectorAll('button[type="submit"],button[name="thaoTac"]').forEach(function (b) { b.disabled = true; });
                document.getElementById('tonKho').textContent = 'Sản phẩm đã ngừng bán.';return;
            }
            var chon = form.querySelector('input[name="idPhienBan"]:checked'), id = chon && chon.value;
            var sl = document.getElementById('soLuong').value;
            thayCacVung(doc, '.online-variants');thayCacVung(doc, '.online-gallery');thayCacVung(doc, '#moTaSanPham');
            form.querySelectorAll('input[name="idPhienBan"]').forEach(function (r) { r.checked = r.value === id; });
            document.getElementById('soLuong').value = sl;
            if (window.VictorKhoiTaoSanPham) window.VictorKhoiTaoSanPham();
            var selected = form.querySelector('input[name="idPhienBan"]:checked');
            form.querySelectorAll('button[name="thaoTac"]').forEach(function (b) { b.disabled = !selected || selected.disabled; });
            if (!selected) document.getElementById('tonKho').textContent = 'Vui lòng chọn lại mẫu giày đang bán.';
        }
        function capNhatDiaChi(doc) {
            var select = document.getElementById('diaChiDaLuu'), fresh = doc.getElementById('diaChiDaLuu');
            if (!select || !fresh || select === document.activeElement) return;
            var value = select.value;
            select.replaceChildren.apply(select, Array.from(fresh.options).map(function (o) { return o.cloneNode(true); }));
            select.value = value;
            if (!select.value) { select.value = '';var input = document.getElementById('diaChi');if (input) input.readOnly = false; }
        }
        function capNhatCheckout(doc) {
            var select = document.getElementById('phieuGiamGiaOnline'), fresh = doc.getElementById('phieuGiamGiaOnline');
            if (!select) return;
            if (!fresh) {
                document.getElementById('nutDatHangOnline').disabled = true;
                bao('Sản phẩm trong đơn đã thay đổi. Vui lòng kiểm tra lại giỏ hàng.');return;
            }
            var products = document.querySelector('.checkout-products'), newProducts = doc.querySelector('.checkout-products');
            var thayDoi = products && newProducts && products.innerHTML !== newProducts.innerHTML
                || select.dataset.tongGoc !== fresh.dataset.tongGoc || select.dataset.giamDot !== fresh.dataset.giamDot
                || select.innerHTML !== fresh.innerHTML;
            if (select === document.activeElement) { pending = true;return; }
            var value = select.value;
            thayVung(products, newProducts);
            select.replaceChildren.apply(select, Array.from(fresh.options).map(function (o) { return o.cloneNode(true); }));
            select.value = value;if (!select.value) select.value = '';
            select.dataset.tongGoc = fresh.dataset.tongGoc;select.dataset.giamDot = fresh.dataset.giamDot;
            thayCacVung(doc, '.checkout-summary-heading');thayCacVung(doc, '.checkout-discount-row');
            if (thayDoi && window.VictorHuyBaoGiaGhn) window.VictorHuyBaoGiaGhn();
            if (select.onchange) select.onchange();
        }
        function canQuanTam(nhom) {
            if (nhom.includes('all') || nhom.includes('gio-hang')) return true;
            var can = [];
            if (path === '/' || path.startsWith('/san-pham/')) can = ['san-pham', 'ton-kho', 'uu-dai'];
            else if (path === '/pos') can = ['san-pham', 'ton-kho', 'uu-dai', 'hoa-don', 'khach-hang'];
            else if (path === '/thanh-toan' || path === '/gio-hang') can = ['san-pham', 'ton-kho', 'uu-dai', 'gio-hang', 'dia-chi'];
            else if (path.startsWith('/don-hang') || path.startsWith('/don-vang-lai/')) can = ['hoa-don', 'van-chuyen'];
            else if (path.startsWith('/tai-khoan/dia-chi')) can = ['dia-chi'];
            else if (path.startsWith('/tai-khoan')) can = ['khach-hang', 'nhan-vien', 'dia-chi'];
            else if (path.startsWith('/quan-ly/')) can = [path.split('/')[2], 'ton-kho', 'uu-dai'];
            return nhom.some(function (n) { return can.includes(n); });
        }
        async function tai() {
            if (window.VictorDangThaoTac) { topics.add('all');clearTimeout(timer);timer = setTimeout(tai, 350);return; }
            if (document.hidden || document.querySelector('dialog[open]')) { pending = true;return; }
            if (loading) { pending = true;return; }
            var nhom = Array.from(topics);topics.clear();
            if (!nhom.length || !canQuanTam(nhom)) return;
            pending = false;loading = true;
            try {
                if (nhom.includes('gio-hang') && path !== '/gio-hang' && kenh !== 'cong-khai') await fetch('/gio-hang', {cache: 'no-store', headers: {'X-Victor-Live': '1'}});
                var phienThaoTac = window.VictorPhienThaoTac || 0;
                var r = await fetch(window.location.href, {cache: 'no-store', headers: {'X-Victor-Live': '1'}});
                if (r.status === 401 || r.status === 403 || new URL(r.url).pathname === '/login' && path !== '/login') {
                    bao('Phiên làm việc đã thay đổi. Vui lòng đăng nhập lại.');if (stream) stream.close();return;
                }
                if (r.status === 404 && path.startsWith('/san-pham/')) {
                    var form = document.getElementById('formChonGiay');
                    if (form) form.querySelectorAll('button[name="thaoTac"]').forEach(function (b) { b.disabled = true; });
                    var ton = document.getElementById('tonKho');if (ton) ton.textContent = 'Sản phẩm đã ngừng bán.';
                    return;
                }
                if (!r.ok) throw new Error('Chưa đồng bộ được dữ liệu.');
                var doc = new DOMParser().parseFromString(await r.text(), 'text/html');
                if (window.VictorDangThaoTac || phienThaoTac !== (window.VictorPhienThaoTac || 0)) { hen('all');return; }
                if (document.querySelector('dialog[open]')) { topics.add('all');pending = true;return; }
                if (path === '/thanh-toan') { capNhatCheckout(doc);capNhatDiaChi(doc); }
                else if (path.startsWith('/don-hang') || path.startsWith('/don-vang-lai/') || /^\/quan-ly\/hoa-don\/\d+$/.test(path)) {
                    thayCacVung(doc, 'main');if (window.VictorKhoiTaoQr) window.VictorKhoiTaoQr();
                } else if (path === '/gio-hang') thayCacVung(doc, 'main');
                else if (path.startsWith('/tai-khoan/dia-chi')) thayCacVung(doc, '.address-list');
                else if (path === '/tai-khoan') thayCacVung(doc, '.account-layout');
                else if (path.startsWith('/tai-khoan/')) capNhatFormQuanLy(doc);
                else if (path.startsWith('/san-pham/')) { capNhatSanPham(doc);thayCacVung(doc, '.product-grid'); }
                else if (path === '/') thayCacVung(doc, '.product-grid');
                else if (path === '/quan-ly/thong-ke') {
                    thayCacVung(doc, '.overview-cards,.stats-middle,.stats-bestsellers');
                } else if (path === '/quan-ly/van-chuyen') {
                    thayCacVung(doc, '.overview-cards,.overview-page > section.panel,.overview-pagination');
                } else if (path.startsWith('/quan-ly/') || path === '/pos') {
                    // Giữ các ô đang nhập, chỉ thay phần dữ liệu hiển thị.
                    thayCacVung(doc, 'table,.overview-cards,.revenue-chart,.overview-pill,.summary-row,.product-admin-images,.pagination,.pos-cart-panel .cart-list,.pos-cart-count,.pos-cart-discount-note');
                    capNhatFormQuanLy(doc);
                    if (path === '/pos') {
                        var checkout = document.querySelector('form[action="/pos/checkout"]');
                        var freshCheckout = doc.querySelector('form[action="/pos/checkout"]');
                        if (checkout && freshCheckout) {
                            checkout.dataset.tongTien = freshCheckout.dataset.tongTien;
                            checkout.querySelectorAll('button[name="hanhDong"]').forEach(function (button) {
                                var freshButton = freshCheckout.querySelector('button[value="' + button.value + '"]');
                                if (freshButton) button.disabled = freshButton.disabled;
                            });
                        }
                    }
                }
                thayCacVung(doc, '.cart-link,.topbar-user > span,.menu-list,.account-dropdown');
                document.dispatchEvent(new CustomEvent('vs:du-lieu-da-cap-nhat'));
                var moiConfig = doc.getElementById('cauHinhTrucTuyen');
                if (moiConfig && moiConfig.dataset.kenh !== kenh) { if (stream) stream.close();kenh = moiConfig.dataset.kenh;ketNoi(); }
            } catch (e) { topics.add('all');clearTimeout(timer);timer = setTimeout(tai, 4000); }
            finally { loading = false;if (pending && topics.size && !document.hidden && !document.querySelector('dialog[open]')) hen('all'); }
        }
        function hen(nhom) {
            nhom.split(',').forEach(function (n) { topics.add(n); });
            clearTimeout(timer);timer = setTimeout(tai, 100);
        }
        function ketNoi() {
            if (!window.EventSource) { poll = setInterval(function () { hen('all');document.dispatchEvent(new CustomEvent('vs:chuong')); },20000);return; }
            stream = new EventSource('/thong-bao-don-hang/truc-tuyen?kenh=' + encodeURIComponent(kenh));
            stream.addEventListener('cap-nhat', function () { document.dispatchEvent(new CustomEvent('vs:chuong'));hen('all'); });
            stream.addEventListener('du-lieu', function (e) { hen(e.data); });
            stream.onerror = function () { document.dispatchEvent(new CustomEvent('vs:chuong'));hen('all'); };
        }
        document.addEventListener('visibilitychange', function () { if (!document.hidden) hen('all'); });
        document.addEventListener('focusout', function () { if (pending) hen('all'); });
        document.addEventListener('close', function () { if (pending) hen('all'); }, true);
        window.addEventListener('pagehide', function () { if (stream) stream.close();clearTimeout(timer);clearInterval(poll); });
        window.addEventListener('pageshow', function (e) { if (e.persisted) { ketNoi();hen('all'); } });
        ketNoi();
    }
    if (document.readyState === 'loading') document.addEventListener('DOMContentLoaded', batDau);else batDau();
})();
