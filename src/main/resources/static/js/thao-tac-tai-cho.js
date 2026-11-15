// Kiểm tra các ô nhập trên cả trang quản lý và trang khách hàng.
(function () {
    'use strict';
    if (window.VictorKiemTraDuLieu) return;
    var states = new WeakMap(), nextId = 0;
    var moneyNames = new Set(['gia_ban', 'gia_tri_giam_gia', 'so_tien_giam_toi_da', 'hoa_don_toi_thieu', 'paidAmount']);
    var lengths = {tuKhoa: 255, keyword: 255, note: 255, email: 100, emailMoi: 100,
        matKhau: 255, matKhauCu: 255};
    var requiredChoices = new Set(['trang_thai', 'trang_thai_kinh_doanh', 'loai_giam_gia',
        'loai_phieu_giam_gia', 'ap_dung_van_chuyen', 'chi_ap_dung_ha_noi', 'paymentMethodId']);
    function state(input) {
        if (!states.has(input)) states.set(input, {message: '', shown: false});
        return states.get(input);
    }
    function named(form, name) { return form && form.elements.namedItem(name); }
    function label(input) {
        var group = input.closest('fieldset');
        var title = input.name === 'sizes' && group ? group.querySelector('legend') : input.labels && input.labels[0];
        return (title ? title.textContent : input.getAttribute('aria-label') || 'Thông tin')
            .replace(/\*/g, '').replace(/\s+/g, ' ').trim();
    }
    function configure(input) {
        if (input.tagName === 'SELECT' && input.form.method === 'post' && requiredChoices.has(input.name)) input.required = true;
        if (lengths[input.name] && !input.hasAttribute('maxlength')) input.maxLength = lengths[input.name];
        if (input.type === 'tel') {
            input.inputMode = 'numeric';
            if (!input.hasAttribute('pattern')) input.pattern = '0[0-9]{9}';
            if (!input.hasAttribute('maxlength')) input.maxLength = 10;
        }
        if (input.type === 'number' && !input.hasAttribute('max'))
            input.max = moneyNames.has(input.name) ? '9999999999999999.99' : '2147483647';
        if (input.type === 'date') {
            if (!input.min) input.min = '0001-01-01';
            if (input.name === 'ngay_sinh' || input.name === 'ngaySinh') {
                var today = new Date();
                input.max = today.getFullYear() + '-' + String(today.getMonth() + 1).padStart(2, '0')
                    + '-' + String(today.getDate()).padStart(2, '0');
            }
        }
        if (input.name === 'sizes' && input.form.hasAttribute('data-min-checked')) {
            var boxes = Array.from(input.form.querySelectorAll('input[name="sizes"]:enabled'));
            input.required = input === boxes[0] && !boxes.some(function (box) { return box.checked; });
        }
    }
    function message(input) {
        var value = input.value, title = label(input), form = input.form;
        if (input.required && !/^(radio|checkbox|file)$/.test(input.type) && !value.trim())
            return (input.tagName === 'SELECT' ? 'Vui lòng chọn ' : 'Vui lòng nhập ') + title.toLowerCase() + '.';
        if (input.type === 'checkbox' && input.required && !input.checked) return 'Vui lòng chọn ít nhất một size.';
        if (input.type === 'password' && value) {
            if (!value.trim()) return 'Mật khẩu không được chỉ chứa khoảng trắng.';
            if (input.autocomplete === 'new-password' && !new URL(form.getAttribute('action') || location.href, location.href).pathname.endsWith('/login')
                && new TextEncoder().encode(value).length > 72)
                return 'Mật khẩu tối đa 72 byte. Ký tự có dấu có thể chiếm nhiều byte.';
            if (input.name === 'nhapLai' && named(form, 'matKhau') && value !== named(form, 'matKhau').value)
                return 'Mật khẩu nhập lại không khớp.';
            if (input.name === 'matKhau' && named(form, 'matKhauCu') && value === named(form, 'matKhauCu').value)
                return 'Mật khẩu mới phải khác mật khẩu hiện tại.';
        }
        if (value && input.type === 'email' && !/^[a-z0-9]+([._%+\-][a-z0-9]+)*@gmail\.com$/i.test(value.trim()))
            return 'Email phải có dạng ten@gmail.com và tối đa 100 ký tự.';
        if (value && input.name === 'ten_tai_khoan' && value.includes('@')
            && !/^[a-z0-9]+([._%+\-][a-z0-9]+)*@gmail\.com$/i.test(value.trim()))
            return 'Nếu dùng email làm tên tài khoản, hãy nhập email có dạng ten@gmail.com.';
        if (input.maxLength >= 0 && value.length > input.maxLength) return title + ' tối đa ' + input.maxLength + ' ký tự.';
        if (value && input.minLength > 0 && value.length < input.minLength)
            return title + ' cần ít nhất ' + input.minLength + ' ký tự.';
        if (value && moneyNames.has(input.name) && !/^[0-9]{1,16}(\.[0-9]{1,2})?$/.test(value))
            return title + ' phải là số không âm, tối đa 16 chữ số và 2 chữ số thập phân.';
        if (input.name === 'gia_tri_giam_gia' && value) {
            if (Number(value) <= 0) return 'Giá trị giảm phải lớn hơn 0.';
            var kind = named(form, 'loai_giam_gia') || named(form, 'loai_phieu_giam_gia');
            if (kind && kind.value === '0' && Number(value) > 100) return 'Giảm theo phần trăm không được vượt quá 100%.';
        }
        var start = named(form, 'ngay_bat_dau'), end = named(form, 'ngay_ket_thuc');
        if (input === end && start && start.value && value && value < start.value)
            return 'Ngày kết thúc phải từ ngày bắt đầu trở đi.';
        start = named(form, 'tuNgay');end = named(form, 'denNgay');
        if (input === end && start && start.value && value && value < start.value)
            return 'Đến ngày phải từ Từ ngày trở đi.';
        if (input.type === 'file' && input.files.length && /image\/(jpeg|png)/.test(input.accept)) {
            var file = input.files[0];
            if (!/\.(jpe?g|png)$/i.test(file.name) || !file.size || file.size > 5 * 1024 * 1024)
                return 'Chọn ảnh JPG hoặc PNG có dung lượng lớn hơn 0 và tối đa 5 MB.';
        }
        var validity = input.validity;
        if (validity.valueMissing) return input.type === 'file' ? 'Vui lòng chọn ảnh.' : 'Vui lòng chọn ' + title.toLowerCase() + '.';
        if (validity.badInput || validity.typeMismatch) return title + ' chưa đúng định dạng.';
        if (validity.patternMismatch) return input.type === 'tel' ? 'Số điện thoại phải đủ 10 số và bắt đầu bằng 0.'
            : input.title || title + ' chưa đúng định dạng.';
        if (validity.tooShort) return title + ' cần ít nhất ' + input.minLength + ' ký tự.';
        if (validity.rangeUnderflow) return title + ' phải từ ' + input.min + ' trở lên.';
        if (validity.rangeOverflow) return input.name === 'ngay_sinh' || input.name === 'ngaySinh'
            ? 'Ngày sinh không được ở tương lai.' : title + ' không được vượt quá ' + input.max + '.';
        if (validity.stepMismatch) return input.step === '1' || !input.step
            ? title + ' phải là số nguyên.' : title + ' chỉ nhận tối đa 2 chữ số thập phân.';
        return '';
    }
    function display(input, text) {
        var info = state(input);
        input.classList.toggle('vs-field-invalid', !!text);
        if (text) input.setAttribute('aria-invalid', 'true');
        else input.removeAttribute('aria-invalid');
        if (!info.error && !text) return;
        if (!info.error || !info.error.isConnected) {
            var error = document.createElement('small');
            error.id = 'vs-field-error-' + (++nextId);error.className = 'vs-field-error';
            var anchor = input.type === 'checkbox' ? input.closest('fieldset') || input : input;
            anchor.insertAdjacentElement('afterend', error);
            var described = (input.getAttribute('aria-describedby') || '').split(/\s+/).filter(Boolean);
            if (info.error) described = described.filter(function (id) { return id !== info.error.id; });
            described.push(error.id);input.setAttribute('aria-describedby', described.join(' '));
            info.error = error;
        }
        info.error.textContent = text;info.error.hidden = !text;
    }
    function validate(input, show) {
        if (!input.matches || !input.matches('input:not([type="hidden"]),select,textarea')) return true;
        configure(input);
        var info = state(input);
        if (info.message && (!input.willValidate || input.validationMessage === info.message)) input.setCustomValidity('');
        info.message = '';
        if (!input.willValidate) { if (info.shown) display(input, '');return true; }
        // Giữ thông báo kiểm tra tiền khách đưa và tải ảnh của từng trang.
        if (!input.validity.customError) {
            info.message = message(input);
            input.setCustomValidity(info.message);
        }
        if (show) info.shown = true;
        if (info.shown) display(input, input.validationMessage);
        return input.validity.valid;
    }
    function validateForm(form, show) {
        var valid = true;
        Array.from(form.elements).forEach(function (input) { if (!validate(input, show)) valid = false; });
        return valid;
    }
    function init() { document.querySelectorAll('form').forEach(function (form) { validateForm(form, false); }); }
    window.VictorKiemTraDuLieu = init;
    document.addEventListener('invalid', function (event) { validate(event.target, true); }, true);
    document.addEventListener('focusout', function (event) { if (event.target.form) validate(event.target, true); });
    function edited(event) { if (event.target.form) validateForm(event.target.form, false); }
    document.addEventListener('input', edited);
    document.addEventListener('change', edited);
    // Cập nhật lại các giá trị được trình duyệt tự điền trước khi kiểm tra HTML.
    document.addEventListener('click', function (event) {
        var button = event.target.closest('button,input[type="submit"],input[type="image"]');
        if (button && button.form && /^(submit|image)$/.test(button.type)
            && !button.formNoValidate && !button.form.noValidate) validateForm(button.form, false);
    }, true);
    document.addEventListener('keydown', function (event) {
        if (event.key === 'Enter' && !event.isComposing && event.target.form
            && event.target.tagName !== 'TEXTAREA' && !event.target.form.noValidate)
            validateForm(event.target.form, false);
    }, true);
    document.addEventListener('submit', function (event) {
        if (event.target.noValidate || event.submitter && event.submitter.formNoValidate) return;
        if (validateForm(event.target, true)) return;
        event.preventDefault();event.stopImmediatePropagation();event.target.reportValidity();
    }, true);
    document.addEventListener('vs:du-lieu-da-cap-nhat', init);
    if (document.readyState === 'loading') document.addEventListener('DOMContentLoaded', init, {once: true});
    else init();
})();

