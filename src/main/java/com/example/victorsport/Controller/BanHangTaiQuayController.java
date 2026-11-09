package com.example.victorsport.Controller;

import com.example.victorsport.Dto.GioHangTaiQuay;
import com.example.victorsport.Dto.KhachHangTaiQuayForm;
import com.example.victorsport.Dto.GiaoHangTaiQuayForm;
import com.example.victorsport.Dto.BaoGiaGhn;
import com.example.victorsport.Service.KiemTraForm;
import jakarta.validation.Valid;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import com.example.victorsport.Dto.NguoiDungDangNhap;
import com.example.victorsport.Dto.SanPhamTrongGio;
import com.example.victorsport.Service.BanHangTaiQuayService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;

@Controller
public class BanHangTaiQuayController {
    private static final String KHOA_GIO_HANG_TAI_QUAY = "gioHangTaiQuay";
    private static final String KHOA_PHIEU_GIAM_GIA = "idPhieuGiamGiaTaiQuay";
    private static final String KHOA_DON_CHO = "idDonChoTaiQuay";
    private static final String KHOA_KHACH_HANG = "idKhachHangTaiQuay";
    private static final String KHOA_BAO_GIA = "baoGiaGhnTaiQuay";

    private final BanHangTaiQuayService banHangTaiQuayService;

    public BanHangTaiQuayController(BanHangTaiQuayService banHangTaiQuayService) {
        this.banHangTaiQuayService = banHangTaiQuayService;
    }

