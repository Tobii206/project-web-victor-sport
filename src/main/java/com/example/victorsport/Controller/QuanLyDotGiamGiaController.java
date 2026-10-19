package com.example.victorsport.Controller;

import com.example.victorsport.Dto.DotGiamGiaForm;
import com.example.victorsport.Dto.NguoiDungDangNhap;
import com.example.victorsport.Dto.SanPhamGiamGiaForm;
import com.example.victorsport.Service.QuanLyDotGiamGiaService;
import com.example.victorsport.Service.KiemTraForm;
import com.example.victorsport.Service.EmailDotGiamGiaService;
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
@RequestMapping("/quan-ly/dot-giam-gia")
public class QuanLyDotGiamGiaController {
    private final QuanLyDotGiamGiaService service;
    private final EmailDotGiamGiaService email;
    public QuanLyDotGiamGiaController(QuanLyDotGiamGiaService service, EmailDotGiamGiaService email) {
        this.service = service;this.email = email;
    }

    private NguoiDungDangNhap quanLy(HttpSession session, Model model) {
        var user = DangNhapController.layNguoiDungDangNhap(session, model);
        if (user != null && !user.isQuanLy()) throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        model.addAttribute("activeMenu", "dot-giam-gia");
        model.addAttribute("tenChucNang", "Quản lý đợt giảm giá");
        return user;
    }

    @GetMapping
    public String danhSach(@RequestParam(defaultValue="") String tuKhoa,
                           @RequestParam(defaultValue="") String trangThai, HttpSession session, Model model) {
        if (quanLy(session, model) == null) return "redirect:/login";
        model.addAttribute("tuKhoa", tuKhoa); model.addAttribute("trangThai", trangThai);
        try { model.addAttribute("danhSach", service.tim(tuKhoa, trangThai)); }
        catch (DataAccessException e) { model.addAttribute("error", "Chưa tải được danh sách. Vui lòng thử lại."); }
        return "Admin/quan-ly-dot-giam-gia";
    }

    @GetMapping("/them")
    public String them(HttpSession session, Model model) {
        if (quanLy(session, model) == null) return "redirect:/login";
        model.addAttribute("duongDanLuu", "/quan-ly/dot-giam-gia/luu");
        if (!model.containsAttribute("uuDai")) model.addAttribute("uuDai", Map.of("trang_thai", true));
        try { model.addAttribute("cacMucUuTien", service.cacMucUuTien()); }
        catch (DataAccessException e) { model.addAttribute("error", "Chưa tải được mức ưu tiên. Vui lòng thử lại."); }
        return "Admin/sua-dot-giam-gia";
    }

    @GetMapping("/{id}")
    public String sua(@PathVariable int id, HttpSession session, Model model, RedirectAttributes flash) {
        if (quanLy(session, model) == null) return "redirect:/login";
        try {
            var data = service.lay(id);
            model.addAttribute("cacMucUuTien", service.cacMucUuTien());
            if (!model.containsAttribute("uuDai")) model.addAttribute("uuDai", data);
            model.addAttribute("idUuDai", id);
            model.addAttribute("duongDanLuu", "/quan-ly/dot-giam-gia/" + id + "/luu");
            model.addAttribute("maUuDai", data.get("ma_dot_giam_gia"));
            model.addAttribute("cacMau", service.cacMau());
            model.addAttribute("mauDaChon", service.mauDaChon(id));
            return "Admin/sua-dot-giam-gia";
        } catch (IllegalArgumentException e) { flash.addFlashAttribute("error", e.getMessage()); }
        catch (DataAccessException e) { flash.addFlashAttribute("error", "Chưa tải được chi tiết. Vui lòng thử lại."); }
        return "redirect:/quan-ly/dot-giam-gia";
    }

