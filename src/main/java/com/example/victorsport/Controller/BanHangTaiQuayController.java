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

import java.util.List;
import java.util.Map;

@Controller
public class BanHangTaiQuayController {
    private static final String KHOA_GIO_HANG_TAI_QUAY = "gioHangTaiQuay";
    private static final String KHOA_PHIEU_GIAM_GIA = "idPhieuGiamGiaTaiQuay";
    private static final String KHOA_DON_CHO = "idDonChoTaiQuay";
    private static final String KHOA_KHACH_HANG = "idKhachHangTaiQuay";

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
        List<Map<String, Object>> donCho = banHangTaiQuayService.layDanhSachDonCho(nguoiDungDangNhap.getId());
        model.addAttribute("danhSachDonCho", donCho);
        model.addAttribute("soDonCho", donCho.size());
        Integer idDonCho = layIdDonCho(phienDangNhap);
        if (idDonCho != null) {
            try {
                model.addAttribute("donChoDangChon", banHangTaiQuayService.layDonCho(idDonCho, nguoiDungDangNhap.getId()));
            } catch (IllegalArgumentException loi) {
                model.addAttribute("error", loi.getMessage());
                model.addAttribute("donChoDaMat", true);
            }
        }
        model.addAttribute("activeMenu", "ban-hang");
        model.addAttribute("keyword", tuKhoaSanPham == null ? "" : tuKhoaSanPham);
        model.addAttribute("customerKeyword", tuKhoaKhachHang == null ? "" : tuKhoaKhachHang);
        model.addAttribute("products", banHangTaiQuayService.timSanPham(tuKhoaSanPham));
        model.addAttribute("customers", banHangTaiQuayService.timKhachHang(tuKhoaKhachHang));
        Integer idKhachHang = (Integer) phienDangNhap.getAttribute(KHOA_KHACH_HANG);
        if (idKhachHang != null) {
            try {
                model.addAttribute("khachHangDangChon", banHangTaiQuayService.layKhachHang(idKhachHang));
            } catch (IllegalArgumentException loi) {
                phienDangNhap.removeAttribute(KHOA_KHACH_HANG);
                model.addAttribute("error", loi.getMessage());
            }
        }
        model.addAttribute("paymentMethods", banHangTaiQuayService.layPhuongThucThanhToan());
        model.addAttribute("discountCoupons", banHangTaiQuayService.layPhieuGiamGiaKhaDung(gioHang.getTamTinh()));
        model.addAttribute("selectedDiscountCouponId", layIdPhieuGiamGia(phienDangNhap));
        model.addAttribute("cart", gioHang);
        model.addAttribute("summary", banHangTaiQuayService.tinhTongKet(gioHang, layIdPhieuGiamGia(phienDangNhap)));
        return "pos";
    }

    @PostMapping("/pos/khach-hang/chon")
    public String chonKhachHang(@RequestParam(value = "customerId", required = false) Integer idKhachHang,
                                HttpSession phienDangNhap, RedirectAttributes thongBao) {
        if (!daDangNhap(phienDangNhap)) {
            return "redirect:/login";
        }
        try {
            if (idKhachHang == null) {
                phienDangNhap.removeAttribute(KHOA_KHACH_HANG);
            } else {
                banHangTaiQuayService.layKhachHang(idKhachHang);
                phienDangNhap.setAttribute(KHOA_KHACH_HANG, idKhachHang);
            }
        } catch (IllegalArgumentException loi) {
            thongBao.addFlashAttribute("error", loi.getMessage());
        }
        return "redirect:/pos";
    }

    @PostMapping("/pos/khach-hang/them")
    public String themKhachHang(@RequestParam("tenKhachHangMoi") String ten,
                                @RequestParam(value = "soDienThoaiMoi", required = false) String soDienThoai,
                                @RequestParam(value = "emailMoi", required = false) String email,
                                HttpSession phienDangNhap, Model model, RedirectAttributes thongBao) {
        NguoiDungDangNhap nhanVien = DangNhapController.layNguoiDungDangNhap(phienDangNhap, model);
        if (nhanVien == null) {
            return "redirect:/login";
        }
        try {
            Integer idKhachHang = banHangTaiQuayService.themKhachHangMoi(ten, soDienThoai, email, nhanVien.getId());
            phienDangNhap.setAttribute(KHOA_KHACH_HANG, idKhachHang);
            thongBao.addFlashAttribute("success", "Đã thêm khách hàng mới và chọn cho đơn hiện tại.");
        } catch (IllegalArgumentException loi) {
            thongBao.addFlashAttribute("loiThemKhachHang", loi.getMessage());
            thongBao.addFlashAttribute("tenMoi", ten);
            thongBao.addFlashAttribute("soDienThoaiMoi", soDienThoai);
            thongBao.addFlashAttribute("emailMoi", email);
        }
        return "redirect:/pos";
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
        phienDangNhap.removeAttribute(KHOA_DON_CHO);
        phienDangNhap.removeAttribute(KHOA_KHACH_HANG);
        phienDangNhap.removeAttribute(KHOA_PHIEU_GIAM_GIA);
        thuocTinhChuyenHuong.addFlashAttribute("success", "Đã mở giỏ mới. Các đơn chờ đã lưu vẫn được giữ.");
        return "redirect:/pos";
    }

    @PostMapping("/pos/checkout")
    public String thanhToan(@RequestParam(value = "customerId", required = false) Integer idKhachHang,
                            @RequestParam(value = "hanhDong", defaultValue = "thanh-toan") String hanhDong,
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
            BanHangTaiQuayService.YeuCauThanhToan yeuCau = new BanHangTaiQuayService.YeuCauThanhToan(
                    idKhachHang, tenKhachHang, soDienThoai, email, diaChi,
                    idPhuongThucThanhToan, ghiChu, layIdPhieuGiamGia(phienDangNhap));
            if ("luu-don-cho".equals(hanhDong)) {
                Integer idDonCho = banHangTaiQuayService.luuDonCho(layGioHang(phienDangNhap), yeuCau,
                        nguoiDungDangNhap, layIdDonCho(phienDangNhap));
                layGioHang(phienDangNhap).xoaTatCa();
                phienDangNhap.removeAttribute(KHOA_DON_CHO);
                phienDangNhap.removeAttribute(KHOA_KHACH_HANG);
                phienDangNhap.removeAttribute(KHOA_PHIEU_GIAM_GIA);
                thuocTinhChuyenHuong.addFlashAttribute("success", "Đã lưu đơn chờ #" + idDonCho + ". Chọn Tiếp tục bán để mở lại.");
                return "redirect:/pos";
            }
            Integer idHoaDon = banHangTaiQuayService.thanhToan(
                    layGioHang(phienDangNhap),
                    yeuCau, nguoiDungDangNhap, layIdDonCho(phienDangNhap)
            );
            layGioHang(phienDangNhap).xoaTatCa();
            phienDangNhap.removeAttribute(KHOA_PHIEU_GIAM_GIA);
            phienDangNhap.removeAttribute(KHOA_DON_CHO);
            phienDangNhap.removeAttribute(KHOA_KHACH_HANG);
            thuocTinhChuyenHuong.addFlashAttribute("success", "Thanh toán thành công. Mã hóa đơn nội bộ: #" + idHoaDon);
        } catch (IllegalArgumentException loi) {
            thuocTinhChuyenHuong.addFlashAttribute("error", loi.getMessage());
        }
        return "redirect:/pos";
    }

    @PostMapping("/pos/don-cho/mo")
    public String moDonCho(@RequestParam("idDonCho") Integer idDonCho, HttpSession phienDangNhap,
                           Model model, RedirectAttributes thongBao) {
        NguoiDungDangNhap nhanVien = DangNhapController.layNguoiDungDangNhap(phienDangNhap, model);
        if (nhanVien == null) {
            return "redirect:/login";
        }
        if (idDonCho.equals(layIdDonCho(phienDangNhap))) {
            return "redirect:/pos";
        }
        if (!layGioHang(phienDangNhap).isRong()) {
            thongBao.addFlashAttribute("error", "Hãy lưu đơn chờ hoặc làm mới giỏ hiện tại trước khi mở đơn khác.");
            return "redirect:/pos";
        }
        try {
            Map<String, Object> donCho = banHangTaiQuayService.layDonCho(idDonCho, nhanVien.getId());
            GioHangTaiQuay gioHang = banHangTaiQuayService.layGioHangDonCho(idDonCho, nhanVien.getId());
            phienDangNhap.setAttribute(KHOA_GIO_HANG_TAI_QUAY, gioHang);
            phienDangNhap.setAttribute(KHOA_DON_CHO, idDonCho);
            phienDangNhap.removeAttribute(KHOA_KHACH_HANG);
            if (donCho.get("id_khach_hang") != null) {
                phienDangNhap.setAttribute(KHOA_KHACH_HANG, donCho.get("id_khach_hang"));
            }
            phienDangNhap.removeAttribute(KHOA_PHIEU_GIAM_GIA);
            if (donCho.get("id_phieu_giam_gia") != null) {
                phienDangNhap.setAttribute(KHOA_PHIEU_GIAM_GIA, donCho.get("id_phieu_giam_gia"));
            }
        } catch (IllegalArgumentException loi) {
            thongBao.addFlashAttribute("error", loi.getMessage());
        }
        return "redirect:/pos";
    }

    @PostMapping("/pos/don-cho/huy")
    public String huyDonCho(@RequestParam("idDonCho") Integer idDonCho, HttpSession phienDangNhap,
                            @RequestParam(value = "quayLai", defaultValue = "pos") String quayLai,
                            Model model, RedirectAttributes thongBao) {
        NguoiDungDangNhap nhanVien = DangNhapController.layNguoiDungDangNhap(phienDangNhap, model);
        if (nhanVien == null) {
            return "redirect:/login";
        }
        try {
            banHangTaiQuayService.huyDonCho(idDonCho, nhanVien.getId());
            if (idDonCho.equals(layIdDonCho(phienDangNhap))) {
                layGioHang(phienDangNhap).xoaTatCa();
                phienDangNhap.removeAttribute(KHOA_DON_CHO);
                phienDangNhap.removeAttribute(KHOA_KHACH_HANG);
                phienDangNhap.removeAttribute(KHOA_PHIEU_GIAM_GIA);
            }
            thongBao.addFlashAttribute("success", "Đã hủy và xóa đơn chờ.");
        } catch (IllegalArgumentException loi) {
            thongBao.addFlashAttribute("error", loi.getMessage());
        }
        if ("hoa-don".equals(quayLai)) {
            return "redirect:/quan-ly/hoa-don";
        }
        return "redirect:/pos";
    }

    private Integer layIdDonCho(HttpSession phienDangNhap) {
        Object idDonCho = phienDangNhap.getAttribute(KHOA_DON_CHO);
        if (idDonCho instanceof Integer) {
            return (Integer) idDonCho;
        }
        return null;
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
