package com.example.victorsport;

import com.example.victorsport.Controller.*;
import com.example.victorsport.Dto.*;
import com.example.victorsport.Service.*;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.math.BigDecimal;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class QuanLyUuDaiTests {
    private MockHttpSession session(String quyen) {
        var s = new MockHttpSession();
        s.setAttribute(DangNhapController.KHOA_NGUOI_DUNG_DANG_NHAP, new NguoiDungDangNhap(2,"admin@gmail.com","Quản lý",quyen));
        return s;
    }

    @Test void nhanVienVaKhachKhongDuocQuanLyUuDai() throws Exception {
        var phieu = mock(QuanLyPhieuGiamGiaService.class); var dot = mock(QuanLyDotGiamGiaService.class);
        var mvc = MockMvcBuilders.standaloneSetup(new QuanLyPhieuGiamGiaController(phieu),new QuanLyDotGiamGiaController(dot)).build();
        for (String duongDan : new String[]{"/quan-ly/phieu-giam-gia", "/quan-ly/dot-giam-gia"}) {
            mvc.perform(get(duongDan)).andExpect(redirectedUrl("/login"));
            mvc.perform(post(duongDan+"/luu").session(session("Nhân viên"))).andExpect(status().isForbidden());
            mvc.perform(post(duongDan+"/1/ngung").session(session("Nhân viên"))).andExpect(status().isForbidden());
        }
        verifyNoInteractions(phieu,dot);
    }

    @Test void duLieuSaiKhongDuocGhiVaGiuLaiForm() throws Exception {
        var phieu = mock(QuanLyPhieuGiamGiaService.class); var dot = mock(QuanLyDotGiamGiaService.class);
        var mvc = MockMvcBuilders.standaloneSetup(new QuanLyPhieuGiamGiaController(phieu),new QuanLyDotGiamGiaController(dot)).build();
        mvc.perform(post("/quan-ly/phieu-giam-gia/luu").session(session("Quản trị viên"))
                .param("ten_phieu_giam_gia","Phiếu mới").param("so_luong_su_dung","-1"))
                .andExpect(redirectedUrl("/quan-ly/phieu-giam-gia/them")).andExpect(flash().attributeExists("error","uuDai"));
        mvc.perform(post("/quan-ly/dot-giam-gia/1/mau/luu").session(session("Quản trị viên"))
                .param("id_chi_tiet_san_pham","abc")).andExpect(flash().attributeExists("error","mauForm"));
        verifyNoInteractions(phieu,dot);
    }

    @Test void adminLuuPhieuDungDuLieuVaMoChiTiet() throws Exception {
        var service = mock(QuanLyPhieuGiamGiaService.class);
        when(service.luu(isNull(),any(PhieuGiamGiaForm.class))).thenReturn(9);
        MockMvcBuilders.standaloneSetup(new QuanLyPhieuGiamGiaController(service)).build()
                .perform(post("/quan-ly/phieu-giam-gia/luu").session(session("Quản trị viên"))
                        .param("ten_phieu_giam_gia","Giảm 10%").param("loai_phieu_giam_gia","0")
                        .param("gia_tri_giam_gia","10").param("so_luong_su_dung","5").param("trang_thai","1")
                        .param("ngay_bat_dau","2026-10-14").param("ngay_ket_thuc","2026-10-20"))
                .andExpect(redirectedUrl("/quan-ly/phieu-giam-gia/9"));
        verify(service).luu(isNull(),any(PhieuGiamGiaForm.class));
    }

    @Test void phanTramVuot100VaNgayDaoNguocBiChan() {
        var jdbc = mock(JdbcTemplate.class);
        var f = new PhieuGiamGiaForm(); f.setLoai_phieu_giam_gia(0); f.setGia_tri_giam_gia(new BigDecimal("101"));
        f.setNgay_bat_dau(LocalDate.now()); f.setNgay_ket_thuc(LocalDate.now());
        assertThrows(IllegalArgumentException.class, () -> new QuanLyPhieuGiamGiaService(jdbc).luu(null,f));
        f.setGia_tri_giam_gia(BigDecimal.TEN); f.setNgay_ket_thuc(LocalDate.now().minusDays(1));
        assertThrows(IllegalArgumentException.class, () -> new QuanLyPhieuGiamGiaService(jdbc).luu(null,f));
        verifyNoInteractions(jdbc);
    }

    @Test void soLuotDaThayDoiKhongDuocGhiDe() {
        var jdbc = mock(JdbcTemplate.class); var f = new PhieuGiamGiaForm();
        f.setLoai_phieu_giam_gia(0); f.setGia_tri_giam_gia(BigDecimal.TEN);
        f.setNgay_bat_dau(LocalDate.now()); f.setNgay_ket_thuc(LocalDate.now()); f.setSo_luong_cu(5);
        assertThrows(IllegalArgumentException.class, () -> new QuanLyPhieuGiamGiaService(jdbc).luu(1,f));
    }

    @Test void boMauKhongThuocDotBiChan() {
        var jdbc = mock(JdbcTemplate.class);
        when(jdbc.queryForList(contains("WITH (UPDLOCK,HOLDLOCK)"),eq(1))).thenReturn(java.util.List.of(java.util.Map.of("id",1)));
        assertThrows(IllegalArgumentException.class, () -> new QuanLyDotGiamGiaService(jdbc).boMau(1,999,2));
        verify(jdbc).update(contains("id_dot_giam_gia=? AND id=?"),eq(2),eq(1),eq(999));
    }
}
