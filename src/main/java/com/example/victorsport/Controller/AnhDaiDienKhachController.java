package com.example.victorsport.Controller;

import com.example.victorsport.Service.AnhDaiDienKhachService;
import com.example.victorsport.Dto.NguoiDungDangNhap;
import jakarta.servlet.http.HttpSession;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AnhDaiDienKhachController {
    private final AnhDaiDienKhachService service;
    public AnhDaiDienKhachController(AnhDaiDienKhachService service) {this.service=service;}
    @PostMapping("/tai-khoan/anh-dai-dien")
    public String doiAnh(@RequestParam(required=false) MultipartFile tepAnh,@RequestParam(required=false) String csrfTaiKhoan,HttpSession session,RedirectAttributes flash) {
        var nhanVien=session.getAttribute(DangNhapController.KHOA_NGUOI_DUNG_DANG_NHAP) instanceof NguoiDungDangNhap nv?nv:null;
        Integer khach=session.getAttribute("khachHangOnlineDangNhap") instanceof Integer id?id:null;
        if(nhanVien==null && khach==null) return "redirect:/login";
        if(csrfTaiKhoan==null || !csrfTaiKhoan.equals(session.getAttribute("csrfTaiKhoan")))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        try {
            if(nhanVien!=null) service.doiAnhNhanVien(nhanVien.getId(),tepAnh);
            else service.doiAnh(khach,tepAnh);
            flash.addFlashAttribute("success","Đã cập nhật ảnh đại diện.");
        }
        catch(IllegalArgumentException e) {flash.addFlashAttribute("error",e.getMessage());}
        catch(IllegalStateException | DataAccessException e) {flash.addFlashAttribute("error","Chưa lưu được ảnh đại diện. Vui lòng thử lại.");}
        return "redirect:/tai-khoan";
    }
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public String quaLon(RedirectAttributes flash) {flash.addFlashAttribute("error","Ảnh đại diện tối đa 5 MB.");return "redirect:/tai-khoan";}
}
