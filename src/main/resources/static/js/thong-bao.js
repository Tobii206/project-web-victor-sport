(function () {
    'use strict';
    if (window.VictorThongBao) return;

    var hangDoi = [], dangMo = null, dialog;
    var daXacNhan = new WeakSet(), dangCho = new WeakSet();

    function taoHopThoai() {
        if (dialog) return;
        dialog = document.createElement('dialog');
        dialog.className = 'vs-notice';
        dialog.setAttribute('aria-labelledby', 'vsNoticeTitle');
        dialog.setAttribute('aria-describedby', 'vsNoticeMessage');
        dialog.innerHTML = '<button type="button" class="vs-notice-close" aria-label="Đóng">×</button>' +
            '<div class="vs-notice-icon" aria-hidden="true"><svg viewBox="0 0 24 24" fill="none"><path d="M12 8v5m0 3v.01"/><circle cx="12" cy="12" r="9"/></svg></div>' +
            '<p class="vs-notice-brand">VICTORSPORT</p><h2 id="vsNoticeTitle"></h2>' +
            '<p id="vsNoticeMessage" class="vs-notice-message"></p>' +
            '<div class="vs-notice-actions"><button type="button" class="vs-notice-cancel">Hủy</button>' +
            '<button type="button" class="vs-notice-accept"></button></div>';
        document.body.appendChild(dialog);
        dialog.querySelector('.vs-notice-close').onclick = function () { dialog.close('huy'); };
        dialog.querySelector('.vs-notice-cancel').onclick = function () { dialog.close('huy'); };
        dialog.querySelector('.vs-notice-accept').onclick = function () { dialog.close('dong-y'); };
        dialog.addEventListener('cancel', function (event) { event.preventDefault(); dialog.close('huy'); });
        dialog.addEventListener('click', function (event) {
            var rect = dialog.getBoundingClientRect();
            if (event.target === dialog && (event.clientX < rect.left || event.clientX > rect.right || event.clientY < rect.top || event.clientY > rect.bottom)) dialog.close('huy');
        });
        dialog.addEventListener('close', function () {
            var yeuCau = dangMo;
            if (!yeuCau) return;
            dangMo = null;
            var dongY = dialog.returnValue === 'dong-y';
            if (yeuCau.nutCu && yeuCau.nutCu.isConnected) yeuCau.nutCu.focus();
            yeuCau.hoanThanh(dongY);
            // Đợi thao tác hiện tại xử lý xong rồi mở thông báo tiếp theo.
            window.setTimeout(moTiep, 0);
        });
    }
    function moTiep() {
        if (dangMo || !hangDoi.length) return;
        if (!document.body) { document.addEventListener('DOMContentLoaded', moTiep, {once: true}); return; }
        taoHopThoai();
        dangMo = hangDoi.shift();
        dangMo.nutCu = document.activeElement;
        dialog.dataset.tone = dangMo.kieu || 'normal';
        dialog.querySelector('.vs-notice-icon path').setAttribute('d', dangMo.kieu === 'success' ? 'm8 12 3 3 5-6' : 'M12 8v5m0 3v.01');
        dialog.querySelector('#vsNoticeTitle').textContent = dangMo.tieuDe || 'Xác nhận thao tác';
        dialog.querySelector('#vsNoticeMessage').textContent = dangMo.noiDung;
        dialog.querySelector('.vs-notice-accept').textContent = dangMo.nhanNut || 'Xác nhận';
        var huy = dialog.querySelector('.vs-notice-cancel');
        huy.hidden = !!dangMo.chiThongBao;
        dialog.returnValue = '';
        dialog.showModal();
        (dangMo.chiThongBao ? dialog.querySelector('.vs-notice-accept') : huy).focus();
    }
    function mo(cauHinh) {
        return new Promise(function (resolve) {
            cauHinh.hoanThanh = resolve;
            hangDoi.push(cauHinh);
            moTiep();
        });
    }
    window.VictorThongBao = {
        xacNhan: function (cauHinh) { return mo(cauHinh); },
        thongBao: function (noiDung, tieuDe, kieu) {
            return mo({noiDung: noiDung, tieuDe: tieuDe || 'Thông báo', nhanNut: 'Đã hiểu', chiThongBao: true, kieu: kieu || 'normal'});
        }
    };
    function hienKetQua() {
        var daHien = new Set();
        document.querySelectorAll('[data-vs-flash]').forEach(function (node) {
            if (node.dataset.vsDaHien) return;
            node.dataset.vsDaHien = 'true';
            var noiDung = node.textContent.trim();
            var kieu = node.dataset.vsFlash;
            var khoa = kieu + ':' + noiDung;
            if (!noiDung || daHien.has(khoa)) return;
            daHien.add(khoa);
            window.VictorThongBao.thongBao(noiDung, kieu === 'success' ? 'Thành công' : 'Không thể thực hiện', kieu === 'success' ? 'success' : 'danger');
        });
    }
    if (document.readyState === 'loading') document.addEventListener('DOMContentLoaded', hienKetQua, {once: true});
    else hienKetQua();

    document.addEventListener('submit', function (event) {
        var form = event.target;
        if (!(form instanceof HTMLFormElement) || !form.hasAttribute('data-vs-confirm')) return;
        if (daXacNhan.has(form)) { daXacNhan.delete(form); return; }
        if (event.defaultPrevented) return;
        event.preventDefault();
        if (dangCho.has(form)) return;
        dangCho.add(form);
        var nut = event.submitter;
        mo({noiDung: form.dataset.vsConfirm, tieuDe: form.dataset.vsTitle,
            nhanNut: form.dataset.vsButton, kieu: form.dataset.vsTone})
            .then(function (dongY) {
                if (!dongY || !form.isConnected) return;
                daXacNhan.add(form);
                try {
                    // Giữ nguyên action, dữ liệu và nút submit đã chọn; vẫn kiểm tra form.
                    form.requestSubmit(nut && nut.form === form && !nut.disabled ? nut : undefined);
                } finally { daXacNhan.delete(form); }
            }).finally(function () { dangCho.delete(form); });
    });
})();
