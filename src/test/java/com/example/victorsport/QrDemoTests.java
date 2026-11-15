package com.example.victorsport;

import com.example.victorsport.Service.*;
import com.example.victorsport.Controller.*;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.math.BigDecimal;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class QrDemoTests {
    private static final String MA="DEMO10"+"a".repeat(32);
    private QrDemoService service(JdbcTemplate j) { var s=new QrDemoService(j);ReflectionTestUtils.setField(s,"bat",true);return s; }
    private JdbcTemplate jdbc(String tt,int waited,int ttDon) {
        var j=mock(JdbcTemplate.class);
        when(j.queryForList(contains("FROM hoa_don WITH"),eq(10),eq(7),eq(0)))
            .thenReturn(List.of(Map.of("tong_tien_sau_giam",new BigDecimal("220900"),"trang_thai_hien_tai",ttDon)));
        when(j.queryForList(contains("FROM giao_dich_thanh_toan gd WITH"),eq(10)))
            .thenReturn(List.of(Map.of("id",30,"so_tien",new BigDecimal("220900"),"trang_thai",tt,"da_cho",waited)));
        when(j.queryForList(contains("WHERE gd.ma_yeu_cau=?"),eq(MA))).thenReturn(List.of(Map.of("id",10)));
        when(j.queryForList(contains("FROM hoa_don WITH"),eq(10),eq(MA)))
            .thenReturn(List.of(Map.of("tong_tien_sau_giam",new BigDecimal("220900"),"trang_thai_hien_tai",ttDon)));
        return j;
    }
    @Test void cheDoTatKhongTaoHayXacNhanTien() {
        var j=mock(JdbcTemplate.class);var s=new QrDemoService(j);
        assertFalse(s.dangBat());assertThrows(IllegalArgumentException.class,()->s.taoGiaoDich(10,BigDecimal.TEN));
        assertThrows(IllegalArgumentException.class,()->s.xuLy(10,7,false,"thanh_cong"));verifyNoInteractions(j);
    }
    @Test void quetXacNhanChiThuMotLanKhongChuyenTrangThaiDon() {
        var j=jdbc("cho_thanh_toan",8,1);
        assertEquals("thanh_cong",service(j).xuLyQuet(MA,"thanh_cong"));
        verify(j).update(contains("UPDATE giao_dich_thanh_toan"),eq("thanh_cong"),eq("Đã thanh toán QR"),eq(30));
        verify(j).update(contains("UPDATE hoa_don SET ngay_thanh_toan"),eq(10));
        verify(j).update(contains("INSERT INTO lich_su_hoa_don"),eq(10),eq(1));
        verify(j,never()).update(contains("chi_tiet_san_pham"),any(Object[].class));
        verify(j,never()).update(contains("SET trang_thai_hien_tai"),any(Object[].class));
    }
    @Test void mayTinhKhongTuXacNhanThanhCongKeCaDaChoLau() {
        var j=jdbc("cho_thanh_toan",100,1);
        assertThrows(IllegalArgumentException.class,()->service(j).xuLy(10,7,false,"thanh_cong"));
        verifyNoInteractions(j);
    }
    @Test void guiLapVaKetQuaDenSauKhongGhiDeGiaoDichDaKetThuc() {
        for(String tt:List.of("thanh_cong","that_bai","da_huy")) {
            var j=jdbc(tt,15,1);assertEquals(tt,service(j).xuLyQuet(MA,"thanh_cong"));
            verify(j,never()).update(anyString(),any(Object[].class));
        }
    }
    @Test void thatBaiVaHuyKhongThuTienKhongHuyDon() {
        for(String tt:List.of("that_bai","da_huy")) {
            var j=jdbc("cho_thanh_toan",1,1);assertEquals(tt,service(j).xuLy(10,7,false,tt));
            verify(j,never()).update(contains("UPDATE hoa_don"),any(Object[].class));
            verify(j,never()).update(contains("INSERT INTO lich_su"),any(Object[].class));
        }
    }
    @Test void donDaHuyKhongTuThanhToan() {
        var j=jdbc("cho_thanh_toan",15,6);
        assertThrows(IllegalArgumentException.class,()->service(j).xuLyQuet(MA,"thanh_cong"));
        verify(j,never()).update(anyString(),any(Object[].class));
    }
    @Test void khachKhacVaKhongCoPhienKhongXuLyDuoc() {
        var j=mock(JdbcTemplate.class);var s=service(j);
        assertNull(s.lay(10,null,false));assertThrows(IllegalArgumentException.class,()->s.xuLy(10,null,false,"thanh_cong"));
        verifyNoInteractions(j);
        assertThrows(IllegalArgumentException.class,()->s.xuLy(10,99,false,"thanh_cong"));
        verify(j,never()).update(anyString(),any(Object[].class));
    }
    @Test void tongTienKhongKhopKhongThuTien() {
        var j=jdbc("cho_thanh_toan",8,1);
        when(j.queryForList(contains("FROM giao_dich_thanh_toan gd WITH"),eq(10)))
            .thenReturn(List.of(Map.of("id",30,"so_tien",BigDecimal.TEN,"trang_thai","cho_thanh_toan","da_cho",8)));
        assertThrows(IllegalArgumentException.class,()->service(j).xuLyQuet(MA,"thanh_cong"));
        verify(j,never()).update(anyString(),any(Object[].class));
    }
    @Test void qrQuetDuocNhungChiLaNoiDungDemoKhongPhaiLenhChuyenTien() throws Exception {
        String uri=service(mock(JdbcTemplate.class)).anhQr("http://192.168.1.2:8080/thanh-toan/qr-demo/quet/"+MA);
        var image=javax.imageio.ImageIO.read(new java.io.ByteArrayInputStream(Base64.getDecoder().decode(uri.substring(uri.indexOf(',')+1))));
        int[] pixels=image.getRGB(0,0,image.getWidth(),image.getHeight(),null,0,image.getWidth());
        var source=new com.google.zxing.RGBLuminanceSource(image.getWidth(),image.getHeight(),pixels);
        String data=new com.google.zxing.qrcode.QRCodeReader().decode(new com.google.zxing.BinaryBitmap(new com.google.zxing.common.HybridBinarizer(source))).getText();
        assertEquals("http://192.168.1.2:8080/thanh-toan/qr-demo/quet/"+MA,data);
    }
    @Test void controllerCanCsrfVaDungPhienChoTuDongThanhToan() throws Exception {
        var s=mock(QrDemoService.class);when(s.dangBat()).thenReturn(true);when(s.xuLy(10,null,true,"da_huy")).thenReturn("da_huy");
        var mvc=MockMvcBuilders.standaloneSetup(new QrDemoController(s)).build();var session=new MockHttpSession();
        session.setAttribute("csrfQrDemo","token");session.setAttribute("donVangLai_10",true);
        mvc.perform(post("/thanh-toan/qr-demo/10/ket-qua").session(session).param("csrf","wrong"))
            .andExpect(status().isForbidden());verify(s,never()).xuLy(anyInt(),any(),anyBoolean(),anyString());
        mvc.perform(post("/thanh-toan/qr-demo/10/ket-qua").session(session).param("csrf","token").param("ketQua","da_huy"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.trangThai").value("da_huy"));
    }
    @Test void endpointBiAnKhiTatCheDoDemo() throws Exception {
        var s=mock(QrDemoService.class);var mvc=MockMvcBuilders.standaloneSetup(new QrDemoController(s)).build();
        mvc.perform(get("/thanh-toan/qr-demo/10")).andExpect(status().isNotFound());
        mvc.perform(post("/thanh-toan/qr-demo/10/ket-qua").param("csrf","x")).andExpect(status().isNotFound());
        verify(s,never()).xuLy(anyInt(),any(),anyBoolean(),anyString());
    }
    @Test void quetChiMoTrangVaPhaiCoCsrfMoiThanhToan() throws Exception {
        var s=mock(QrDemoService.class);when(s.dangBat()).thenReturn(true);
        when(s.layTheoMa(MA)).thenReturn(Map.of("id",10,"ma_hoa_don","HD00010","so_tien",BigDecimal.TEN,"trang_thai","cho_thanh_toan"));
        when(s.xuLyQuet(MA,"thanh_cong")).thenReturn("thanh_cong");
        var mvc=MockMvcBuilders.standaloneSetup(new QrDemoController(s)).build();var session=new MockHttpSession();
        mvc.perform(post("/thanh-toan/qr-demo/quet/"+MA+"/xac-nhan").session(session).param("csrf","fake")).andExpect(status().isForbidden());
        mvc.perform(get("/thanh-toan/qr-demo/quet/"+MA).session(session)).andExpect(status().isOk())
            .andExpect(view().name("client/cart/qr-demo")).andExpect(model().attribute("trenDienThoai",true))
            .andExpect(header().string("Referrer-Policy","no-referrer"));
        verify(s,never()).xuLyQuet(anyString(),anyString());
        mvc.perform(post("/thanh-toan/qr-demo/quet/"+MA+"/xac-nhan").session(session).param("csrf",session.getAttribute("csrfQrDemo").toString()))
            .andExpect(status().isOk()).andExpect(jsonPath("$.trangThai").value("thanh_cong"));
        verify(s).xuLyQuet(MA,"thanh_cong");
    }
    @Test void maGiaHetHanVaMaKhacKhongXuLyDuoc() throws Exception {
        var j=mock(JdbcTemplate.class);var s=service(j);
        assertNull(s.layTheoMa("123"));verifyNoInteractions(j);
        assertThrows(IllegalArgumentException.class,()->s.xuLyQuet(MA,"thanh_cong"));
        verify(j,never()).update(anyString(),any(Object[].class));
        var fake=mock(QrDemoService.class);when(fake.dangBat()).thenReturn(true);
        var session=new MockHttpSession();session.setAttribute("csrfQrDemo","csrf");session.setAttribute("qrDemoDaQuet_"+MA,true);
        MockMvcBuilders.standaloneSetup(new QrDemoController(fake)).build()
            .perform(post("/thanh-toan/qr-demo/quet/DEMO11"+"b".repeat(32)+"/xac-nhan").session(session).param("csrf","csrf"))
            .andExpect(status().isForbidden());verify(fake,never()).xuLyQuet(anyString(),anyString());
    }
    @Test void mayTinhChiDocTrangThaiKhongTuThuTien() throws Exception {
        var s=mock(QrDemoService.class);when(s.dangBat()).thenReturn(true);
        when(s.lay(10,7,false)).thenReturn(Map.of("trang_thai","thanh_cong","trang_thai_hien_tai",1,"het_han",false));
        var session=new MockHttpSession();session.setAttribute("khachHangOnlineDangNhap",7);
        var mvc=MockMvcBuilders.standaloneSetup(new QrDemoController(s)).build();
        mvc.perform(get("/thanh-toan/qr-demo/10/trang-thai").session(session)).andExpect(status().isOk())
            .andExpect(jsonPath("$.trangThai").value("thanh_cong")).andExpect(header().string("Cache-Control","no-store"));
        mvc.perform(get("/thanh-toan/qr-demo/10/trang-thai")).andExpect(status().isNotFound());
        verify(s,never()).xuLyQuet(anyString(),anyString());verify(s,never()).xuLy(anyInt(),any(),anyBoolean(),anyString());
    }
    @Test void lienKetQuetDungLanThayLocalhostVaHoTroDomain() {
        var c=new QrDemoController(mock(QrDemoService.class)) { @Override protected String diaChiLan(){return "192.168.1.22";} };
        var request=new org.springframework.mock.web.MockHttpServletRequest();request.setServerName("localhost");request.setServerPort(8080);
        assertEquals("http://192.168.1.22:8080/thanh-toan/qr-demo/quet/"+MA,c.urlQuet(request,MA));
        ReflectionTestUtils.setField(c,"publicUrl","https://demo.example.com/");
        assertEquals("https://demo.example.com/thanh-toan/qr-demo/quet/"+MA,c.urlQuet(request,MA));
        ReflectionTestUtils.setField(c,"publicUrl","javascript:bad");assertThrows(org.springframework.web.server.ResponseStatusException.class,()->c.urlQuet(request,MA));
    }
    @Test void checkoutDemoKhongCanKhoaVnpayVaChuyenDenQr() throws Exception {
        var s=mock(SanPhamOnlineService.class);var session=new MockHttpSession();session.setAttribute("gioHangOnline",new LinkedHashMap<>(Map.of(9,1)));
        when(s.datHang(anyMap(),isNull(),eq("Long"),eq("0325327602"),eq("Hà Nội"),eq(false),eq("QR_DEMO"))).thenReturn(10);
        MockMvcBuilders.standaloneSetup(new SanPhamOnlineController(s,mock(DiaChiKhachHangService.class))).build()
            .perform(post("/thanh-toan").session(session).param("ten","Long").param("sdt","0325327602").param("diaChi","Hà Nội").param("phuongThuc","QR_DEMO"))
            .andExpect(redirectedUrl("/thanh-toan/qr-demo/10"));assertEquals(true,session.getAttribute("donVangLai_10"));
    }
    @Test void ghiChuCuChiHienDaThanhToanOnlineKhiDaThuDu() {
        for(boolean paid:List.of(false,true)) {
            var j=mock(JdbcTemplate.class);
            when(j.queryForList(contains("SUM(CASE"),eq(111))).thenReturn(List.of(Map.of("da_thu",paid?new BigDecimal("100000"):BigDecimal.ZERO)));
            when(j.queryForList(contains("pt.nha_cung_cap=N'QR_DEMO'"),eq(111))).thenReturn(List.of(Map.of("id",30)));
            var hd=new HashMap<String,Object>();hd.put("id",111);hd.put("loai_don",1);
            hd.put("tong_tien_sau_giam",new BigDecimal("100000"));hd.put("ghi_chu","QR DEMO giả lập, không thu tiền thật");
            ReflectionTestUtils.invokeMethod(new HoaDonService(j),"themThanhToan",hd);
            assertEquals(paid?"Đã thanh toán QR":"Chờ thanh toán online",hd.get("ghi_chu"));
        }
    }
    private JdbcTemplate jdbcTaiLai(String tt,int seconds,int order) {
        var j=jdbc(tt,seconds,order);
        when(j.queryForList(contains("FROM giao_dich_thanh_toan gd WITH"),eq(10))).thenReturn(List.of(Map.of(
            "id",30,"so_tien",new BigDecimal("220900"),"trang_thai",tt,"da_cho",seconds,"ma_yeu_cau",MA)));
        when(j.update(contains("UPDATE giao_dich_thanh_toan SET trang_thai=N'cho_thanh_toan'"),anyString(),any(BigDecimal.class),eq(30),eq(MA))).thenReturn(1);
        return j;
    }
    @Test void taiLaiSauThatBaiHoacHetHanDoiMaKhongTaoThemDonVaTruKho() {
        for(String tt:List.of("that_bai","da_huy","cho_thanh_toan")) {
            var j=jdbcTaiLai(tt,901,1);service(j).taoLai(10,7,false,MA);
            var ma=org.mockito.ArgumentCaptor.forClass(String.class);
            verify(j).update(contains("UPDATE giao_dich_thanh_toan SET trang_thai=N'cho_thanh_toan'"),ma.capture(),eq(new BigDecimal("220900")),eq(30),eq(MA));
            assertNotEquals(MA,ma.getValue());assertTrue(ma.getValue().matches("DEMO10[a-f0-9]{32}"));
            verify(j,never()).update(contains("chi_tiet_san_pham"),any(Object[].class));
            verify(j,never()).update(contains("INSERT INTO hoa_don"),any(Object[].class));
            verify(j,never()).update(contains("INSERT INTO giao_dich_thanh_toan"),any(Object[].class));
        }
    }
    @Test void khongTaiLaiKhiDaTraTienDonDaHuyHoacQrConHan() {
        for(var tinhHuong:List.of(new Object[]{"thanh_cong",30,1},new Object[]{"that_bai",30,6},new Object[]{"cho_thanh_toan",30,1},new Object[]{"that_bai",30,4})) {
            var j=jdbcTaiLai((String)tinhHuong[0],(int)tinhHuong[1],(int)tinhHuong[2]);
            assertThrows(IllegalArgumentException.class,()->service(j).taoLai(10,7,false,MA));
            verify(j,never()).update(anyString(),any(Object[].class));
        }
    }
    @Test void taiLaiCanChuDonVaKhongVuotQuaThanhToanPhuongThucKhac() {
        var j=jdbcTaiLai("that_bai",30,1);
        assertThrows(IllegalArgumentException.class,()->service(j).taoLai(10,null,false,MA));
        verifyNoInteractions(j);
        when(j.queryForList(contains("SELECT id FROM giao_dich_thanh_toan WHERE"),eq(10))).thenReturn(List.of(Map.of("id",99)));
        assertThrows(IllegalArgumentException.class,()->service(j).taoLai(10,7,false,MA));
        verify(j,never()).update(anyString(),any(Object[].class));
    }
    @Test void bamLapMaCuKhongDoiMaMoiLanHai() {
        var j=jdbcTaiLai("that_bai",30,1);
        when(j.queryForList(contains("FROM giao_dich_thanh_toan gd WITH"),eq(10))).thenReturn(List.of(Map.of(
            "id",30,"so_tien",new BigDecimal("220900"),"trang_thai","cho_thanh_toan","da_cho",0,"ma_yeu_cau","DEMO10"+"b".repeat(32))));
        service(j).taoLai(10,7,false,MA);verify(j,never()).update(anyString(),any(Object[].class));
    }
    @Test void endpointTaiLaiCanCsrfVaTruyenDungQuyenKhach() throws Exception {
        var s=mock(QrDemoService.class);when(s.dangBat()).thenReturn(true);
        var mvc=MockMvcBuilders.standaloneSetup(new QrDemoController(s)).build();var session=new MockHttpSession();
        session.setAttribute("csrfQrDemo","csrf");session.setAttribute("khachHangOnlineDangNhap",7);
        mvc.perform(post("/thanh-toan/qr-demo/10/tai-lai").session(session).param("csrf","wrong").param("maCu",MA)).andExpect(status().isForbidden());
        verify(s,never()).taoLai(anyInt(),any(),anyBoolean(),anyString());
        mvc.perform(post("/thanh-toan/qr-demo/10/tai-lai").session(session).param("csrf","csrf").param("maCu",MA))
            .andExpect(status().isOk()).andExpect(jsonPath("$.url").value("/thanh-toan/qr-demo/10"));
        verify(s).taoLai(10,7,false,MA);
    }
    @Test void demNguocDung120GiayVaHetHanChuyenThatBai() {
        for(int seconds:List.of(0,119,120,121)) {
            var j=mock(JdbcTemplate.class);
            when(j.queryForList(contains("WHERE gd.ma_yeu_cau=?"),eq(MA))).thenReturn(List.of(Map.of("id",10,"da_cho",seconds,"trang_thai","cho_thanh_toan")));
            var gd=service(j).layTheoMa(MA);
            assertEquals(Math.max(0,120-seconds),gd.get("con_lai"));
            assertEquals(seconds>=120,gd.get("het_han"));
            assertEquals(seconds>=120?"that_bai":"cho_thanh_toan",gd.get("trang_thai"));
        }
    }
    @Test void het120GiayKhongGhiNhanTienDuDangMoTrangXacNhan() {
        var j=jdbc("cho_thanh_toan",120,1);
        when(j.queryForList(contains("WHERE gd.ma_yeu_cau=?"),eq(MA))).thenReturn(List.of(Map.of("id",10,"da_cho",120,"trang_thai","cho_thanh_toan")));
        assertEquals("that_bai",service(j).xuLyQuet(MA,"thanh_cong"));
        verify(j,never()).update(contains("UPDATE hoa_don"),any(Object[].class));
        verify(j,never()).update(contains("thanh_cong"),any(Object[].class));
    }
    @Test void hetHanKhongGhiDeGiaoDichDaThanhCong() {
        var j=mock(JdbcTemplate.class);
        when(j.queryForList(contains("WHERE gd.ma_yeu_cau=?"),eq(MA))).thenReturn(List.of(Map.of("id",10,"da_cho",120,"trang_thai","thanh_cong")));
        assertEquals("thanh_cong",service(j).layTheoMa(MA).get("trang_thai"));
        verify(j,never()).update(anyString(),any(Object[].class));
    }
    @Test void dungHaiPhutMoiChoTaiLaiQrConCho() {
        var j=jdbcTaiLai("cho_thanh_toan",120,1);service(j).taoLai(10,7,false,MA);
        verify(j).update(contains("UPDATE giao_dich_thanh_toan SET trang_thai=N'cho_thanh_toan'"),anyString(),any(BigDecimal.class),eq(30),eq(MA));
        var chuaHet=jdbcTaiLai("cho_thanh_toan",119,1);
        assertThrows(IllegalArgumentException.class,()->service(chuaHet).taoLai(10,7,false,MA));
        verify(chuaHet,never()).update(anyString(),any(Object[].class));
    }
    @Test void tuDanhDauHetHanChiCapNhatQrDangChoVaBoQuaKhiTatCheDo() {
        var j=mock(JdbcTemplate.class);new QrDemoService(j).danhDauHetHan();verifyNoInteractions(j);
        service(j).danhDauHetHan();
        verify(j).update(argThat((String sql)->sql.contains("pt.nha_cung_cap=N'QR_DEMO'")
            && sql.contains("gd.trang_thai=N'cho_thanh_toan'") && sql.contains("DATEADD(SECOND,-120") && sql.contains("SET trang_thai=N'that_bai'")));
    }
}
