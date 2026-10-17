package com.example.victorsport.Controller;

import com.example.victorsport.Service.HoaDonService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
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
        return "NhanVien/hoa-don";
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
        model.addAttribute("lichSu", hoaDonService.layLichSu(id));
        return "NhanVien/chi-tiet-hoa-don";
    }

    @PostMapping("/quan-ly/hoa-don/{id}/huy")
    public String huyHoaDon(@PathVariable int id,@RequestParam(defaultValue="chi-tiet") String quayLai,
                            HttpSession session,Model model,RedirectAttributes thongBao) {
        var user = DangNhapController.layNguoiDungDangNhap(session,model);
        if (user == null) return "redirect:/login";
        if (!user.isQuanLy()) throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        try {
            hoaDonService.huyHoaDon(id,user.getId());
            thongBao.addFlashAttribute("success","Đã hủy hóa đơn và hoàn lại tồn kho.");
        } catch (IllegalArgumentException e) { thongBao.addFlashAttribute("error",e.getMessage()); }
        catch (DataAccessException e) { thongBao.addFlashAttribute("error","Chưa hủy được hóa đơn. Vui lòng thử lại."); }
        if ("danh-sach".equals(quayLai)) return "redirect:/quan-ly/hoa-don";
        return "redirect:/quan-ly/hoa-don/" + id;
    }

    @PostMapping("/quan-ly/hoa-don/{id}/xac-nhan-thanh-toan")
    public String xacNhanThanhToan(@PathVariable int id,
                                  @RequestParam(defaultValue="chi-tiet") String quayLai,
                                  HttpSession session,Model model,RedirectAttributes thongBao) {
        var user = DangNhapController.layNguoiDungDangNhap(session,model);
        if (user == null) return "redirect:/login";
        if (!user.isQuanLy()) throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        try {
            hoaDonService.xacNhanThanhToan(id,user.getId());
            thongBao.addFlashAttribute("success","Đã xác nhận thanh toán. Đơn hàng đang vận chuyển.");
        } catch (IllegalArgumentException e) { thongBao.addFlashAttribute("error",e.getMessage()); }
        catch (DataAccessException e) { thongBao.addFlashAttribute("error","Chưa xác nhận được thanh toán. Vui lòng thử lại."); }
        if ("danh-sach".equals(quayLai)) return "redirect:/quan-ly/hoa-don";
        return "redirect:/quan-ly/hoa-don/" + id;
    }
}
