package com.example.victorsport.Controller;

import com.example.victorsport.Dto.NguoiDungDangNhap;
import com.example.victorsport.Service.DangNhapService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class DangNhapController {
    public static final String KHOA_NGUOI_DUNG_DANG_NHAP = "nguoiDungDangNhap";

    private final DangNhapService dangNhapService;

    public DangNhapController(DangNhapService dangNhapService) {
        this.dangNhapService = dangNhapService;
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
            if ("tai-khoan".equals(quayLai)) {
                return "redirect:/tai-khoan";
            }
            return "redirect:/pos";
        }
        return "login";
    }

    @PostMapping("/login")
    public String dangNhap(@RequestParam("emailDangNhap") String email,
                           @RequestParam("matKhauDangNhap") String matKhau,
                           @RequestParam(value = "quayLai", defaultValue = "pos") String quayLai,
                           HttpSession phienDangNhap,
                           RedirectAttributes thuocTinhChuyenHuong) {
        DangNhapService.KetQuaDangNhap ketQua = dangNhapService.dangNhap(email, matKhau);
        if (!ketQua.isThanhCong()) {
            thuocTinhChuyenHuong.addFlashAttribute("error", ketQua.getThongBao());
            if ("tai-khoan".equals(quayLai)) {
                return "redirect:/login?quayLai=tai-khoan";
            }
            return "redirect:/login";
        }

        phienDangNhap.setAttribute(KHOA_NGUOI_DUNG_DANG_NHAP, ketQua.getNguoiDung());
        if ("tai-khoan".equals(quayLai)) {
            return "redirect:/tai-khoan";
        }
        return "redirect:/pos";
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