(function () {
    'use strict';
    if (window.VictorThaoTacTaiCho) return;
    window.VictorThaoTacTaiCho = true;
    var dangGui = false, formDaNhap = new WeakSet();
    var trangDaHienThi = location.pathname + location.search;
    function khoa(form) {
        var action = new URL(form.getAttribute('action') || location.href, location.href).href;
        if (new URL(action).pathname === '/pos/checkout') return action;
        return action + '|' + Array.from(form.querySelectorAll('input[type="hidden"]'))
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
        var trangThaiAnh = form && form.querySelector('[data-trang-thai-anh]');
        if (trangThaiAnh) {
            trangThaiAnh.textContent = 'Đang lưu ảnh…';
            document.querySelectorAll('[data-tu-luu-anh-mau] input[type="file"]:enabled').forEach(function (input) {
                if (!nut.includes(input)) nut.push(input);
            });
        }
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
            var yeuCau = new URL(url, location.href);
            var formMau = options.method === 'GET' && /^\/quan-ly\/san-pham\/\d+$/.test(dich.pathname)
                && yeuCau.hash === '#form-mau-giay' && document.getElementById('form-mau-giay');
            if (formMau) {
                var dauTrang = document.querySelector('.topbar');
                var leTren = (dauTrang ? dauTrang.getBoundingClientRect().height : 0) + 18;
                var viTriForm = window.scrollY + formMau.getBoundingClientRect().top - leTren;
                formMau.focus({preventScroll: true});
                window.scrollTo({left: 0, top: Math.max(0, viTriForm),
                    behavior: window.matchMedia('(prefers-reduced-motion: reduce)').matches ? 'instant' : 'smooth'});
            } else window.scrollTo({left: x, top: y, behavior: 'instant'});
        } catch (e) {
            // Không tự gửi lại POST: máy chủ có thể đã ghi dữ liệu trước khi mất kết nối.
            thongBao(options.method === 'POST'
                ? 'Chưa nhận được kết quả. Hãy kiểm tra dữ liệu trước khi thực hiện lại để tránh thao tác trùng.'
                : 'Chưa tải được dữ liệu. Bạn có thể thử lại.');
        } finally {
            nut.forEach(function (b) { b.disabled = false; });
            if (trangThaiAnh && trangThaiAnh.isConnected) {
                trangThaiAnh.textContent = '';
                form.elements.namedItem('tepAnh').value = '';
            }
            if (vungCu) vungCu.removeAttribute('aria-busy');
            var main = document.querySelector('main');if (main) main.removeAttribute('aria-busy');
            thanh.remove();dangGui = false;window.VictorDangThaoTac = false;
        }
    }
    document.addEventListener('change', function (event) {
        var size = event.target;
        if (!size.matches('input[data-ton-theo-size]')) return;
        var vungTon = document.getElementById(size.getAttribute('aria-controls'));
        if (!vungTon) return;
        var oTon = vungTon.querySelector('input');
        vungTon.hidden = !size.checked;
        oTon.disabled = !size.checked;
        size.setAttribute('aria-expanded', String(size.checked));
        if (size.checked) oTon.focus({preventScroll: true});
    });
    document.addEventListener('change', function (event) {
        var input = event.target, form = input.form;
        if (!form || !form.hasAttribute('data-tu-luu-anh-mau') || input.type !== 'file' || !input.files.length) return;
        var file = input.files[0], trangThai = form.querySelector('[data-trang-thai-anh]');
        if (dangGui || !/\.(jpe?g|png)$/i.test(file.name) || !file.size || file.size > 5 * 1024 * 1024) {
            trangThai.textContent = dangGui ? 'Đang xử lý thao tác khác. Vui lòng chọn lại ảnh sau.'
                : 'Chọn ảnh JPG hoặc PNG có dung lượng tối đa 5 MB.';
            input.value = '';
            return;
        }
        form.requestSubmit();
    });
    // Window nhận sự kiện sau các hộp thoại xác nhận ở document.
    window.addEventListener('submit', function (e) {
        if (e.defaultPrevented || !(e.target instanceof HTMLFormElement)) return;
        var form = e.target, nut = e.submitter;
        var method = ((nut && nut.getAttribute('formmethod')) || form.method || 'get').toUpperCase();
        var url = new URL((nut && nut.getAttribute('formaction')) || form.getAttribute('action') || location.href, location.href);
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
