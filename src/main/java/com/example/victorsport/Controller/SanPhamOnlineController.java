package com.example.victorsport.Controller;

import com.example.victorsport.Service.SanPhamOnlineService;
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
            model.addAttribute("phienBan", service.layPhienBan(id));
            model.addAttribute("danhSachAnh", service.layAnh(id));
        } catch (IllegalArgumentException loi) { model.addAttribute("error", loi.getMessage()); }
        catch (DataAccessException loi) { model.addAttribute("error", "Chưa tải được sản phẩm. Vui lòng thử lại sau."); }
        return "san-pham-online";
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
            Map<Integer, Integer> gio = layGio(session, "gioHangOnline");
            int tong = gio.getOrDefault(idPhienBan, 0) + soLuong;
            service.kiemTraSoLuong(pb, tong);
            gio.put(idPhienBan, tong);
            tb.addFlashAttribute("success", "Đã thêm sản phẩm vào giỏ hàng.");
        } catch (IllegalArgumentException loi) { tb.addFlashAttribute("error", loi.getMessage()); }
        catch (DataAccessException loi) { tb.addFlashAttribute("error", "Chưa thể thêm sản phẩm. Vui lòng thử lại."); }
        return "redirect:/san-pham/" + id;
    }

    @GetMapping("/gio-hang")
    public String gioHang(HttpSession session, Model model) {
        hienThiGio(layGio(session, "gioHangOnline"), model);
        return "gio-hang-online";
    }

    @PostMapping("/gio-hang/sua")
    public String sua(@RequestParam int idPhienBan, @RequestParam int soLuong, HttpSession session, RedirectAttributes tb) {
        Map<Integer, Integer> gio = layGio(session, "gioHangOnline");
        try {
            if (soLuong <= 0) { gio.remove(idPhienBan); }
            else if (gio.containsKey(idPhienBan)) {
                service.kiemTraSoLuong(service.layPhienBanTheoId(idPhienBan), soLuong);
                gio.put(idPhienBan, soLuong);
            }
        } catch (IllegalArgumentException loi) { tb.addFlashAttribute("error", loi.getMessage()); }
        catch (DataAccessException loi) { tb.addFlashAttribute("error", "Chưa cập nhật được giỏ hàng."); }
        return "redirect:/gio-hang";
    }

    @GetMapping("/thanh-toan")
    public String thanhToan(@RequestParam(defaultValue = "gio") String loai, HttpSession session, Model model) {
        model.addAttribute("loai", "mua".equals(loai) ? "mua" : "gio");
        hienThiGio(layGio(session, khoaGio(loai)), model);
        return "thanh-toan-online";
    }

    @PostMapping("/thanh-toan")
    public String datHang(@RequestParam(defaultValue = "gio") String loai, @RequestParam String ten,
                          @RequestParam String sdt, @RequestParam String diaChi, HttpSession session, RedirectAttributes tb) {
        synchronized (session) {
            Map<Integer, Integer> gio = layGio(session, khoaGio(loai));
            try {
                Integer kh = (Integer) session.getAttribute("khachHangOnlineDangNhap");
                Integer id = service.datHang(gio, kh, ten, sdt, diaChi);
                gio.clear();
                tb.addFlashAttribute("success", "Đặt hàng thành công. Mã hóa đơn: HD" + String.format("%05d", id) + ". Thanh toán khi nhận hàng.");
                return "redirect:/gio-hang";
            } catch (IllegalArgumentException loi) { tb.addFlashAttribute("error", loi.getMessage()); }
            catch (DataAccessException loi) { tb.addFlashAttribute("error", "Chưa đặt được hàng. Vui lòng thử lại sau."); }
            tb.addFlashAttribute("ten", ten); tb.addFlashAttribute("sdt", sdt); tb.addFlashAttribute("diaChi", diaChi);
            return "redirect:/thanh-toan?loai=" + ("mua".equals(loai) ? "mua" : "gio");
        }
    }

    private void hienThiGio(Map<Integer, Integer> gio, Model model) {
        model.addAttribute("gioDaChon", gio);
        try {
            List<Map<String, Object>> ds = service.layGioHang(gio);
            model.addAttribute("danhSach", ds); model.addAttribute("tongTien", service.tinhTong(ds));
        } catch (IllegalArgumentException loi) { model.addAttribute("error", loi.getMessage()); }
        catch (DataAccessException loi) { model.addAttribute("error", "Chưa tải được giỏ hàng. Vui lòng thử lại."); }
    }

    private String khoaGio(String loai) { return "mua".equals(loai) ? "muaNgayOnline" : "gioHangOnline"; }

    @SuppressWarnings("unchecked")
    private Map<Integer, Integer> layGio(HttpSession session, String khoa) {
        Map<Integer, Integer> gio = (Map<Integer, Integer>) session.getAttribute(khoa);
        if (gio == null) { gio = new LinkedHashMap<>(); session.setAttribute(khoa, gio); }
        return gio;
    }
}
