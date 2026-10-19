package com.example.victorsport.Controller;

import com.example.victorsport.Service.HoaDonService;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@Controller
@RequestMapping("/don-hang")
public class DonHangController {
    private final HoaDonService service;
    public DonHangController(HoaDonService service) { this.service = service; }

    private Integer khachHang(HttpSession session) {
        Object id = session.getAttribute("khachHangOnlineDangNhap");
        if (id instanceof Integer) return (Integer) id;
        return null;
    }

    @GetMapping
    public String danhSach(HttpSession session,HttpServletResponse response,Model model) {
        response.setHeader("Cache-Control","no-store");
        Integer khach = khachHang(session);
        if (khach == null) return "redirect:/login";
        try { model.addAttribute("donHang",service.layDonCuaKhach(khach)); }
        catch (IllegalArgumentException e) { model.addAttribute("error",e.getMessage()); }
        catch (DataAccessException e) { model.addAttribute("error","Chưa tải được đơn hàng. Vui lòng thử lại."); }
        return "client/order/show";
    }

    @GetMapping("/{id}")
    public String chiTiet(@PathVariable int id,HttpSession session,HttpServletResponse response,Model model,RedirectAttributes flash) {
        response.setHeader("Cache-Control","no-store");
        Integer khach = khachHang(session);
        if (khach == null) return "redirect:/login";
        try {
            var don = service.layDonCuaKhach(id,khach);
            if (don == null || don.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Không tìm thấy đơn hàng.");
            model.addAttribute("don",don);
            model.addAttribute("sanPham",service.laySanPhamTrongHoaDon(id));
            model.addAttribute("lichSu",service.layLichSu(id));
            return "client/order/detail";
        } catch (IllegalArgumentException e) { flash.addFlashAttribute("error",e.getMessage()); }
        catch (DataAccessException e) { flash.addFlashAttribute("error","Chưa tải được chi tiết đơn hàng. Vui lòng thử lại."); }
        return "redirect:/don-hang";
    }

    @PostMapping("/{id}/da-nhan-hang")
    public String daNhanHang(@PathVariable int id,@RequestParam(defaultValue="chi-tiet") String quayLai,
                             HttpSession session,RedirectAttributes flash) {
        Integer khach = khachHang(session);
        if (khach == null) return "redirect:/login";
        try {
            service.xacNhanDaNhanHang(id,khach);
            flash.addFlashAttribute("success","Đã xác nhận nhận hàng. Cảm ơn bạn đã mua sắm tại VictorSport!");
        } catch (IllegalArgumentException e) { flash.addFlashAttribute("error",e.getMessage()); }
        catch (DataAccessException e) { flash.addFlashAttribute("error","Chưa xác nhận được nhận hàng. Vui lòng thử lại."); }
        if ("danh-sach".equals(quayLai)) return "redirect:/don-hang";
        return "redirect:/don-hang/" + id;
    }

    @PostMapping("/{id}/huy")
    public String huyDon(@PathVariable int id,@RequestParam(defaultValue="chi-tiet") String quayLai,
                         HttpSession session,RedirectAttributes flash) {
        Integer khach = khachHang(session);
        if (khach == null) return "redirect:/login";
        try {
            service.huyDonCuaKhach(id,khach);
            flash.addFlashAttribute("success","Đã hủy đơn hàng.");
        } catch (IllegalArgumentException e) { flash.addFlashAttribute("error",e.getMessage()); }
        catch (DataAccessException e) { flash.addFlashAttribute("error","Chưa hủy được đơn hàng. Vui lòng thử lại."); }
        if ("danh-sach".equals(quayLai)) return "redirect:/don-hang";
        return "redirect:/don-hang/" + id;
    }
}