    @GetMapping("/pos")
    public String hienThiBanHangTaiQuay(@RequestParam(value = "keyword", required = false) String tuKhoaSanPham,
                                        @RequestParam(value = "customerKeyword", required = false) String tuKhoaKhachHang,
                                        HttpSession phienDangNhap,
                                        Model model) {
        NguoiDungDangNhap nguoiDungDangNhap = DangNhapController.layNguoiDungDangNhap(phienDangNhap, model);
        if (nguoiDungDangNhap == null) {
            return "redirect:/login";
        }

        GioHangTaiQuay gioHang = layGioHang(phienDangNhap);
        List<Map<String, Object>> donCho = banHangTaiQuayService.layDanhSachDonCho(nguoiDungDangNhap.getId());
        model.addAttribute("danhSachDonCho", donCho);
        model.addAttribute("soDonCho", donCho.size());
        Integer idDonCho = layIdDonCho(phienDangNhap);
        if (idDonCho != null) {
            try {
                model.addAttribute("donChoDangChon", banHangTaiQuayService.layDonCho(idDonCho, nguoiDungDangNhap.getId()));
            } catch (IllegalArgumentException loi) {
                model.addAttribute("error", loi.getMessage());
                model.addAttribute("donChoDaMat", true);
            }
        }
        model.addAttribute("activeMenu", "ban-hang");
        model.addAttribute("keyword", tuKhoaSanPham == null ? "" : tuKhoaSanPham);
        model.addAttribute("customerKeyword", tuKhoaKhachHang == null ? "" : tuKhoaKhachHang);
        model.addAttribute("products", banHangTaiQuayService.timSanPham(tuKhoaSanPham));
        model.addAttribute("customers", banHangTaiQuayService.timKhachHang(tuKhoaKhachHang));
        Integer idKhachHang = (Integer) phienDangNhap.getAttribute(KHOA_KHACH_HANG);
        if (idKhachHang != null) {
            try {
                model.addAttribute("khachHangDangChon", banHangTaiQuayService.layKhachHang(idKhachHang));
            } catch (IllegalArgumentException loi) {
                phienDangNhap.removeAttribute(KHOA_KHACH_HANG);
                model.addAttribute("error", loi.getMessage());
            }
        }
        model.addAttribute("paymentMethods", banHangTaiQuayService.layPhuongThucThanhToan());
        if(phienDangNhap.getAttribute("csrfGiaoHangTaiQuay")==null)
            phienDangNhap.setAttribute("csrfGiaoHangTaiQuay",java.util.UUID.randomUUID().toString());
        var cacPhieu=banHangTaiQuayService.layPhieuGiamGiaKhaDung(gioHang.getTamTinh());
        Integer phieuDaChon=layIdPhieuGiamGia(phienDangNhap);
        if(phieuDaChon!=null && cacPhieu.stream().noneMatch(p -> phieuDaChon.equals(p.getId())))
            phienDangNhap.removeAttribute(KHOA_PHIEU_GIAM_GIA);
        model.addAttribute("discountCoupons", cacPhieu);
        model.addAttribute("cart", gioHang);
        var phieuThuCong=layIdPhieuGiamGia(phienDangNhap);
        var tongKet=banHangTaiQuayService.tinhTongKet(gioHang,phieuThuCong);
        model.addAttribute("selectedDiscountCouponId",phieuThuCong!=null?phieuThuCong:(tongKet==null?null:tongKet.getIdPhieuGiamGia()));
        model.addAttribute("summary",tongKet);
        if(phienDangNhap.getAttribute(KHOA_BAO_GIA) instanceof BaoGiaGhn q) {
            model.addAttribute("giaoHangTaiQuay",Map.of("ten",q.ten(),"sdt",q.sdt(),"diaChi",q.diaChiChiTiet(),"tenTinh",q.tenTinh(),"tenXa",q.tenXa()));
        }
        var thongTinNhan=new java.util.LinkedHashMap<String,Object>();
        if(model.asMap().get("khachHangDangChon") instanceof com.example.victorsport.Dto.LuaChonKhachHang khach) {
            var diaChi=banHangTaiQuayService.layDiaChiGiaoHangMacDinh(khach.getId());
            thongTinNhan.put("ten",khach.getTenKhachHang());thongTinNhan.put("sdt",khach.getSoDienThoai());
            thongTinNhan.put("diaChi",diaChi.get("dia_chi_cu_the"));thongTinNhan.put("tenTinh",diaChi.get("thanh_pho"));thongTinNhan.put("tenXa",diaChi.get("phuong"));
        }
        if(!Boolean.TRUE.equals(model.asMap().get("khachHangVuaDoi"))
                && model.asMap().get("donChoDangChon") instanceof Map<?,?> don
                && java.util.Objects.equals(don.get("id_khach_hang"),phienDangNhap.getAttribute(KHOA_KHACH_HANG))
                && don.get("loai_don") instanceof Number loai && loai.intValue()==2) {
            thongTinNhan.put("ten",don.get("ten_khach_hang"));thongTinNhan.put("sdt",don.get("so_dien_thoai_khach_hang"));
            thongTinNhan.put("diaChi",don.get("dia_chi_chi_tiet"));thongTinNhan.put("tenTinh",don.get("ten_tinh_ghn"));thongTinNhan.put("tenXa",don.get("ten_xa_ghn"));
        }
        if(model.asMap().get("giaoHangTaiQuay") instanceof Map<?,?> daNhap)
            daNhap.forEach((key,value)->thongTinNhan.put(key.toString(),value));
        model.addAttribute("thongTinNhanHang",thongTinNhan);
        return "admin/pos/show";
    }

    @PostMapping("/pos/giao-hang/bo")
    @org.springframework.web.bind.annotation.ResponseBody
    public org.springframework.http.ResponseEntity<?> boGiaoHang(@RequestParam(required=false) String csrfPos,HttpSession session) {
        if(!daDangNhap(session)) return org.springframework.http.ResponseEntity.status(401).build();
        if(csrfPos==null || !csrfPos.equals(session.getAttribute("csrfGiaoHangTaiQuay")))
            return org.springframework.http.ResponseEntity.status(403).build();
        synchronized(session) {session.removeAttribute(KHOA_BAO_GIA);session.removeAttribute("maBaoGiaTaiQuay");}
        return org.springframework.http.ResponseEntity.noContent().build();
    }

