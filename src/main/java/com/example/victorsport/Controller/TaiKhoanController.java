package com.example.victorsport.Controller;

import com.example.victorsport.Dto.NguoiDungDangNhap;
import com.example.victorsport.Service.TaiKhoanService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

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
        if (!(nhanVien instanceof NguoiDungDangNhap) && !(idKhachHang instanceof Integer)) {
            return "redirect:/login?quayLai=tai-khoan";
        }
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
        return "client/account/show";
    }

    @PostMapping("/tai-khoan/dang-xuat")
    public String dangXuat(HttpSession phienDangNhap) {
        phienDangNhap.invalidate();
        return "redirect:/login";
    }
}
