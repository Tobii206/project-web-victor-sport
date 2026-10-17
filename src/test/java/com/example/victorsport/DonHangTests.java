package com.example.victorsport;

import com.example.victorsport.Controller.*;
import com.example.victorsport.Dto.NguoiDungDangNhap;
import com.example.victorsport.Service.HoaDonService;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.jdbc.core.JdbcTemplate;
import java.util.*;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class DonHangTests {
    @Test void adminHuyHoaDonNhanVienKhongDuocHuy() throws Exception {
        var service=mock(HoaDonService.class);
        var mvc=MockMvcBuilders.standaloneSetup(new HoaDonController(service)).build();
        mvc.perform(post("/quan-ly/hoa-don/10/huy")).andExpect(redirectedUrl("/login"));
        mvc.perform(post("/quan-ly/hoa-don/10/huy").session(nhanVien("Nhân viên"))).andExpect(status().isForbidden());
        verifyNoInteractions(service);
        mvc.perform(post("/quan-ly/hoa-don/10/huy").session(nhanVien("Quản trị viên")).param("quayLai","danh-sach"))
                .andExpect(redirectedUrl("/quan-ly/hoa-don")).andExpect(flash().attributeExists("success"));
        verify(service).huyHoaDon(10,2);
    }

    @Test void adminKhongHuyDuocHoaDonDaXacNhan() {
        for(int trangThai:new int[]{0,2,3,4,5,6}) {
            var jdbc=mock(JdbcTemplate.class);
            when(jdbc.queryForList(contains("JOIN quyen_han"),eq(2))).thenReturn(List.of(Map.of("id",2)));
            when(jdbc.queryForList(contains("WITH (UPDLOCK,HOLDLOCK)"),eq(10)))
                    .thenReturn(List.of(Map.of("trang_thai_hien_tai",trangThai)));
            assertThrows(IllegalArgumentException.class,()->new HoaDonService(jdbc).huyHoaDon(10,2));
            verify(jdbc,never()).update(anyString(),any(Object[].class));
        }
    }

    @Test void adminHuyDonHoanKhoVaLuuNguoiHuy() {
        var jdbc=mock(JdbcTemplate.class);
        when(jdbc.queryForList(contains("JOIN quyen_han"),eq(2))).thenReturn(List.of(Map.of("id",2)));
        when(jdbc.queryForList(contains("WITH (UPDLOCK,HOLDLOCK)"),eq(10)))
                .thenReturn(List.of(Map.of("trang_thai_hien_tai",1)));
        when(jdbc.queryForList(contains("FROM hoa_don_chi_tiet"),eq(10)))
                .thenReturn(List.of(Map.of("id_chi_tiet_san_pham",9,"so_luong",2)));
        when(jdbc.update(contains("UPDATE chi_tiet_san_pham"),eq(2),eq(9))).thenReturn(1);
        when(jdbc.update(contains("UPDATE hoa_don"),eq(2),eq(10))).thenReturn(1);
        new HoaDonService(jdbc).huyHoaDon(10,2);
        verify(jdbc).update(contains("UPDATE chi_tiet_san_pham"),eq(2),eq(9));
        verify(jdbc).update(contains("UPDATE giao_dich_thanh_toan"),eq(10));
        verify(jdbc).update(contains("INSERT INTO lich_su_hoa_don"),eq(10),eq(2),eq(2));
    }

    @Test void huyDonChiDungKhachDangDangNhap() throws Exception {
        var service = mock(HoaDonService.class);
        var mvc = MockMvcBuilders.standaloneSetup(new DonHangController(service)).build();
        mvc.perform(post("/don-hang/10/huy")).andExpect(redirectedUrl("/login"));
        mvc.perform(post("/don-hang/10/huy").session(nhanVien("Quản trị viên"))).andExpect(redirectedUrl("/login"));
        verifyNoInteractions(service);
        mvc.perform(post("/don-hang/10/huy").session(khach()).param("idKhachHang","999").param("quayLai","danh-sach"))
                .andExpect(redirectedUrl("/don-hang")).andExpect(flash().attribute("success","Đã hủy đơn hàng."));
        verify(service).huyDonCuaKhach(10,7);
    }

    @Test void huyDonLoiKhongBaoThanhCong() throws Exception {
        var service = mock(HoaDonService.class);
        doThrow(new IllegalArgumentException("Chỉ được hủy đơn đang chờ xác nhận.")).when(service).huyDonCuaKhach(10,7);
        MockMvcBuilders.standaloneSetup(new DonHangController(service)).build()
                .perform(post("/don-hang/10/huy").session(khach()))
                .andExpect(redirectedUrl("/don-hang/10")).andExpect(flash().attributeCount(1))
                .andExpect(flash().attribute("error","Chỉ được hủy đơn đang chờ xác nhận."));
    }

    @Test void donKhongThuocKhachKhongDuocHuy() {
        var jdbc = mock(JdbcTemplate.class);
        when(jdbc.queryForList(contains("FROM khach_hang"),eq(7))).thenReturn(List.of(Map.of("id",7)));
        assertThrows(IllegalArgumentException.class,()->new HoaDonService(jdbc).huyDonCuaKhach(10,7));
        verify(jdbc,never()).update(anyString(),any(Object[].class));
    }

    @Test void chiTrangThaiChoXacNhanMoiDuocHuy() {
        for (int trangThai : new int[]{0,2,3,4,5,6}) {
            var jdbc = mock(JdbcTemplate.class);
            when(jdbc.queryForList(contains("FROM khach_hang"),eq(7))).thenReturn(List.of(Map.of("id",7)));
            when(jdbc.queryForList(contains("WITH (UPDLOCK,HOLDLOCK)"),eq(10),eq(7)))
                    .thenReturn(List.of(Map.of("trang_thai_hien_tai",trangThai)));
            assertThrows(IllegalArgumentException.class,()->new HoaDonService(jdbc).huyDonCuaKhach(10,7));
            verify(jdbc,never()).update(anyString(),any(Object[].class));
        }
    }

    @Test void donDaCoTienThanhToanKhongTuHuy() {
        var jdbc = mock(JdbcTemplate.class);
        when(jdbc.queryForList(contains("FROM khach_hang"),eq(7))).thenReturn(List.of(Map.of("id",7)));
        when(jdbc.queryForList(contains("WITH (UPDLOCK,HOLDLOCK)"),eq(10),eq(7)))
                .thenReturn(List.of(Map.of("trang_thai_hien_tai",1)));
        when(jdbc.queryForList(contains("FROM giao_dich_thanh_toan"),eq(10))).thenReturn(List.of(Map.of("id",1)));
        assertThrows(IllegalArgumentException.class,()->new HoaDonService(jdbc).huyDonCuaKhach(10,7));
        verify(jdbc,never()).update(anyString(),any(Object[].class));
    }

    private MockHttpSession nhanVien(String quyen) {
        var s = new MockHttpSession();
        s.setAttribute(DangNhapController.KHOA_NGUOI_DUNG_DANG_NHAP,new NguoiDungDangNhap(2,"admin@gmail.com","Quản lý",quyen));
        return s;
    }
    private MockHttpSession khach() {
        var s = new MockHttpSession();s.setAttribute("khachHangOnlineDangNhap",7);return s;
    }

    @Test void chiAdminDuocXacNhanThanhToan() throws Exception {
        var service = mock(HoaDonService.class);
        var mvc = MockMvcBuilders.standaloneSetup(new HoaDonController(service)).build();
        mvc.perform(post("/quan-ly/hoa-don/10/xac-nhan-thanh-toan")).andExpect(redirectedUrl("/login"));
        mvc.perform(post("/quan-ly/hoa-don/10/xac-nhan-thanh-toan").session(nhanVien("Nhân viên"))).andExpect(status().isForbidden());
        verifyNoInteractions(service);
        mvc.perform(post("/quan-ly/hoa-don/10/xac-nhan-thanh-toan").session(nhanVien("Quản trị viên")))
                .andExpect(redirectedUrl("/quan-ly/hoa-don/10")).andExpect(flash().attributeExists("success"));
        verify(service).xacNhanThanhToan(10,2);
    }

    @Test void khachChiDuocXacNhanBangIdTrongPhien() throws Exception {
        var service = mock(HoaDonService.class);
        var mvc = MockMvcBuilders.standaloneSetup(new DonHangController(service)).build();
        mvc.perform(post("/don-hang/10/da-nhan-hang")).andExpect(redirectedUrl("/login"));
        mvc.perform(post("/don-hang/10/da-nhan-hang").session(nhanVien("Quản trị viên"))).andExpect(redirectedUrl("/login"));
        verifyNoInteractions(service);
        mvc.perform(post("/don-hang/10/da-nhan-hang").session(khach()).param("idKhachHang","999"))
                .andExpect(redirectedUrl("/don-hang/10")).andExpect(flash().attributeExists("success"));
        verify(service).xacNhanDaNhanHang(10,7);
    }

    @Test void khongDuocXemSanPhamVaLichSuCuaDonKhac() throws Exception {
        var service = mock(HoaDonService.class);
        MockMvcBuilders.standaloneSetup(new DonHangController(service)).build()
                .perform(get("/don-hang/10").session(khach())).andExpect(status().isNotFound());
        verify(service).layDonCuaKhach(10,7);
        verify(service,never()).laySanPhamTrongHoaDon(anyInt());
        verify(service,never()).layLichSu(anyInt());
    }

    @Test void danhSachVaChiTietDocDungTaiKhoan() throws Exception {
        var service = mock(HoaDonService.class);var s = khach();
        when(service.layDonCuaKhach(10,7)).thenReturn(Map.of("id",10));
        var mvc = MockMvcBuilders.standaloneSetup(new DonHangController(service)).build();
        mvc.perform(get("/don-hang").session(s)).andExpect(view().name("KhachHang/don-hang"))
                .andExpect(header().string("Cache-Control","no-store"));
        mvc.perform(get("/don-hang/10").session(s)).andExpect(view().name("KhachHang/chi-tiet-don-hang"));
        verify(service).layDonCuaKhach(7);verify(service).layDonCuaKhach(10,7);
    }

    @Test void loiNhanHangHienThongBaoVaKhongBaoThanhCong() throws Exception {
        var service = mock(HoaDonService.class);
        doThrow(new IllegalArgumentException("Đơn không đang vận chuyển.")).when(service).xacNhanDaNhanHang(10,7);
        MockMvcBuilders.standaloneSetup(new DonHangController(service)).build()
                .perform(post("/don-hang/10/da-nhan-hang").session(khach()).param("quayLai","danh-sach"))
                .andExpect(redirectedUrl("/don-hang")).andExpect(flash().attribute("error","Đơn không đang vận chuyển."))
                .andExpect(flash().attributeCount(1));
    }

    @Test void donDaNhanHoacDangVanChuyenKhongDuocAdminXacNhanLai() {
        for (int trangThai : new int[]{4,5,6,0}) {
            var jdbc = mock(JdbcTemplate.class);
            when(jdbc.queryForList(contains("JOIN quyen_han"),eq(2))).thenReturn(List.of(Map.of("id",2)));
            when(jdbc.queryForList(contains("WITH (UPDLOCK,HOLDLOCK)"),eq(10)))
                    .thenReturn(List.of(Map.of("trang_thai_hien_tai",trangThai)));
            assertThrows(IllegalArgumentException.class,()->new HoaDonService(jdbc).xacNhanThanhToan(10,2));
            verify(jdbc,never()).update(anyString(),any(Object[].class));
        }
    }

    @Test void daThanhToanDuKhongTaoGiaoDichTrung() {
        var jdbc = mock(JdbcTemplate.class);
        when(jdbc.queryForList(contains("JOIN quyen_han"),eq(2))).thenReturn(List.of(Map.of("id",2)));
        when(jdbc.queryForList(contains("WITH (UPDLOCK,HOLDLOCK)"),eq(10)))
                .thenReturn(List.of(Map.of("trang_thai_hien_tai",1,"tong_tien_sau_giam",new BigDecimal("100000"))));
        when(jdbc.queryForList(contains("FROM hoa_don_chi_tiet"),eq(10))).thenReturn(List.of(Map.of("id",1)));
        when(jdbc.queryForObject(contains("SUM(so_tien)"),eq(BigDecimal.class),eq(10))).thenReturn(new BigDecimal("100000"));
        when(jdbc.update(contains("UPDATE hoa_don"),eq(2),eq(10))).thenReturn(1);
        new HoaDonService(jdbc).xacNhanThanhToan(10,2);
        verify(jdbc,never()).update(contains("INSERT INTO giao_dich_thanh_toan"),any(Object[].class));
        verify(jdbc).update(contains("INSERT INTO lich_su_hoa_don"),eq(10),eq(2),eq(2));
    }
}
