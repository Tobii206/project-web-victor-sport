package com.example.victorsport.Controller;

import com.example.victorsport.Dto.NguoiDungDangNhap;
import com.example.victorsport.Dto.KhachHangForm;
import com.example.victorsport.Service.KiemTraForm;
import jakarta.validation.Valid;
import org.springframework.validation.BindingResult;
import com.example.victorsport.Service.QuanLyKhachHangService;
import com.example.victorsport.Service.TaiKhoanService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;

@Controller
@RequestMapping("/quan-ly/khach-hang")
public class QuanLyKhachHangController {
    private final QuanLyKhachHangService service;
    private final TaiKhoanService taiKhoanService;
    public QuanLyKhachHangController(QuanLyKhachHangService service,TaiKhoanService taiKhoanService) {this.service=service;this.taiKhoanService=taiKhoanService;}
    private NguoiDungDangNhap nguoiDung(HttpSession session,Model model,boolean ghi) {
        var user=DangNhapController.layNguoiDungDangNhap(session,model);
        if(user!=null && !(user.isQuanLy()||(!ghi&&user.isXemKhachHang()))) throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        model.addAttribute("activeMenu","khach-hang");return user;
    }
    @PostMapping("/{id}/mo-khoa")
    public String moKhoa(@PathVariable int id,HttpSession session,Model model,RedirectAttributes flash) {
        if(nguoiDung(session,model,true)==null) return "redirect:/login";
        try {var kh=service.lay(id);taiKhoanService.moKhoaSauDoiMatKhau(kh.get("ten_tai_khoan").toString());flash.addFlashAttribute("success","Đã mở khóa đăng nhập khách hàng.");}
        catch(IllegalArgumentException e){flash.addFlashAttribute("error",e.getMessage());}
        catch(DataAccessException e){flash.addFlashAttribute("error","Chưa mở khóa được tài khoản.");}
        return "redirect:/quan-ly/khach-hang";
    }
    @GetMapping
    public String danhSach(@RequestParam Map<String,String> loc,HttpSession session,Model model) {
        if(nguoiDung(session,model,false)==null) return "redirect:/login";
        model.addAttribute("loc",loc);
        try {model.addAttribute("khachHang",service.tim(loc));}
        catch(DataAccessException e) {model.addAttribute("error","Chưa tải được khách hàng. Vui lòng thử lại.");model.addAttribute("khachHang",List.of());}
        return "Admin/quan-ly-khach-hang";
    }
    @GetMapping("/them")
    public String them(HttpSession session,Model model) {
        if(nguoiDung(session,model,true)==null) return "redirect:/login";
        if(!model.containsAttribute("khachHang")) model.addAttribute("khachHang",Map.of("trang_thai",true));
        return "Admin/sua-khach-hang";
    }
    @GetMapping("/{id}")
    public String sua(@PathVariable int id,HttpSession session,Model model,RedirectAttributes flash) {
        if(nguoiDung(session,model,false)==null) return "redirect:/login";
        try {
            var kh=service.lay(id);
            if(!model.containsAttribute("khachHang")) model.addAttribute("khachHang",kh);
            model.addAttribute("idKhachHang",id);model.addAttribute("maKhachHang",kh.get("ma_khach_hang"));
            return "Admin/sua-khach-hang";
        } catch(IllegalArgumentException e) {flash.addFlashAttribute("error",e.getMessage());return "redirect:/quan-ly/khach-hang";}
    }
    @PostMapping({"/luu","/{id}/luu"})
    public String luu(@PathVariable(required=false) Integer id,@Valid @ModelAttribute KhachHangForm duLieu,BindingResult loiForm,@RequestParam Map<String,String> data,HttpSession session,Model model,RedirectAttributes flash) {
        var user=nguoiDung(session,model,true);if(user==null)return "redirect:/login";
        try {
            KiemTraForm.kiemTra(loiForm);
            service.luu(id,data,user.getId());
            // Chỉ mở khóa sau khi việc lưu mật khẩu đã hoàn thành thành công.
            if(!data.getOrDefault("mat_khau","").isEmpty()) taiKhoanService.moKhoaSauDoiMatKhau(data.get("ten_tai_khoan"));
            flash.addFlashAttribute("success",id==null?"Đã thêm khách hàng.":"Đã cập nhật thông tin khách hàng.");
            return "redirect:/quan-ly/khach-hang";
        }
        catch(IllegalArgumentException e) {flash.addFlashAttribute("error",e.getMessage());}
        catch(DataAccessException e) {flash.addFlashAttribute("error","Không lưu được khách hàng. Vui lòng thử lại.");}
        Map<String,Object> form=new LinkedHashMap<>(data);form.remove("mat_khau");form.put("trang_thai","1".equals(data.get("trang_thai")));
        String gioi=data.getOrDefault("gioi_tinh","");form.put("gioi_tinh",gioi.isEmpty()?null:"1".equals(gioi));
        flash.addFlashAttribute("khachHang",form);
        return "redirect:/quan-ly/khach-hang/"+(id==null?"them":id);
    }
    @PostMapping("/{id}/ngung-hoat-dong")
    public String ngung(@PathVariable int id,HttpSession session,Model model,RedirectAttributes flash) {
        var user=nguoiDung(session,model,true);if(user==null)return "redirect:/login";
        try {service.ngungHoatDong(id,user.getId());flash.addFlashAttribute("success","Đã chuyển khách hàng sang không hoạt động.");}
        catch(IllegalArgumentException e) {flash.addFlashAttribute("error",e.getMessage());}
        catch(DataAccessException e) {flash.addFlashAttribute("error","Không cập nhật được trạng thái. Vui lòng thử lại.");}
        return "redirect:/quan-ly/khach-hang";
    }
}
