package com.example.victorsport;

import com.example.victorsport.Service.*;
import com.example.victorsport.Controller.*;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.test.util.ReflectionTestUtils;
import java.util.*;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class LuongThanhToanMoiTests {
    JdbcTemplate jdbcCod() {
        var jdbc=mock(JdbcTemplate.class);
        when(jdbc.queryForList(contains("JOIN quyen_han"),eq(2))).thenReturn(List.of(Map.of("id",2)));
        when(jdbc.queryForList(contains("WITH (UPDLOCK,HOLDLOCK)"),eq(10)))
            .thenReturn(List.of(Map.of("trang_thai_hien_tai",1,"tong_tien_sau_giam",new BigDecimal("130000"))));
        when(jdbc.queryForList(contains("hoa_don_chi_tiet"),eq(10))).thenReturn(List.of(Map.of("id",1)));
        return jdbc;
    }
    @Test void xacNhanDonCodKhongGhiNhanThuTien() {
        var jdbc=jdbcCod();new HoaDonService(jdbc).xacNhanDonCod(10,2);
        verify(jdbc).update(contains("trang_thai_hien_tai=2"),eq(2),eq(10));
        verify(jdbc,never()).update(contains("INSERT INTO giao_dich_thanh_toan"),any(Object[].class));
        verify(jdbc,never()).queryForObject(contains("SUM(so_tien)"),eq(BigDecimal.class),any(Object[].class));
    }
    @Test void trangThaiDonDiTuanTuDaXacNhanSangChuanBiVaBanGiao() {
        var jdbc=jdbcCod();var service=new HoaDonService(jdbc);
        when(jdbc.update(contains("SET trang_thai_hien_tai=3"),eq(2),eq(10))).thenReturn(1);
        when(jdbc.update(contains("SET trang_thai_hien_tai=4"),eq(2),eq(10))).thenReturn(1);
        service.batDauChuanBi(10,2);
        service.banGiaoShipper(10,2);
        verify(jdbc).update(argThat(sql -> sql.contains("SET trang_thai_hien_tai=3") && sql.contains("AND trang_thai_hien_tai=2")),eq(2),eq(10));
        verify(jdbc).update(argThat(sql -> sql.contains("SET trang_thai_hien_tai=4") && sql.contains("AND trang_thai_hien_tai=3")),eq(2),eq(10));
        verify(jdbc).update(contains("INSERT INTO lich_su_hoa_don"),eq(10),eq(3),eq("Cửa hàng bắt đầu chuẩn bị hàng."),eq(2),eq(2));
        verify(jdbc).update(contains("INSERT INTO lich_su_hoa_don"),eq(10),eq(4),eq("Shipper đã lấy kiện hàng; đơn chuyển sang đang vận chuyển."),eq(2),eq(2));
    }
    @Test void khongXacNhanTienCodKhiDonChuaGiao() {
        var jdbc=jdbcCod();var service=new HoaDonService(jdbc);
        assertThrows(IllegalArgumentException.class,()->service.xacNhanThanhToan(10,2));
        assertThrows(IllegalArgumentException.class,()->service.xacNhanThuTienCod(10,2));
        verify(jdbc,never()).update(contains("INSERT INTO giao_dich_thanh_toan"),any(Object[].class));
    }
    @Test void chuaChoGhiNhanThuCodTruocKhiKhachXacNhanDaNhan() {
        var jdbc=jdbcCod();var service=new HoaDonService(jdbc);
        when(jdbc.queryForList(contains("WITH (UPDLOCK,HOLDLOCK)"),eq(10)))
            .thenReturn(List.of(Map.of("trang_thai_hien_tai",4,"tong_tien_sau_giam",new BigDecimal("130000"))));
        assertThrows(IllegalArgumentException.class,()->service.xacNhanThuTienCod(10,2));
        verify(jdbc,never()).update(contains("INSERT INTO giao_dich_thanh_toan"),any(Object[].class));
    }
    @Test void daThuTienCodKhongThuLapVaKhongDoiTrangThaiDaNhan() {
        var jdbc=jdbcCod();
        when(jdbc.queryForList(contains("WITH (UPDLOCK,HOLDLOCK)"),eq(10)))
            .thenReturn(List.of(Map.of("trang_thai_hien_tai",5,"tong_tien_sau_giam",new BigDecimal("130000"))));
        when(jdbc.queryForObject(contains("SUM(so_tien)"),eq(BigDecimal.class),eq(10))).thenReturn(new BigDecimal("130000"));
        new HoaDonService(jdbc).xacNhanThuTienCod(10,2);
        verify(jdbc,never()).update(anyString(),any(Object[].class));
    }
    @Test void chonPhieuTotNhatTinhCaPhiShipVaKhongTruFreeShipVaoTienHang() {
        var service=new SanPhamOnlineService(mock(JdbcTemplate.class));
        ReflectionTestUtils.setField(service,"phiGiaoHang",new BigDecimal("30000"));
        var free=Map.<String,Object>of("id",2,"tien_giam",BigDecimal.ZERO,"giam_phi_van_chuyen",new BigDecimal("30000"));
        assertEquals(2,service.chonPhieuTotNhat(List.of(Map.of("id",1,"tien_giam",new BigDecimal("20000")),free)));
        assertEquals(BigDecimal.ZERO,free.get("tien_giam"));
    }
    @Test void khachVangLaiKhongDungPhienKhongXemHoacSuaDuocDon() throws Exception {
        var service=mock(HoaDonService.class);
        var mvc=MockMvcBuilders.standaloneSetup(new DonVangLaiController(service)).build();
        mvc.perform(get("/don-vang-lai/10")).andExpect(status().isNotFound());
        verifyNoInteractions(service);
        assertThrows(IllegalArgumentException.class,()->new HoaDonService(mock(JdbcTemplate.class)).huyDonVangLai(10,false));
        assertThrows(IllegalArgumentException.class,()->new HoaDonService(mock(JdbcTemplate.class)).chuyenQrSangCod(10,null,false));
    }
    @Test void doiQrSangCodKhongTinIdKhachGuiLen() throws Exception {
        var service=mock(HoaDonService.class);var s=new MockHttpSession();s.setAttribute("khachHangOnlineDangNhap",7);
        MockMvcBuilders.standaloneSetup(new DonVangLaiController(service)).build()
            .perform(post("/thanh-toan/qr/10/doi-cod").session(s).param("idKhachHang","99"))
            .andExpect(redirectedUrl("/thanh-toan/qr/10"));
        verify(service).chuyenQrSangCod(10,7,false);
    }
}