    @PostMapping("/pos/giao-hang/bao-gia")
    @org.springframework.web.bind.annotation.ResponseBody
    public org.springframework.http.ResponseEntity<?> baoGiaGiaoHang(@Valid @ModelAttribute GiaoHangTaiQuayForm form,
            BindingResult loi,@RequestParam(required=false) String csrfPos,HttpSession session) {
        if(!daDangNhap(session)) return org.springframework.http.ResponseEntity.status(401).body(Map.of("error","Vui lòng đăng nhập lại."));
        if(csrfPos==null || !csrfPos.equals(session.getAttribute("csrfGiaoHangTaiQuay")))
            return org.springframework.http.ResponseEntity.status(403).body(Map.of("error","Phiên giao hàng không hợp lệ. Vui lòng tải lại trang."));
        synchronized(session) {
            try {
                if(loi.hasErrors()) throw new IllegalArgumentException("Vui lòng nhập đủ tên, số điện thoại và địa chỉ nhận hàng hợp lệ.");
                var q=banHangTaiQuayService.baoGiaGiaoHang(layGioHang(session),layIdPhieuGiamGia(session),form);
                String ma=java.util.UUID.randomUUID().toString();
                session.setAttribute(KHOA_BAO_GIA,q);session.setAttribute("maBaoGiaTaiQuay",ma);
                return org.springframework.http.ResponseEntity.ok().cacheControl(org.springframework.http.CacheControl.noStore())
                        .body(Map.of("phi",q.phi(),"tong",q.tienHang().add(q.phi()),"maBaoGia",ma,"hetHan",q.hetHan().toEpochMilli()));
            } catch(IllegalArgumentException | IllegalStateException ex) {
                return org.springframework.http.ResponseEntity.badRequest().body(Map.of("error",ex.getMessage()));
            } catch(org.springframework.dao.DataAccessException ex) {
                return org.springframework.http.ResponseEntity.status(503).body(Map.of("error","Chưa lấy được phí giao hàng. Vui lòng thử lại."));
            }
        }
    }

    @PostMapping("/pos/khach-hang/chon")
    public String chonKhachHang(@RequestParam(value = "customerId", required = false) Integer idKhachHang,
                                HttpSession phienDangNhap, RedirectAttributes thongBao) {
        if (!daDangNhap(phienDangNhap)) {
            return "redirect:/login";
        }
        synchronized(phienDangNhap) {
        try {
            Object khachCu=phienDangNhap.getAttribute(KHOA_KHACH_HANG);
            if (idKhachHang == null) {
                phienDangNhap.removeAttribute(KHOA_KHACH_HANG);
            } else {
                banHangTaiQuayService.layKhachHang(idKhachHang);
                phienDangNhap.setAttribute(KHOA_KHACH_HANG, idKhachHang);
            }
            if(!java.util.Objects.equals(khachCu,idKhachHang)) {
                phienDangNhap.removeAttribute(KHOA_BAO_GIA);phienDangNhap.removeAttribute("maBaoGiaTaiQuay");
                thongBao.addFlashAttribute("khachHangVuaDoi",true);
            }
        } catch (IllegalArgumentException loi) {
            thongBao.addFlashAttribute("error", loi.getMessage());
        }
        }
        return "redirect:/pos";
    }

