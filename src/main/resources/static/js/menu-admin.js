var nutMenuAdmin = document.getElementById('nutAnHienMenu');
var khungAdmin = document.querySelector('.app-shell');
var menuAdmin = document.getElementById('menuAdmin');
if (nutMenuAdmin && khungAdmin) {
    nutMenuAdmin.onclick = function () {
        khungAdmin.classList.toggle('menu-hidden');
        var dangHien = !khungAdmin.classList.contains('menu-hidden');
        if (menuAdmin) { menuAdmin.inert = !dangHien; }
        this.setAttribute('aria-expanded', dangHien);
        this.setAttribute('aria-label', dangHien ? 'Ẩn menu' : 'Hiện menu');
        this.title = dangHien ? 'Ẩn menu' : 'Hiện menu';
    };
}
