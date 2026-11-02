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

    private boolean laFreeship(Map<String,Object> data) {
        Object value=data.get("ap_dung_van_chuyen");
        return Boolean.TRUE.equals(value) || value instanceof Number n && n.intValue()==1;
    }
    private void loaiManHinh(Model model,boolean freeship) {
        model.addAttribute("phieuFreeship",freeship);
        model.addAttribute("tenChucNang",freeship?"Quản lý phiếu giảm giá vận chuyển":"Quản lý phiếu giảm tiền hàng");
        model.addAttribute("duongDanDanhSach","/quan-ly/phieu-giam-gia?loai="+(freeship?"1":"0"));
    }

    private NguoiDungDangNhap quanLy(HttpSession session, Model model) {
        var user = DangNhapController.layNguoiDungDangNhap(session, model);
        if (user != null && !user.isQuanLy()) throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        model.addAttribute("activeMenu", "phieu-giam-gia");
        model.addAttribute("tenChucNang", "Quản lý phiếu giảm giá");
        return user;
    }

    @GetMapping
    public String danhSach(@RequestParam(defaultValue="") String tuKhoa,
                           @RequestParam(defaultValue="") String trangThai,@RequestParam(defaultValue="0") String loai, HttpSession session, Model model) {
        if (quanLy(session, model) == null) return "redirect:/login";
        loai="1".equals(loai)?"1":"0";
        model.addAttribute("tuKhoa", tuKhoa); model.addAttribute("trangThai", trangThai);
        model.addAttribute("loai",loai);
        loaiManHinh(model,"1".equals(loai));
        try { model.addAttribute("danhSach", loai.isEmpty()?service.tim(tuKhoa,trangThai):service.tim(tuKhoa,trangThai,loai)); }
        catch (DataAccessException e) { model.addAttribute("error", "Chưa tải được danh sách. Vui lòng thử lại."); }
        return "admin/voucher/show";
    }

    @GetMapping({"/them","/them-freeship"})
    public String them(HttpSession session, Model model,jakarta.servlet.http.HttpServletRequest request) {
        if (quanLy(session, model) == null) return "redirect:/login";
        boolean freeship=request.getRequestURI().endsWith("/them-freeship");
        loaiManHinh(model,freeship);
        model.addAttribute("duongDanLuu", "/quan-ly/phieu-giam-gia/"+(freeship?"luu-freeship":"luu"));
        if (!model.containsAttribute("uuDai")) model.addAttribute("uuDai", Map.of("trang_thai", true, "so_luong_su_dung", 0,"loai_phieu_giam_gia",freeship));
        return "admin/voucher/update";
    }

    @GetMapping("/{id}")
    public String sua(@PathVariable int id, HttpSession session, Model model, RedirectAttributes flash) {
        if (quanLy(session, model) == null) return "redirect:/login";
        try {
            var data = service.lay(id);
            boolean freeship=laFreeship(data);
            loaiManHinh(model,freeship);
            if (!model.containsAttribute("uuDai")) model.addAttribute("uuDai", data);
            model.addAttribute("idUuDai", id);
            model.addAttribute("duongDanLuu", "/quan-ly/phieu-giam-gia/" + id + (freeship?"/luu-freeship":"/luu"));
            model.addAttribute("maUuDai", data.get("ma_phieu_giam_gia"));
            
            return "admin/voucher/update";
        } catch (IllegalArgumentException e) { flash.addFlashAttribute("error", e.getMessage()); }
        catch (DataAccessException e) { flash.addFlashAttribute("error", "Chưa tải được chi tiết. Vui lòng thử lại."); }
        return "redirect:/quan-ly/phieu-giam-gia";
    }

    @PostMapping({"/luu", "/{id}/luu","/luu-freeship","/{id}/luu-freeship"})
    public String luu(@PathVariable(required=false) Integer id,
                      @Valid @ModelAttribute PhieuGiamGiaForm form, BindingResult loiForm,
                      @RequestParam Map<String,String> data, HttpSession session, Model model, RedirectAttributes flash,jakarta.servlet.http.HttpServletRequest request) {
        if (quanLy(session, model) == null) return "redirect:/login";
        boolean freeship=request.getRequestURI().endsWith("/luu-freeship");
        form.setAp_dung_van_chuyen(freeship?1:0);
        if(!freeship) form.setChi_ap_dung_ha_noi(0);
        try {
            KiemTraForm.kiemTra(loiForm);
            if(id!=null && laFreeship(service.lay(id))!=freeship)
                throw new IllegalArgumentException("Vui lòng mở đúng màn hình của loại phiếu cần sửa.");
            int ketQua = service.luu(id, form);
            flash.addFlashAttribute("success", freeship?"Đã lưu phiếu giảm giá vận chuyển.":"Đã lưu phiếu giảm tiền hàng.");
            return "redirect:/quan-ly/phieu-giam-gia/" + ketQua;
        } catch (IllegalArgumentException e) { flash.addFlashAttribute("error", e.getMessage()); }
        catch (DataAccessException e) { flash.addFlashAttribute("error", "Không lưu được phiếu giảm giá. Vui lòng thử lại."); }
        Map<String,Object> nhap = new LinkedHashMap<>(data);
        nhap.put("trang_thai", "1".equals(data.get("trang_thai")));
        nhap.put("loai_phieu_giam_gia", "1".equals(data.get("loai_phieu_giam_gia")));
        nhap.put("ap_dung_van_chuyen", freeship);
        nhap.put("chi_ap_dung_ha_noi", "1".equals(data.get("chi_ap_dung_ha_noi")));
        flash.addFlashAttribute("uuDai", nhap);
        return "redirect:/quan-ly/phieu-giam-gia/" + (id == null ? (freeship?"them-freeship":"them") : id);
    }

    @PostMapping("/{id}/ngung")
    public String ngung(@PathVariable int id, HttpSession session, Model model, RedirectAttributes flash) {
        if (quanLy(session, model) == null) return "redirect:/login";
        boolean freeship=false;
        try { freeship=laFreeship(service.lay(id));service.ngung(id); flash.addFlashAttribute("success", "Đã ngừng áp dụng phiếu."); }
        catch (IllegalArgumentException e) { flash.addFlashAttribute("error", e.getMessage()); }
        catch (DataAccessException e) { flash.addFlashAttribute("error", "Chưa cập nhật được trạng thái. Vui lòng thử lại."); }
        return "redirect:/quan-ly/phieu-giam-gia?loai="+(freeship?"1":"0");
    }
}
