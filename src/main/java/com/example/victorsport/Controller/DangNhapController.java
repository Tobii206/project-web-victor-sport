package com.example.victorsport.Controller;

import com.example.victorsport.Dto.NguoiDungDangNhap;
import com.example.victorsport.Dto.DangNhapForm;
import com.example.victorsport.Service.KiemTraForm;
import jakarta.validation.Valid;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import com.example.victorsport.Service.DangNhapService;
import com.example.victorsport.Service.TaiKhoanService;
import com.example.victorsport.Service.SanPhamOnlineService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.dao.DataAccessException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class DangNhapController {
    public static final String KHOA_NGUOI_DUNG_DANG_NHAP = "nguoiDungDangNhap";

    private final DangNhapService dangNhapService;
    private final TaiKhoanService taiKhoanService;
    private final SanPhamOnlineService sanPhamOnlineService;

    public DangNhapController(DangNhapService dangNhapService, TaiKhoanService taiKhoanService, SanPhamOnlineService sanPhamOnlineService) {
        this.dangNhapService = dangNhapService;
        this.taiKhoanService = taiKhoanService;
        this.sanPhamOnlineService = sanPhamOnlineService;
    }

    @GetMapping("/login")
    public String hienThiTrangDangNhap(HttpSession phienDangNhap, HttpServletResponse phanHoi,
                                       @RequestParam(value = "quayLai", defaultValue = "pos") String quayLai,
                                       Model model) {
        chongLuuCacheTrangDangNhap(phanHoi);
        model.addAttribute("quayLai", "pos");
        if ("tai-khoan".equals(quayLai)) {
            model.addAttribute("quayLai", "tai-khoan");
        }
        if (phienDangNhap.getAttribute(KHOA_NGUOI_DUNG_DANG_NHAP) instanceof NguoiDungDangNhap) {
            return "redirect:/";
        }
        if (phienDangNhap.getAttribute("khachHangOnlineDangNhap") instanceof Integer) {
            return "redirect:/";
        }
        return "KhachHang/login";
    }

    @PostMapping("/login")
    public String dangNhap(@Valid @ModelAttribute DangNhapForm form, BindingResult loiForm,
                           @RequestParam(value = "quayLai", defaultValue = "pos") String quayLai,
                           HttpServletRequest request,
                           RedirectAttributes thuocTinhChuyenHuong) {
        HttpSession phienDangNhap = request.getSession();
        if (phienDangNhap.getAttribute(KHOA_NGUOI_DUNG_DANG_NHAP) != null
                || phienDangNhap.getAttribute("khachHangOnlineDangNhap") != null) {
            return "redirect:/";
        }
        try {
            KiemTraForm.kiemTra(loiForm);
            String taiKhoan = form.getTaiKhoan();
            String matKhau = form.getMatKhau();
            if (dangNhapService.laTaiKhoanNhanVien(taiKhoan)) {
                DangNhapService.KetQuaDangNhap ketQua = dangNhapService.dangNhap(taiKhoan, matKhau);
                if (!ketQua.isThanhCong()) { throw new IllegalArgumentException(ketQua.getThongBao()); }
                request.changeSessionId();
                phienDangNhap.removeAttribute("khachHangOnlineDangNhap");
                phienDangNhap.setAttribute(KHOA_NGUOI_DUNG_DANG_NHAP, ketQua.getNguoiDung());
                return "redirect:/";
            }
            Integer id = taiKhoanService.dangNhapKhachHang(taiKhoan, matKhau);
            var gioDaLuu=sanPhamOnlineService.layGioDaLuu(id);
            request.changeSessionId();
            phienDangNhap.removeAttribute(KHOA_NGUOI_DUNG_DANG_NHAP);
            phienDangNhap.setAttribute("khachHangOnlineDangNhap", id);
            phienDangNhap.setAttribute("gioHangOnline",gioDaLuu);
            phienDangNhap.removeAttribute("muaNgayOnline");
            return "redirect:/";
        } catch (IllegalArgumentException loi) {
            thuocTinhChuyenHuong.addFlashAttribute("error", loi.getMessage());
        } catch (DataAccessException loi) {
            thuocTinhChuyenHuong.addFlashAttribute("error", "Chưa thể đăng nhập. Vui lòng thử lại sau.");
        }
        if ("tai-khoan".equals(quayLai)) { return "redirect:/login?quayLai=tai-khoan"; }
        return "redirect:/login";
    }

    @PostMapping("/logout")
    public String dangXuat(HttpSession phienDangNhap) {
        phienDangNhap.invalidate();
        return "redirect:/login";
    }

    private void chongLuuCacheTrangDangNhap(HttpServletResponse phanHoi) {
        phanHoi.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");
        phanHoi.setHeader("Pragma", "no-cache");
        phanHoi.setDateHeader("Expires", 0);
    }

    public static NguoiDungDangNhap layNguoiDungDangNhap(HttpSession phienDangNhap, Model model) {
        Object nguoiDungDangNhap = phienDangNhap.getAttribute(KHOA_NGUOI_DUNG_DANG_NHAP);
        if (nguoiDungDangNhap instanceof NguoiDungDangNhap) {
            NguoiDungDangNhap nguoiDung = (NguoiDungDangNhap) nguoiDungDangNhap;
            model.addAttribute("currentUser", nguoiDung);
            return nguoiDung;
        }
        return null;
    }
}
