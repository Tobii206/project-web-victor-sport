package com.example.victorsport;

import com.example.victorsport.Controller.*;
import com.example.victorsport.Dto.NguoiDungDangNhap;
import com.example.victorsport.Service.HoaDonService;
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

class NhanHangThanhToanCodTests {
    @Test void dongBoDonCuDaNhanKhongThuLapVaCoLichSu() {
        var jdbc=jdbc();var service=new HoaDonService(jdbc);
        when(jdbc.queryForObject(contains("SUM(so_tien)"),eq(BigDecimal.class),eq(10))).thenReturn(BigDecimal.ZERO,new BigDecimal("130000"));
        assertTrue(service.dongBoCodDonDaNhan(10));assertFalse(service.dongBoCodDonDaNhan(10));
        verify(jdbc).update(contains("INSERT INTO giao_dich_thanh_toan"),eq(10),eq(4),eq(new BigDecimal("130000")),isNull());
        verify(jdbc).update(contains("Đồng bộ thanh toán COD"),eq(10));
        verify(jdbc,never()).update(contains("SET trang_thai_hien_tai=5"),any(Object[].class));
    }
    @Test void khoiDongChiDongBoDonCodDaNhanConThieuTien() {
        var jdbc=mock(JdbcTemplate.class);var service=mock(HoaDonService.class);
        when(jdbc.queryForList(contains("SELECT hd.id FROM hoa_don"),eq(Integer.class))).thenReturn(List.of(10,11));
        when(service.dongBoCodDonDaNhan(10)).thenReturn(true);
        assertEquals(1,new com.example.victorsport.Config.ThanhToanCodDaNhanConfig(jdbc,service).dongBo());
        verify(jdbc).queryForList(argThat(sql->sql.contains("hd.trang_thai_hien_tai=5") && sql.contains("=N'COD'")
                && sql.contains("gd.trang_thai=N'thanh_cong'") && sql.contains("<hd.tong_tien_sau_giam")),eq(Integer.class));
        verify(service).dongBoCodDonDaNhan(10);verify(service).dongBoCodDonDaNhan(11);
    }
    JdbcTemplate jdbc() {
        var jdbc=mock(JdbcTemplate.class);
        when(jdbc.queryForList(contains("JOIN quyen_han"),eq(2))).thenReturn(List.of(Map.of("id",2)));
        when(jdbc.queryForList(contains("FROM khach_hang"),eq(7))).thenReturn(List.of(Map.of("id",7)));
        when(jdbc.queryForList(contains("WITH (UPDLOCK,HOLDLOCK)"),eq(10)))
                .thenReturn(List.of(Map.of("trang_thai_hien_tai",5,"tong_tien_sau_giam",new BigDecimal("130000"))));
        when(jdbc.queryForList(contains("SELECT TOP 1 pt.nha_cung_cap"),eq(10))).thenReturn(List.of(Map.of("nha_cung_cap","COD")));
        when(jdbc.queryForObject(contains("SUM(so_tien)"),eq(BigDecimal.class),eq(10))).thenReturn(BigDecimal.ZERO);
        when(jdbc.queryForList(contains("SELECT TOP 1 id FROM phuong_thuc_thanh_toan"))).thenReturn(List.of(Map.of("id",4)));
        return jdbc;
    }
    @Test void caBaLuongNhanHangGhiNhanCodDungMotLan() {
        for(int role:new int[]{0,1,2}) {
            var jdbc=jdbc();var service=new HoaDonService(jdbc);
            Integer admin=role==2?2:null;
            if(role==0) when(jdbc.update(contains("SET trang_thai_hien_tai=5"),eq(10),eq(7))).thenReturn(1,0);
            else if(role==1) when(jdbc.update(contains("SET trang_thai_hien_tai=5"),eq(10))).thenReturn(1,0);
            else when(jdbc.update(contains("SET trang_thai_hien_tai=5"),eq(2),eq(10))).thenReturn(1,0);
            Runnable nhan=role==0?()->service.xacNhanDaNhanHang(10,7):role==1?()->service.nhanDonVangLai(10,true):()->service.xacNhanGiaoKhachVangLai(10,2);
            nhan.run();assertThrows(IllegalArgumentException.class,nhan::run);
            verify(jdbc).update(contains("INSERT INTO giao_dich_thanh_toan"),eq(10),eq(4),eq(new BigDecimal("130000")),eq(admin));
            verify(jdbc).update(contains("ngay_thanh_toan=SYSDATETIME()"),eq(admin),eq(10));
            if(role==0)verify(jdbc).update(contains("INSERT INTO lich_su_hoa_don"),eq(10),eq("Khách hàng xác nhận đã nhận hàng. Đã nhận tiền COD."),eq(7));
            else if(role==1)verify(jdbc).update(contains("INSERT INTO lich_su_hoa_don"),eq(10),eq("Khách vãng lai xác nhận đã nhận hàng. Đã nhận tiền COD."));
            else verify(jdbc).update(contains("INSERT INTO lich_su_hoa_don"),eq(10),eq(5),eq("Cửa hàng xác nhận khách đã nhận hàng. Đã nhận tiền COD."),eq(2),eq(2));
        }
    }
    @Test void chiThuPhanConThieuVaKhongThuLaiDonDaThanhToan() {
        for(String daThu:List.of("30000","130000")) {
            var jdbc=jdbc();var service=new HoaDonService(jdbc);
            when(jdbc.update(contains("SET trang_thai_hien_tai=5"),eq(10))).thenReturn(1);
            when(jdbc.queryForObject(contains("SUM(so_tien)"),eq(BigDecimal.class),eq(10))).thenReturn(new BigDecimal(daThu));
            service.nhanDonVangLai(10,true);
            if(daThu.equals("130000")) {verify(jdbc,never()).update(contains("INSERT INTO giao_dich_thanh_toan"),any(Object[].class));verify(jdbc).update(contains("INSERT INTO lich_su_hoa_don"),eq(10),eq("Khách vãng lai xác nhận đã nhận hàng."));}
            else verify(jdbc).update(contains("INSERT INTO giao_dich_thanh_toan"),eq(10),eq(4),eq(new BigDecimal("100000")),isNull());
        }
    }
    @Test void donQrNhanHangKhongGhiNhanThemKhoanThu() {
        var jdbc=jdbc();var service=new HoaDonService(jdbc);
        when(jdbc.update(contains("SET trang_thai_hien_tai=5"),eq(10))).thenReturn(1);
        when(jdbc.queryForList(contains("SELECT TOP 1 pt.nha_cung_cap"),eq(10))).thenReturn(List.of(Map.of("nha_cung_cap","VietQR")));
        service.nhanDonVangLai(10,true);
        verify(jdbc,never()).update(contains("INSERT INTO giao_dich_thanh_toan"),any(Object[].class));
        verify(jdbc).update(contains("INSERT INTO lich_su_hoa_don"),eq(10),eq("Khách vãng lai xác nhận đã nhận hàng."));
    }
    @Test void lichSuCuHienTheoKhoanThuCodThucTe() {
        for(boolean cod:List.of(false,true)) {
            var j=mock(JdbcTemplate.class);
            when(j.queryForList(contains("SELECT ls.trang_thai,ls.thoi_gian,ls.ghi_chu"),eq(10))).thenReturn(List.of(new HashMap<>(Map.of("ghi_chu","Khách hàng xác nhận đã nhận hàng; nếu thanh toán COD, đã trả đủ tiền COD."))));
            when(j.queryForList(contains("SELECT TOP 1 gd.id"),eq(10))).thenReturn(cod?List.of(Map.of("id",1)):List.of());
            assertEquals("Khách hàng xác nhận đã nhận hàng."+(cod?" Đã nhận tiền COD.":""),new HoaDonService(j).layLichSu(10).get(0).get("ghi_chu"));
            verify(j,never()).update(anyString(),any(Object[].class));
        }
    }
    @Test void cacDuongDanThuCodRiengKhongConHoatDong() throws Exception {
        var service=mock(HoaDonService.class);
        var mvc=MockMvcBuilders.standaloneSetup(new HoaDonController(service),new VanChuyenController(null,null,service)).build();
        var session=new MockHttpSession();session.setAttribute(DangNhapController.KHOA_NGUOI_DUNG_DANG_NHAP,new NguoiDungDangNhap(2,"a@gmail.com","A","Quản trị viên"));
        mvc.perform(post("/quan-ly/hoa-don/10/thu-tien-cod").session(session)).andExpect(status().isNotFound());
        mvc.perform(post("/quan-ly/van-chuyen/10/thu-cod").session(session)).andExpect(status().isNotFound());
        verifyNoInteractions(service);
    }
}
