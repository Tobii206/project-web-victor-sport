package com.example.victorsport.Controller;

import com.example.victorsport.Dto.DiaChiForm;
import com.example.victorsport.Service.DiaChiKhachHangService;
import com.example.victorsport.Service.KiemTraForm;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.dao.DataAccessException;

@Controller
@RequestMapping("/tai-khoan/dia-chi")
public class DiaChiKhachHangController {
    private final DiaChiKhachHangService service;
    public DiaChiKhachHangController(DiaChiKhachHangService service){this.service=service;}

    @GetMapping
    public String danhSach(@RequestParam(required=false) Integer sua,HttpSession session,Model model,HttpServletResponse response) {
        Integer kh=(Integer)session.getAttribute("khachHangOnlineDangNhap");
        if(kh==null) return "redirect:/login";
        response.setHeader("Cache-Control","no-store");
        try {
            model.addAttribute("diaChiDaLuu",service.danhSach(kh));
            if(sua!=null) model.addAttribute("diaChiSua",service.chiTiet(kh,sua));
        } catch(IllegalArgumentException e){model.addAttribute("error",e.getMessage());}
        catch(DataAccessException e){model.addAttribute("error","Chưa tải được địa chỉ. Vui lòng thử lại.");}
        return "KhachHang/dia-chi";
    }

    @PostMapping("/luu")
    public String luu(@RequestParam(required=false) Integer id,@Valid @ModelAttribute DiaChiForm form,
                      BindingResult loi,HttpSession session,RedirectAttributes tb) {
        Integer kh=(Integer)session.getAttribute("khachHangOnlineDangNhap");
        if(kh==null) return "redirect:/login";
        try {
            KiemTraForm.kiemTra(loi);service.luu(kh,id,form);tb.addFlashAttribute("success","Đã lưu địa chỉ.");
        } catch(IllegalArgumentException e){tb.addFlashAttribute("error",e.getMessage());tb.addFlashAttribute("formCu",form);return "redirect:/tai-khoan/dia-chi"+(id==null?"":"?sua="+id);}
        catch(DataAccessException e){tb.addFlashAttribute("error","Chưa lưu được địa chỉ. Vui lòng thử lại.");tb.addFlashAttribute("formCu",form);return "redirect:/tai-khoan/dia-chi"+(id==null?"":"?sua="+id);}
        return "redirect:/tai-khoan/dia-chi";
    }

    @PostMapping("/{id}/{thaoTac:xoa|mac-dinh}")
    public String thayDoi(@PathVariable int id,@PathVariable String thaoTac,HttpSession session,RedirectAttributes tb) {
        Integer kh=(Integer)session.getAttribute("khachHangOnlineDangNhap");
        if(kh==null) return "redirect:/login";
        try {
            if("xoa".equals(thaoTac)) service.xoa(kh,id);else service.datMacDinh(kh,id);
            tb.addFlashAttribute("success","Đã cập nhật địa chỉ.");
        } catch(IllegalArgumentException e){tb.addFlashAttribute("error",e.getMessage());}
        catch(DataAccessException e){tb.addFlashAttribute("error","Chưa cập nhật được địa chỉ. Vui lòng thử lại.");}
        return "redirect:/tai-khoan/dia-chi";
    }
}
