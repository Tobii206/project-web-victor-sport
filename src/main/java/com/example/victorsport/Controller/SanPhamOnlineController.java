package com.example.victorsport.Controller;

import com.example.victorsport.Service.SanPhamOnlineService;
import com.example.victorsport.Dto.ThanhToanForm;
import com.example.victorsport.Service.KiemTraForm;
import jakarta.validation.Valid;
import org.springframework.validation.BindingResult;
import jakarta.servlet.http.HttpSession;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
public class SanPhamOnlineController {
    private final SanPhamOnlineService service;
    public SanPhamOnlineController(SanPhamOnlineService service) { this.service = service; }

    @GetMapping("/san-pham/{id}")
    public String chiTiet(@PathVariable int id, Model model) {
        try {
            model.addAttribute("sanPham", service.laySanPham(id));
            List<Map<String, Object>> phienBan = service.layPhienBan(id);
            int tongTonKho = 0;
            for (Map<String, Object> mau : phienBan) tongTonKho += ((Number) mau.get("so_luong")).intValue();
            model.addAttribute("phienBan", phienBan);
            model.addAttribute("tongTonKho", tongTonKho);
            model.addAttribute("danhSachAnh", service.layAnh(id));
        } catch (IllegalArgumentException loi) { model.addAttribute("error", loi.getMessage()); }
        catch (DataAccessException loi) { model.addAttribute("error", "Chưa tải được sản phẩm. Vui lòng thử lại sau."); }
        if (model.containsAttribute("sanPham")) {
            try { model.addAttribute("sanPhamCungLoai", service.laySanPhamCungLoai(id)); }
            catch (DataAccessException loi) { model.addAttribute("loiGoiY", "Chưa tải được sản phẩm gợi ý. Vui lòng thử lại sau."); }
        }
        return "KhachHang/san-pham-online";
    }

    @PostMapping("/san-pham/{id}/chon")
    public String chon(@PathVariable int id, @RequestParam int idPhienBan, @RequestParam int soLuong,
                       @RequestParam(defaultValue = "them") String thaoTac, HttpSession session, RedirectAttributes tb) {
        try {
            Map<String, Object> pb = service.layPhienBanTheoId(idPhienBan);
            if (((Number) pb.get("id_san_pham")).intValue() != id) { throw new IllegalArgumentException("Mẫu giày không thuộc sản phẩm này."); }
            service.kiemTraSoLuong(pb, soLuong);
            if ("mua".equals(thaoTac)) {
                Map<Integer, Integer> muaNgay = new LinkedHashMap<>();
                muaNgay.put(idPhienBan, soLuong);
                session.setAttribute("muaNgayOnline", muaNgay);
                return "redirect:/thanh-toan?loai=mua";
            }
            Integer kh=(Integer)session.getAttribute("khachHangOnlineDangNhap");
            if(kh!=null) session.setAttribute("gioHangOnline",service.themVaoGioDaLuu(kh,idPhienBan,soLuong));
            else {
                Map<Integer, Integer> gio = layGio(session, "gioHangOnline");
                int tong = gio.getOrDefault(idPhienBan, 0) + soLuong;
                service.kiemTraSoLuong(pb, tong);
                gio.put(idPhienBan, tong);
            }
            tb.addFlashAttribute("success", "Đã thêm sản phẩm vào giỏ hàng.");
        } catch (IllegalArgumentException loi) { tb.addFlashAttribute("error", loi.getMessage()); }
        catch (DataAccessException loi) { tb.addFlashAttribute("error", "Chưa thể thêm sản phẩm. Vui lòng thử lại."); }
        return "redirect:/san-pham/" + id;
    }

    @GetMapping("/gio-hang")
    public String gioHang(HttpSession session, Model model) {
        hienThiGio(session,"gioHangOnline", model);
        return "KhachHang/gio-hang-online";
    }

