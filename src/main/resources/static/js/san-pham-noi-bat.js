var khungNoiBat = document.querySelector(".shop-featured");
var cacSanPham = document.querySelectorAll(".shop-featured-slide");
var sanPhamDangHien = 0;

if (cacSanPham.length > 1) {
    setInterval(function () {
        // Dừng khi đang xem hoặc bấm vào sản phẩm.
        if (document.hidden || khungNoiBat.matches(":hover") || khungNoiBat.contains(document.activeElement)) {
            return;
        }

        cacSanPham[sanPhamDangHien].classList.remove("active");
        cacSanPham[sanPhamDangHien].setAttribute("aria-hidden", "true");
        cacSanPham[sanPhamDangHien].inert = true;

        sanPhamDangHien++;
        if (sanPhamDangHien >= cacSanPham.length) {
            sanPhamDangHien = 0;
        }

        cacSanPham[sanPhamDangHien].classList.add("active");
        cacSanPham[sanPhamDangHien].setAttribute("aria-hidden", "false");
        cacSanPham[sanPhamDangHien].inert = false;
    }, 4000);
}
