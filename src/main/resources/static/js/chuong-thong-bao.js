(function () {
    function khoiTao() {
        document.querySelectorAll('.vs-bell').forEach(function (box) {
            if (box.dataset.ready) return;
            box.dataset.ready = 'true';
            var button = box.querySelector('.vs-bell-button'), panel = box.querySelector('.vs-bell-panel');
            var count = box.querySelector('.vs-bell-count'), list = box.querySelector('.vs-bell-list');
            var status = box.querySelector('.vs-bell-status'), readAll = box.querySelector('.vs-bell-read-all');
            var retry = box.querySelector('.vs-bell-retry'), csrf = '', maxId = 0, loading = false, saving = false, stopped = false, canTaiLai = false;
            var url = '/thong-bao-don-hang?kenh=' + encodeURIComponent(box.dataset.kenh);
            function dong() { panel.hidden = true;button.setAttribute('aria-expanded', 'false'); }
            async function doc(id, tatCa) {
                var r = await fetch('/thong-bao-don-hang/da-doc', {method: 'POST', headers: {'X-CSRF-Chuong': csrf, 'Content-Type': 'application/x-www-form-urlencoded'},
                    body: new URLSearchParams({kenh: box.dataset.kenh, id: id, tatCa: tatCa})});
                if (!r.ok) throw new Error('Chưa đánh dấu được thông báo. Vui lòng thử lại.');
            }
            function hienThi(data) {
                csrf = data.csrf;maxId = 0;
                count.hidden = data.chuaDoc === 0;count.textContent = data.chuaDoc > 99 ? '99+' : data.chuaDoc;
                button.setAttribute('aria-label', 'Thông báo đơn hàng, ' + data.chuaDoc + ' chưa đọc');
                readAll.disabled = !data.chuaDoc;
                list.replaceChildren();status.hidden = data.danhSach.length > 0;status.textContent = 'Chưa có thông báo đơn hàng.';retry.hidden = true;
                var trangThai = {1: 'Đơn hàng mới', 2: 'Đã xác nhận đơn', 3: 'Đang chuẩn bị hàng', 4: 'Đang vận chuyển', 5: 'Đơn hàng hoàn thành', 6: 'Đơn hàng đã hủy'};
                data.danhSach.forEach(function (dong) {
                    maxId = Math.max(maxId, Number(dong.id));
                    var a = document.createElement('a');a.className = 'vs-bell-item' + (dong.da_doc ? '' : ' unread');
                    var prefix = box.dataset.kenh === 'admin' ? '/quan-ly/hoa-don/' : '/don-hang/';
                    a.href = prefix + Number(dong.id_hoa_don);
                    var title = document.createElement('strong');title.textContent = dong.ma_hoa_don + ' · ' + (trangThai[dong.trang_thai] || 'Cập nhật đơn hàng');
                    var text = document.createElement('p');text.textContent = dong.ghi_chu || 'Xem chi tiết đơn hàng.';
                    var time = document.createElement('time');time.textContent = dong.thoi_gian ? dong.thoi_gian.replace('T', ' ') : '';
                    a.append(title, text, time);
                    a.addEventListener('click', async function (e) {
                        if (e.ctrlKey || e.metaKey || e.shiftKey || e.altKey || dong.da_doc) return;
                        e.preventDefault();if (saving) return;saving = true;
                        try { await doc(dong.id, false);window.location.assign(a.href); }
                        catch (error) { status.hidden = false;status.textContent = error.message; }
                        finally { saving = false; }
                    });
                    list.appendChild(a);
                });
            }
            async function tai() {
                if (stopped || document.hidden) return;
                if (loading || saving) { canTaiLai = true;return; }
                loading = true;
                try {
                    var r = await fetch(url, {cache: 'no-store', headers: {'Accept': 'application/json'}});
                    if (r.status === 401 || r.status === 403) { stopped = true;count.hidden = true;readAll.disabled = true;list.replaceChildren();throw new Error('Phiên đăng nhập đã thay đổi. Vui lòng tải lại trang.'); }
                    if (!r.ok) throw new Error('Chưa tải được thông báo. Vui lòng thử lại.');
                    hienThi(await r.json());
                } catch (error) { status.hidden = false;status.textContent = error.message;retry.hidden = stopped; }
                finally { loading = false;if (canTaiLai) { canTaiLai = false;tai(); } }
            }
            button.addEventListener('click', function () { panel.hidden = !panel.hidden;button.setAttribute('aria-expanded', String(!panel.hidden));if (!panel.hidden) tai(); });
            document.addEventListener('click', function (e) { if (!box.contains(e.target)) dong(); });
            box.addEventListener('keydown', function (e) { if (e.key === 'Escape') { dong();button.focus(); } });
            readAll.addEventListener('click', async function () {
                if (saving || !maxId) return;saving = true;readAll.disabled = true;
                try { await doc(maxId, true); }
                catch (e) { status.hidden = false;status.textContent = e.message; }
                finally { saving = false;tai(); }
            });
            retry.addEventListener('click', tai);
            document.addEventListener('visibilitychange', function () { if (!document.hidden) tai(); });
            window.addEventListener('focus', tai);
            document.addEventListener('vs:chuong', tai);
            tai();
        });
    }
    if (document.readyState === 'loading') document.addEventListener('DOMContentLoaded', khoiTao);
    else khoiTao();
})();
