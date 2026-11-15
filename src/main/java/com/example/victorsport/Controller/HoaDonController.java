package com.example.victorsport.Controller;

import com.example.victorsport.Service.HoaDonService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

@Controller
public class HoaDonController {
    private final HoaDonService hoaDonService;

    public HoaDonController(HoaDonService hoaDonService) {
        this.hoaDonService = hoaDonService;
    }

    @GetMapping("/quan-ly/hoa-don")
    public String hienThiDanhSach(@RequestParam(value = "tuKhoa", required = false) String tuKhoa,
                                  @RequestParam(required=false) Integer trangThai,
                                  @RequestParam(defaultValue="") String loaiDon,
                                  @RequestParam(defaultValue="") String thanhToan,
                                  @RequestParam(defaultValue="1") int trang,
                                  @RequestParam(required=false) @org.springframework.format.annotation.DateTimeFormat(iso=org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate tuNgay,
                                  @RequestParam(required=false) @org.springframework.format.annotation.DateTimeFormat(iso=org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate denNgay,
                                  HttpSession phienDangNhap, Model model) {
        if (DangNhapController.layNguoiDungDangNhap(phienDangNhap, model) == null) {
            return "redirect:/login";
        }
        model.addAttribute("activeMenu", "hoa-don");
        model.addAttribute("tuKhoa", tuKhoa);
        model.addAttribute("trangThai",trangThai);model.addAttribute("loaiDon",loaiDon);model.addAttribute("thanhToan",thanhToan);
        model.addAttribute("tuNgay",tuNgay);model.addAttribute("denNgay",denNgay);
        model.addAllAttributes(Map.of("danhSachHoaDon",java.util.List.of(),"trang",1,"soTrang",1,"tongHoaDon",0,
                "tuDong",0,"denDong",0,"trangBatDau",1,"trangKetThuc",1));
        try {model.addAllAttributes(hoaDonService.layTrangHoaDon(tuKhoa,trangThai,loaiDon,thanhToan,tuNgay,denNgay,trang));}
        catch(IllegalArgumentException ex) {model.addAttribute("error",ex.getMessage());model.addAttribute("danhSachHoaDon",java.util.List.of());}
        catch(DataAccessException ex) {model.addAttribute("error","Chưa tải được hóa đơn. Vui lòng thử lại.");model.addAttribute("danhSachHoaDon",java.util.List.of());}
        return "admin/order/show";
    }

    @GetMapping("/quan-ly/hoa-don/{id}")
    public String hienThiChiTiet(@PathVariable Integer id, HttpSession phienDangNhap,
                                 Model model, RedirectAttributes thongBao) {
        if (DangNhapController.layNguoiDungDangNhap(phienDangNhap, model) == null) {
            return "redirect:/login";
        }
        Map<String, Object> hoaDon = hoaDonService.layHoaDon(id);
        if (hoaDon == null) {
            thongBao.addFlashAttribute("error", "Không tìm thấy hóa đơn.");
            return "redirect:/quan-ly/hoa-don";
        }
        model.addAttribute("activeMenu", "hoa-don");
        model.addAttribute("hoaDon", hoaDon);
        model.addAttribute("danhSachSanPham", hoaDonService.laySanPhamTrongHoaDon(id));
        model.addAttribute("lichSu", hoaDonService.layLichSu(id));
        return "admin/order/detail";
    }

    @PostMapping("/quan-ly/hoa-don/{id}/huy")
    public String huyHoaDon(@PathVariable int id,@RequestParam(defaultValue="chi-tiet") String quayLai,
                            HttpSession session,Model model,RedirectAttributes thongBao) {
        var user = DangNhapController.layNguoiDungDangNhap(session,model);
        if (user == null) return "redirect:/login";
        if (!user.isXuLyDonHang()) throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        try {
            hoaDonService.huyHoaDon(id,user.getId());
            thongBao.addFlashAttribute("success","Đã hủy hóa đơn và hoàn lại tồn kho.");
        } catch (IllegalArgumentException e) { thongBao.addFlashAttribute("error",e.getMessage()); }
        catch (DataAccessException e) { thongBao.addFlashAttribute("error","Chưa hủy được hóa đơn. Vui lòng thử lại."); }
        if ("danh-sach".equals(quayLai)) return "redirect:/quan-ly/hoa-don";
        return "redirect:/quan-ly/hoa-don/" + id;
    }

    @PostMapping("/quan-ly/hoa-don/{id}/dia-chi")
    public String suaDiaChi(@PathVariable int id, @RequestParam String ten, @RequestParam String sdt,
                            @RequestParam String diaChi, @RequestParam(required = false) Integer tinh,
                            @RequestParam(required = false) Integer xa, HttpSession session, Model model,
                            RedirectAttributes flash) {
        var user = DangNhapController.layNguoiDungDangNhap(session, model);
        if (user == null) return "redirect:/login";
        if (!user.isQuanLy()) throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        try {
            hoaDonService.suaDiaChiGiaoHang(id, user.getId(), ten, sdt, diaChi, tinh, xa);
            flash.addFlashAttribute("success", "Đã cập nhật thông tin nhận hàng.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            flash.addFlashAttribute("error", e.getMessage());
        } catch (DataAccessException e) {
            flash.addFlashAttribute("error", "Chưa lưu được địa chỉ giao hàng. Vui lòng thử lại.");
        }
        return "redirect:/quan-ly/hoa-don/" + id;
    }

    @PostMapping("/quan-ly/hoa-don/{id}/xac-nhan-thanh-toan")
    public String xacNhanThanhToan(@PathVariable int id,
                                  @RequestParam(defaultValue="chi-tiet") String quayLai,
                                  HttpSession session,Model model,RedirectAttributes thongBao) {
        var user = DangNhapController.layNguoiDungDangNhap(session,model);
        if (user == null) return "redirect:/login";
        if (!user.isXuLyDonHang()) throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        try {
            hoaDonService.xacNhanThanhToan(id,user.getId());
            thongBao.addFlashAttribute("success","Đã xác nhận thanh toán và đơn hàng. Đơn chuyển sang chờ chuẩn bị.");
        } catch (IllegalArgumentException e) { thongBao.addFlashAttribute("error",e.getMessage()); }
        catch (DataAccessException e) { thongBao.addFlashAttribute("error","Chưa xác nhận được thanh toán. Vui lòng thử lại."); }
        if ("van-chuyen".equals(quayLai)) return "redirect:/quan-ly/van-chuyen";
        if ("danh-sach".equals(quayLai)) return "redirect:/quan-ly/hoa-don";
        return "redirect:/quan-ly/hoa-don/" + id;
    }

    @PostMapping("/quan-ly/hoa-don/{id}/so-luong")
    public String suaSoLuong(@PathVariable int id, @RequestParam int idDong, @RequestParam int soLuongCu,
                             @RequestParam int soLuong, HttpSession session, Model model, RedirectAttributes flash) {
        var user = DangNhapController.layNguoiDungDangNhap(session, model);
        if (user == null) return "redirect:/login";
        if (!user.isQuanLy()) throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        try {
            hoaDonService.suaSoLuongDonCod(id, idDong, soLuongCu, soLuong, user.getId());
            flash.addFlashAttribute("success", "Đã cập nhật số lượng, tồn kho và tổng thanh toán.");
        } catch (IllegalArgumentException e) {
            flash.addFlashAttribute("error", e.getMessage());
        } catch (DataAccessException e) {
            flash.addFlashAttribute("error", "Chưa cập nhật được số lượng. Vui lòng tải lại hóa đơn.");
        }
        return "redirect:/quan-ly/hoa-don/" + id;
    }

    @PostMapping("/quan-ly/hoa-don/{id}/chuan-bi")
    public String batDauChuanBi(@PathVariable int id,@RequestParam(defaultValue="chi-tiet") String quayLai,
                                HttpSession session,Model model,RedirectAttributes flash) {
        var user=DangNhapController.layNguoiDungDangNhap(session,model);
        if(user==null) return "redirect:/login";
        if(!user.isXuLyDonHang()) throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        try { hoaDonService.batDauChuanBi(id,user.getId()); flash.addFlashAttribute("success","Đơn hàng chuyển sang trạng thái đang chuẩn bị."); }
        catch(IllegalArgumentException e){flash.addFlashAttribute("error",e.getMessage());}
        catch(DataAccessException e){flash.addFlashAttribute("error","Chưa cập nhật được đơn hàng. Vui lòng thử lại.");}
        if ("van-chuyen".equals(quayLai)) return "redirect:/quan-ly/van-chuyen";
        return "redirect:/quan-ly/hoa-don"+("danh-sach".equals(quayLai)?"":"/"+id);
    }

    @PostMapping("/quan-ly/hoa-don/{id}/gui-hang")
    public String giaoHang(@PathVariable int id,HttpSession session,Model model,RedirectAttributes flash) {
        var user=DangNhapController.layNguoiDungDangNhap(session,model);
        if(user==null) return "redirect:/login";
        if(!user.isXuLyDonHang()) throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        try {
            hoaDonService.banGiaoShipper(id,user.getId());
            flash.addFlashAttribute("success","Đã gửi hàng. Mã vận đơn GHN được tạo tự động cho đơn giao qua GHN.");
        } catch(IllegalArgumentException | IllegalStateException e) {flash.addFlashAttribute("error",e.getMessage());}
        catch(DataAccessException e) {flash.addFlashAttribute("error","Chưa cập nhật được giao hàng. Vui lòng thử lại.");}
        return "redirect:/quan-ly/hoa-don/"+id;
    }

    @PostMapping("/quan-ly/hoa-don/{id}/{thaoTac:xac-nhan-don-cod|giao-khach-vang-lai}")
    public String xuLyCod(@PathVariable int id,@PathVariable String thaoTac,@RequestParam(defaultValue="chi-tiet") String quayLai,
                          HttpSession session,Model model,RedirectAttributes flash) {
        var user=DangNhapController.layNguoiDungDangNhap(session,model);
        if(user==null) return "redirect:/login";
        if(!user.isXuLyDonHang()) throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        try {
            if(thaoTac.equals("xac-nhan-don-cod")) hoaDonService.xacNhanDonCod(id,user.getId());
            else hoaDonService.xacNhanGiaoKhachVangLai(id,user.getId());
            flash.addFlashAttribute("success","Đã cập nhật đơn hàng.");
        } catch(IllegalArgumentException e){flash.addFlashAttribute("error",e.getMessage());}
        catch(DataAccessException e){flash.addFlashAttribute("error","Chưa cập nhật được đơn hàng. Vui lòng thử lại.");}
        if ("van-chuyen".equals(quayLai)) return "redirect:/quan-ly/van-chuyen";
        return "redirect:/quan-ly/hoa-don"+("danh-sach".equals(quayLai)?"":"/"+id);
    }
}
