package com.example.victorsport.Controller;

import com.example.victorsport.Service.TraHangService;
import com.example.victorsport.Service.TraHangService.Quyen;
import jakarta.servlet.http.*;
import java.util.*;
import org.springframework.dao.DataAccessException;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class TraHangController {
    private final TraHangService service;
    public TraHangController(TraHangService service){this.service=service;}
    private boolean quanTri(HttpServletRequest r){return r.getRequestURI().substring(r.getContextPath().length()).startsWith("/quan-ly/");}
    private Quyen quyen(int hd,boolean admin,HttpSession s,Model m){
        if(admin){var u=DangNhapController.layNguoiDungDangNhap(s,m);if(u==null||!u.isXuLyDonHang())throw new ResponseStatusException(HttpStatus.FORBIDDEN);return new Quyen(null,false,u.getId());}
        Integer kh=s.getAttribute("khachHangOnlineDangNhap") instanceof Integer n?n:null;
        if(kh==null&&!DonVangLaiController.dungPhien(s,hd))throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        return new Quyen(kh,kh==null&&DonVangLaiController.dungPhien(s,hd),null);
    }
    private void model(Model m,HttpSession s,boolean admin){
        if(s.getAttribute("csrfTraHang")==null)s.setAttribute("csrfTraHang",UUID.randomUUID().toString());
        m.addAttribute("csrfTraHang",s.getAttribute("csrfTraHang"));m.addAttribute("quanTri",admin);
        m.addAttribute("baseTra",admin?"/quan-ly/tra-hang":"/tra-hang");m.addAttribute("activeMenu","tra-hang");
        m.addAttribute("trangThaiTra",TraHangService.TRANG_THAI);m.addAttribute("nganHang",TraHangService.NGAN_HANG);
    }
    private void csrf(HttpSession s,String v){if(v==null||v.isBlank()||!v.equals(s.getAttribute("csrfTraHang")))throw new ResponseStatusException(HttpStatus.FORBIDDEN);}
    @ExceptionHandler(org.springframework.web.multipart.MaxUploadSizeExceededException.class)
    public String anhQuaLon(HttpServletRequest r,RedirectAttributes flash){
        flash.addFlashAttribute("error","Mỗi ảnh minh chứng tối đa 5 MB, tối đa 5 ảnh cho một yêu cầu.");
        String path=r.getRequestURI().substring(r.getContextPath().length());
        return "redirect:"+(path.matches("/(quan-ly/)?tra-hang/don/[0-9]+")?path:"/don-hang");
    }
    @GetMapping({"/tra-hang/don/{id}","/quan-ly/tra-hang/don/{id}"})
    public String form(@PathVariable int id,HttpServletRequest r,HttpServletResponse response,HttpSession s,Model m){
        response.setHeader("Cache-Control","no-store");boolean admin=quanTri(r);var q=quyen(id,admin,s,m);model(m,s,admin);
        try{m.addAllAttributes(service.form(id,q));}catch(IllegalArgumentException ex){throw new ResponseStatusException(HttpStatus.NOT_FOUND,ex.getMessage());}
        m.addAttribute("maTao",UUID.randomUUID().toString());return "client/return/show";
    }
    @PostMapping({"/tra-hang/don/{id}","/quan-ly/tra-hang/don/{id}"})
    public String tao(@PathVariable int id,@RequestParam Map<String,String> f,@RequestParam(required=false) List<Integer> dong,
                      @RequestParam(required=false) List<MultipartFile> minhChung,HttpServletRequest r,HttpSession s,Model m,RedirectAttributes flash){
        csrf(s,f.get("csrf"));boolean admin=quanTri(r);var q=quyen(id,admin,s,m);String base=admin?"/quan-ly/tra-hang":"/tra-hang";
        try{int yc=service.tao(id,q,TraHangService.soLuong(dong,f,"sl_"),f,minhChung);flash.addFlashAttribute("success","Đã gửi yêu cầu trả hàng.");return "redirect:"+base+"/"+yc;}
        catch(IllegalArgumentException ex){flash.addFlashAttribute("error",ex.getMessage());}
        catch(DataAccessException ex){flash.addFlashAttribute("error","Chưa tạo được yêu cầu. Vui lòng tải lại trang và thử lại.");}
        flash.addFlashAttribute("formTraHang",f);flash.addFlashAttribute("dongDaChon",dong==null?List.of():dong);return "redirect:"+base+"/don/"+id;
    }
    @GetMapping({"/tra-hang/{id:[0-9]+}","/quan-ly/tra-hang/{id:[0-9]+}"})
    public String chiTiet(@PathVariable int id,HttpServletRequest r,HttpServletResponse response,HttpSession s,Model m){
        response.setHeader("Cache-Control","no-store");boolean admin=quanTri(r);
        try{var q=quyen(service.idHoaDon(id),admin,s,m);model(m,s,admin);m.addAllAttributes(service.chiTiet(id,q));}
        catch(IllegalArgumentException ex){throw new ResponseStatusException(HttpStatus.NOT_FOUND,ex.getMessage());}
        return admin?"admin/return/show":"client/return/show";
    }
    @GetMapping({"/tra-hang/{id}/anh/{anh}","/quan-ly/tra-hang/{id}/anh/{anh}"}) @ResponseBody
    public ResponseEntity<byte[]> anh(@PathVariable int id,@PathVariable int anh,HttpServletRequest r,HttpSession s,Model m){
        try{var q=quyen(service.idHoaDon(id),quanTri(r),s,m);byte[] data=service.anh(id,anh,q);
            return ResponseEntity.ok().cacheControl(CacheControl.noStore()).header("X-Content-Type-Options","nosniff").contentType(MediaType.IMAGE_PNG).body(data);}
        catch(IllegalArgumentException ex){throw new ResponseStatusException(HttpStatus.NOT_FOUND);}
    }
    @PostMapping("/tra-hang/{id}/{action:huy|gui-hang}")
    public String khachXuLy(@PathVariable int id,@PathVariable String action,@RequestParam Map<String,String> f,HttpSession s,Model m,RedirectAttributes flash){
        csrf(s,f.get("csrf"));
        try{var q=quyen(service.idHoaDon(id),false,s,m);service.khachXuLy(id,q,action,f.get("hangVanChuyen"),f.get("maVanDon"));flash.addFlashAttribute("success","Đã cập nhật yêu cầu trả hàng.");}
        catch(IllegalArgumentException ex){flash.addFlashAttribute("error",ex.getMessage());}
        catch(DataAccessException ex){flash.addFlashAttribute("error","Chưa cập nhật được yêu cầu. Vui lòng thử lại.");}
        return "redirect:/tra-hang/"+id;
    }
    @GetMapping("/quan-ly/tra-hang")
    public String danhSach(@RequestParam(defaultValue="") String tuKhoa,@RequestParam(defaultValue="") String trangThai,
            @RequestParam(defaultValue="1") int trang,HttpSession s,Model m,HttpServletResponse response){
        response.setHeader("Cache-Control","no-store");var q=quyen(0,true,s,m);model(m,s,true);
        m.addAttribute("tuKhoa",tuKhoa);m.addAttribute("trangThai",trangThai);
        try{m.addAllAttributes(service.danhSach(q,tuKhoa,trangThai,trang));}
        catch(IllegalArgumentException ex){m.addAttribute("error",ex.getMessage());}
        return "admin/return/show";
    }
    @GetMapping("/quan-ly/tra-hang/hang-loi")
    public String hangLoi(HttpSession s,Model m,HttpServletResponse response){
        response.setHeader("Cache-Control","no-store");var q=quyen(0,true,s,m);model(m,s,true);m.addAttribute("hangLoi",service.hangLoi(q));m.addAttribute("xemHangLoi",true);return "admin/return/show";
    }
    @PostMapping("/quan-ly/tra-hang/{id}/xu-ly")
    public String adminXuLy(@PathVariable int id,@RequestParam String action,@RequestParam Map<String,String> f,HttpSession s,Model m,RedirectAttributes flash){
        csrf(s,f.get("csrf"));var q=quyen(0,true,s,m);
        try{service.quanTri(id,q,action,f);flash.addFlashAttribute("success","Đã cập nhật trả hàng và hoàn tiền.");}
        catch(IllegalArgumentException ex){flash.addFlashAttribute("error",ex.getMessage());flash.addFlashAttribute("formXuLy",f);}
        catch(DataAccessException ex){flash.addFlashAttribute("error","Chưa xử lý được yêu cầu. Không có thay đổi tiền hoặc tồn kho được lưu.");}
        return "redirect:/quan-ly/tra-hang/"+id;
    }
}
