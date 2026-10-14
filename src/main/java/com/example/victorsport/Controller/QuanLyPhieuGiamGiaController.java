package com.example.victorsport.Controller;

import com.example.victorsport.Dto.PhieuGiamGiaForm;
import com.example.victorsport.Dto.NguoiDungDangNhap;
import com.example.victorsport.Service.QuanLyPhieuGiamGiaService;
import com.example.victorsport.Service.KiemTraForm;
import jakarta.servlet.http.HttpSession;
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
@RequestMapping("/quan-ly/phieu-giam-gia")
public class QuanLyPhieuGiamGiaController {
    private final QuanLyPhieuGiamGiaService service;
    public QuanLyPhieuGiamGiaController(QuanLyPhieuGiamGiaService service) { this.service = service; }

    private NguoiDungDangNhap quanLy(HttpSession session, Model model) {
        var user = DangNhapController.layNguoiDungDangNhap(session, model);
        if (user != null && !user.isQuanLy()) throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        model.addAttribute("activeMenu", "phieu-giam-gia");
        model.addAttribute("tenChucNang", "Quản lý phiếu giảm giá");
        return user;
    }

    @GetMapping
    public String danhSach(@RequestParam(defaultValue="") String tuKhoa,
                           @RequestParam(defaultValue="") String trangThai, HttpSession session, Model model) {
        if (quanLy(session, model) == null) return "redirect:/login";
        model.addAttribute("tuKhoa", tuKhoa); model.addAttribute("trangThai", trangThai);
        try { model.addAttribute("danhSach", service.tim(tuKhoa, trangThai)); }
        catch (DataAccessException e) { model.addAttribute("error", "Chưa tải được danh sách. Vui lòng thử lại."); }
        return "Admin/quan-ly-phieu-giam-gia";
    }

    @GetMapping("/them")
    public String them(HttpSession session, Model model) {
        if (quanLy(session, model) == null) return "redirect:/login";
        model.addAttribute("duongDanLuu", "/quan-ly/phieu-giam-gia/luu");
        if (!model.containsAttribute("uuDai")) model.addAttribute("uuDai", Map.of("trang_thai", true, "so_luong_su_dung", 0));
        return "Admin/sua-phieu-giam-gia";
    }

    @GetMapping("/{id}")
    public String sua(@PathVariable int id, HttpSession session, Model model, RedirectAttributes flash) {
        if (quanLy(session, model) == null) return "redirect:/login";
        try {
            var data = service.lay(id);
            if (!model.containsAttribute("uuDai")) model.addAttribute("uuDai", data);
            model.addAttribute("idUuDai", id);
            model.addAttribute("duongDanLuu", "/quan-ly/phieu-giam-gia/" + id + "/luu");
            model.addAttribute("maUuDai", data.get("ma_phieu_giam_gia"));
            
            return "Admin/sua-phieu-giam-gia";
        } catch (IllegalArgumentException e) { flash.addFlashAttribute("error", e.getMessage()); }
        catch (DataAccessException e) { flash.addFlashAttribute("error", "Chưa tải được chi tiết. Vui lòng thử lại."); }
        return "redirect:/quan-ly/phieu-giam-gia";
    }

    @PostMapping({"/luu", "/{id}/luu"})
    public String luu(@PathVariable(required=false) Integer id,
                      @Valid @ModelAttribute PhieuGiamGiaForm form, BindingResult loiForm,
                      @RequestParam Map<String,String> data, HttpSession session, Model model, RedirectAttributes flash) {
        if (quanLy(session, model) == null) return "redirect:/login";
        try {
            KiemTraForm.kiemTra(loiForm);
            int ketQua = service.luu(id, form);
            flash.addFlashAttribute("success", "Đã lưu phiếu giảm giá.");
            return "redirect:/quan-ly/phieu-giam-gia/" + ketQua;
        } catch (IllegalArgumentException e) { flash.addFlashAttribute("error", e.getMessage()); }
        catch (DataAccessException e) { flash.addFlashAttribute("error", "Không lưu được phiếu giảm giá. Vui lòng thử lại."); }
        Map<String,Object> nhap = new LinkedHashMap<>(data);
        nhap.put("trang_thai", "1".equals(data.get("trang_thai")));
        nhap.put("loai_phieu_giam_gia", "1".equals(data.get("loai_phieu_giam_gia")));
        flash.addFlashAttribute("uuDai", nhap);
        return "redirect:/quan-ly/phieu-giam-gia/" + (id == null ? "them" : id);
    }

    @PostMapping("/{id}/ngung")
    public String ngung(@PathVariable int id, HttpSession session, Model model, RedirectAttributes flash) {
        if (quanLy(session, model) == null) return "redirect:/login";
        try { service.ngung(id); flash.addFlashAttribute("success", "Đã ngừng áp dụng phiếu giảm giá."); }
        catch (IllegalArgumentException e) { flash.addFlashAttribute("error", e.getMessage()); }
        catch (DataAccessException e) { flash.addFlashAttribute("error", "Chưa cập nhật được trạng thái. Vui lòng thử lại."); }
        return "redirect:/quan-ly/phieu-giam-gia";
    }
}