    @PostMapping("/pos/khach-hang/them")
    public String themKhachHang(@Valid @ModelAttribute KhachHangTaiQuayForm form, BindingResult loiForm,
                                HttpSession phienDangNhap, Model model, RedirectAttributes thongBao) {
        NguoiDungDangNhap nhanVien = DangNhapController.layNguoiDungDangNhap(phienDangNhap, model);
        if (nhanVien == null) {
            return "redirect:/login";
        }
        String ten = form.getTenKhachHangMoi();
        String soDienThoai = form.getSoDienThoaiMoi();
        String email = form.getEmailMoi();
        try {
            KiemTraForm.kiemTra(loiForm);
            Integer idKhachHang = banHangTaiQuayService.themKhachHangMoi(ten, soDienThoai, email, nhanVien.getId());
            synchronized(phienDangNhap) {
                phienDangNhap.setAttribute(KHOA_KHACH_HANG, idKhachHang);
                phienDangNhap.removeAttribute(KHOA_BAO_GIA);phienDangNhap.removeAttribute("maBaoGiaTaiQuay");
            }
            thongBao.addFlashAttribute("khachHangVuaDoi",true);
            thongBao.addFlashAttribute("success", "Đã thêm khách hàng mới và chọn cho đơn hiện tại.");
        } catch (IllegalArgumentException loi) {
            thongBao.addFlashAttribute("loiThemKhachHang", loi.getMessage());
            thongBao.addFlashAttribute("tenMoi", ten);
            thongBao.addFlashAttribute("soDienThoaiMoi", soDienThoai);
            thongBao.addFlashAttribute("emailMoi", email);
        }
        return "redirect:/pos";
    }

    @PostMapping("/pos/phieu-giam-gia/chon")
    public String chonPhieuGiamGia(@RequestParam(value = "discountCouponId", required = false) Integer idPhieuGiamGia,
                                   HttpSession phienDangNhap,
                                   RedirectAttributes thuocTinhChuyenHuong) {
        if (!daDangNhap(phienDangNhap)) {
            return "redirect:/login";
        }

        if (idPhieuGiamGia == null || idPhieuGiamGia <= 0) {
            phienDangNhap.removeAttribute(KHOA_PHIEU_GIAM_GIA);
            thuocTinhChuyenHuong.addFlashAttribute("success", "Đã chọn chế độ tự động áp dụng phiếu tốt nhất.");
        } else {
            var gio=layGioHang(phienDangNhap);
            if(gio.isRong() || banHangTaiQuayService.layPhieuGiamGiaKhaDung(gio.getTamTinh()).stream().noneMatch(p -> idPhieuGiamGia.equals(p.getId()))) {
                phienDangNhap.removeAttribute(KHOA_PHIEU_GIAM_GIA);
                thuocTinhChuyenHuong.addFlashAttribute("error","Phiếu không đủ điều kiện, đã hết hạn hoặc không áp dụng tại quầy. Vui lòng chọn lại.");
                return "redirect:/pos";
            }
            phienDangNhap.setAttribute(KHOA_PHIEU_GIAM_GIA, idPhieuGiamGia);
            thuocTinhChuyenHuong.addFlashAttribute("success", "Đã áp dụng phiếu giảm giá bạn chọn.");
        }
        return "redirect:/pos";
    }

    @PostMapping("/pos/cart/add")
    public String themVaoGio(@RequestParam("detailId") Integer idChiTietSanPham,
                             @RequestParam(value = "quantity", defaultValue = "1") Integer soLuong,
                             HttpSession phienDangNhap,
                             RedirectAttributes thuocTinhChuyenHuong) {
        if (!daDangNhap(phienDangNhap)) {
            return "redirect:/login";
        }

        if (soLuong <= 0) {
            thuocTinhChuyenHuong.addFlashAttribute("error", "Số lượng thêm phải là số nguyên từ 1 trở lên.");
            return "redirect:/pos";
        }
        SanPhamTrongGio sanPham = banHangTaiQuayService.taoSanPhamTrongGio(idChiTietSanPham, soLuong);
        if (sanPham == null) {
            thuocTinhChuyenHuong.addFlashAttribute("error", "Sản phẩm không tồn tại hoặc đã ngừng bán.");
            return "redirect:/pos";
        }
        if (sanPham.getSoLuongTon() <= 0) {
            thuocTinhChuyenHuong.addFlashAttribute("error", "Sản phẩm đã hết hàng. Vui lòng gợi ý sản phẩm khác cho khách.");
            return "redirect:/pos";
        }

        var gio = layGioHang(phienDangNhap);
        var dangCo = gio.timSanPham(idChiTietSanPham);
        long tong = (dangCo == null ? 0L : dangCo.getSoLuongMua().longValue()) + soLuong;
        if (tong > sanPham.getSoLuongTon()) {
            thuocTinhChuyenHuong.addFlashAttribute("error", "Số lượng trong giỏ sẽ vượt tồn kho của mẫu.");
            return "redirect:/pos";
        }
        if (dangCo != null) dangCo.setSoLuongTon(sanPham.getSoLuongTon());
        gio.themHoacCapNhat(sanPham);
        thuocTinhChuyenHuong.addFlashAttribute("success", "Đã thêm sản phẩm vào giỏ bán hàng.");
        return "redirect:/pos";
    }

