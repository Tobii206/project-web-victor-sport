package com.example.victorsport.Controller;

import com.example.victorsport.Dto.TaiKhoanKhachForm;
import com.example.victorsport.Service.*;
import jakarta.servlet.http.*;
import jakarta.validation.Valid;
import org.springframework.validation.BindingResult;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;

@Controller
public class TuPhucVuTaiKhoanController {
    private final TuPhucVuTaiKhoanService service;
    private final DatLaiMatKhauService datLai;
    private final TaiKhoanService taiKhoan;
    public TuPhucVuTaiKhoanController(TuPhucVuTaiKhoanService s,DatLaiMatKhauService d,TaiKhoanService t) {service=s;datLai=d;taiKhoan=t;}
    private Integer khach(HttpSession session) {return session.getAttribute("khachHangOnlineDangNhap") instanceof Integer id?id:null;}
    private String trang(String cheDo,String tieuDe,HttpSession session,HttpServletResponse response,Model model) {
        response.setHeader("Cache-Control","no-store");
        if(session.getAttribute("csrfTaiKhoan")==null) session.setAttribute("csrfTaiKhoan",UUID.randomUUID().toString());
        model.addAttribute("csrfTaiKhoan",session.getAttribute("csrfTaiKhoan"));
        model.addAttribute("cheDo",cheDo);model.addAttribute("tieuDe",tieuDe);
        return "KhachHang/tu-phuc-vu-tai-khoan";
    }
    private void kiemTraToken(HttpSession session,String token) {
        if(token==null || !token.equals(session.getAttribute("csrfTaiKhoan"))) throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Vui lòng tải lại trang.");
    }
    @GetMapping("/dang-ky")
    public String dangKy(HttpSession s,HttpServletResponse r,Model m) {return trang("dang-ky","Tạo tài khoản",s,r,m);}
    @PostMapping("/dang-ky")
    public String dangKy(@Valid @ModelAttribute TaiKhoanKhachForm form,BindingResult loi,@RequestParam String matKhau,
                         @RequestParam String nhapLai,@RequestParam(required=false) String csrfTaiKhoan,HttpSession s,RedirectAttributes f) {
        kiemTraToken(s,csrfTaiKhoan);
        try {KiemTraForm.kiemTra(loi);service.dangKy(form,matKhau,nhapLai);f.addFlashAttribute("success","Đăng ký thành công. Vui lòng đăng nhập bằng email.");return "redirect:/login";}
        catch(IllegalArgumentException e) {f.addFlashAttribute("error",e.getMessage());}
        catch(DataAccessException e) {f.addFlashAttribute("error","Chưa tạo được tài khoản. Vui lòng thử lại.");}
        f.addFlashAttribute("duLieu",form);return "redirect:/dang-ky";
    }
    @GetMapping("/tai-khoan/chinh-sua")
    public String sua(HttpSession s,HttpServletResponse r,Model m) {
        Integer id=khach(s);if(id==null) return "redirect:/login";
        if(!m.containsAttribute("duLieu")) m.addAttribute("duLieu",service.thongTin(id));
        return trang("chinh-sua","Thông tin cá nhân",s,r,m);
    }
    @PostMapping("/tai-khoan/chinh-sua")
    public String sua(@Valid @ModelAttribute TaiKhoanKhachForm form,BindingResult loi,@RequestParam(required=false) String csrfTaiKhoan,HttpSession s,RedirectAttributes f) {
        Integer id=khach(s);if(id==null)return "redirect:/login";kiemTraToken(s,csrfTaiKhoan);
        try {KiemTraForm.kiemTra(loi);service.capNhat(id,form);f.addFlashAttribute("success","Đã lưu thông tin cá nhân.");return "redirect:/tai-khoan";}
        catch(IllegalArgumentException e) {f.addFlashAttribute("error",e.getMessage());}
        catch(DataAccessException e) {f.addFlashAttribute("error","Chưa lưu được thông tin. Vui lòng thử lại.");}
        f.addFlashAttribute("duLieu",form);return "redirect:/tai-khoan/chinh-sua";
    }
    @GetMapping("/tai-khoan/doi-mat-khau")
    public String doi(HttpSession s,HttpServletResponse r,Model m) {
        if(khach(s)==null)return "redirect:/login";return trang("doi-mat-khau","Đổi mật khẩu",s,r,m);
    }
    @PostMapping("/tai-khoan/doi-mat-khau")
    public String doi(@RequestParam String matKhauCu,@RequestParam String matKhau,@RequestParam String nhapLai,
                      @RequestParam(required=false) String csrfTaiKhoan,HttpSession s,RedirectAttributes f) {
        Integer id=khach(s);if(id==null)return "redirect:/login";kiemTraToken(s,csrfTaiKhoan);
        try {service.doiMatKhau(id,matKhauCu,matKhau,nhapLai);s.invalidate();f.addFlashAttribute("success","Đã đổi mật khẩu. Vui lòng đăng nhập lại.");return "redirect:/login";}
        catch(IllegalArgumentException e) {f.addFlashAttribute("error",e.getMessage());}
        catch(DataAccessException e) {f.addFlashAttribute("error","Chưa đổi được mật khẩu. Vui lòng thử lại.");}
        return "redirect:/tai-khoan/doi-mat-khau";
    }
    @GetMapping("/quen-mat-khau")
    public String quen(HttpSession s,HttpServletResponse r,Model m) {m.addAttribute("guiEmailSanSang",datLai.sanSang());return trang("quen-mat-khau","Quên mật khẩu",s,r,m);}
    @PostMapping("/quen-mat-khau")
    public String gui(@RequestParam String email,@RequestParam(required=false) String csrfTaiKhoan,HttpSession s,RedirectAttributes f) {
        kiemTraToken(s,csrfTaiKhoan);
        try {datLai.guiMa(email);f.addFlashAttribute("success","Nếu email có tài khoản đang hoạt động, mã xác minh sẽ được gửi. Mã có hiệu lực 10 phút.");}
        catch(IllegalArgumentException e){f.addFlashAttribute("error",e.getMessage());}
        catch(DataAccessException e){f.addFlashAttribute("error","Chưa gửi được mã. Vui lòng thử lại sau.");}
        f.addFlashAttribute("email",email);return "redirect:/quen-mat-khau";
    }
    @PostMapping("/dat-lai-mat-khau")
    public String datLai(@RequestParam String email,@RequestParam String ma,@RequestParam String matKhau,@RequestParam String nhapLai,
                         @RequestParam(required=false) String csrfTaiKhoan,HttpSession s,RedirectAttributes f) {
        kiemTraToken(s,csrfTaiKhoan);
        try {
            if(!datLai.datLai(email,ma,matKhau,nhapLai)) throw new IllegalArgumentException("Mã không đúng, đã hết hạn hoặc đã thử quá 5 lần.");
            taiKhoan.moKhoaBangEmail(email);
            s.invalidate();f.addFlashAttribute("success","Đã đặt lại mật khẩu. Vui lòng đăng nhập.");return "redirect:/login";
        } catch(IllegalArgumentException e){f.addFlashAttribute("error",e.getMessage());}
        catch(DataAccessException e){f.addFlashAttribute("error","Chưa đặt lại được mật khẩu. Vui lòng thử lại.");}
        f.addFlashAttribute("email",email);return "redirect:/quen-mat-khau";
    }
}
