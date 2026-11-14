package com.example.victorsport;

import com.example.victorsport.Controller.*;
import com.example.victorsport.Dto.NguoiDungDangNhap;
import com.example.victorsport.Service.*;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.math.BigDecimal;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class GioiTinhBienTheTests {
    private JdbcTemplate jdbc() {
        var j=mock(JdbcTemplate.class);
        when(j.queryForList(startsWith("SELECT id FROM san_pham WITH"),eq(8))).thenReturn(List.of(Map.of("id",8)));
        when(j.queryForObject(startsWith("SELECT COUNT(*) FROM"),eq(Integer.class),any(Object[].class))).thenReturn(1);
        when(j.queryForObject(startsWith("SELECT COUNT(*) FROM chi_tiet_san_pham"),eq(Integer.class),any(Object[].class))).thenReturn(0);
        when(j.queryForObject(startsWith("SELECT gia_ban"),eq(BigDecimal.class),eq(8))).thenReturn(BigDecimal.TEN);
        when(j.queryForObject(contains("INSERT INTO chi_tiet_san_pham"),eq(Integer.class),any(Object[].class))).thenReturn(11,12,13);
        return j;
    }
    private Map<String,String> data(String gender) {
        return Map.of("doi_tuong",gender,"id_mau_sac","1","id_kich_thuoc","2","id_form_chan","3","so_luong","5","trang_thai","1");
    }
    @Test void cungMauSizeFormKhacGioiTinhTaoBaTonKhoRieng() {
        var j=jdbc();var s=new QuanLySanPhamService(j);
        assertEquals(11,s.luuMau(8,null,data("NAM"),2));
        assertEquals(12,s.luuMau(8,null,data("NU"),2));
        assertEquals(13,s.luuMau(8,null,data("UNISEX"),2));
        for(String gender:List.of("NAM","NU","UNISEX"))
            verify(j).queryForObject(contains("AND doi_tuong=?"),eq(Integer.class),eq(8),eq(1),eq(2),eq(3),eq(gender),eq(0));
        verify(j,never()).update(contains("SET so_luong=so_luong+?"),any(Object[].class));
    }
    @Test void trungCaGioiTinhThiCongTonVaoDungMaMau() {
        var j=jdbc();var s=new QuanLySanPhamService(j);
        when(j.queryForObject(startsWith("SELECT COUNT(*) FROM chi_tiet_san_pham"),eq(Integer.class),eq(8),eq(1),eq(2),eq(3),eq("NU"),eq(0))).thenReturn(1);
        when(j.queryForObject(contains("SELECT id FROM chi_tiet_san_pham"),eq(Integer.class),eq(8),eq(1),eq(2),eq(3),eq("NU"))).thenReturn(22);
        when(j.update(contains("SET so_luong=so_luong+?"),eq(5),eq(2),eq(22),eq(5))).thenReturn(1);
        assertEquals(22,s.luuMau(8,null,data("NU"),2));
        verify(j).update(contains("SET so_luong=so_luong+?"),eq(5),eq(2),eq(22),eq(5));
        assertThrows(IllegalArgumentException.class,()->s.luuMau(8,null,data("KHAC"),2));
    }
    @Test void nhomBangNamNuUnisexKhongLamMatAnhChungCuaMau() {
        var rows=new ArrayList<Map<String,Object>>();int id=1;
        for(String gender:List.of("NAM","NU","UNISEX"))rows.add(Map.of("id",id++,"id_mau_sac",1,"ten_mau_sac","Đen","doi_tuong",gender));
        var group=QuanLySanPhamService.nhomTheoMau(rows,List.of(Map.of("id_mau_sac",1,"duong_dan_anh","/image/den.png"))).get(0);
        assertEquals(3,((List<?>)group.get("cacNhomGioiTinh")).size());
        assertEquals(3,((List<?>)group.get("cacMau")).size());assertNotNull(group.get("anh"));
    }
    @Test void onlineKhongChoGuiGioiTinhKhacMauDangMua() throws Exception {
        var s=mock(SanPhamOnlineService.class);
        when(s.layPhienBanTheoId(12)).thenReturn(Map.of("id_san_pham",8,"doi_tuong","NU","so_luong",5));
        var mvc=MockMvcBuilders.standaloneSetup(new SanPhamOnlineController(s,mock(DiaChiKhachHangService.class))).build();
        var session=new MockHttpSession();
        mvc.perform(post("/san-pham/8/chon").session(session).param("idPhienBan","12").param("soLuong","1").param("doiTuong","NAM"))
                .andExpect(redirectedUrl("/san-pham/8")).andExpect(flash().attributeExists("error"));
        assertNull(session.getAttribute("gioHangOnline"));
        mvc.perform(post("/san-pham/8/chon").session(session).param("idPhienBan","12").param("soLuong","1").param("doiTuong","NU"))
                .andExpect(flash().attributeExists("success"));
        assertEquals(Map.of(12,1),session.getAttribute("gioHangOnline"));
    }
    @Test void locTaiQuayGiuLuaChonKhiThemHangVaChoChonLaiTatCa() {
        var s=mock(BanHangTaiQuayService.class);var c=new BanHangTaiQuayController(s);
        var session=new MockHttpSession();session.setAttribute(DangNhapController.KHOA_NGUOI_DUNG_DANG_NHAP,new NguoiDungDangNhap(2,"nv@gmail.com","Nhân viên","Bán hàng"));
        for(String gender:List.of("NAM","NU","UNISEX")) {
            c.hienThiBanHangTaiQuay("Nike",null,gender,session,new ExtendedModelMap());
            c.hienThiBanHangTaiQuay("Nike",null,null,session,new ExtendedModelMap());
            verify(s,times(2)).timSanPham("Nike",gender);
        }
        c.hienThiBanHangTaiQuay("Nike",null,"",session,new ExtendedModelMap());verify(s).timSanPham("Nike");
    }
}