    @PostMapping("/pos/cart/update")
    public String capNhatGio(@RequestParam("detailId") Integer idChiTietSanPham,
                             @RequestParam("quantity") Integer soLuong,
                             HttpSession phienDangNhap,
                             RedirectAttributes thuocTinhChuyenHuong) {
        if (!daDangNhap(phienDangNhap)) {
            return "redirect:/login";
        }

        var gio = layGioHang(phienDangNhap);
        if (soLuong < 0) {
            thuocTinhChuyenHuong.addFlashAttribute("error", "Số lượng không được âm.");
            return "redirect:/pos";
        }
        if (soLuong > 0) {
            var hienTai = banHangTaiQuayService.taoSanPhamTrongGio(idChiTietSanPham, soLuong);
            if (hienTai == null || soLuong > hienTai.getSoLuongTon()) {
                thuocTinhChuyenHuong.addFlashAttribute("error", "Mẫu đã ngừng bán hoặc không đủ tồn kho.");
                return "redirect:/pos";
            }
            var dangCo = gio.timSanPham(idChiTietSanPham);
            if (dangCo != null) dangCo.setSoLuongTon(hienTai.getSoLuongTon());
        }
        gio.capNhatSoLuong(idChiTietSanPham, soLuong);
        thuocTinhChuyenHuong.addFlashAttribute("success", "Đã cập nhật giỏ hàng.");
        return "redirect:/pos";
    }

    @PostMapping("/pos/cart/remove")
    public String xoaKhoiGio(@RequestParam("detailId") Integer idChiTietSanPham,
                             HttpSession phienDangNhap,
                             RedirectAttributes thuocTinhChuyenHuong) {
        if (!daDangNhap(phienDangNhap)) {
            return "redirect:/login";
        }

        layGioHang(phienDangNhap).xoaSanPham(idChiTietSanPham);
        thuocTinhChuyenHuong.addFlashAttribute("success", "Đã xóa sản phẩm khỏi giỏ hàng.");
        return "redirect:/pos";
    }

    @PostMapping("/pos/cart/clear")
    public String xoaGioHang(HttpSession phienDangNhap, RedirectAttributes thuocTinhChuyenHuong) {
        if (!daDangNhap(phienDangNhap)) {
            return "redirect:/login";
        }

        layGioHang(phienDangNhap).xoaTatCa();
        phienDangNhap.removeAttribute(KHOA_BAO_GIA);
        phienDangNhap.removeAttribute(KHOA_DON_CHO);
        phienDangNhap.removeAttribute(KHOA_KHACH_HANG);
        phienDangNhap.removeAttribute(KHOA_PHIEU_GIAM_GIA);
        thuocTinhChuyenHuong.addFlashAttribute("success", "Đã mở giỏ mới. Các đơn chờ đã lưu vẫn được giữ.");
        return "redirect:/pos";
    }