    @PostMapping("/gio-hang/sua")
    public String sua(@RequestParam int idPhienBan, @RequestParam int soLuong, HttpSession session, RedirectAttributes tb) {
        try {
            Integer kh=(Integer)session.getAttribute("khachHangOnlineDangNhap");
            if(kh!=null) {
                session.setAttribute("gioHangOnline",service.suaGioDaLuu(kh,idPhienBan,soLuong));
                return "redirect:/gio-hang";
            }
            Map<Integer, Integer> gio = layGio(session, "gioHangOnline");
            if (soLuong <= 0) { gio.remove(idPhienBan); }
            else if (gio.containsKey(idPhienBan)) {
                service.kiemTraSoLuong(service.layPhienBanTheoId(idPhienBan), soLuong);
                gio.put(idPhienBan, soLuong);
            }
        } catch (IllegalArgumentException loi) { tb.addFlashAttribute("error", loi.getMessage()); }
        catch (DataAccessException loi) { tb.addFlashAttribute("error", "Chưa cập nhật được giỏ hàng."); }
        return "redirect:/gio-hang";
    }

    @PostMapping("/gio-hang/xoa-khi-tai-lai")
    @ResponseBody
    @ResponseStatus(org.springframework.http.HttpStatus.NO_CONTENT)
    public void xoaGioKhachVangLai(HttpSession session) {
        synchronized (session) {
            if (session.getAttribute("khachHangOnlineDangNhap") == null
                    && session.getAttribute("nguoiDungDangNhap") == null) {
                session.removeAttribute("gioHangOnline");
                session.removeAttribute("muaNgayOnline");
            }
        }
    }

    @GetMapping("/thanh-toan")
    public String thanhToan(@RequestParam(defaultValue = "gio") String loai, HttpSession session, Model model) {
        model.addAttribute("loai", "mua".equals(loai) ? "mua" : "gio");
        hienThiGio(session, khoaGio(loai), model);
        return "KhachHang/thanh-toan-online";
    }

    @PostMapping("/thanh-toan")
    public String datHang(@RequestParam(defaultValue = "gio") String loai,
                          @Valid @ModelAttribute ThanhToanForm form, BindingResult loiForm, HttpSession session, RedirectAttributes tb) {
        String ten = form.getTen();
        String sdt = form.getSdt();
        String diaChi = form.getDiaChi();
        synchronized (session) {
            try {
                KiemTraForm.kiemTra(loiForm);
                Map<Integer, Integer> gio = layGio(session, khoaGio(loai));
                Integer kh = (Integer) session.getAttribute("khachHangOnlineDangNhap");
                Integer id = service.datHang(gio, kh, ten, sdt, diaChi,"mua".equals(loai));
                gio.clear();
                tb.addFlashAttribute("success", "Đặt hàng thành công. Mã hóa đơn: HD" + String.format("%05d", id) + ". Thanh toán khi nhận hàng.");
                return "redirect:/gio-hang";
            } catch (IllegalArgumentException loi) { tb.addFlashAttribute("error", loi.getMessage()); }
            catch (DataAccessException loi) { tb.addFlashAttribute("error", "Chưa đặt được hàng. Vui lòng thử lại sau."); }
            tb.addFlashAttribute("ten", ten); tb.addFlashAttribute("sdt", sdt); tb.addFlashAttribute("diaChi", diaChi);
            return "redirect:/thanh-toan?loai=" + ("mua".equals(loai) ? "mua" : "gio");
        }
    }

    private void hienThiGio(HttpSession session,String khoa, Model model) {
        try {
            Map<Integer,Integer> gio=layGio(session,khoa);
            model.addAttribute("gioDaChon", gio);
            List<Map<String, Object>> ds = service.layGioHang(gio);
            model.addAttribute("danhSach", ds); model.addAttribute("tongTien", service.tinhTong(ds));
        } catch (IllegalArgumentException loi) { model.addAttribute("error", loi.getMessage()); }
        catch (DataAccessException loi) { model.addAttribute("error", "Chưa tải được giỏ hàng. Vui lòng thử lại."); }
    }

    private String khoaGio(String loai) { return "mua".equals(loai) ? "muaNgayOnline" : "gioHangOnline"; }

    @SuppressWarnings("unchecked")
    private Map<Integer, Integer> layGio(HttpSession session, String khoa) {
        Integer kh=(Integer)session.getAttribute("khachHangOnlineDangNhap");
        if(kh!=null && "gioHangOnline".equals(khoa)) {
            Map<Integer,Integer> gio=service.layGioDaLuu(kh);
            session.setAttribute(khoa,gio);
            return gio;
        }
        Map<Integer, Integer> gio = (Map<Integer, Integer>) session.getAttribute(khoa);
        if (gio == null) { gio = new LinkedHashMap<>(); session.setAttribute(khoa, gio); }
        return gio;
    }
}
