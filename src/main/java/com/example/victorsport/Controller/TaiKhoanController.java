package com.example.victorsport.Controller;

import com.example.victorsport.Dto.NguoiDungDangNhap;
import com.example.victorsport.Service.TaiKhoanService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class TaiKhoanController {
    private static final String KHOA_KHACH_HANG = "khachHangOnlineDangNhap";
    private final TaiKhoanService taiKhoanService;

    public TaiKhoanController(TaiKhoanService taiKhoanService) {
        this.taiKhoanService = taiKhoanService;
    }

    @GetMapping("/tai-khoan")
    public String hienThiTaiKhoan(HttpSession phienDangNhap, HttpServletResponse phanHoi, Model model) {
        phanHoi.setHeader("Cache-Control", "no-store");
        Object nhanVien = phienDangNhap.getAttribute(DangNhapController.KHOA_NGUOI_DUNG_DANG_NHAP);
        Object idKhachHang = phienDangNhap.getAttribute(KHOA_KHACH_HANG);
        try {
            if (nhanVien instanceof NguoiDungDangNhap) {
                NguoiDungDangNhap nguoiDung = (NguoiDungDangNhap) nhanVien;
                model.addAttribute("thongTin", taiKhoanService.layThongTinNhanVien(nguoiDung.getId()));
                model.addAttribute("laNhanVien", true);
            } else if (idKhachHang instanceof Integer) {
                model.addAttribute("thongTin", taiKhoanService.layThongTinKhachHang((Integer) idKhachHang));
            }
        } catch (IllegalArgumentException loi) {
            phienDangNhap.invalidate();
            model.addAttribute("error", loi.getMessage());
        } catch (DataAccessException loi) {
            model.addAttribute("error", "Chưa tải được thông tin tài khoản. Vui lòng thử lại sau.");
            model.addAttribute("loiKetNoi", true);
        }
        return "tai-khoan";
    }

    @PostMapping("/tai-khoan/dang-nhap")
    public String dangNhapKhachHang(@RequestParam("taiKhoan") String taiKhoan,
                                    @RequestParam("matKhau") String matKhau,
                                    HttpServletRequest request, RedirectAttributes thongBao) {
        HttpSession phienDangNhap = request.getSession();
        if (phienDangNhap.getAttribute(DangNhapController.KHOA_NGUOI_DUNG_DANG_NHAP) != null) {
            thongBao.addFlashAttribute("error", "Bạn đang dùng tài khoản nhân viên. Hãy đăng xuất trước khi đổi tài khoản.");
            return "redirect:/tai-khoan";
        }
        try {
            Integer id = taiKhoanService.dangNhapKhachHang(taiKhoan, matKhau);
            request.changeSessionId();
            phienDangNhap.setAttribute(KHOA_KHACH_HANG, id);
        } catch (IllegalArgumentException loi) {
            thongBao.addFlashAttribute("error", loi.getMessage());
        } catch (DataAccessException loi) {
            thongBao.addFlashAttribute("error", "Chưa thể đăng nhập. Vui lòng thử lại sau.");
        }
        return "redirect:/tai-khoan";
    }

    @PostMapping("/tai-khoan/dang-xuat")
    public String dangXuat(HttpSession phienDangNhap) {
        phienDangNhap.invalidate();
        return "redirect:/tai-khoan";
    }
}