    @PostMapping("/pos/checkout")
    public String thanhToan(@RequestParam(value = "customerId", required = false) Integer idKhachHang,
                            @RequestParam(value = "hanhDong", defaultValue = "thanh-toan") String hanhDong,
                            @RequestParam(value = "customerName", required = false) String tenKhachHang,
                            @RequestParam(value = "customerPhone", required = false) String soDienThoai,
                            @RequestParam(value = "customerEmail", required = false) String email,
                            @RequestParam(value = "customerAddress", required = false) String diaChi,
                            @RequestParam(value = "paymentMethodId", required = false) Integer idPhuongThucThanhToan,
                            @RequestParam(value = "paidAmount", required = false) String tienKhachDaThanhToan,
                            @RequestParam(value = "note", required = false) String ghiChu,
                            @RequestParam(defaultValue="false") boolean giaoHang,
                            @RequestParam(required=false) String maBaoGia,
                            @RequestParam(required=false) String csrfPos,
                            @RequestParam(required=false) Integer tinh,
                            @RequestParam(required=false) Integer xa,
                            HttpSession phienDangNhap,
                            Model model,
                            RedirectAttributes thuocTinhChuyenHuong) {
        NguoiDungDangNhap nguoiDungDangNhap = DangNhapController.layNguoiDungDangNhap(phienDangNhap, model);
        if (nguoiDungDangNhap == null) {
            return "redirect:/login";
        }

        synchronized(phienDangNhap) {
        try {
            java.math.BigDecimal tienDaThu=null;
            if(!"luu-don-cho".equals(hanhDong)) {
                if(tienKhachDaThanhToan==null || !tienKhachDaThanhToan.trim().matches("[0-9]{1,16}(\\.[0-9]{1,2})?"))
                    throw new IllegalArgumentException("Vui lòng nhập số tiền khách đưa hợp lệ.");
                tienDaThu=new java.math.BigDecimal(tienKhachDaThanhToan.trim());
            }
            BanHangTaiQuayService.YeuCauThanhToan yeuCau = new BanHangTaiQuayService.YeuCauThanhToan(
                    idKhachHang, tenKhachHang, soDienThoai, email, diaChi,
                    idPhuongThucThanhToan, ghiChu, layIdPhieuGiamGia(phienDangNhap),tienDaThu);
            if(giaoHang) {
                if(csrfPos==null || !csrfPos.equals(phienDangNhap.getAttribute("csrfGiaoHangTaiQuay")))
                    throw new IllegalArgumentException("Phiên giao hàng không hợp lệ. Vui lòng tải lại trang.");
                if(!(phienDangNhap.getAttribute(KHOA_BAO_GIA) instanceof BaoGiaGhn q)
                        || maBaoGia==null || !maBaoGia.equals(phienDangNhap.getAttribute("maBaoGiaTaiQuay")))
                    throw new IllegalArgumentException("Vui lòng chờ lấy phí giao hàng trước khi thanh toán.");
                if(!q.ten().equals(tenKhachHang==null?"":tenKhachHang.trim()) || !q.sdt().equals(soDienThoai==null?"":soDienThoai.trim())
                        || !q.diaChiChiTiet().equals(diaChi==null?"":diaChi.trim())
                        || !Integer.valueOf(q.tinh()).equals(tinh) || !Integer.valueOf(q.xa()).equals(xa))
                    throw new IllegalArgumentException("Thông tin người nhận đã thay đổi. Vui lòng chờ cập nhật phí giao hàng.");
                yeuCau=new BanHangTaiQuayService.YeuCauThanhToan(idKhachHang,q.ten(),q.sdt(),email,q.diaChi(),
                        idPhuongThucThanhToan,ghiChu,layIdPhieuGiamGia(phienDangNhap),tienDaThu);
                yeuCau.setGiaoHang(q);
            }
            if ("luu-don-cho".equals(hanhDong)) {
                Integer idDonCho = banHangTaiQuayService.luuDonCho(layGioHang(phienDangNhap), yeuCau,
                        nguoiDungDangNhap, layIdDonCho(phienDangNhap));
                layGioHang(phienDangNhap).xoaTatCa();
                phienDangNhap.removeAttribute(KHOA_DON_CHO);
                phienDangNhap.removeAttribute(KHOA_KHACH_HANG);
                phienDangNhap.removeAttribute(KHOA_PHIEU_GIAM_GIA);
                phienDangNhap.removeAttribute(KHOA_BAO_GIA);
                thuocTinhChuyenHuong.addFlashAttribute("success", "Đã lưu đơn chờ #" + idDonCho + ". Chọn Tiếp tục bán để mở lại.");
                return "redirect:/pos";
            }
            Integer idHoaDon = banHangTaiQuayService.thanhToan(
                    layGioHang(phienDangNhap),
                    yeuCau, nguoiDungDangNhap, layIdDonCho(phienDangNhap)
            );
            layGioHang(phienDangNhap).xoaTatCa();
            phienDangNhap.removeAttribute(KHOA_PHIEU_GIAM_GIA);
            phienDangNhap.removeAttribute(KHOA_DON_CHO);
            phienDangNhap.removeAttribute(KHOA_KHACH_HANG);
            phienDangNhap.removeAttribute(KHOA_BAO_GIA);
            thuocTinhChuyenHuong.addFlashAttribute("success", (giaoHang?"Đã thanh toán và xác nhận đơn giao hàng tại quầy. Mã hóa đơn: #":"Thanh toán thành công. Mã hóa đơn nội bộ: #") + idHoaDon);
            if(yeuCau.getTienThua().signum()>0) thuocTinhChuyenHuong.addFlashAttribute("success",
                    "Thanh toán thành công. Mã hóa đơn: #"+idHoaDon+". Tiền thừa trả khách: "+
                            java.text.NumberFormat.getNumberInstance(java.util.Locale.forLanguageTag("vi-VN")).format(yeuCau.getTienThua())+" đ.");
            thuocTinhChuyenHuong.addFlashAttribute("hoaDonVuaTao",idHoaDon);
        } catch (IllegalArgumentException loi) {
            thuocTinhChuyenHuong.addFlashAttribute("error", loi.getMessage());
            thuocTinhChuyenHuong.addFlashAttribute("paidAmount",tienKhachDaThanhToan);
            thuocTinhChuyenHuong.addFlashAttribute("paymentMethodIdDaChon", idPhuongThucThanhToan);
            thuocTinhChuyenHuong.addFlashAttribute("giaoHangDaChon", giaoHang);
        } catch (org.springframework.dao.DataAccessException loi) {
            thuocTinhChuyenHuong.addFlashAttribute("error", "Chưa lưu được thanh toán. Kiểm tra hóa đơn trước khi thử lại.");
            thuocTinhChuyenHuong.addFlashAttribute("paidAmount", tienKhachDaThanhToan);
            thuocTinhChuyenHuong.addFlashAttribute("paymentMethodIdDaChon", idPhuongThucThanhToan);
            thuocTinhChuyenHuong.addFlashAttribute("giaoHangDaChon", giaoHang);
        }
        }
        return "redirect:/pos";
    }

