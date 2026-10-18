package com.example.victorsport;

import com.example.victorsport.Config.GhnConfig;
import com.example.victorsport.Dto.BaoGiaGhn;
import com.example.victorsport.Service.*;
import com.example.victorsport.Controller.*;
import com.example.victorsport.Dto.NguoiDungDangNhap;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.http.*;
import org.springframework.web.client.RestClient;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.test.util.ReflectionTestUtils;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class GhnTichHopTests {
    @Test void diaChiMoiPreviewVaTaoVanDonChiDungStaging() {
        var config=new GhnConfig();config.setEnabled(true);config.setToken("test-only");config.setShopId(123);
        var builder=RestClient.builder();var server=MockRestServiceServer.bindTo(builder).build();
        String url="https://dev-online-gateway.ghn.vn/shiip/public-api";
        server.expect(requestTo(url+"/v3/master-data/province/all?limit=200")).andExpect(method(HttpMethod.GET))
            .andRespond(withSuccess("{\"code\":200,\"data\":[{\"_id\":1001,\"name\":\"Hà Nội\"}]}",MediaType.APPLICATION_JSON));
        server.expect(requestTo(url+"/v2/shipping-order/preview")).andExpect(method(HttpMethod.POST)).andExpect(header("Token","test-only"))
            .andRespond(withSuccess("{\"code\":200,\"data\":{\"total_fee\":31000,\"order_code\":\"\"}}",MediaType.APPLICATION_JSON));
        server.expect(requestTo(url+"/v2/shipping-order/create")).andExpect(method(HttpMethod.POST)).andExpect(header("ShopId","123"))
            .andRespond(withSuccess("{\"code\":200,\"data\":{\"total_fee\":31000,\"order_code\":\"TEST123\"}}",MediaType.APPLICATION_JSON));
        server.expect(requestTo(url+"/v2/shipping-order/detail?order_code=TEST123")).andExpect(method(HttpMethod.GET))
            .andRespond(withSuccess("{\"code\":200,\"data\":{\"order_code\":\"TEST123\",\"status\":\"delivering\"}}",MediaType.APPLICATION_JSON));
        var ghn=new GhnService(config,builder);
        assertEquals("1001",ghn.tinhMoi().get(0).get("id"));
        assertEquals(new BigDecimal("31000"),ghn.docPhi(ghn.xemTruoc(Map.of("is_new_to_address",true))));
        assertEquals("TEST123",ghn.taoDon(Map.of("client_order_code","VS-1")).get("order_code"));
        assertEquals("delivering",ghn.chiTietDon("TEST123").get("status"));server.verify();
    }
    @Test void guiDiaChiKhoMoiKhiShopChuaKhaiKhoGui() {
        var config=new GhnConfig();config.setEnabled(true);config.setToken("test-only");config.setShopId(123);
        config.setFromAddress("Cầu Giấy, Hà Nội");config.setFromProvinceName("Hà Nội");config.setFromWardName("Phường Cầu Giấy");
        var builder=RestClient.builder();var server=MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo("https://dev-online-gateway.ghn.vn/shiip/public-api/v2/shipping-order/preview"))
            .andExpect(org.springframework.test.web.client.match.MockRestRequestMatchers.content().json("""
                {"from_address":"Cầu Giấy, Hà Nội","from_province_name":"Hà Nội","from_ward_name":"Phường Cầu Giấy","from_phone":"0325327602","is_new_from_address":true}
                """))
            .andRespond(withSuccess("{\"code\":200,\"data\":{\"total_fee\":35000}}",MediaType.APPLICATION_JSON));
        assertEquals(new BigDecimal("35000"),new GhnService(config,builder).docPhi(new GhnService(config,builder).xemTruoc(Map.of())));
        server.verify();
    }
    @Test void danhMucDiaChiDuocLuuTamKhongGoiLaiLienTuc() {
        var config=new GhnConfig();config.setEnabled(true);config.setToken("test-only");config.setShopId(123);
        var builder=RestClient.builder();var server=MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo("https://dev-online-gateway.ghn.vn/shiip/public-api/v3/master-data/province/all?limit=200"))
            .andRespond(withSuccess("{\"code\":200,\"data\":[{\"_id\":1001,\"name\":\"Hà Nội\"}]}",MediaType.APPLICATION_JSON));
        var service=new GhnService(config,builder);assertEquals(service.tinhMoi(),service.tinhMoi());server.verify();
    }
    BaoGiaGhn quote(Instant han) {
        return new BaoGiaGhn(Map.of(7,1),"An","0900000001","Địa chỉ","COD",new BigDecimal("100000"),new BigDecimal("31000"),
                1001,1002,"Hà Nội","Cầu Giấy",1000,35,25,15,han);
    }
    @Test void baoGiaGhepDiaChiChiTietVoiDiaBanDaXacMinh() {
        var ghn=mock(GhnService.class);
        when(ghn.tinhMoi()).thenReturn(List.of(Map.of("id","1001","ten","Hải Phòng")));
        when(ghn.xaMoi(1001)).thenReturn(List.of(Map.of("id","1002","ten","Phường Lê Chân")));
        when(ghn.kichThuoc(1)).thenReturn(new int[]{1000,35,25,15});
        when(ghn.xemTruoc(anyMap())).thenAnswer(call->{Map<String,Object> body=call.getArgument(0);
            assertEquals("Số 10 ngõ 67, Phường Lê Chân, Hải Phòng",body.get("to_address"));
            assertEquals("Phường Lê Chân",body.get("to_ward_name"));return Map.of("total_fee",45000);});
        when(ghn.docPhi(anyMap())).thenReturn(new BigDecimal("45000"));
        var q=new GiaoHangGhnService(mock(JdbcTemplate.class),ghn).baoGia(Map.of(7,1),"An","0900000001","Số 10 ngõ 67","COD",new BigDecimal("100000"),1001,1002);
        assertEquals("Số 10 ngõ 67",q.diaChiChiTiet());
        assertEquals("Số 10 ngõ 67, Phường Lê Chân, Hải Phòng",q.diaChi());
    }
    @Test void baoGiaHetHanDoiGioDoiDiaChiDoiGiaBiChan() {
        var q=quote(Instant.now().plusSeconds(60));
        q.kiemTra(Map.of(7,1),"An","0900000001","Địa chỉ","COD",new BigDecimal("100000"));
        assertThrows(IllegalArgumentException.class,()->q.kiemTra(Map.of(7,2),"An","0900000001","Địa chỉ","COD",new BigDecimal("100000")));
        assertThrows(IllegalArgumentException.class,()->q.kiemTra(Map.of(7,1),"An","0900000001","Khác","COD",new BigDecimal("100000")));
        assertThrows(IllegalArgumentException.class,()->q.kiemTra(Map.of(7,1),"An","0900000001","Địa chỉ","COD",new BigDecimal("110000")));
        assertThrows(IllegalArgumentException.class,()->quote(Instant.now().minusSeconds(1)).kiemTra(Map.of(7,1),"An","0900000001","Địa chỉ","COD",new BigDecimal("100000")));
        assertThrows(UnsupportedOperationException.class,()->q.gio().put(9,1));
    }
    Map<String,Object> hoaDon(int state) {
        var hd=new HashMap<String,Object>();hd.put("trang_thai_hien_tai",state);hd.put("tong_tien_sau_giam",new BigDecimal("131000"));
        hd.put("tong_tien",new BigDecimal("100000"));hd.put("ten_khach_hang","An");hd.put("so_dien_thoai_khach_hang","0900000001");hd.put("dia_chi_khach_hang","Địa chỉ");return hd;
    }
    Map<String,Object> vanDon() {
        return new HashMap<>(Map.of("ma_don_shop","VS-10-fixed","ten_tinh","Hà Nội","ten_xa","Cầu Giấy","khoi_luong",1000,"dai",35,"rong",25,"cao",15));
    }
    @Test void codThuDungTongKhongThuPhiHaiLanVaGiuMaChongTrung() {
        var jdbc=mock(JdbcTemplate.class);var ghn=mock(GhnService.class);var svc=new GiaoHangGhnService(jdbc,ghn);
        when(jdbc.queryForList(contains("FROM hoa_don WITH"),eq(10))).thenReturn(List.of(hoaDon(4)));
        var vd=vanDon();when(jdbc.queryForList(contains("FROM van_don_ghn WITH"),eq(10))).thenReturn(List.of(vd));
        when(jdbc.queryForObject(contains("SUM(so_tien)"),eq(BigDecimal.class),eq(10))).thenReturn(BigDecimal.ZERO);
        when(jdbc.queryForList(contains("pt.nha_cung_cap"),eq(10))).thenReturn(List.of(Map.of("nha_cung_cap","COD")));
        var result=Map.<String,Object>of("order_code","TEST123","total_fee",31000);
        when(ghn.taoDon(anyMap())).thenAnswer(call->{Map<String,Object> body=call.getArgument(0);
            assertEquals(131000,body.get("cod_amount"));assertEquals(1,body.get("payment_type_id"));
            assertEquals("VS-10-fixed",body.get("client_order_code"));assertEquals(true,body.get("is_new_to_address"));return result;});
        when(ghn.docPhi(result)).thenReturn(new BigDecimal("31000"));
        assertEquals("TEST123",svc.taoVanDon(10));
        vd.put("ma_van_don","TEST123");assertEquals("TEST123",svc.taoVanDon(10));verify(ghn,times(1)).taoDon(anyMap());
        verify(jdbc,never()).update(contains("UPDATE hoa_don"),any(Object[].class));
    }
    @Test void donChuaXacNhanVaQrChuaDuTienKhongTaoVanDon() {
        var jdbc=mock(JdbcTemplate.class);var ghn=mock(GhnService.class);var svc=new GiaoHangGhnService(jdbc,ghn);
        when(jdbc.queryForList(contains("FROM hoa_don WITH"),eq(10))).thenReturn(List.of(hoaDon(1)));
        when(jdbc.queryForList(contains("FROM van_don_ghn WITH"),eq(10))).thenReturn(List.of(vanDon()));
        assertThrows(IllegalArgumentException.class,()->svc.taoVanDon(10));
        when(jdbc.queryForList(contains("FROM hoa_don WITH"),eq(10))).thenReturn(List.of(hoaDon(4)));
        when(jdbc.queryForObject(contains("SUM(so_tien)"),eq(BigDecimal.class),eq(10))).thenReturn(BigDecimal.ZERO);
        when(jdbc.queryForList(contains("pt.nha_cung_cap"),eq(10))).thenReturn(List.of(Map.of("nha_cung_cap","VietQR")));
        assertThrows(IllegalArgumentException.class,()->svc.taoVanDon(10));verifyNoInteractions(ghn);
    }
    @Test void dongBoGhnKhongTuThuTienHaySuaDonDaNhan() {
        var jdbc=mock(JdbcTemplate.class);var ghn=mock(GhnService.class);var svc=new GiaoHangGhnService(jdbc,ghn);
        when(jdbc.queryForList(contains("SELECT ma_van_don"),eq(10))).thenReturn(List.of(Map.of("ma_van_don","TEST123")));
        when(ghn.chiTietDon("TEST123")).thenReturn(Map.of("order_code","TEST123","status","delivered"));svc.dongBo(10);
        verify(jdbc).update(contains("UPDATE van_don_ghn SET trang_thai"),eq("delivered"),eq(10));
        verify(jdbc,never()).update(contains("UPDATE hoa_don"),any(Object[].class));
        verify(jdbc,never()).update(contains("giao_dich_thanh_toan"),any(Object[].class));
    }
    @Test void checkoutTinhPhiGhnVaFreeshipVaoTongHoaDon() {
        var jdbc=mock(JdbcTemplate.class);var giaoHang=mock(GiaoHangGhnService.class);
        var svc=spy(new SanPhamOnlineService(jdbc));ReflectionTestUtils.setField(svc,"giaoHangGhn",giaoHang);
        when(giaoHang.dangBat()).thenReturn(true);
        var dong=new HashMap<String,Object>();dong.put("id",7);dong.put("so_luong",3);dong.put("so_luong_mua",1);
        dong.put("gia_ban",new BigDecimal("100000"));dong.put("thanh_tien_goc",new BigDecimal("100000"));dong.put("thanh_tien",new BigDecimal("100000"));
        doReturn(List.of(dong)).when(svc).layGioHang(anyMap());
        when(jdbc.update(contains("UPDATE chi_tiet_san_pham"),any(),any(),any(),any())).thenReturn(1);
        when(jdbc.queryForList(contains("FROM phieu_giam_gia WITH"),eq(3),any()))
            .thenReturn(List.of(Map.of("ten_phieu_giam_gia","Freeship","loai_phieu_giam_gia",true,"gia_tri_giam_gia",new BigDecimal("30000"))));
        when(jdbc.update(contains("so_luong_su_dung=so_luong_su_dung-1"),eq(3))).thenReturn(1);
        when(jdbc.queryForObject(contains("INSERT INTO hoa_don"),eq(Integer.class),any(Object[].class))).thenReturn(10);
        when(jdbc.queryForList(contains("nha_cung_cap=N'COD'"))).thenReturn(List.of(Map.of("id",2)));
        var q=quote(Instant.now().plusSeconds(60));
        assertEquals(10,svc.datHang(Map.of(7,1),null,"An","0900000001","Địa chỉ",false,"COD",3,new BigDecimal("101000"),q));
        verify(jdbc).update(contains("UPDATE hoa_don SET phi_van_chuyen"),eq(new BigDecimal("1000.00")),eq(3),eq(10));
        verify(giaoHang).luu(10,q);
    }
    @Test void batGhnKhongChoDatHangBoQuaBaoGia() {
        var jdbc=mock(JdbcTemplate.class);var giaoHang=mock(GiaoHangGhnService.class);
        var svc=new SanPhamOnlineService(jdbc);ReflectionTestUtils.setField(svc,"giaoHangGhn",giaoHang);
        when(giaoHang.dangBat()).thenReturn(true);
        assertThrows(IllegalArgumentException.class,()->svc.datHang(Map.of(7,1),null,"An","0900000001","Địa chỉ",false,"COD",null,null));
        verifyNoInteractions(jdbc);
    }
    @Test void taoVanDonBatBuocAdminVaMaBaoVe() throws Exception {
        var controller=new VanChuyenController(mock(VanChuyenService.class),mock(GhnService.class));var svc=mock(GiaoHangGhnService.class);
        ReflectionTestUtils.setField(controller,"giaoHang",svc);var mvc=MockMvcBuilders.standaloneSetup(controller).build();
        var s=new MockHttpSession();s.setAttribute(DangNhapController.KHOA_NGUOI_DUNG_DANG_NHAP,new NguoiDungDangNhap(1,"a@gmail.com","A","Nhân viên"));
        mvc.perform(post("/quan-ly/van-chuyen/10/ghn/tao").session(s)).andExpect(status().isForbidden());
        s.setAttribute(DangNhapController.KHOA_NGUOI_DUNG_DANG_NHAP,new NguoiDungDangNhap(1,"a@gmail.com","A","Quản trị viên"));
        mvc.perform(post("/quan-ly/van-chuyen/10/ghn/tao").session(s)).andExpect(status().isForbidden());verifyNoInteractions(svc);
    }
}
