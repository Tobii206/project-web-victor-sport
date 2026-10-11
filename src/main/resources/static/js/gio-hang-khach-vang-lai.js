// Khách chưa đăng nhập: chuyển trang giữ giỏ, tải lại trang thì xóa giỏ tạm.
var lanMoTrang = performance.getEntriesByType("navigation")[0];
if (sessionStorage.getItem("gioKhachDaXoa") === "1") {
    sessionStorage.removeItem("gioKhachDaXoa");
} else if (lanMoTrang && lanMoTrang.type === "reload") {
    document.documentElement.style.visibility = "hidden";
    fetch("/gio-hang/xoa-khi-tai-lai", { method: "POST", credentials: "same-origin" })
        .then(function (phanHoi) {
            if (!phanHoi.ok) throw new Error("Chưa xóa được giỏ hàng tạm.");
            // Chỉ xóa một lần, lần tải kế tiếp hiển thị dữ liệu giỏ mới.
            sessionStorage.setItem("gioKhachDaXoa", "1");
            window.location.reload();
        })
        .catch(function () {
            document.documentElement.style.visibility = "";
            alert("Chưa xóa được giỏ hàng tạm. Vui lòng tải lại trang.");
        });
}
