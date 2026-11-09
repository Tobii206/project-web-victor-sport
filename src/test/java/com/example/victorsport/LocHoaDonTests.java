package com.example.victorsport;

import com.example.victorsport.Controller.*;
import com.example.victorsport.Dto.NguoiDungDangNhap;
import com.example.victorsport.Service.HoaDonService;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.time.LocalDate;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class LocHoaDonTests {
    @Test void locKetHopDuDieuKienVaTinhTronNgayKetThuc() {
        var jdbc=mock(JdbcTemplate.class);var tu=LocalDate.of(2026,11,1);var den=LocalDate.of(2026,11,3);
        new HoaDonService(jdbc).layDanhSachHoaDon(" HD ",4,"online","chua-thanh-toan",tu,den);
        verify(jdbc).queryForList(argThat(sql->sql.contains("trang_thai_hien_tai=?") && sql.contains("AND loai_don=1")
                && sql.contains("ngay_tao>=?") && sql.contains("ngay_tao<?") && sql.contains("gd.id_hoa_don=hoa_don.id")
                && sql.contains("gd.trang_thai=N'thanh_cong'") && sql.contains(")<ISNULL(tong_tien_sau_giam,0)")
                && sql.contains("ORDER BY COALESCE(ngay_thanh_toan,ngay_tao) DESC,id DESC")),
                eq("%HD%"),eq("%HD%"),eq("%HD%"),eq(4),eq(tu.atStartOfDay()),eq(den.plusDays(1).atStartOfDay()));
    }
    @Test void locTaiQuayDaThanhToanDuKhongLayGiaoDichChoHoacDaHuy() {
        var jdbc=mock(JdbcTemplate.class);new HoaDonService(jdbc).layDanhSachHoaDon(null,null,"tai-quay","da-thanh-toan",null,null);
        verify(jdbc).queryForList(argThat(sql->sql.contains("loai_don IN (0,2)") && sql.contains(")>=ISNULL(tong_tien_sau_giam,0)")
                && sql.contains("gd.trang_thai=N'thanh_cong'") && sql.contains("ISNULL(gd.xoa_mem,0)=0")),eq("%%"),eq("%%"),eq("%%"));
    }
    @Test void timKiemCuVanGiuThuTuVaBoQuaLocKhiKhongChon() {
        var jdbc=mock(JdbcTemplate.class);new HoaDonService(jdbc).layDanhSachHoaDon("090");
        verify(jdbc).queryForList(argThat(sql->sql.contains("so_dien_thoai_khach_hang LIKE ?")
                && sql.contains("ORDER BY COALESCE(ngay_thanh_toan,ngay_tao) DESC,id DESC")
                && !sql.contains("trang_thai_hien_tai=?")),eq("%090%"),eq("%090%"),eq("%090%"));
    }
    @Test void chanNgayDaoNguocVaGiaTriLocKhongHopLe() {
        var jdbc=mock(JdbcTemplate.class);var service=new HoaDonService(jdbc);var ngay=LocalDate.of(2026,11,3);
        assertThrows(IllegalArgumentException.class,()->service.layDanhSachHoaDon("",null,"","",ngay,ngay.minusDays(1)));
        assertThrows(IllegalArgumentException.class,()->service.layDanhSachHoaDon("",9,"","",null,null));
        assertThrows(IllegalArgumentException.class,()->service.layDanhSachHoaDon("",null,"sai","",null,null));
        assertThrows(IllegalArgumentException.class,()->service.layDanhSachHoaDon("",null,"","sai",null,null));
        verifyNoInteractions(jdbc);
    }
    MockHttpSession session() {
        var s=new MockHttpSession();s.setAttribute(DangNhapController.KHOA_NGUOI_DUNG_DANG_NHAP,new NguoiDungDangNhap(2,"a@gmail.com","A","Quản trị viên"));return s;
    }
    @Test void controllerGiuLuaChonVaTruyenDungDieuKien() throws Exception {
        var service=mock(HoaDonService.class);var tu=LocalDate.of(2026,11,1);var den=LocalDate.of(2026,11,3);
        MockMvcBuilders.standaloneSetup(new HoaDonController(service)).build()
                .perform(get("/quan-ly/hoa-don").session(session()).param("tuKhoa","HD").param("trangThai","4").param("loaiDon","online")
                        .param("thanhToan","chua-thanh-toan").param("tuNgay",tu.toString()).param("denNgay",den.toString()))
                .andExpect(model().attribute("trangThai",4)).andExpect(model().attribute("loaiDon","online"))
                .andExpect(model().attribute("thanhToan","chua-thanh-toan")).andExpect(model().attribute("tuNgay",tu)).andExpect(model().attribute("denNgay",den));
        verify(service).layTrangHoaDon("HD",4,"online","chua-thanh-toan",tu,den,1);
    }
    @Test void boLocLoiBaoLoiVaKhongHienDanhSachSai() throws Exception {
        var service=mock(HoaDonService.class);
        when(service.layTrangHoaDon(isNull(),eq(9),eq(""),eq(""),isNull(),isNull(),eq(1))).thenThrow(new IllegalArgumentException("Trạng thái hóa đơn không hợp lệ."));
        MockMvcBuilders.standaloneSetup(new HoaDonController(service)).build()
                .perform(get("/quan-ly/hoa-don").session(session()).param("trangThai","9"))
                .andExpect(model().attributeExists("error")).andExpect(model().attribute("danhSachHoaDon",List.of()));
    }
    @Test void chuaDangNhapKhongDuocDungBoLoc() throws Exception {
        var service=mock(HoaDonService.class);
        MockMvcBuilders.standaloneSetup(new HoaDonController(service)).build()
                .perform(get("/quan-ly/hoa-don").param("trangThai","4")).andExpect(redirectedUrl("/login"));
        verifyNoInteractions(service);
    }
}
