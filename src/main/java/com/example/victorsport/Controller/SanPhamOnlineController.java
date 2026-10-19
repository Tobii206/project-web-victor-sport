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
    private final com.example.victorsport.Service.DiaChiKhachHangService diaChiService;
    public SanPhamOnlineController(SanPhamOnlineService service,com.example.victorsport.Service.DiaChiKhachHangService diaChiService) {
        this.service = service;this.diaChiService=diaChiService;
    }

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
        return "client/product/detail";
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
        return "client/cart/show";
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
    public String thanhToan(@RequestParam(defaultValue = "gio") String loai, HttpSession session, Model model,
                            jakarta.servlet.http.HttpServletResponse response) {
        response.setHeader("Cache-Control","no-store");
        model.addAttribute("loai", "mua".equals(loai) ? "mua" : "gio");
        hienThiGio(session, khoaGio(loai), model);
        model.addAttribute("dungGhn",service.dungGhn());
        if(session.getAttribute("csrfGhn")==null) session.setAttribute("csrfGhn",java.util.UUID.randomUUID().toString());
        model.addAttribute("csrfGhn",session.getAttribute("csrfGhn"));
        var baoGia=session.getAttribute("baoGiaGhn_"+khoaGio(loai)) instanceof com.example.victorsport.Dto.BaoGiaGhn q?q:null;
        if(baoGia!=null && service.dungGhn()) model.addAttribute("baoGiaGhn",baoGia);
        if(model.getAttribute("tongGoc") instanceof java.math.BigDecimal tongGoc) {
            var phiGoc=service.dungGhn()?(baoGia==null?java.math.BigDecimal.ZERO:baoGia.phi()):service.phiGiaoHang();
            var phi=phiGoc;
            model.addAttribute("phiGiaoHangGoc",phiGoc);
            boolean coDot=model.getAttribute("giamTheoDot") instanceof java.math.BigDecimal giam && giam.signum()>0;
            model.addAttribute("coDotGiamGia",coDot);
            try {
                var cacPhieu=service.dungGhn()?service.layPhieuKhaDung(tongGoc,coDot,phiGoc,baoGia==null?null:baoGia.tenTinh()):service.layPhieuKhaDung(tongGoc,coDot);
                model.addAttribute("phieuKhaDung",cacPhieu);
                if(!model.containsAttribute("idPhieuDaChon"))
                    model.addAttribute("idPhieuDaChon",service.chonPhieuTotNhat(cacPhieu));
                for(var phieu:cacPhieu) {
                    if(phieu.get("id").equals(model.getAttribute("idPhieuDaChon"))) {
                        var giamPhieu=(java.math.BigDecimal)phieu.get("tien_giam");
                        var giamDot=(java.math.BigDecimal)model.getAttribute("giamTheoDot");
                        model.addAttribute("tongTien",tongGoc.subtract(giamPhieu.max(giamDot)));
                        phi=phi.subtract((java.math.BigDecimal)phieu.getOrDefault("giam_phi_van_chuyen",java.math.BigDecimal.ZERO));
                        break;
                    }
                }
            }
            catch(DataAccessException e){model.addAttribute("loiPhieu","Chưa tải được phiếu giảm giá. Vui lòng tải lại trang.");}
            model.addAttribute("phiGiaoHang",phi);
            if(model.getAttribute("tongTien") instanceof java.math.BigDecimal tong)
                model.addAttribute("tongTien",tong.add(phi));
        }
        if(baoGia!=null && service.dungGhn()) {
            if(!model.containsAttribute("ten")) model.addAttribute("ten",baoGia.ten());
            if(!model.containsAttribute("sdt")) model.addAttribute("sdt",baoGia.sdt());
            if(!model.containsAttribute("diaChi")) model.addAttribute("diaChi",baoGia.diaChiChiTiet());
            if(!model.containsAttribute("phuongThucDaChon")) model.addAttribute("phuongThucDaChon",baoGia.phuongThuc());
        }
        Integer kh=(Integer)session.getAttribute("khachHangOnlineDangNhap");
        if(kh!=null) {
            try {
                var thongTin=service.layThongTinNguoiNhan(kh);
                if(!model.containsAttribute("ten")) model.addAttribute("ten",thongTin.get("ten_khach_hang"));
                if(!model.containsAttribute("sdt")) model.addAttribute("sdt",thongTin.get("so_dien_thoai"));
                var ds=diaChiService.danhSach(kh);
                model.addAttribute("diaChiDaLuu",ds);
                if(!model.containsAttribute("diaChi") && !ds.isEmpty()) {
                    model.addAttribute("diaChi",ds.get(0).get(service.dungGhn()?"dia_chi_cu_the":"dia_chi_day_du"));
                    model.addAttribute("idDiaChiDaChon",ds.get(0).get("id"));
                }
            } catch(IllegalArgumentException e){model.addAttribute("error",e.getMessage());}
            catch(DataAccessException e){model.addAttribute("error","Chưa tải được địa chỉ đã lưu. Bạn có thể nhập địa chỉ nhận hàng.");}
        }
        return "client/cart/checkout";
    }

    @PostMapping("/thanh-toan/ghn/bao-gia")
    public String baoGia(@RequestParam(defaultValue="gio") String loai,@Valid @ModelAttribute ThanhToanForm form,
                         BindingResult loiForm,@RequestParam(required=false) String csrfGhn,HttpSession session,RedirectAttributes tb) {
        if(csrfGhn==null || !csrfGhn.equals(session.getAttribute("csrfGhn")))
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.FORBIDDEN);
        synchronized(session) {
            session.removeAttribute("baoGiaGhn_"+khoaGio(loai));
            String diaChi=form.getDiaChi();
            try {
                KiemTraForm.kiemTra(loiForm);
                long now=System.currentTimeMillis();
                if(session.getAttribute("lanBaoGiaGhn") instanceof Long lan && now-lan<3000)
                    throw new IllegalArgumentException("Vui lòng đợi vài giây trước khi lấy lại phí.");
                session.setAttribute("lanBaoGiaGhn",now);
                Integer kh=(Integer)session.getAttribute("khachHangOnlineDangNhap");
                if(form.getIdDiaChi()!=null) {
                    if(kh==null) throw new IllegalArgumentException("Vui lòng đăng nhập để dùng địa chỉ đã lưu.");
                    diaChi=diaChiService.chiTiet(kh,form.getIdDiaChi()).get(service.dungGhn()?"dia_chi_cu_the":"dia_chi_day_du").toString();
                }
                if(form.getTinhGhn()==null || form.getXaGhn()==null) throw new IllegalArgumentException("Vui lòng chọn tỉnh/thành và phường/xã giao hàng.");
                var q=service.baoGiaGhn(layGio(session,khoaGio(loai)),form.getTen(),form.getSdt(),diaChi,form.getPhuongThuc(),form.getTinhGhn(),form.getXaGhn());
                session.setAttribute("baoGiaGhn_"+khoaGio(loai),q);
                tb.addFlashAttribute("success","Đã cập nhật phí GHN. Kiểm tra địa chỉ và tổng tiền trước khi đặt hàng.");
            }catch(IllegalArgumentException | IllegalStateException e){tb.addFlashAttribute("error",e.getMessage());}
            catch(DataAccessException e){tb.addFlashAttribute("error","Chưa lấy được phí giao hàng. Vui lòng thử lại.");}
            tb.addFlashAttribute("ten",form.getTen());tb.addFlashAttribute("sdt",form.getSdt());tb.addFlashAttribute("diaChi",diaChi);
            tb.addFlashAttribute("idDiaChiDaChon",form.getIdDiaChi());tb.addFlashAttribute("phuongThucDaChon",form.getPhuongThuc());
            return "redirect:/thanh-toan?loai="+("mua".equals(loai)?"mua":"gio");
        }
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
                if(form.getIdDiaChi()!=null) {
                    if(kh==null) throw new IllegalArgumentException("Vui lòng đăng nhập để sử dụng địa chỉ đã lưu.");
                    diaChi=diaChiService.chiTiet(kh,form.getIdDiaChi()).get(service.dungGhn()?"dia_chi_cu_the":"dia_chi_day_du").toString();
                }
                Integer id;
                if(service.dungGhn()) {
                    var baoGia=session.getAttribute("baoGiaGhn_"+khoaGio(loai)) instanceof com.example.victorsport.Dto.BaoGiaGhn q?q:null;
                    if(baoGia==null || !java.util.Objects.equals(form.getTinhGhn(),baoGia.tinh()) || !java.util.Objects.equals(form.getXaGhn(),baoGia.xa()))
                        throw new IllegalArgumentException("Vui lòng lấy lại phí GHN cho địa chỉ đã chọn.");
                    if(!diaChi.trim().equals(baoGia.diaChiChiTiet()))
                        throw new IllegalArgumentException("Địa chỉ chi tiết đã thay đổi. Vui lòng lấy lại phí GHN.");
                    diaChi=baoGia.diaChi();
                    id=service.datHang(gio,kh,ten,sdt,diaChi,"mua".equals(loai),form.getPhuongThuc(),form.getIdPhieuGiamGia(),form.getTongTienDuKien(),baoGia);
                    session.removeAttribute("baoGiaGhn_"+khoaGio(loai));
                }
                else if(form.getIdPhieuGiamGia()!=null || form.getTongTienDuKien()!=null)
                    id=service.datHang(gio,kh,ten,sdt,diaChi,"mua".equals(loai),form.getPhuongThuc(),form.getIdPhieuGiamGia(),form.getTongTienDuKien());
                else if ("QR".equals(form.getPhuongThuc()))
                    id = service.datHang(gio, kh, ten, sdt, diaChi,"mua".equals(loai),"QR");
                else id = service.datHang(gio, kh, ten, sdt, diaChi,"mua".equals(loai));
                gio.clear();
                if(kh==null) session.setAttribute("donVangLai_"+id,true);
                if ("QR".equals(form.getPhuongThuc())) {
                    if (kh == null) session.setAttribute("donQr_" + id, true);
                    return "redirect:/thanh-toan/qr/" + id;
                }
                tb.addFlashAttribute("success", "Đặt hàng thành công. Mã hóa đơn: HD" + String.format("%05d", id) + ". Thanh toán khi nhận hàng.");
                return kh==null?"redirect:/don-vang-lai/"+id:"redirect:/don-hang/"+id;
            } catch (IllegalArgumentException loi) { tb.addFlashAttribute("error", loi.getMessage()); }
            catch (DataAccessException loi) { tb.addFlashAttribute("error", "Chưa đặt được hàng. Vui lòng thử lại sau."); }
            tb.addFlashAttribute("ten", ten); tb.addFlashAttribute("sdt", sdt); tb.addFlashAttribute("diaChi", diaChi);
            tb.addFlashAttribute("phuongThucDaChon", form.getPhuongThuc());
            tb.addFlashAttribute("idDiaChiDaChon",form.getIdDiaChi());
            tb.addFlashAttribute("idPhieuDaChon",form.getIdPhieuGiamGia());
            return "redirect:/thanh-toan?loai=" + ("mua".equals(loai) ? "mua" : "gio");
        }
    }

    @GetMapping("/thanh-toan/qr/{id}")
    public String thanhToanQr(@PathVariable int id,HttpSession session,Model model,
                             jakarta.servlet.http.HttpServletResponse response) {
        response.setHeader("Cache-Control","no-store");
        Integer kh = (Integer) session.getAttribute("khachHangOnlineDangNhap");
        var thanhToan = service.layThanhToanQr(id,kh,Boolean.TRUE.equals(session.getAttribute("donQr_" + id)));
        if (thanhToan == null || thanhToan.isEmpty()) throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND);
        model.addAttribute("thanhToan",thanhToan);
        model.addAttribute("idHoaDon",id);
        return "client/cart/qr";
    }

    private void hienThiGio(HttpSession session,String khoa, Model model) {
        try {
            Map<Integer,Integer> gio=layGio(session,khoa);
            model.addAttribute("gioDaChon", gio);
            List<Map<String, Object>> ds = service.layGioHang(gio);
            model.addAttribute("danhSach", ds); model.addAttribute("tongTien", service.tinhTong(ds));
            var tongGoc=service.tongTienGoc(ds);
            if(tongGoc!=null) {
                model.addAttribute("tongGoc",tongGoc);
                model.addAttribute("giamTheoDot",tongGoc.subtract(service.tinhTong(ds)));
            }
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
