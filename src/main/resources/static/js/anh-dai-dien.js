(function () {
    'use strict';
    var preview = null, activeInput = null, saved = '', hadAvatar = false, saving = false;
    function clearPreview() {
        if (preview) { URL.revokeObjectURL(preview);preview = null; }
    }
    async function changed() {
        if (saving) return;
        var input = document.getElementById('tepAnhDaiDien'), image = document.getElementById('anhDaiDienKhach');
        if (!input || !image) return;
        var hint = document.getElementById('huongDanAnhDaiDien');
        clearPreview();input.setCustomValidity('');hint.classList.remove('is-error');
        var file = input.files[0];
        if (!file) {
            image.src = saved;image.parentElement.classList.toggle('has-avatar', hadAvatar);
            hint.textContent = '';return;
        }
        if (!/\.(jpe?g|png)$/i.test(file.name) || !['image/jpeg', 'image/png'].includes(file.type) || file.size > 5 * 1024 * 1024) {
            image.src = saved;image.parentElement.classList.toggle('has-avatar', hadAvatar);
            hint.textContent = 'Chọn ảnh JPG/PNG, dung lượng tối đa 5 MB.';hint.classList.add('is-error');
            input.setCustomValidity(hint.textContent);input.reportValidity();return;
        }
        preview = URL.createObjectURL(file);image.src = preview;image.parentElement.classList.add('has-avatar');
        var form = input.form, body = new FormData(form);
        saving = true;input.disabled = true;form.setAttribute('aria-busy', 'true');
        var button = document.getElementById('nutDoiAnhDaiDien');
        if (button) button.disabled = true;
        hint.textContent = 'Đang lưu ảnh…';
        try {
            var response = await fetch(form.action, { method: 'POST', body: body, credentials: 'same-origin', headers: { 'X-Requested-With': 'XMLHttpRequest' } });
            if (!response.ok) throw new Error('Không lưu được ảnh. Vui lòng thử lại.');
            var page = new DOMParser().parseFromString(await response.text(), 'text/html');
            var error = page.querySelector('[data-vs-flash="error"]');
            if (error) throw new Error(error.textContent.trim());
            var uploaded = page.getElementById('anhDaiDienKhach');
            if (!uploaded || !page.querySelector('[data-vs-flash="success"]'))
                throw new Error('Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.');
            saved = uploaded.getAttribute('src');hadAvatar = true;clearPreview();
            image = document.getElementById('anhDaiDienKhach');
            if (image) { image.src = saved;image.parentElement.classList.add('has-avatar'); }
            document.querySelectorAll('[data-avatar-tai-khoan]').forEach(function (avatar) { avatar.src = saved; });
            hint.textContent = 'Đã lưu ảnh đại diện.';input.value = '';
        } catch (error) {
            clearPreview();
            image = document.getElementById('anhDaiDienKhach');
            if (image) { image.src = saved;image.parentElement.classList.toggle('has-avatar', hadAvatar); }
            hint.textContent = error.message || 'Không lưu được ảnh. Vui lòng thử lại.';
            hint.classList.add('is-error');input.value = '';
        } finally {
            saving = false;input.disabled = false;form.removeAttribute('aria-busy');
            if (button) button.disabled = false;
        }
    }
    window.VictorKhoiTaoAnhDaiDien = function () {
        var input = document.getElementById('tepAnhDaiDien'), image = document.getElementById('anhDaiDienKhach');
        if (!input || !image) { clearPreview();activeInput = null;return; }
        if (input === activeInput) {
            if (preview) { image.src = preview;image.parentElement.classList.add('has-avatar'); }
            return;
        }
        clearPreview();activeInput = input;saved = image.getAttribute('src');hadAvatar = image.parentElement.classList.contains('has-avatar');
        input.addEventListener('change', changed);
        var button = document.getElementById('nutDoiAnhDaiDien');
        if (button) button.addEventListener('click', function () { if (!saving) input.click(); });
        input.form.addEventListener('submit', function (event) { event.preventDefault(); });
    };
    document.addEventListener('vs:du-lieu-da-cap-nhat', window.VictorKhoiTaoAnhDaiDien);
    window.addEventListener('pagehide', clearPreview);
    window.VictorKhoiTaoAnhDaiDien();
})();
