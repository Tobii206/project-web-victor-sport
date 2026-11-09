package com.example.victorsport;

import com.example.victorsport.Controller.*;
import com.example.victorsport.Dto.*;
import com.example.victorsport.Service.*;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class GiaoHangTaiQuayTests {
    private final BigDecimal tienHang=new BigDecimal("800000.00"),phi=new BigDecimal("30000"),tong=new BigDecimal("830000.00");
    private NguoiDungDangNhap nhanVien(){return new NguoiDungDangNhap(2,"nv@example.com","Nhân viên","Bán hàng");}
    private GioHangTaiQuay gio(){var g=new GioHangTaiQuay();g.themHoacCapNhat(new SanPhamTrongGio(1,"SP1","Giày","Đen","40",1,5,new BigDecimal("1000000")));return g;}
    private BaoGiaGhn quote(Instant expiry){return new BaoGiaGhn(Map.of(1,1),"Người nhận","0912345678","12 Đường A, Phường B, Hà Nội","QR",tienHang,phi,1,2,"Hà Nội","Phường B",1000,30,20,15,expiry);}
    private BanHangTaiQuayService service(JdbcTemplate jdbc,GiaoHangGhnService ghn) {
        var s=spy(new BanHangTaiQuayService(jdbc));ReflectionTestUtils.setField(s,"giaoHangGhn",ghn);
        doReturn(new TongKetBanHang(new BigDecimal("1000000"),new BigDecimal("200000"),tienHang,"Ưu đãi",null)).when(s).tinhTongKet(any(),nullable(Integer.class));
        when(jdbc.queryForList(contains("WITH (UPDLOCK, HOLDLOCK)"),eq(10),eq(2))).thenReturn(List.of(Map.of("id",10)));
        when(jdbc.queryForList(contains("FROM chi_tiet_san_pham"),eq(Integer.class),eq(1))).thenReturn(List.of(5));
        when(jdbc.queryForList(contains("SELECT nha_cung_cap"),eq(1))).thenReturn(List.of(Map.of("nha_cung_cap","Tiền mặt")));
        return s;
    }
    private BanHangTaiQuayService.YeuCauThanhToan request(BaoGiaGhn q,BigDecimal paid) {
        var r=new BanHangTaiQuayService.YeuCauThanhToan(null,q.ten(),q.sdt(),null,q.diaChi(),1,"Giao giờ hành chính",null,paid);r.setGiaoHang(q);return r;
    }
    @Test void thuDuCaPhiShipVaDonChuyenSangChoChuanBi() {
        var jdbc=mock(JdbcTemplate.class);var ghn=mock(GiaoHangGhnService.class);var s=service(jdbc,ghn);var q=quote(Instant.now().plusSeconds(300));
        assertEquals(10,s.thanhToan(gio(),request(q,tong),nhanVien(),10));
        verify(jdbc).update(contains("SET loai_don=2,phi_van_chuyen=?"),eq(phi),eq(tong),eq(q.ten()),eq(q.sdt()),eq(q.diaChi()),eq(10));
        verify(jdbc).update(contains("INSERT INTO giao_dich_thanh_toan"),eq(10),eq(1),eq(tong),eq("POS-10"),eq("HD10"),eq(2),eq("Thanh toán tại quầy"));
        verify(jdbc).update(contains("INSERT INTO lich_su_hoa_don"),eq(10),eq(2),contains("Chờ chuẩn bị hàng"),eq(2),eq(2));
        verify(ghn).luu(10,q);verify(ghn,never()).taoVanDon(anyInt());
        verify(jdbc,times(1)).update(contains("UPDATE chi_tiet_san_pham"),eq(1),eq(2),eq(1));
    }
    @Test void thieuPhiShipHoacBaoGiaHetHanKhongThuTienHayTruKho() {
        for(boolean expired:List.of(false,true)) {
            var jdbc=mock(JdbcTemplate.class);var ghn=mock(GiaoHangGhnService.class);var s=service(jdbc,ghn);
            var q=quote(expired?Instant.now().minusSeconds(1):Instant.now().plusSeconds(300));
            assertThrows(IllegalArgumentException.class,()->s.thanhToan(gio(),request(q,expired?tong:tienHang),nhanVien(),10));
            verify(jdbc,never()).update(anyString(),any(Object[].class));verifyNoInteractions(ghn);
        }
    }
    @Test void tienThuaGiaoHangTinhSauKhiCongPhiShip() {
        var jdbc=mock(JdbcTemplate.class);var ghn=mock(GiaoHangGhnService.class);var s=service(jdbc,ghn);
        when(jdbc.queryForList(contains("nha_cung_cap=N'POS'"),eq(1))).thenReturn(List.of(Map.of("id",1)));
        var r=request(quote(Instant.now().plusSeconds(300)),new BigDecimal("900000"));
        s.thanhToan(gio(),r,nhanVien(),10);
        assertEquals(0,new BigDecimal("70000").compareTo(r.getTienThua()));
        verify(jdbc).update(contains("INSERT INTO giao_dich_thanh_toan"),eq(10),eq(1),eq(tong),eq("POS-10"),eq("HD10"),eq(2),contains("70000.00"));
    }
    @Test void gioHoacUuDaiThayDoiPhaiTinhLaiPhi() {
        var jdbc=mock(JdbcTemplate.class);var ghn=mock(GiaoHangGhnService.class);var s=service(jdbc,ghn);var q=quote(Instant.now().plusSeconds(300));
        var g=gio();g.getDanhSachSanPham().get(0).setSoLuongMua(2);
        assertThrows(IllegalArgumentException.class,()->s.thanhToan(g,request(q,tong),nhanVien(),10));
        doReturn(new TongKetBanHang(tienHang,BigDecimal.ZERO,new BigDecimal("900000"),"",null)).when(s).tinhTongKet(any(),nullable(Integer.class));
        assertThrows(IllegalArgumentException.class,()->s.thanhToan(gio(),request(q,tong),nhanVien(),10));
        verify(jdbc,never()).update(anyString(),any(Object[].class));verifyNoInteractions(ghn);
    }
    @Test void chanCodCaKhiBaoGiaVaKhiThanhToan() {
        var jdbc=mock(JdbcTemplate.class);var ghn=mock(GiaoHangGhnService.class);var s=service(jdbc,ghn);
        when(jdbc.queryForList(contains("SELECT nha_cung_cap"),eq(1))).thenReturn(List.of(Map.of("nha_cung_cap","COD")));
        assertThrows(IllegalArgumentException.class,()->s.baoGiaGiaoHang(gio(),null,new GiaoHangTaiQuayForm("Người nhận","0912345678","12 Đường A",1,2,1)));
        assertThrows(IllegalArgumentException.class,()->s.thanhToan(gio(),request(quote(Instant.now().plusSeconds(300)),tong),nhanVien(),10));
        verifyNoInteractions(ghn);verify(jdbc,never()).update(anyString(),any(Object[].class));
    }
    @Test void baoGiaDungTienSauUuDaiVaKhongThuHoCod() {
        var jdbc=mock(JdbcTemplate.class);var ghn=mock(GiaoHangGhnService.class);var s=service(jdbc,ghn);
        s.baoGiaGiaoHang(gio(),null,new GiaoHangTaiQuayForm(" Người nhận ","0912345678","12 Đường A",1,2,1));
        verify(ghn).baoGia(eq(Map.of(1,1)),eq("Người nhận"),eq("0912345678"),eq("12 Đường A"),eq("QR"),eq(tienHang),eq(1),eq(2));
    }
    @Test void luuDonChoGiuThongTinGiaoHangNhungChuaThuTienHayTruKho() {
        var jdbc=mock(JdbcTemplate.class);var ghn=mock(GiaoHangGhnService.class);var s=service(jdbc,ghn);var q=quote(Instant.now().plusSeconds(300));
        assertEquals(10,s.luuDonCho(gio(),request(q,null),nhanVien(),10));
        verify(ghn).luu(10,q);verify(jdbc).update(contains("SET loai_don=2,phi_van_chuyen=?"),eq(phi),eq(tong),eq(q.ten()),eq(q.sdt()),eq(q.diaChi()),eq(10));
        verify(jdbc,never()).update(contains("INSERT INTO giao_dich_thanh_toan"),any(Object[].class));
        verify(jdbc,never()).update(contains("UPDATE chi_tiet_san_pham"),any(Object[].class));
    }
    private MockHttpSession session() {
        var s=new MockHttpSession();s.setAttribute(DangNhapController.KHOA_NGUOI_DUNG_DANG_NHAP,nhanVien());
        s.setAttribute("csrfGiaoHangTaiQuay","token");s.setAttribute("gioHangTaiQuay",gio());return s;
    }
    @Test void apiBaoGiaCanDangNhapVaCsrfVaKhongNhanPhiTuTrinhDuyet() throws Exception {
        var service=mock(BanHangTaiQuayService.class);var mvc=MockMvcBuilders.standaloneSetup(new BanHangTaiQuayController(service)).build();
        mvc.perform(post("/pos/giao-hang/bao-gia")).andExpect(status().isUnauthorized());
        mvc.perform(post("/pos/giao-hang/bao-gia").session(session())).andExpect(status().isForbidden());
        var q=quote(Instant.now().plusSeconds(300));when(service.baoGiaGiaoHang(any(),isNull(),any())).thenReturn(q);
        var session=session();
        mvc.perform(post("/pos/giao-hang/bao-gia").session(session).param("csrfPos","token").param("ten",q.ten()).param("sdt",q.sdt()).param("diaChi","12 Đường A")
                .param("tinh","1").param("xa","2").param("paymentMethodId","1").param("phi","1"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.phi").value(30000)).andExpect(jsonPath("$.tong").value(830000));
        assertSame(q,session.getAttribute("baoGiaGhnTaiQuay"));
    }
    @Test void checkoutKhongBaoGiaVaDiaChiKhacBiChanConDungThiDungPhiServer() throws Exception {
        var service=mock(BanHangTaiQuayService.class);var mvc=MockMvcBuilders.standaloneSetup(new BanHangTaiQuayController(service)).build();
        var session=session();var q=quote(Instant.now().plusSeconds(300));
        var request=post("/pos/checkout").session(session).param("csrfPos","token").param("giaoHang","true").param("maBaoGia","quote")
                .param("paidAmount","830000").param("customerName",q.ten()).param("customerPhone",q.sdt()).param("customerAddress","12 Đường A").param("paymentMethodId","1").param("tinh","1").param("xa","2");
        mvc.perform(request).andExpect(flash().attributeExists("error"));verifyNoInteractions(service);
        session.setAttribute("baoGiaGhnTaiQuay",q);session.setAttribute("maBaoGiaTaiQuay","quote");
        mvc.perform(post("/pos/checkout").session(session).param("csrfPos","token").param("giaoHang","true").param("maBaoGia","quote")
                .param("paidAmount","830000").param("customerName",q.ten()).param("customerPhone",q.sdt()).param("customerAddress","Địa chỉ khác"))
                .andExpect(flash().attributeExists("error"));verifyNoInteractions(service);
        mvc.perform(request).andExpect(flash().attributeExists("success"));
        verify(service).thanhToan(any(),argThat(r->r.getGiaoHang()==q && r.getDiaChi().equals(q.diaChi())),any(),isNull());
    }
}
