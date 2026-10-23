(function () {
    'use strict';
    if (window.VictorThaoTacTaiCho) return;
    window.VictorThaoTacTaiCho = true;
    var dangGui = false, formDaNhap = new WeakSet();
    var trangDaHienThi = location.pathname + location.search;
    function khoa(form) {
        if (new URL(form.action, location.href).pathname === '/pos/checkout') return form.action;
        return form.action + '|' + Array.from(form.querySelectorAll('input[type="hidden"]'))
            .filter(function (el) { return /^(id|ma)|id$/i.test(el.name); })
            .map(function (el) { return el.name + '=' + el.value; }).sort().join('&');
    }
    document.addEventListener('input', function (e) { if (e.target.form) formDaNhap.add(e.target.form); });
    document.addEventListener('change', function (e) { if (e.target.form) formDaNhap.add(e.target.form); });
    function thongBao(text) {
        if (window.VictorThongBao) window.VictorThongBao.thongBao(text, 'Thông báo', 'danger');
    }
    function trangHoTro(path) {
        return path === '/' || path === '/pos' || path === '/gio-hang' || path === '/thanh-toan'
            || path.startsWith('/quan-ly/') || path.startsWith('/don-hang')
            || path.startsWith('/san-pham/') || path.startsWith('/tai-khoan/dia-chi');
    }
    function giuForm(formGui) {
        return Array.from(document.querySelectorAll('main form')).filter(function (form) {
            return form !== formGui && formDaNhap.has(form);
        }).map(function (form) {
            return {khoa: khoa(form), form: form, giaTri: Array.from(form.elements).filter(function (el) {
                return el.name && !/^(hidden|file|submit|button)$/.test(el.type);
            }).map(function (el) { return {ten: el.name, value: el.value, checked: el.checked, type: el.type}; })};
        });
    }
    function khoiTao() {
        ['VictorKhoiTaoSanPham', 'VictorKhoiTaoDiaChi', 'VictorKhoiTaoDiaChiThanhToan',
            'VictorKhoiTaoGhn', 'VictorKhoiTaoPhieuOnline', 'VictorKhoiTaoQr', 'VictorKhoiTaoModern'].forEach(function (ten) {
            if (typeof window[ten] === 'function') window[ten]();
        });
        document.dispatchEvent(new CustomEvent('vs:du-lieu-da-cap-nhat'));
    }
    function thayNoiDung(doc, formGui, action) {
        var main = document.querySelector('main'), moi = doc.querySelector('main');
        if (!main || !moi) throw new Error('Không tải được nội dung trang.');
        var giu = giuForm(formGui);
        // Khi đổi đơn chờ, dùng thông tin của đơn vừa mở.
        if (/\/pos\/(don-cho\/mo|cart\/clear|checkout)$/.test(action)) giu = [];
        var dangMo = Array.from(main.querySelectorAll('details')).map(function (d) { return d.open; });
        var chonAnh = document.getElementById('mauAnh');
        var idAnh = chonAnh && chonAnh.value;
        if (location.pathname === '/') {
            ['#san-pham', '#nhu-cau'].forEach(function (selector) {
                var cu = document.querySelector(selector), fresh = doc.querySelector(selector);
                if (cu && fresh) cu.replaceWith(fresh);
            });
        } else {
            main.replaceWith(moi);
            if (location.pathname === '/pos') {
                var cu = document.querySelector('.main-area > section.content-layout');
                var fresh = doc.querySelector('.main-area > section.content-layout');
                if (cu && fresh) cu.replaceWith(fresh);
            }
        }
        // Các script trong HTML tải về không chạy. Khởi tạo lại đúng các bộ chọn cần thiết.
        document.querySelectorAll('main script').forEach(function (script) { script.remove(); });
        giu.forEach(function (banNhap) {
            var forms = Array.from(document.querySelectorAll('main form')).filter(function (f) { return khoa(f) === banNhap.khoa; });
            if (forms.length !== 1) return;
            var form = forms[0];
            if (banNhap.form.querySelector('input[type="file"]') && Array.from(banNhap.form.querySelectorAll('input[type="file"]')).some(function (el) { return el.files.length; })) {
                form.replaceWith(banNhap.form);form = banNhap.form;
            } else banNhap.giaTri.forEach(function (giaTri) {
                Array.from(form.elements).filter(function (el) { return el.name === giaTri.ten; }).forEach(function (el) {
                    if (el.type === 'radio' || el.type === 'checkbox') { if (el.value === giaTri.value) el.checked = giaTri.checked; }
                    else if (el.tagName !== 'SELECT' || Array.from(el.options).some(function (o) { return o.value === giaTri.value; })) el.value = giaTri.value;
                    if (el.tagName === 'SELECT' && el.hasAttribute('data-cu')) el.dataset.cu = giaTri.value;
                });
            });
            formDaNhap.add(form);
            document.dispatchEvent(new CustomEvent('vs:giu-ban-nhap', {detail: form}));
        });
        document.querySelectorAll('main details').forEach(function (d, i) { if (dangMo[i]) d.open = true; });
        var moiChonAnh = document.getElementById('mauAnh');
        if (moiChonAnh && Array.from(moiChonAnh.options).some(function (o) { return o.value === idAnh; })) moiChonAnh.value = idAnh;
        ['.cart-link'].forEach(function (selector) {
            var cu = document.querySelector(selector), fresh = doc.querySelector(selector);
            if (cu && fresh) cu.replaceWith(fresh);
        });
        document.title = doc.title || document.title;
        var hopKhach = document.getElementById('themKhachHang'), hopKhachMoi = doc.getElementById('themKhachHang');
        if (hopKhach && hopKhachMoi && formGui && formGui.closest('#themKhachHang')) {
            if (hopKhach.open) hopKhach.close();
            hopKhach.replaceChildren.apply(hopKhach, Array.from(hopKhachMoi.childNodes));
            if (hopKhach.querySelector('.alert.error')) hopKhach.showModal();
        }
        khoiTao();
        var daBao = new Set();
        document.querySelectorAll('main [data-vs-flash]').forEach(function (el) {
            var text = el.textContent.trim();
            if (!text || daBao.has(text)) return;
            daBao.add(text);el.dataset.vsDaHien = 'true';
            if (window.VictorThongBao) window.VictorThongBao.thongBao(text,
                el.dataset.vsFlash === 'success' ? 'Thành công' : 'Không thể thực hiện',
                el.dataset.vsFlash === 'success' ? 'success' : 'danger');
        });
        document.dispatchEvent(new CustomEvent('vs:chuong'));
    }
    async function gui(url, options, form, action, lichSu) {
        if (dangGui) return;
        dangGui = true;window.VictorDangThaoTac = true;
        window.VictorPhienThaoTac = (window.VictorPhienThaoTac || 0) + 1;
        var x = window.scrollX, y = window.scrollY;
        var oCu = document.activeElement, idO = oCu && oCu.id;
        var cacVungCuon = Array.from(document.querySelectorAll('.cart-list,.product-admin-table,.invoice-table,.overview-table'))
            .map(function (el) { return {x: el.scrollLeft, y: el.scrollTop}; });
        var vungCu = document.querySelector('main');
        var nut = form ? Array.from(form.elements).filter(function (el) {
            return el.matches('input,select,textarea,button') && el.type !== 'hidden' && !el.disabled;
        }) : [];
        nut.forEach(function (b) { b.disabled = true; });
        if (vungCu) vungCu.setAttribute('aria-busy', 'true');
        var thanh = document.createElement('div');thanh.className = 'vs-loading-bar';thanh.setAttribute('role', 'status');thanh.setAttribute('aria-label', 'Đang cập nhật');document.body.appendChild(thanh);
        try {
            var r = await fetch(url, options);
            if (!r.ok) throw new Error('Chưa thực hiện được thao tác. Vui lòng kiểm tra lại.');
            var dich = new URL(r.url || url, location.href);
            if (dich.origin !== location.origin || dich.pathname !== location.pathname) { location.assign(dich.href);return; }
            if (!(r.headers.get('content-type') || '').includes('text/html')) throw new Error('Máy chủ chưa trả về nội dung trang.');
            var doc = new DOMParser().parseFromString(await r.text(), 'text/html');
            thayNoiDung(doc, form, action);
            document.querySelectorAll('.cart-list,.product-admin-table,.invoice-table,.overview-table').forEach(function (el, i) {
                if (cacVungCuon[i]) { el.scrollLeft = cacVungCuon[i].x;el.scrollTop = cacVungCuon[i].y; }
            });
            if (idO && !document.querySelector('dialog[open]')) {
                var oMoi = document.getElementById(idO);
                if (oMoi && !oMoi.disabled) oMoi.focus({preventScroll: true});
            }
            if (lichSu && location.href !== dich.href) history.pushState({vsTaiCho: true}, '', dich.pathname + dich.search + (new URL(url, location.href).hash || ''));
            if (!lichSu && dich.search !== location.search) history.replaceState({vsTaiCho: true}, '', dich.pathname + dich.search + location.hash);
            trangDaHienThi = location.pathname + location.search;
            window.scrollTo({left: x, top: y, behavior: 'instant'});
        } catch (e) {
            // Không tự gửi lại POST: máy chủ có thể đã ghi dữ liệu trước khi mất kết nối.
            thongBao(options.method === 'POST'
                ? 'Chưa nhận được kết quả. Hãy kiểm tra dữ liệu trước khi thực hiện lại để tránh thao tác trùng.'
                : 'Chưa tải được dữ liệu. Bạn có thể thử lại.');
        } finally {
            nut.forEach(function (b) { b.disabled = false; });
            if (vungCu) vungCu.removeAttribute('aria-busy');
            var main = document.querySelector('main');if (main) main.removeAttribute('aria-busy');
            thanh.remove();dangGui = false;window.VictorDangThaoTac = false;
        }
    }
    // Window nhận sự kiện sau các hộp thoại xác nhận ở document.
    window.addEventListener('submit', function (e) {
        if (e.defaultPrevented || !(e.target instanceof HTMLFormElement)) return;
        var form = e.target, nut = e.submitter;
        var method = ((nut && nut.getAttribute('formmethod')) || form.method || 'get').toUpperCase();
        var url = new URL((nut && nut.getAttribute('formaction')) || form.action || location.href, location.href);
        if (!trangHoTro(location.pathname) || url.origin !== location.origin || form.target && form.target !== '_self' || method === 'DIALOG') return;
        if (method === 'GET' && url.pathname !== location.pathname) return;
        if (method === 'POST' && !(/^\/pos\//.test(url.pathname) || /^\/quan-ly\//.test(url.pathname)
            || /^\/gio-hang\//.test(url.pathname) || /^\/tai-khoan\/dia-chi\//.test(url.pathname)
            || /^\/don-hang\//.test(url.pathname) || url.pathname === '/thanh-toan/ghn/bao-gia')) return;
        e.preventDefault();if (dangGui) return;
        var data = new FormData(form);
        if (nut && nut.name) data.append(nut.name, nut.value);
        var options = {method: method, credentials: 'same-origin', cache: 'no-store', headers: {'X-Victor-Action': '1'}};
        if (method === 'GET') {
            url.search = new URLSearchParams(data).toString();
        } else if ((form.enctype || '').includes('multipart/form-data')) options.body = data;
        else options.body = new URLSearchParams(data);
        gui(url.href, options, form, url.pathname, method === 'GET');
    });
    document.addEventListener('click', function (e) {
        var a = e.target.closest('a[href]');
        if (!a || e.defaultPrevented || e.button !== 0 || e.ctrlKey || e.metaKey || e.shiftKey || e.altKey || a.target || a.hasAttribute('download')) return;
        var url = new URL(a.href, location.href);
        if (!trangHoTro(location.pathname) || url.origin !== location.origin || url.pathname !== location.pathname
            || url.search === location.search
            || a.closest('.account-dropdown,.menu-list,.chuong-panel')) return;
        e.preventDefault();
        gui(url.href, {method: 'GET', credentials: 'same-origin', cache: 'no-store'}, null, url.pathname, true);
    });
    window.addEventListener('popstate', function () {
        // Đổi #san-pham/#lien-he là cuộn trong trang, không tải dữ liệu và kéo vị trí cuộn về chỗ cũ.
        if (location.pathname + location.search === trangDaHienThi) return;
        if (trangHoTro(location.pathname)) gui(location.href, {method: 'GET', cache: 'no-store'}, null, location.pathname, false);
    });
})();
