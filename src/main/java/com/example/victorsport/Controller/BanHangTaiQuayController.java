package com.example.victorsport.Controller;

import com.example.victorsport.Dto.GioHangTaiQuay;
import com.example.victorsport.Dto.NguoiDungDangNhap;
import com.example.victorsport.Dto.SanPhamTrongGio;
import com.example.victorsport.Service.BanHangTaiQuayService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class BanHangTaiQuayController {
    private static final String KHOA_GIO_HANG_TAI_QUAY = "gioHangTaiQuay";
    private static final String KHOA_PHIEU_GIAM_GIA = "idPhieuGiamGiaTaiQuay";

    private final BanHangTaiQuayService banHangTaiQuayService;

    public BanHangTaiQuayController(BanHangTaiQuayService banHangTaiQuayService) {
        this.banHangTaiQuayService = banHangTaiQuayService;
    }

    @GetMapping("/pos")
    public String hienThiBanHangTaiQuay(@RequestParam(value = "keyword", required = false) String tuKhoaSanPham,
                                        @RequestParam(value = "customerKeyword", required = false) String tuKhoaKhachHang,
                                        HttpSession phienDangNhap,
                                        Model model) {
        NguoiDungDangNhap nguoiDungDangNhap = DangNhapController.layNguoiDungDangNhap(phienDangNhap, model);
        if (nguoiDungDangNhap == null) {
            return "redirect:/login";
        }

        GioHangTaiQuay gioHang = layGioHang(phienDangNhap);
        model.addAttribute("activeMenu", "ban-hang");
        model.addAttribute("keyword", tuKhoaSanPham == null ? "" : tuKhoaSanPham);
        model.addAttribute("customerKeyword", tuKhoaKhachHang == null ? "" : tuKhoaKhachHang);
        model.addAttribute("products", banHangTaiQuayService.timSanPham(tuKhoaSanPham));
        model.addAttribute("customers", banHangTaiQuayService.timKhachHang(tuKhoaKhachHang));
        model.addAttribute("paymentMethods", banHangTaiQuayService.layPhuongThucThanhToan());
        model.addAttribute("discountCoupons", banHangTaiQuayService.layPhieuGiamGiaKhaDung(gioHang.getTamTinh()));
        model.addAttribute("selectedDiscountCouponId", layIdPhieuGiamGia(phienDangNhap));
        model.addAttribute("cart", gioHang);
        model.addAttribute("summary", banHangTaiQuayService.tinhTongKet(gioHang, layIdPhieuGiamGia(phienDangNhap)));
        return "pos";
    }

    @PostMapping("/pos/phieu-giam-gia/chon")
    public String chonPhieuGiamGia(@RequestParam(value = "discountCouponId", required = false) Integer idPhieuGiamGia,
                                   HttpSession phienDangNhap,
                                   RedirectAttributes thuocTinhChuyenHuong) {
        if (!daDangNhap(phienDangNhap)) {
            return "redirect:/login";
        }

        if (idPhieuGiamGia == null || idPhieuGiamGia <= 0) {
            phienDangNhap.removeAttribute(KHOA_PHIEU_GIAM_GIA);
            thuocTinhChuyenHuong.addFlashAttribute("success", "Đã chọn chế độ tự động áp dụng phiếu tốt nhất.");
        } else {
            phienDangNhap.setAttribute(KHOA_PHIEU_GIAM_GIA, idPhieuGiamGia);
            thuocTinhChuyenHuong.addFlashAttribute("success", "Đã chọn phiếu giảm giá thủ công.");
        }
        return "redirect:/pos";
    }

    @PostMapping("/pos/cart/add")
    public String themVaoGio(@RequestParam("detailId") Integer idChiTietSanPham,
                             @RequestParam(value = "quantity", defaultValue = "1") Integer soLuong,
                             HttpSession phienDangNhap,
                             RedirectAttributes thuocTinhChuyenHuong) {
        if (!daDangNhap(phienDangNhap)) {
            return "redirect:/login";
        }

        SanPhamTrongGio sanPham = banHangTaiQuayService.taoSanPhamTrongGio(idChiTietSanPham, soLuong);
        if (sanPham == null) {
            thuocTinhChuyenHuong.addFlashAttribute("error", "Sản phẩm không tồn tại hoặc đã ngừng bán.");
            return "redirect:/pos";
        }
        if (sanPham.getSoLuongTon() <= 0) {
            thuocTinhChuyenHuong.addFlashAttribute("error", "Sản phẩm đã hết hàng. Vui lòng gợi ý sản phẩm khác cho khách.");
            return "redirect:/pos";
        }

        layGioHang(phienDangNhap).themHoacCapNhat(sanPham);
        thuocTinhChuyenHuong.addFlashAttribute("success", "Đã thêm sản phẩm vào giỏ bán hàng.");
        return "redirect:/pos";
    }

    @PostMapping("/pos/cart/update")
    public String capNhatGio(@RequestParam("detailId") Integer idChiTietSanPham,
                             @RequestParam("quantity") Integer soLuong,
                             HttpSession phienDangNhap,
                             RedirectAttributes thuocTinhChuyenHuong) {
        if (!daDangNhap(phienDangNhap)) {
            return "redirect:/login";
        }

        layGioHang(phienDangNhap).capNhatSoLuong(idChiTietSanPham, soLuong);
        thuocTinhChuyenHuong.addFlashAttribute("success", "Đã cập nhật giỏ hàng.");
        return "redirect:/pos";
    }

    @PostMapping("/pos/cart/remove")
    public String xoaKhoiGio(@RequestParam("detailId") Integer idChiTietSanPham,
                             HttpSession phienDangNhap,
                             RedirectAttributes thuocTinhChuyenHuong) {
        if (!daDangNhap(phienDangNhap)) {
            return "redirect:/login";
        }

        layGioHang(phienDangNhap).xoaSanPham(idChiTietSanPham);
        thuocTinhChuyenHuong.addFlashAttribute("success", "Đã xóa sản phẩm khỏi giỏ hàng.");
        return "redirect:/pos";
    }

    @PostMapping("/pos/cart/clear")
    public String xoaGioHang(HttpSession phienDangNhap, RedirectAttributes thuocTinhChuyenHuong) {
        if (!daDangNhap(phienDangNhap)) {
            return "redirect:/login";
        }

        layGioHang(phienDangNhap).xoaTatCa();
        thuocTinhChuyenHuong.addFlashAttribute("success", "Đã hủy giỏ bán hàng hiện tại.");
        return "redirect:/pos";
    }

    @PostMapping("/pos/checkout")
    public String thanhToan(@RequestParam(value = "customerId", required = false) Integer idKhachHang,
                            @RequestParam(value = "customerName", required = false) String tenKhachHang,
                            @RequestParam(value = "customerPhone", required = false) String soDienThoai,
                            @RequestParam(value = "customerEmail", required = false) String email,
                            @RequestParam(value = "customerAddress", required = false) String diaChi,
                            @RequestParam(value = "paymentMethodId", required = false) Integer idPhuongThucThanhToan,
                            @RequestParam(value = "note", required = false) String ghiChu,
                            HttpSession phienDangNhap,
                            Model model,
                            RedirectAttributes thuocTinhChuyenHuong) {
        NguoiDungDangNhap nguoiDungDangNhap = DangNhapController.layNguoiDungDangNhap(phienDangNhap, model);
        if (nguoiDungDangNhap == null) {
            return "redirect:/login";
        }

        try {
            Integer idHoaDon = banHangTaiQuayService.thanhToan(
                    layGioHang(phienDangNhap),
                    new BanHangTaiQuayService.YeuCauThanhToan(idKhachHang, tenKhachHang, soDienThoai, email,
                            diaChi, idPhuongThucThanhToan, ghiChu, layIdPhieuGiamGia(phienDangNhap)),
                    nguoiDungDangNhap
            );
            layGioHang(phienDangNhap).xoaTatCa();
            phienDangNhap.removeAttribute(KHOA_PHIEU_GIAM_GIA);
            thuocTinhChuyenHuong.addFlashAttribute("success", "Thanh toán thành công. Mã hóa đơn nội bộ: #" + idHoaDon);
        } catch (IllegalArgumentException loi) {
            thuocTinhChuyenHuong.addFlashAttribute("error", loi.getMessage());
        }
        return "redirect:/pos";
    }

    private GioHangTaiQuay layGioHang(HttpSession phienDangNhap) {
        Object gioHangHienTai = phienDangNhap.getAttribute(KHOA_GIO_HANG_TAI_QUAY);
        if (gioHangHienTai instanceof GioHangTaiQuay) {
            return (GioHangTaiQuay) gioHangHienTai;
        }
        GioHangTaiQuay gioHang = new GioHangTaiQuay();
        phienDangNhap.setAttribute(KHOA_GIO_HANG_TAI_QUAY, gioHang);
        return gioHang;
    }

    private boolean daDangNhap(HttpSession phienDangNhap) {
        return phienDangNhap.getAttribute(DangNhapController.KHOA_NGUOI_DUNG_DANG_NHAP) instanceof NguoiDungDangNhap;
    }

    private Integer layIdPhieuGiamGia(HttpSession phienDangNhap) {
        Object idPhieuGiamGia = phienDangNhap.getAttribute(KHOA_PHIEU_GIAM_GIA);
        if (idPhieuGiamGia instanceof Integer) {
            return (Integer) idPhieuGiamGia;
        }
        return null;
    }
}