    @PostMapping({"/luu", "/{id}/luu"})
    public String luu(@PathVariable(required=false) Integer id,
                      @Valid @ModelAttribute DotGiamGiaForm form, BindingResult loiForm,
                      @RequestParam Map<String,String> data, HttpSession session, Model model, RedirectAttributes flash) {
        if (quanLy(session, model) == null) return "redirect:/login";
        try {
            KiemTraForm.kiemTra(loiForm);
            var ketQua = service.luuVaKiemTraThongBao(id, form);
            String thongBao = "Đã lưu đợt giảm giá.";
            if (ketQua.guiEmail()) {
                boolean daXepLich = id == null ? email.thongBaoDotMoi(ketQua.id()) : email.thongBaoBatLai(ketQua.id());
                thongBao += daXepLich
                    ? " Hệ thống đang gửi email thông báo đến khách hàng đã đăng ký."
                    : " Chưa gửi được thông báo email. Vui lòng kiểm tra cấu hình gửi mail hoặc hàng đợi gửi.";
            }
            flash.addFlashAttribute("success", thongBao);
            return "redirect:/quan-ly/dot-giam-gia/" + ketQua.id();
        } catch (IllegalArgumentException e) { flash.addFlashAttribute("error", e.getMessage()); }
        catch (DataAccessException e) { flash.addFlashAttribute("error", "Không lưu được đợt giảm giá. Vui lòng thử lại."); }
        Map<String,Object> nhap = new LinkedHashMap<>(data);
        nhap.put("trang_thai", "1".equals(data.get("trang_thai")));
        nhap.put("loai_giam_gia", "1".equals(data.get("loai_giam_gia")));
        flash.addFlashAttribute("uuDai", nhap);
        return "redirect:/quan-ly/dot-giam-gia/" + (id == null ? "them" : id);
    }

    @PostMapping("/{id}/ngung")
    public String ngung(@PathVariable int id, HttpSession session, Model model, RedirectAttributes flash) {
        if (quanLy(session, model) == null) return "redirect:/login";
        try { service.ngung(id); flash.addFlashAttribute("success", "Đã ngừng áp dụng đợt giảm giá."); }
        catch (IllegalArgumentException e) { flash.addFlashAttribute("error", e.getMessage()); }
        catch (DataAccessException e) { flash.addFlashAttribute("error", "Chưa cập nhật được trạng thái. Vui lòng thử lại."); }
        return "redirect:/quan-ly/dot-giam-gia";
    }

    @PostMapping("/{id}/mau/luu")
    public String luuMau(@PathVariable int id, @Valid @ModelAttribute SanPhamGiamGiaForm form, BindingResult loiForm,
                        @RequestParam Map<String,String> data, HttpSession session, Model model, RedirectAttributes flash) {
        var user = quanLy(session, model); if (user == null) return "redirect:/login";
        try {
            KiemTraForm.kiemTra(loiForm); service.luuMau(id, form, user.getId());
            flash.addFlashAttribute("success", "Đã áp dụng ưu đãi cho mẫu giày.");
        } catch (IllegalArgumentException e) { flash.addFlashAttribute("error", e.getMessage()); flash.addFlashAttribute("mauForm", data); }
        catch (DataAccessException e) { flash.addFlashAttribute("error", "Không lưu được mẫu giảm giá. Vui lòng thử lại."); flash.addFlashAttribute("mauForm", data); }
        return "redirect:/quan-ly/dot-giam-gia/" + id + "#mau-giam-gia";
    }

    @PostMapping("/{id}/mau/{mau}/bo")
    public String boMau(@PathVariable int id, @PathVariable int mau, HttpSession session, Model model, RedirectAttributes flash) {
        var user = quanLy(session, model); if (user == null) return "redirect:/login";
        try { service.boMau(id, mau, user.getId()); flash.addFlashAttribute("success", "Đã bỏ mẫu giày khỏi đợt giảm giá."); }
        catch (IllegalArgumentException e) { flash.addFlashAttribute("error", e.getMessage()); }
        catch (DataAccessException e) { flash.addFlashAttribute("error", "Chưa bỏ được mẫu giày. Vui lòng thử lại."); }
        return "redirect:/quan-ly/dot-giam-gia/" + id + "#mau-giam-gia";
    }
}
