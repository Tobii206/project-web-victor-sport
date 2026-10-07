package com.example.victorsport.Controller;

import com.example.victorsport.Service.HoaDonService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

@Controller
public class HoaDonController {
    private final HoaDonService hoaDonService;

    public HoaDonController(HoaDonService hoaDonService) {
        this.hoaDonService = hoaDonService;
    }

    @GetMapping("/quan-ly/hoa-don")
    public String hienThiDanhSach(@RequestParam(value = "tuKhoa", required = false) String tuKhoa,
                                  HttpSession phienDangNhap, Model model) {
        if (DangNhapController.layNguoiDungDangNhap(phienDangNhap, model) == null) {
            return "redirect:/login";
        }
        model.addAttribute("activeMenu", "hoa-don");
        model.addAttribute("tuKhoa", tuKhoa);
        model.addAttribute("danhSachHoaDon", hoaDonService.layDanhSachHoaDon(tuKhoa));
        return "hoa-don";
    }

    @GetMapping("/quan-ly/hoa-don/{id}")
    public String hienThiChiTiet(@PathVariable Integer id, HttpSession phienDangNhap,
                                 Model model, RedirectAttributes thongBao) {
        if (DangNhapController.layNguoiDungDangNhap(phienDangNhap, model) == null) {
            return "redirect:/login";
        }
        Map<String, Object> hoaDon = hoaDonService.layHoaDon(id);
        if (hoaDon == null) {
            thongBao.addFlashAttribute("error", "Không tìm thấy hóa đơn.");
            return "redirect:/quan-ly/hoa-don";
        }
        model.addAttribute("activeMenu", "hoa-don");
        model.addAttribute("hoaDon", hoaDon);
        model.addAttribute("danhSachSanPham", hoaDonService.laySanPhamTrongHoaDon(id));
        return "chi-tiet-hoa-don";
    }
}
