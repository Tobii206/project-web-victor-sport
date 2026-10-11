var menuTaiKhoan = document.getElementById('menuTaiKhoan');
var nutMenuTaiKhoan = document.getElementById('nutMenuTaiKhoan');
var cacMucTaiKhoan = document.getElementById('cacMucTaiKhoan');

function dongMenuTaiKhoan() {
    menuTaiKhoan.classList.remove('is-open');
    nutMenuTaiKhoan.setAttribute('aria-expanded', 'false');
    cacMucTaiKhoan.inert = true;
}

if (menuTaiKhoan && nutMenuTaiKhoan && cacMucTaiKhoan) {
    nutMenuTaiKhoan.onclick = function () {
        var dangMo = menuTaiKhoan.classList.toggle('is-open');
        this.setAttribute('aria-expanded', dangMo);
        cacMucTaiKhoan.inert = !dangMo;
    };
    document.addEventListener('click', function (suKien) {
        if (!menuTaiKhoan.contains(suKien.target)) { dongMenuTaiKhoan(); }
    });
    document.addEventListener('keydown', function (suKien) {
        if (suKien.key === 'Escape' && menuTaiKhoan.classList.contains('is-open')) {
            dongMenuTaiKhoan();
            nutMenuTaiKhoan.focus();
        }
    });
}
