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
        }
    }
    @Test void chiThuPhanConThieuVaKhongThuLaiDonDaThanhToan() {
        for(String daThu:List.of("30000","130000")) {
            var jdbc=jdbc();var service=new HoaDonService(jdbc);
            when(jdbc.update(contains("SET trang_thai_hien_tai=5"),eq(10))).thenReturn(1);
            when(jdbc.queryForObject(contains("SUM(so_tien)"),eq(BigDecimal.class),eq(10))).thenReturn(new BigDecimal(daThu));
            service.nhanDonVangLai(10,true);
            if(daThu.equals("130000")) verify(jdbc,never()).update(contains("INSERT INTO giao_dich_thanh_toan"),any(Object[].class));
            else verify(jdbc).update(contains("INSERT INTO giao_dich_thanh_toan"),eq(10),eq(4),eq(new BigDecimal("100000")),isNull());
        }
    }
    @Test void donQrNhanHangKhongGhiNhanThemKhoanThu() {
        var jdbc=jdbc();var service=new HoaDonService(jdbc);
        when(jdbc.update(contains("SET trang_thai_hien_tai=5"),eq(10))).thenReturn(1);
        when(jdbc.queryForList(contains("SELECT TOP 1 pt.nha_cung_cap"),eq(10))).thenReturn(List.of(Map.of("nha_cung_cap","VietQR")));
        service.nhanDonVangLai(10,true);
        verify(jdbc,never()).update(contains("INSERT INTO giao_dich_thanh_toan"),any(Object[].class));
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
