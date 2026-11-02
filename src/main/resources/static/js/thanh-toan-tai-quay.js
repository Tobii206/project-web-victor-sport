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
        var dung = daThu !== null && canThu !== null && daThu === canThu;
        input.setCustomValidity(dung || !input.value ? '' : 'Tiền khách đã thanh toán phải đúng bằng tổng tiền cần thu.');
        return dung;
    }
    document.addEventListener('input', function (event) {
        if (event.target.id === 'paidAmount') kiemTra(event.target);
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
        var input = document.getElementById('paidAmount');
        if (input) kiemTra(input);
    });
})();