    @PostMapping("/pos/don-cho/mo")
    public String moDonCho(@RequestParam("idDonCho") Integer idDonCho, HttpSession phienDangNhap,
                           Model model, RedirectAttributes thongBao) {
        NguoiDungDangNhap nhanVien = DangNhapController.layNguoiDungDangNhap(phienDangNhap, model);
        if (nhanVien == null) {
            return "redirect:/login";
        }
        if (idDonCho.equals(layIdDonCho(phienDangNhap))) {
            return "redirect:/pos";
        }
        if (!layGioHang(phienDangNhap).isRong()) {
            thongBao.addFlashAttribute("error", "Hãy lưu đơn chờ hoặc làm mới giỏ hiện tại trước khi mở đơn khác.");
            return "redirect:/pos";
        }
        try {
            Map<String, Object> donCho = banHangTaiQuayService.layDonCho(idDonCho, nhanVien.getId());
            GioHangTaiQuay gioHang = banHangTaiQuayService.layGioHangDonCho(idDonCho, nhanVien.getId());
            phienDangNhap.setAttribute(KHOA_GIO_HANG_TAI_QUAY, gioHang);
            phienDangNhap.setAttribute(KHOA_DON_CHO, idDonCho);
            phienDangNhap.removeAttribute(KHOA_BAO_GIA);
            phienDangNhap.removeAttribute(KHOA_KHACH_HANG);
            if (donCho.get("id_khach_hang") != null) {
                phienDangNhap.setAttribute(KHOA_KHACH_HANG, donCho.get("id_khach_hang"));
            }
            phienDangNhap.removeAttribute(KHOA_PHIEU_GIAM_GIA);
            if (donCho.get("id_phieu_giam_gia") != null) {
                phienDangNhap.setAttribute(KHOA_PHIEU_GIAM_GIA, donCho.get("id_phieu_giam_gia"));
            }
        } catch (IllegalArgumentException loi) {
            thongBao.addFlashAttribute("error", loi.getMessage());
        }
        return "redirect:/pos";
    }

