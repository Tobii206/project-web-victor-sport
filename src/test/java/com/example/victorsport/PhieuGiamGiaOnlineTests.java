package com.example.victorsport;

import com.example.victorsport.Controller.SanPhamOnlineController;
import com.example.victorsport.Service.*;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.math.BigDecimal;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class PhieuGiamGiaOnlineTests {
    @Test void tuChonMucGiamCaoNhatVaFreeshipDuPhiShipBangKhong() {
        var service=new SanPhamOnlineService(mock(JdbcTemplate.class));
        assertEquals(2,service.chonPhieuTotNhat(List.of(
                Map.of("id",1,"tien_giam",new BigDecimal("100000")),
                Map.of("id",2,"tien_giam",new BigDecimal("200000")),
                Map.of("id",3,"tien_giam",new BigDecimal("50000")))));
        assertEquals(4,service.chonPhieuTotNhat(List.of(Map.of("id",4,"tien_giam",BigDecimal.ZERO))));
        assertNull(service.chonPhieuTotNhat(List.of()));
    }

    @Test void chonSanVaGiuLuaChonKhongDungPhieuKhiDatHangLoi() throws Exception {
        var service=mock(SanPhamOnlineService.class);
        var goc=new BigDecimal("1000000");var giam=new BigDecimal("100000");
        var cacPhieu=List.<Map<String,Object>>of(Map.of("id",3,"tien_giam",giam));
        when(service.tongTienGoc(anyList())).thenReturn(goc);
        when(service.tinhTong(anyList())).thenReturn(goc);
        when(service.layPhieuKhaDung(goc,false)).thenReturn(cacPhieu);
        when(service.chonPhieuTotNhat(cacPhieu)).thenReturn(3);
        var mvc=MockMvcBuilders.standaloneSetup(new SanPhamOnlineController(service,mock(DiaChiKhachHangService.class))).build();
        mvc.perform(get("/thanh-toan")).andExpect(model().attribute("idPhieuDaChon",3))
                .andExpect(model().attribute("tongTien",new BigDecimal("900000")));
        mvc.perform(get("/thanh-toan").flashAttr("idPhieuDaChon",""))
                .andExpect(model().attribute("idPhieuDaChon",""))
                .andExpect(model().attribute("tongTien",goc));
    }

    @Test void sanPhamDangGiamChiHienFreeshipVaKhongTruTienHang() {
        var jdbc=mock(JdbcTemplate.class);var service=new SanPhamOnlineService(jdbc);
        var thuong=new HashMap<String,Object>(Map.of("id",1,"ten_phieu_giam_gia","Giảm 20%","loai_phieu_giam_gia",false,"gia_tri_giam_gia",new BigDecimal("20")));
        var ship=new HashMap<String,Object>(Map.of("id",2,"ten_phieu_giam_gia","Freeship nội thành","loai_phieu_giam_gia",true,"gia_tri_giam_gia",new BigDecimal("30000")));
        when(jdbc.queryForList(contains("FROM phieu_giam_gia"),eq(new BigDecimal("1000000")))).thenReturn(List.of(thuong,ship));
        var ds=service.layPhieuKhaDung(new BigDecimal("1000000"),true);
        assertEquals(1,ds.size());assertEquals(2,ds.get(0).get("id"));assertEquals(BigDecimal.ZERO,ds.get(0).get("tien_giam"));
        assertEquals(2,service.layPhieuKhaDung(new BigDecimal("1000000"),false).size());
    }

    @Test void guiTrucTiepPhieuThuongVanBiChanKhiCoDotGiam() {
        var jdbc=mock(JdbcTemplate.class);var service=spy(new SanPhamOnlineService(jdbc));
        var dong=new HashMap<String,Object>();dong.put("id",9);dong.put("so_luong",3);dong.put("so_luong_mua",1);
        dong.put("gia_ban",new BigDecimal("1000000"));dong.put("thanh_tien_goc",new BigDecimal("1000000"));dong.put("thanh_tien",new BigDecimal("900000"));
        doReturn(List.of(dong)).when(service).layGioHang(anyMap());
        when(jdbc.update(contains("UPDATE chi_tiet_san_pham"),any(),any(),any(),any())).thenReturn(1);
        when(jdbc.queryForList(contains("FROM phieu_giam_gia WITH"),eq(3),any()))
                .thenReturn(List.of(Map.of("ten_phieu_giam_gia","Giảm 50%","loai_phieu_giam_gia",false,"gia_tri_giam_gia",new BigDecimal("50"))));
        var loi=assertThrows(IllegalArgumentException.class,()->service.datHang(Map.of(9,1),null,"Long","0325327602","Hà Nội",false,"COD",3,new BigDecimal("500000")));
        assertTrue(loi.getMessage().contains("chỉ được dùng voucher freeship"));
        verify(jdbc,never()).update(contains("so_luong_su_dung=so_luong_su_dung-1"),anyInt());
    }

    @Test void phanTramCoTranVaTienMatKhongVuotTongDon() {
        var service=new SanPhamOnlineService(mock(JdbcTemplate.class));
        assertEquals(new BigDecimal("100000.00"),service.tinhGiamPhieu(Map.of("loai_phieu_giam_gia",false,
                "gia_tri_giam_gia",new BigDecimal("20"),"so_tien_giam_toi_da",new BigDecimal("100000")),new BigDecimal("1000000")));
        assertEquals(new BigDecimal("50000.00"),service.tinhGiamPhieu(Map.of("loai_phieu_giam_gia",true,
                "gia_tri_giam_gia",new BigDecimal("100000")),new BigDecimal("50000")));
    }

    @Test void guiPhieuVaTongDaXacNhanVaoLuongDatHang() throws Exception {
        var service=mock(SanPhamOnlineService.class);var session=new MockHttpSession();
        session.setAttribute("gioHangOnline",new LinkedHashMap<>(Map.of(9,1)));
        when(service.datHang(anyMap(),isNull(),anyString(),anyString(),anyString(),eq(false),eq("COD"),eq(3),eq(new BigDecimal("900000.00")))).thenReturn(20);
        MockMvcBuilders.standaloneSetup(new SanPhamOnlineController(service,mock(DiaChiKhachHangService.class))).build()
                .perform(post("/thanh-toan").session(session).param("ten","Long").param("sdt","0325327602").param("diaChi","Hà Nội")
                        .param("idPhieuGiamGia","3").param("tongTienDuKien","900000.00"))
                .andExpect(redirectedUrl("/gio-hang")).andExpect(flash().attributeExists("success"));
        verify(service).datHang(anyMap(),isNull(),eq("Long"),eq("0325327602"),eq("Hà Nội"),eq(false),eq("COD"),eq(3),eq(new BigDecimal("900000.00")));
    }

    @Test void phieuHetLuotKhongTaoHoaDon() {
        var jdbc=mock(JdbcTemplate.class);var service=spy(new SanPhamOnlineService(jdbc));
        var dong=new HashMap<String,Object>();dong.put("id",9);dong.put("so_luong",3);dong.put("so_luong_mua",1);
        dong.put("gia_ban",new BigDecimal("1000000"));dong.put("thanh_tien_goc",new BigDecimal("1000000"));dong.put("thanh_tien",new BigDecimal("1000000"));
        doReturn(List.of(dong)).when(service).layGioHang(anyMap());
        when(jdbc.update(contains("UPDATE chi_tiet_san_pham"),any(),any(),any(),any())).thenReturn(1);
        var loi=assertThrows(IllegalArgumentException.class,()->service.datHang(Map.of(9,1),null,"Long","0325327602","Hà Nội",false,"COD",3,new BigDecimal("900000")));
        assertTrue(loi.getMessage().contains("Phiếu"));
        verify(jdbc,never()).queryForObject(contains("INSERT INTO hoa_don"),eq(Integer.class),any(Object[].class));
    }

    @Test void huyDonHoanLaiLuotPhieu() {
        var jdbc=mock(JdbcTemplate.class);
        when(jdbc.queryForList(contains("JOIN quyen_han"),eq(2))).thenReturn(List.of(Map.of("id",2)));
        when(jdbc.queryForList(contains("WITH (UPDLOCK,HOLDLOCK)"),eq(10)))
                .thenReturn(List.of(Map.of("trang_thai_hien_tai",1,"id_phieu_giam_gia",3)));
        when(jdbc.update(contains("UPDATE hoa_don"),eq(2),eq(10))).thenReturn(1);
        new HoaDonService(jdbc).huyHoaDon(10,2);
        verify(jdbc).update(contains("so_luong_su_dung=so_luong_su_dung+1"),eq(3));
    }
}
