package com.example.victorsport.Controller;

import com.example.victorsport.Service.ThongKeService;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

@Controller
public class ThongKeController {
    private final ThongKeService service;
    public ThongKeController(ThongKeService service) { this.service=service; }

    @GetMapping("/quan-ly/thong-ke")
    public String hienThi(@RequestParam(required=false) String tuNgay, @RequestParam(required=false) String denNgay,
                          @RequestParam(defaultValue="tat-ca") String kenh, HttpSession session,
                          HttpServletResponse response, Model model) {
        var user=DangNhapController.layNguoiDungDangNhap(session,model);
        if(user==null) return "redirect:/login";
        if(!user.isQuanLy()) throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        response.setHeader("Cache-Control","no-store");
        model.addAttribute("activeMenu","thong-ke");
        model.addAttribute("tieuDeQuanLy","Thống kê");
        model.addAttribute("kenh",kenh);
        LocalDate homNay=LocalDate.now();
        try {
            LocalDate tu=tuNgay==null?homNay.withDayOfMonth(1):LocalDate.parse(tuNgay);
            LocalDate den=denNgay==null?homNay:LocalDate.parse(denNgay);
            model.addAttribute("tuNgay",tu); model.addAttribute("denNgay",den);
            model.addAllAttributes(service.thongKe(tu,den,kenh));
        } catch(DateTimeParseException e) { model.addAttribute("error","Vui lòng chọn ngày hợp lệ."); }
        catch(IllegalArgumentException e) { model.addAttribute("error",e.getMessage()); }
        catch(DataAccessException e) { model.addAttribute("error","Chưa tải được thống kê. Vui lòng thử lại."); }
        return "Admin/thong-ke";
    }
}
