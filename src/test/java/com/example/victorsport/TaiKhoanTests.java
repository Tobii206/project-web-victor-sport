package com.example.victorsport;

import com.example.victorsport.Controller.DangNhapController;
import com.example.victorsport.Controller.TaiKhoanController;
import com.example.victorsport.Dto.NguoiDungDangNhap;
import com.example.victorsport.Service.TaiKhoanService;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class TaiKhoanTests {
    @Test void emailVaTenTaiKhoanDungChungMatKhauVaBoDemKhoa() {
        var jdbc=mock(JdbcTemplate.class);var service=new TaiKhoanService(jdbc);
        Map<String,Object> kh=Map.of("id",1,"ten_tai_khoan","khach01","mat_khau",new BCryptPasswordEncoder().encode("matkhaumoi"));
        when(jdbc.queryForList(contains("LOWER(ten_tai_khoan)"),eq("khach01"))).thenReturn(List.of(kh));
        when(jdbc.queryForList(contains("LOWER(ten_tai_khoan)"),eq("khach@gmail.com"))).thenReturn(List.of());
        when(jdbc.queryForList(contains("LOWER(email)"),eq("khach@gmail.com"))).thenReturn(List.of(kh));
        assertEquals(1,service.dangNhapKhachHang(" KHACH@GMAIL.COM ","matkhaumoi"));
        assertEquals(1,service.dangNhapKhachHang("khach01","matkhaumoi"));
        for(int i=0;i<5;i++) {
            String taiKhoan=i%2==0?"khach01":"khach@gmail.com";
            assertThrows(IllegalArgumentException.class,()->service.dangNhapKhachHang(taiKhoan,"saipass"));
        }
        assertTrue(assertThrows(IllegalArgumentException.class,()->service.dangNhapKhachHang("khach@gmail.com","matkhaumoi")).getMessage().contains("khóa"));
        service.moKhoaSauDoiMatKhau("khach01");
        assertEquals(1,service.dangNhapKhachHang("khach@gmail.com","matkhaumoi"));
    }
    @Test void emailTrungPhaiDungTenTaiKhoan() {
        var jdbc=mock(JdbcTemplate.class);var service=new TaiKhoanService(jdbc);
        when(jdbc.queryForList(contains("LOWER(email)"),eq("trung@gmail.com")))
                .thenReturn(List.of(Map.of("id",1),Map.of("id",2)));
        assertTrue(assertThrows(IllegalArgumentException.class,()->service.dangNhapKhachHang("trung@gmail.com","123456")).getMessage().contains("nhiều khách hàng"));
    }
    @Test void doiMatKhauMoKhoaTaiKhoanDaSaiNamLan() {
        var jdbc=mock(JdbcTemplate.class);var service=new TaiKhoanService(jdbc);
        String matKhauMoi=new BCryptPasswordEncoder().encode("matkhaumoi");
        when(jdbc.queryForList(anyString(),eq("khach01"))).thenReturn(List.of(Map.of("id",1,"mat_khau",matKhauMoi)));
        for(int i=0;i<5;i++) assertThrows(IllegalArgumentException.class,()->service.dangNhapKhachHang("khach01","saipass"));
        assertTrue(assertThrows(IllegalArgumentException.class,()->service.dangNhapKhachHang("khach01","matkhaumoi")).getMessage().contains("khóa"));
        service.moKhoaSauDoiMatKhau(" KHACH01 ");
        assertEquals(1,service.dangNhapKhachHang("khach01","matkhaumoi"));
    }
    @Test
    void nhanVienVaQuanLyDangNhapTrenCungTrangVaGiuDungQuyen() throws Exception {
        com.example.victorsport.Service.DangNhapService nv = mock(com.example.victorsport.Service.DangNhapService.class);
        TaiKhoanService kh = mock(TaiKhoanService.class);
        NguoiDungDangNhap quanLy = new NguoiDungDangNhap(1, "ql@gmail.com", "Quản lý", "Quản trị viên"); quanLy.themChucNang("ORDER_WRITE");
        when(nv.laTaiKhoanNhanVien("ql@gmail.com")).thenReturn(true);
        when(nv.dangNhap("ql@gmail.com", "123456")).thenReturn(
                com.example.victorsport.Service.DangNhapService.KetQuaDangNhap.thanhCong(quanLy));
        MockHttpSession session = new MockHttpSession();
        String idCu = session.getId();
        MockMvcBuilders.standaloneSetup(new DangNhapController(nv, kh, mock(com.example.victorsport.Service.SanPhamOnlineService.class))).build()
                .perform(post("/login").session(session).param("taiKhoan", "ql@gmail.com").param("matKhau", "123456"))
                .andExpect(redirectedUrl("/"));
        assertSame(quanLy, session.getAttribute(DangNhapController.KHOA_NGUOI_DUNG_DANG_NHAP));
        assertNotEquals(idCu, session.getId());
        assertNull(session.getAttribute("khachHangOnlineDangNhap"));
        verifyNoInteractions(kh);
    }

    @Test
    void saiMatKhauNhanVienKhongChuyenSangDangNhapKhachHang() throws Exception {
        com.example.victorsport.Service.DangNhapService nv = mock(com.example.victorsport.Service.DangNhapService.class);
        TaiKhoanService kh = mock(TaiKhoanService.class);
        when(nv.laTaiKhoanNhanVien("nv@gmail.com")).thenReturn(true);
        when(nv.dangNhap("nv@gmail.com", "saipass")).thenReturn(
                com.example.victorsport.Service.DangNhapService.KetQuaDangNhap.thatBai("Tài khoản bị khóa"));
        MockHttpSession session = new MockHttpSession();
        MockMvcBuilders.standaloneSetup(new DangNhapController(nv, kh, mock(com.example.victorsport.Service.SanPhamOnlineService.class))).build()
                .perform(post("/login").session(session).param("taiKhoan", "nv@gmail.com").param("matKhau", "saipass"))
                .andExpect(redirectedUrl("/login")).andExpect(flash().attribute("error", "Tài khoản bị khóa"));
        assertNull(session.getAttribute(DangNhapController.KHOA_NGUOI_DUNG_DANG_NHAP));
        verifyNoInteractions(kh);
    }

    @Test
    void khachChuaDangNhapKhongDocHoSoTuDatabase() throws Exception {
        TaiKhoanService service = mock(TaiKhoanService.class);
        taoTrang(service).perform(get("/tai-khoan")).andExpect(redirectedUrl("/login?quayLai=tai-khoan"))
                .andExpect(header().string("Cache-Control", "no-store"))
                .andExpect(model().attributeDoesNotExist("thongTin"));
        verifyNoInteractions(service);
    }

    @Test
    void hoSoKhachHangLayIdTuPhienDangNhap() throws Exception {
        TaiKhoanService service = mock(TaiKhoanService.class);
        Map<String, Object> hoSo = Map.of("ho_ten", "Nguyễn An");
        when(service.layThongTinKhachHang(7)).thenReturn(hoSo);
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("khachHangOnlineDangNhap", 7);
        taoTrang(service).perform(get("/tai-khoan").session(session).param("id", "99"))
                .andExpect(model().attribute("thongTin", hoSo))
                .andExpect(model().attributeDoesNotExist("laNhanVien"));
        verify(service).layThongTinKhachHang(7);
        verify(service, never()).layThongTinKhachHang(99);
        assertNull(DangNhapController.layNguoiDungDangNhap(session, new org.springframework.ui.ExtendedModelMap()));
    }

    @Test
    void hoSoNhanVienDungPhienNhanVien() throws Exception {
        TaiKhoanService service = mock(TaiKhoanService.class);
        Map<String, Object> hoSo = Map.of("ho_ten", "Trần Bình");
        when(service.layThongTinNhanVien(2)).thenReturn(hoSo);
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(DangNhapController.KHOA_NGUOI_DUNG_DANG_NHAP,
                new NguoiDungDangNhap(2, "nv@gmail.com", "Trần Bình", "Nhân viên"));
        taoTrang(service).perform(get("/tai-khoan").session(session))
                .andExpect(model().attribute("thongTin", hoSo)).andExpect(model().attribute("laNhanVien", true));
        verify(service, never()).layThongTinKhachHang(anyInt());
    }

    @Test
    void dangNhapKhachHangVaDangXuat() throws Exception {
        TaiKhoanService service = mock(TaiKhoanService.class);
        when(service.dangNhapKhachHang("nguyenan", "123456")).thenReturn(7);
        MockHttpSession session = new MockHttpSession();
        String idCu = session.getId();
        MockMvc mvc = taoTrang(service);
        mvc.perform(post("/login").session(session)
                        .param("taiKhoan", "nguyenan").param("matKhau", "123456"))
                .andExpect(redirectedUrl("/"));
        assertEquals(7, session.getAttribute("khachHangOnlineDangNhap"));
        assertNotEquals(idCu, session.getId());
        assertNull(session.getAttribute(DangNhapController.KHOA_NGUOI_DUNG_DANG_NHAP));
        mvc.perform(post("/tai-khoan/dang-xuat").session(session)).andExpect(redirectedUrl("/login"));
        assertTrue(session.isInvalid());
    }

    @Test
    void matKhauBcryptVaDuLieuMauCuDeuDangNhapDuoc() {
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        when(jdbc.queryForList(anyString(), eq("nguyenan"))).thenReturn(List.of(
                Map.of("id", 7, "mat_khau", new BCryptPasswordEncoder().encode("123456"))));
        TaiKhoanService service = new TaiKhoanService(jdbc);
        assertEquals(7, service.dangNhapKhachHang(" NguyenAn ", "123456"));
        when(jdbc.queryForList(anyString(), eq("nguyenan"))).thenReturn(List.of(Map.of("id", 7, "mat_khau", "123456")));
        assertEquals(7, service.dangNhapKhachHang("nguyenan", "123456"));
    }

    @Test
    void saiNamLanThiKhoaDangNhap() {
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        when(jdbc.queryForList(anyString(), eq("nguyenan"))).thenReturn(List.of(Map.of("id", 7, "mat_khau", "123456")));
        TaiKhoanService service = new TaiKhoanService(jdbc);
        for (int lan = 1; lan <= 5; lan++) {
            assertThrows(IllegalArgumentException.class, () -> service.dangNhapKhachHang("nguyenan", "saipass"));
        }
        assertTrue(assertThrows(IllegalArgumentException.class,
                () -> service.dangNhapKhachHang("nguyenan", "123456")).getMessage().contains("bị khóa"));
        verify(jdbc, times(5)).queryForList(anyString(), eq("nguyenan"));
    }

    private MockMvc taoTrang(TaiKhoanService service) {
        InternalResourceViewResolver resolver = new InternalResourceViewResolver();
        resolver.setPrefix("/WEB-INF/views/");
        resolver.setSuffix(".jsp");
        return MockMvcBuilders.standaloneSetup(new TaiKhoanController(service),
                new DangNhapController(mock(com.example.victorsport.Service.DangNhapService.class), service, mock(com.example.victorsport.Service.SanPhamOnlineService.class)))
                .setViewResolvers(resolver).build();
    }
}
