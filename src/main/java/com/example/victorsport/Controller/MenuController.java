package com.example.victorsport.Controller;

import com.example.victorsport.Dto.NguoiDungDangNhap;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

@Controller
public class MenuController {
    private static final Map<String, String> TEN_CHUC_NANG = Map.of(
            "san-pham", "Quản lý sản phẩm",
            "hoa-don", "Quản lý hóa đơn",
            "dot-giam-gia", "Quản lý đợt giảm giá",
            "phieu-giam-gia", "Quản lý phiếu giảm giá",
            "nhan-vien", "Quản lý nhân viên",
            "khach-hang", "Quản lý khách hàng",
            "gio-hang", "Quản lý giỏ hàng"
    );

    @GetMapping("/quan-ly/{maChucNang}")
    public String hienThiChucNangDangPhatTrien(@PathVariable String maChucNang,
                                               HttpSession phienDangNhap,
                                               Model model) {
        NguoiDungDangNhap nguoiDungDangNhap = DangNhapController.layNguoiDungDangNhap(phienDangNhap, model);
        if (nguoiDungDangNhap == null) {
            return "redirect:/login";
        }

        model.addAttribute("activeMenu", maChucNang);
        model.addAttribute("tenChucNang", TEN_CHUC_NANG.getOrDefault(maChucNang, "Chức năng"));
        return "dang-phat-trien";
    }
}
