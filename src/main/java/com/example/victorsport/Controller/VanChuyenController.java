package com.example.victorsport.Controller;

import com.example.victorsport.Dto.TinhPhiGhnForm;
import com.example.victorsport.Service.GhnService;
import com.example.victorsport.Service.VanChuyenService;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.validation.BindingResult;
import org.springframework.dao.DataAccessException;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.Map;

@Controller
@RequestMapping("/quan-ly/van-chuyen")
public class VanChuyenController {
    private final VanChuyenService service;
    private final GhnService ghn;
    public VanChuyenController(VanChuyenService service,GhnService ghn) { this.service=service; this.ghn=ghn; }

    private boolean quanLy(HttpSession session,Model model) {
        var user=DangNhapController.layNguoiDungDangNhap(session,model);
        if(user==null) return false;
        if(!user.isQuanLy()) throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        return true;
    }
    @GetMapping
    public String hienThi(@RequestParam(defaultValue="") String tuKhoa, @RequestParam(required=false) Integer trangThai,
                          @RequestParam(defaultValue="1") int trang, HttpSession session, HttpServletResponse response,Model model) {
        if(!quanLy(session,model)) return "redirect:/login";
        response.setHeader("Cache-Control","no-store");
        model.addAttribute("activeMenu","van-chuyen"); model.addAttribute("tieuDeQuanLy","Quản lý vận chuyển");
        model.addAttribute("tuKhoa",tuKhoa); model.addAttribute("trangThai",trangThai);
        model.addAttribute("ghnSanSang",ghn.sanSang());
        try { model.addAllAttributes(service.danhSach(tuKhoa,trangThai,trang)); }
        catch(IllegalArgumentException e) { model.addAttribute("error",e.getMessage()); }
        catch(DataAccessException e) { model.addAttribute("error","Chưa tải được danh sách vận chuyển. Vui lòng thử lại."); }
        return "Admin/van-chuyen";
    }

    @GetMapping("/ghn/dia-chi")
    @ResponseBody
    public ResponseEntity<?> diaChi(@RequestParam(defaultValue="tinh") String cap,@RequestParam(defaultValue="0") int ma,
                                   HttpSession session,Model model) {
        if(!quanLy(session,model)) return traLoi(HttpStatus.UNAUTHORIZED,Map.of("error","Vui lòng đăng nhập lại."));
        try {
            Object data=switch(cap) {
                case "tinh" -> ghn.tinh();
                case "huyen" -> ghn.huyen(ma);
                case "xa" -> ghn.xa(ma);
                default -> throw new IllegalArgumentException("Cấp địa chỉ không hợp lệ.");
            };
            return traLoi(HttpStatus.OK,data);
        } catch(IllegalArgumentException e) { return traLoi(HttpStatus.BAD_REQUEST,Map.of("error",e.getMessage())); }
        catch(IllegalStateException e) { return traLoi(HttpStatus.SERVICE_UNAVAILABLE,Map.of("error",e.getMessage())); }
    }

    @PostMapping("/ghn/tinh-phi")
    @ResponseBody
    public ResponseEntity<?> tinhPhi(@Valid @ModelAttribute TinhPhiGhnForm form, BindingResult loi,
                                    HttpSession session,Model model) {
        if(!quanLy(session,model)) return traLoi(HttpStatus.UNAUTHORIZED,Map.of("error","Vui lòng đăng nhập lại."));
        if(loi.hasErrors()) return traLoi(HttpStatus.BAD_REQUEST,Map.of("error","Vui lòng chọn đủ địa chỉ GHN và nhập kích thước, khối lượng, giá trị hàng hợp lệ."));
        try { return traLoi(HttpStatus.OK,Map.of("phi",ghn.tinhPhi(form))); }
        catch(IllegalArgumentException e) { return traLoi(HttpStatus.BAD_REQUEST,Map.of("error",e.getMessage())); }
        catch(IllegalStateException e) { return traLoi(HttpStatus.SERVICE_UNAVAILABLE,Map.of("error",e.getMessage())); }
    }
    private ResponseEntity<?> traLoi(HttpStatus status,Object body) {
        return ResponseEntity.status(status).cacheControl(CacheControl.noStore()).body(body);
    }
}
