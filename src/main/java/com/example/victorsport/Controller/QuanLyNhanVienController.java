package com.example.victorsport.Controller;

import com.example.victorsport.Dto.NguoiDungDangNhap;
import com.example.victorsport.Dto.NhanVienForm;
import com.example.victorsport.Service.KiemTraForm;
import jakarta.validation.Valid;
import org.springframework.validation.BindingResult;
import com.example.victorsport.Service.QuanLyNhanVienService;
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
@RequestMapping("/quan-ly/nhan-vien")
public class QuanLyNhanVienController {
    private final QuanLyNhanVienService service;
    public QuanLyNhanVienController(QuanLyNhanVienService service) { this.service=service; }
    private NguoiDungDangNhap quanLy(HttpSession session,Model model) {
        var user=DangNhapController.layNguoiDungDangNhap(session,model);
        if (user!=null && !user.isQuanLy()) throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        model.addAttribute("activeMenu","nhan-vien");
        return user;
    }
    @GetMapping
    public String danhSach(@RequestParam Map<String,String> loc,HttpSession session,Model model) {
        if (quanLy(session,model)==null) return "redirect:/login";
        model.addAttribute("loc",loc);
        model.addAttribute("cacQuyen",service.cacQuyen());
        try { model.addAttribute("nhanVien",service.tim(loc)); }
        catch (IllegalArgumentException e) { model.addAttribute("error",e.getMessage());model.addAttribute("nhanVien",List.of()); }
        return "Admin/quan-ly-nhan-vien";
    }
    @GetMapping("/them")
    public String them(HttpSession session,Model model) {
        if (quanLy(session,model)==null) return "redirect:/login";
        model.addAttribute("cacQuyen",service.cacQuyen());
        if (!model.containsAttribute("nhanVien")) model.addAttribute("nhanVien",Map.of("trang_thai",true));
        return "Admin/sua-nhan-vien";
    }
    @GetMapping("/{id}")
    public String sua(@PathVariable int id,HttpSession session,Model model,RedirectAttributes flash) {
        if (quanLy(session,model)==null) return "redirect:/login";
        try {
            var nv=service.lay(id);
            if (!model.containsAttribute("nhanVien")) model.addAttribute("nhanVien",nv);
            model.addAttribute("idNhanVien",id);model.addAttribute("maNhanVien",nv.get("ma_nhan_vien"));
            model.addAttribute("cacQuyen",service.cacQuyen());
            return "Admin/sua-nhan-vien";
        } catch (IllegalArgumentException e) { flash.addFlashAttribute("error",e.getMessage());return "redirect:/quan-ly/nhan-vien"; }
    }
    @PostMapping({"/luu","/{id}/luu"})
    public String luu(@PathVariable(required=false) Integer id,@Valid @ModelAttribute NhanVienForm duLieu,BindingResult loiForm,@RequestParam Map<String,String> data,HttpSession session,Model model,RedirectAttributes flash) {
        var user=quanLy(session,model);if(user==null) return "redirect:/login";
        try {
            KiemTraForm.kiemTra(loiForm);
            service.luu(id,data,user.getId());
            flash.addFlashAttribute("success",id==null?"Đã thêm nhân viên.":"Đã cập nhật thông tin nhân viên.");
            return "redirect:/quan-ly/nhan-vien";
        } catch (IllegalArgumentException e) { flash.addFlashAttribute("error",e.getMessage()); }
        catch (DataAccessException e) { flash.addFlashAttribute("error","Không lưu được nhân viên. Vui lòng thử lại."); }
        Map<String,Object> form=new LinkedHashMap<>(data);
        form.remove("mat_khau");form.put("trang_thai","1".equals(data.get("trang_thai")));
        flash.addFlashAttribute("nhanVien",form);
        return "redirect:/quan-ly/nhan-vien/"+(id==null?"them":id);
    }
    @PostMapping("/{id}/nghi-viec")
    public String nghiViec(@PathVariable int id,HttpSession session,Model model,RedirectAttributes flash) {
        var user=quanLy(session,model);if(user==null) return "redirect:/login";
        try { service.nghiViec(id,user.getId());flash.addFlashAttribute("success","Đã chuyển nhân viên sang trạng thái nghỉ việc."); }
        catch (IllegalArgumentException e) { flash.addFlashAttribute("error",e.getMessage()); }
        catch (DataAccessException e) { flash.addFlashAttribute("error","Không cập nhật được trạng thái. Vui lòng thử lại."); }
        return "redirect:/quan-ly/nhan-vien";
    }
}