    @PostMapping("/pos/don-cho/huy")
    public String huyDonCho(@RequestParam("idDonCho") Integer idDonCho, HttpSession phienDangNhap,
                            @RequestParam(value = "quayLai", defaultValue = "pos") String quayLai,
                            Model model, RedirectAttributes thongBao) {
        NguoiDungDangNhap nhanVien = DangNhapController.layNguoiDungDangNhap(phienDangNhap, model);
        if (nhanVien == null) {
            return "redirect:/login";
        }
        try {
            banHangTaiQuayService.huyDonCho(idDonCho, nhanVien.getId());
            if (idDonCho.equals(layIdDonCho(phienDangNhap))) {
                layGioHang(phienDangNhap).xoaTatCa();
                phienDangNhap.removeAttribute(KHOA_DON_CHO);
                phienDangNhap.removeAttribute(KHOA_KHACH_HANG);
                phienDangNhap.removeAttribute(KHOA_PHIEU_GIAM_GIA);
            }
            thongBao.addFlashAttribute("success", "Đã hủy và xóa đơn chờ.");
        } catch (IllegalArgumentException loi) {
            thongBao.addFlashAttribute("error", loi.getMessage());
        }
        if ("hoa-don".equals(quayLai)) {
            return "redirect:/quan-ly/hoa-don";
        }
        return "redirect:/pos";
    }

    private Integer layIdDonCho(HttpSession phienDangNhap) {
        Object idDonCho = phienDangNhap.getAttribute(KHOA_DON_CHO);
        if (idDonCho instanceof Integer) {
            return (Integer) idDonCho;
        }
        return null;
    }

    private GioHangTaiQuay layGioHang(HttpSession phienDangNhap) {
        Object gioHangHienTai = phienDangNhap.getAttribute(KHOA_GIO_HANG_TAI_QUAY);
        if (gioHangHienTai instanceof GioHangTaiQuay) {
            return (GioHangTaiQuay) gioHangHienTai;
        }
        GioHangTaiQuay gioHang = new GioHangTaiQuay();
        phienDangNhap.setAttribute(KHOA_GIO_HANG_TAI_QUAY, gioHang);
        return gioHang;
    }

    private boolean daDangNhap(HttpSession phienDangNhap) {
        return phienDangNhap.getAttribute(DangNhapController.KHOA_NGUOI_DUNG_DANG_NHAP) instanceof NguoiDungDangNhap;
    }

    private Integer layIdPhieuGiamGia(HttpSession phienDangNhap) {
        Object idPhieuGiamGia = phienDangNhap.getAttribute(KHOA_PHIEU_GIAM_GIA);
        if (idPhieuGiamGia instanceof Integer) {
            return (Integer) idPhieuGiamGia;
        }
        return null;
    }
}
