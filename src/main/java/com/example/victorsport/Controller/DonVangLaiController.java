package com.example.victorsport.Controller;

import com.example.victorsport.Service.HoaDonService;
import jakarta.servlet.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import org.springframework.dao.DataAccessException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class DonVangLaiController {
    private final HoaDonService service;
    public DonVangLaiController(HoaDonService service) {this.service=service;}
    public static boolean dungPhien(HttpSession s,int id) {return Boolean.TRUE.equals(s.getAttribute("donVangLai_"+id)) || Boolean.TRUE.equals(s.getAttribute("donQr_"+id));}
    @GetMapping("/tra-cuu-don-hang")
    public String traCuu(HttpServletResponse r) {r.setHeader("Cache-Control","no-store");return "client/order/lookup";}
    @PostMapping("/tra-cuu-don-hang")
    public String traCuu(@RequestParam String ma,@RequestParam String sdt,HttpSession s,RedirectAttributes f) {
        long now=System.currentTimeMillis();long moc=s.getAttribute("traCuuTu") instanceof Long t?t:0;
        int dem=s.getAttribute("soLanTraCuu") instanceof Integer n?n:0;
        if(now-moc>300000){moc=now;dem=0;}
        s.setAttribute("traCuuTu",moc);s.setAttribute("soLanTraCuu",dem+1);
        if(dem>=10){f.addFlashAttribute("error","Bạn đã tra cứu nhiều lần. Vui lòng thử lại sau 5 phút.");return "redirect:/tra-cuu-don-hang";}
        try {
            Integer id=service.traCuuDon(ma,sdt);
            if(id!=null){s.setAttribute("donVangLai_"+id,true);return "redirect:/don-vang-lai/"+id;}
            f.addFlashAttribute("error","Không tìm thấy đơn khách vãng lai với thông tin này.");
        }catch(DataAccessException e){f.addFlashAttribute("error","Chưa tra cứu được đơn. Vui lòng thử lại.");}
        return "redirect:/tra-cuu-don-hang";
    }
    @GetMapping("/don-vang-lai/{id}")
    public String chiTiet(@PathVariable int id,HttpSession s,HttpServletResponse r,Model m) {
        r.setHeader("Cache-Control","no-store");
        if(!dungPhien(s,id)) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        var don=service.layDonVangLai(id,dungPhien(s,id));
        if(don==null) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        m.addAttribute("don",don);m.addAttribute("khachVangLai",true);
        m.addAttribute("sanPham",service.laySanPhamTrongHoaDon(id));m.addAttribute("lichSu",service.layLichSu(id));
        return "client/order/detail";
    }
    @PostMapping("/don-vang-lai/{id}/{thaoTac:huy|da-nhan-hang}")
    public String xuLy(@PathVariable int id,@PathVariable String thaoTac,HttpSession s,RedirectAttributes f) {
        if(!dungPhien(s,id)) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        try {
            if(thaoTac.equals("huy")) service.huyDonVangLai(id,dungPhien(s,id));else service.nhanDonVangLai(id,dungPhien(s,id));
            f.addFlashAttribute("success","Đã cập nhật đơn hàng.");
        }catch(IllegalArgumentException e){f.addFlashAttribute("error",e.getMessage());}
        catch(DataAccessException e){f.addFlashAttribute("error","Chưa cập nhật được đơn hàng.");}
        return "redirect:/don-vang-lai/"+id;
    }
    @PostMapping("/thanh-toan/qr/{id}/doi-cod")
    public String doiCod(@PathVariable int id,HttpSession s,RedirectAttributes f) {
        try {
            Integer kh=s.getAttribute("khachHangOnlineDangNhap") instanceof Integer n?n:null;
            service.chuyenQrSangCod(id,kh,dungPhien(s,id));f.addFlashAttribute("success","Đã đổi sang thanh toán khi nhận hàng. Không chuyển khoản QR cho đơn này nữa.");
        }catch(IllegalArgumentException e){f.addFlashAttribute("error",e.getMessage());}
        catch(DataAccessException e){f.addFlashAttribute("error","Chưa đổi được phương thức thanh toán.");}
        return "redirect:/thanh-toan/qr/"+id;
    }
}
