package com.example.victorsport;

import com.example.victorsport.Service.*;
import com.example.victorsport.Controller.*;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.math.BigDecimal;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.util.*;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class VnpaySandboxTests {
    private static final String MA="VS10"+"a".repeat(32);
    private VnpaySandboxService service(JdbcTemplate jdbc) {
        var s=new VnpaySandboxService(jdbc);
        ReflectionTestUtils.setField(s,"tmnCode","TESTONLY");
        ReflectionTestUtils.setField(s,"hashSecret","test-secret-not-a-real-merchant-key");
        return s;
    }
    private Map<String,String> thamSo(String response,String status) {
        var p=new TreeMap<String,String>();
        p.put("vnp_TmnCode","TESTONLY");p.put("vnp_TxnRef",MA);p.put("vnp_Amount","22090000");
        p.put("vnp_ResponseCode",response);p.put("vnp_TransactionStatus",status);p.put("vnp_TransactionNo","123456");
        return p;
    }
    private Map<String,String> ky(Map<String,String> input) throws Exception {
        var p=new TreeMap<>(input);p.remove("vnp_SecureHash");
        var parts=new ArrayList<String>();
        for(var e:p.entrySet()) parts.add(URLEncoder.encode(e.getKey(),StandardCharsets.UTF_8)+"="+URLEncoder.encode(e.getValue(),StandardCharsets.UTF_8));
        var mac=Mac.getInstance("HmacSHA512");mac.init(new SecretKeySpec("test-secret-not-a-real-merchant-key".getBytes(StandardCharsets.UTF_8),"HmacSHA512"));
        p.put("vnp_SecureHash",HexFormat.of().formatHex(mac.doFinal(String.join("&",parts).getBytes(StandardCharsets.UTF_8))));
        return p;
    }
    private JdbcTemplate jdbc(String trangThai,int ttDon) {
        var j=mock(JdbcTemplate.class);
        when(j.queryForList(contains("SELECT gd.id_hoa_don"),eq(MA))).thenReturn(List.of(Map.of("id_hoa_don",10)));
        when(j.queryForList(contains("FROM hoa_don WITH"),eq(10))).thenReturn(List.of(Map.of("tong_tien_sau_giam",new BigDecimal("220900"),"trang_thai_hien_tai",ttDon)));
        when(j.queryForList(contains("SELECT id,so_tien,trang_thai"),eq(10),eq(MA)))
                .thenReturn(List.of(Map.of("id",30,"so_tien",new BigDecimal("220900"),"trang_thai",trangThai)));
        return j;
    }
    @Test void lienKetChiDungSandboxNhan100VaCoChuKyThoiHan15Phut() {
        var s=service(mock(JdbcTemplate.class));
        String url=s.taoDuongDan(MA,new BigDecimal("220900"),"127.0.0.1",ZonedDateTime.of(2026,11,14,10,0,0,0,ZoneId.of("Asia/Ho_Chi_Minh")));
        assertEquals("sandbox.vnpayment.vn",URI.create(url).getHost());
        var p=new TreeMap<String,String>();
        for(String part:URI.create(url).getRawQuery().split("&")) {var v=part.split("=",2);p.put(v[0],URLDecoder.decode(v[1],StandardCharsets.UTF_8));}
        assertEquals("22090000",p.get("vnp_Amount"));assertEquals("20261114101500",p.get("vnp_ExpireDate"));
        assertEquals("20261114100000",p.get("vnp_CreateDate"));assertTrue(s.chuKyHopLe(p));
        assertThrows(IllegalArgumentException.class,()->s.taoDuongDan(MA,new BigDecimal("1.5"),"127.0.0.1",ZonedDateTime.now()));
    }
    @Test void chuaCauHinhKhongTaoDonHayTruKho() {
        var j=mock(JdbcTemplate.class);var online=new SanPhamOnlineService(j);
        ReflectionTestUtils.setField(online,"vnpay",new VnpaySandboxService(j));
        assertThrows(IllegalArgumentException.class,()->online.datHang(Map.of(1,1),null,"Long","0325327602","Hà Nội",false,"VNPAY"));
        verifyNoInteractions(j);
    }
    @Test void chuKyGiaVaSaiMerchantKhongDocDb() throws Exception {
        var j=mock(JdbcTemplate.class);var s=service(j);var p=ky(thamSo("00","00"));
        p.put("vnp_Amount","1");assertEquals("97",s.nhanIpn(p).RspCode());
        var other=thamSo("00","00");other.put("vnp_TmnCode","OTHER");
        assertEquals("97",s.nhanIpn(ky(other)).RspCode());verifyNoInteractions(j);
    }
    @Test void saiSoTienKhongCapNhatTien() throws Exception {
        var j=jdbc("cho_thanh_toan",1);var p=thamSo("00","00");p.put("vnp_Amount","10000");
        assertEquals("04",service(j).nhanIpn(ky(p)).RspCode());verify(j,never()).update(anyString(),any(Object[].class));
    }
    @Test void khongTimThayDonTra01() throws Exception {
        var j=mock(JdbcTemplate.class);assertEquals("01",service(j).nhanIpn(ky(thamSo("00","00"))).RspCode());
        verify(j,never()).update(anyString(),any(Object[].class));
    }
    @Test void thanhCongGhiNhanTienKhongDoiTrangThaiDonVaKhongTruKho() throws Exception {
        var j=jdbc("cho_thanh_toan",1);assertEquals("00",service(j).nhanIpn(ky(thamSo("00","00"))).RspCode());
        verify(j).update(contains("UPDATE giao_dich_thanh_toan"),eq("thanh_cong"),eq("123456"),anyString(),anyString(),eq(30));
        verify(j).update(contains("UPDATE hoa_don SET ngay_thanh_toan"),eq(10));
        verify(j).update(contains("INSERT INTO lich_su_hoa_don"),eq(10),eq(1),anyString());
        verify(j,never()).update(contains("chi_tiet_san_pham"),any(Object[].class));
        verify(j,never()).update(contains("SET trang_thai_hien_tai"),any(Object[].class));
    }
    @Test void ipnLapLaiKhongCongTienLanHai() throws Exception {
        var j=jdbc("thanh_cong",1);assertEquals("02",service(j).nhanIpn(ky(thamSo("00","00"))).RspCode());
        verify(j,never()).update(anyString(),any(Object[].class));
    }
    @Test void huyVaThatBaiKhongGhiNhanDaThanhToan() throws Exception {
        for(String code:List.of("24","51","11")) {
            var j=jdbc("cho_thanh_toan",1);assertEquals("00",service(j).nhanIpn(ky(thamSo(code,"02"))).RspCode());
            verify(j).update(contains("UPDATE giao_dich_thanh_toan"),eq(code.equals("24")?"da_huy":"that_bai"),anyString(),anyString(),anyString(),eq(30));
            verify(j,never()).update(contains("UPDATE hoa_don"),any(Object[].class));
        }
    }
    @Test void response00NhungTransactionStatusLoiKhongThuTien() throws Exception {
        var j=jdbc("cho_thanh_toan",1);service(j).nhanIpn(ky(thamSo("00","02")));
        verify(j,never()).update(contains("UPDATE hoa_don"),any(Object[].class));
    }
    @Test void thongBaoThanhCongDenMuonGiuDonDaHuyDeDoiSoat() throws Exception {
        var j=jdbc("da_huy",6);assertEquals("00",service(j).nhanIpn(ky(thamSo("00","00"))).RspCode());
        verify(j).update(contains("INSERT INTO lich_su_hoa_don"),eq(10),eq(6),contains("đối soát"));
        verify(j,never()).update(contains("SET trang_thai_hien_tai"),any(Object[].class));
    }
    @Test void returnChiHienThiKhongTuXacNhanTien() throws Exception {
        var s=mock(VnpaySandboxService.class);when(s.chuKyHopLe(anyMap())).thenReturn(true);when(s.timDonTheoMa(MA)).thenReturn(10);
        when(s.layGiaoDich(10,7,false)).thenReturn(Map.of("id",10,"trang_thai","cho_thanh_toan"));
        var session=new MockHttpSession();session.setAttribute("khachHangOnlineDangNhap",7);
        MockMvcBuilders.standaloneSetup(new VnpaySandboxController(s)).build()
            .perform(get("/thanh-toan/vnpay/ket-qua").session(session).param("vnp_TxnRef",MA).param("vnp_ResponseCode","00"))
            .andExpect(status().isOk()).andExpect(view().name("client/cart/vnpay")).andExpect(header().string("Cache-Control","no-store"));
        verify(s,never()).nhanIpn(anyMap());
    }
    @Test void ipnKhongCanDangNhapTraDungJsonVaChanThamSoTrung() throws Exception {
        var s=mock(VnpaySandboxService.class);when(s.nhanIpn(anyMap())).thenReturn(new VnpaySandboxService.KetQua("00","Confirm success"));
        var mvc=MockMvcBuilders.standaloneSetup(new VnpaySandboxController(s)).build();
        mvc.perform(get("/thanh-toan/vnpay/ipn").param("vnp_TxnRef",MA)).andExpect(status().isOk()).andExpect(jsonPath("$.RspCode").value("00"));
        clearInvocations(s);
        mvc.perform(get("/thanh-toan/vnpay/ipn").param("vnp_Amount","1","2")).andExpect(jsonPath("$.RspCode").value("97"));
        verifyNoInteractions(s);
    }
    @Test void khongChoXemThanhToanCuaKhachKhac() throws Exception {
        var s=mock(VnpaySandboxService.class);
        MockMvcBuilders.standaloneSetup(new VnpaySandboxController(s)).build().perform(get("/thanh-toan/vnpay/10")).andExpect(status().isNotFound());
        var j=mock(JdbcTemplate.class);assertNull(service(j).layGiaoDich(10,null,false));verifyNoInteractions(j);
    }
    @Test void checkoutVnpayGiuQuyenKhachVangLaiVaXoaDungSanPhamDaMua() throws Exception {
        var s=mock(SanPhamOnlineService.class);var session=new MockHttpSession();
        var gio=new LinkedHashMap<>(Map.of(9,1));session.setAttribute("gioHangOnline",gio);
        when(s.datHang(anyMap(),isNull(),eq("Long"),eq("0325327602"),eq("Hà Nội"),eq(false),eq("VNPAY"))).thenReturn(10);
        MockMvcBuilders.standaloneSetup(new SanPhamOnlineController(s,mock(DiaChiKhachHangService.class))).build()
            .perform(post("/thanh-toan").session(session).param("ten","Long").param("sdt","0325327602").param("diaChi","Hà Nội").param("phuongThuc","VNPAY"))
            .andExpect(redirectedUrl("/thanh-toan/vnpay/10"));
        assertEquals(true,session.getAttribute("donVangLai_10"));assertTrue(gio.isEmpty());
    }
    @Test void adminKhongTheTuXacNhanTienVnpayChuaThanhToan() {
        var j=mock(JdbcTemplate.class);
        when(j.queryForList(contains("SELECT nv.id"),eq(2))).thenReturn(List.of(Map.of("id",2)));
        when(j.queryForList(contains("SELECT * FROM hoa_don WITH"),eq(10)))
            .thenReturn(List.of(Map.of("trang_thai_hien_tai",1,"tong_tien_sau_giam",new BigDecimal("220900"))));
        when(j.queryForList(contains("SELECT TOP 1 pt.nha_cung_cap"),eq(10))).thenReturn(List.of(Map.of("nha_cung_cap","VNPAY_SANDBOX")));
        when(j.queryForList(contains("SELECT id FROM hoa_don_chi_tiet"),eq(10))).thenReturn(List.of(Map.of("id",1)));
        when(j.queryForObject(contains("SUM(so_tien)"),eq(BigDecimal.class),eq(10))).thenReturn(BigDecimal.ZERO);
        when(j.queryForList(contains("SELECT gd.id FROM giao_dich_thanh_toan"),eq(10))).thenReturn(List.of(Map.of("id",30)));
        assertThrows(IllegalArgumentException.class,()->new HoaDonService(j).xacNhanThanhToan(10,2));
        verify(j,never()).update(anyString(),any(Object[].class));
    }
    @Test void adminXacNhanDonVnpayDaTraDuKhongTaoThemKhoanThu() {
        var j=mock(JdbcTemplate.class);
        when(j.queryForList(contains("SELECT nv.id"),eq(2))).thenReturn(List.of(Map.of("id",2)));
        when(j.queryForList(contains("SELECT * FROM hoa_don WITH"),eq(10)))
            .thenReturn(List.of(Map.of("trang_thai_hien_tai",1,"tong_tien_sau_giam",new BigDecimal("220900"))));
        when(j.queryForList(contains("SELECT TOP 1 pt.nha_cung_cap"),eq(10))).thenReturn(List.of(Map.of("nha_cung_cap","VNPAY_SANDBOX")));
        when(j.queryForList(contains("SELECT id FROM hoa_don_chi_tiet"),eq(10))).thenReturn(List.of(Map.of("id",1)));
        when(j.queryForObject(contains("SUM(so_tien)"),eq(BigDecimal.class),eq(10))).thenReturn(new BigDecimal("220900"));
        when(j.queryForList(contains("SELECT gd.id FROM giao_dich_thanh_toan"),eq(10))).thenReturn(List.of(Map.of("id",30)));
        when(j.update(contains("UPDATE hoa_don SET trang_thai_hien_tai=2"),eq(2),eq(10))).thenReturn(1);
        new HoaDonService(j).xacNhanThanhToan(10,2);
        verify(j,never()).update(contains("INSERT INTO giao_dich_thanh_toan"),any(Object[].class));
        verify(j,never()).update(contains("chi_tiet_san_pham"),any(Object[].class));
    }
}
