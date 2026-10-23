package com.example.victorsport;

import com.example.victorsport.Controller.*;
import com.example.victorsport.Dto.NguoiDungDangNhap;
import com.example.victorsport.Service.*;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.time.LocalDate;
import java.util.*;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ThongKeVanChuyenTests {
    MockHttpSession phien(String quyen) {
        var session=new MockHttpSession();
        session.setAttribute(DangNhapController.KHOA_NGUOI_DUNG_DANG_NHAP,new NguoiDungDangNhap(2,"test@gmail.com","Kiểm tra",quyen));
        return session;
    }
    @Test void trangVaApiChiChoQuanTriVien() throws Exception {
        var tk=mock(ThongKeService.class); var vc=mock(VanChuyenService.class); var ghn=mock(GhnService.class);
        var mvc=MockMvcBuilders.standaloneSetup(new ThongKeController(tk),new VanChuyenController(vc,ghn,mock(com.example.victorsport.Service.HoaDonService.class))).build();
        for(String path:List.of("/quan-ly/thong-ke","/quan-ly/van-chuyen")) {
            mvc.perform(get(path)).andExpect(redirectedUrl("/login"));
            mvc.perform(get(path).session(phien("Nhân viên"))).andExpect(status().isForbidden());
        }
        mvc.perform(get("/quan-ly/van-chuyen/ghn/dia-chi").session(phien("Nhân viên"))).andExpect(status().isForbidden());
        mvc.perform(post("/quan-ly/van-chuyen/ghn/tinh-phi").session(phien("Nhân viên"))).andExpect(status().isForbidden());
        verifyNoInteractions(tk,vc,ghn);
    }
    @Test void validatePhiTruocKhiGoiGhn() throws Exception {
        var ghn=mock(GhnService.class);
        var mvc=MockMvcBuilders.standaloneSetup(new VanChuyenController(mock(VanChuyenService.class),ghn,mock(com.example.victorsport.Service.HoaDonService.class))).build();
        mvc.perform(post("/quan-ly/van-chuyen/ghn/tinh-phi").session(phien("Quản trị viên"))
                .param("tinh","201").param("huyen","1444").param("xa","00123").param("khoiLuong","-1")
                .param("dai","35").param("rong","25").param("cao","15").param("giaTriHang","0"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.error").exists());
        verifyNoInteractions(ghn);
    }
    @Test void chuaCoTokenApiBaoLoiKhongTraPhiBangKhong() throws Exception {
        var ghn=mock(GhnService.class); when(ghn.tinh()).thenThrow(new IllegalStateException("Chưa cấu hình GHN thử nghiệm."));
        var mvc=MockMvcBuilders.standaloneSetup(new VanChuyenController(mock(VanChuyenService.class),ghn,mock(com.example.victorsport.Service.HoaDonService.class))).build();
        mvc.perform(get("/quan-ly/van-chuyen/ghn/dia-chi").session(phien("Quản trị viên")))
                .andExpect(status().isServiceUnavailable()).andExpect(jsonPath("$.error").exists())
                .andExpect(header().string("Cache-Control","no-store"));
    }
    @Test void locNgayVaKenhKhongHopLeKhongTruyVanDatabase() {
        var jdbc=mock(JdbcTemplate.class); var service=new ThongKeService(jdbc); var ngay=LocalDate.of(2026,10,18);
        assertThrows(IllegalArgumentException.class,()->service.thongKe(ngay,ngay.minusDays(1),"tat-ca"));
        assertThrows(IllegalArgumentException.class,()->service.thongKe(ngay.minusDays(366),ngay,"tat-ca"));
        assertThrows(IllegalArgumentException.class,()->service.thongKe(ngay,ngay,"loai-khong-hop-le"));
        verifyNoInteractions(jdbc);
    }
    @Test void bieuDoCoCaNgayKhongPhatSinhHoaDon() {
        var jdbc=mock(JdbcTemplate.class); var ngay=LocalDate.of(2026,10,18);
        when(jdbc.queryForMap(anyString(),any(Object[].class))).thenReturn(Map.of("doanh_thu",new BigDecimal("200000")));
        when(jdbc.queryForList(contains("GROUP BY CAST"),any(Object[].class)))
                .thenReturn(List.of(Map.of("ngay",java.sql.Date.valueOf(ngay),"doanh_thu",new BigDecimal("200000"),"so_don",1)));
        var ketQua=new ThongKeService(jdbc).thongKe(ngay.minusDays(1),ngay,"tat-ca");
        var bieuDo=(List<?>)ketQua.get("bieuDo"); assertEquals(2,bieuDo.size());
        assertEquals(BigDecimal.ZERO,((Map<?,?>)bieuDo.get(0)).get("doanh_thu"));
        assertEquals(new BigDecimal("100.00"),((Map<?,?>)bieuDo.get(1)).get("cao"));
    }
    @Test void khongChapNhanLocVanChuyenSai() {
        var jdbc=mock(JdbcTemplate.class); var service=new VanChuyenService(jdbc);
        assertThrows(IllegalArgumentException.class,()->service.danhSach("",9,1));
        assertThrows(IllegalArgumentException.class,()->service.danhSach("",null,0));
        assertThrows(IllegalArgumentException.class,()->service.danhSach("a".repeat(101),null,1));
        verifyNoInteractions(jdbc);
    }
}
