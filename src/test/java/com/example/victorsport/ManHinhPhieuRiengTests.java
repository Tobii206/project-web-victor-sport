package com.example.victorsport;

import com.example.victorsport.Controller.*;
import com.example.victorsport.Dto.*;
import com.example.victorsport.Service.QuanLyPhieuGiamGiaService;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ManHinhPhieuRiengTests {
    MockHttpSession session(String role) {
        var s=new MockHttpSession();s.setAttribute(DangNhapController.KHOA_NGUOI_DUNG_DANG_NHAP,new NguoiDungDangNhap(2,"a@gmail.com","A",role));return s;
    }
    @Test void haiManHinhThemCoLoaiVaDuongDanLuuRieng() throws Exception {
        var service=mock(QuanLyPhieuGiamGiaService.class);var mvc=MockMvcBuilders.standaloneSetup(new QuanLyPhieuGiamGiaController(service)).build();
        mvc.perform(get("/quan-ly/phieu-giam-gia/them").session(session("Quản trị viên")))
                .andExpect(model().attribute("phieuFreeship",false)).andExpect(model().attribute("duongDanLuu","/quan-ly/phieu-giam-gia/luu"));
        mvc.perform(get("/quan-ly/phieu-giam-gia/them-freeship").session(session("Quản trị viên")))
                .andExpect(model().attribute("phieuFreeship",true)).andExpect(model().attribute("duongDanLuu","/quan-ly/phieu-giam-gia/luu-freeship"));
        verifyNoInteractions(service);
    }
    @Test void suaMoDungManHinhTheoLoaiDaLuu() throws Exception {
        var service=mock(QuanLyPhieuGiamGiaService.class);var mvc=MockMvcBuilders.standaloneSetup(new QuanLyPhieuGiamGiaController(service)).build();
        for(boolean ship:new boolean[]{false,true}) {
            when(service.lay(9)).thenReturn(Map.of("ap_dung_van_chuyen",ship));
            mvc.perform(get("/quan-ly/phieu-giam-gia/9").session(session("Quản trị viên")))
                    .andExpect(model().attribute("phieuFreeship",ship))
                    .andExpect(model().attribute("duongDanLuu","/quan-ly/phieu-giam-gia/9/"+(ship?"luu-freeship":"luu")));
        }
    }
    @Test void khongDoiLoaiBangCachSuaTruongAn() throws Exception {
        for(boolean ship:new boolean[]{false,true}) {
            var service=mock(QuanLyPhieuGiamGiaService.class);when(service.luu(isNull(),any())).thenReturn(9);
            var mvc=MockMvcBuilders.standaloneSetup(new QuanLyPhieuGiamGiaController(service)).build();
            mvc.perform(post("/quan-ly/phieu-giam-gia/"+(ship?"luu-freeship":"luu")).session(session("Quản trị viên"))
                    .param("ap_dung_van_chuyen",ship?"0":"1").param("chi_ap_dung_ha_noi","1")
                    .param("ten_phieu_giam_gia","Phiếu A").param("loai_phieu_giam_gia","1").param("gia_tri_giam_gia","30000")
                    .param("ngay_bat_dau","2026-11-01").param("ngay_ket_thuc","2026-12-31").param("so_luong_su_dung","10").param("trang_thai","1"))
                    .andExpect(redirectedUrl("/quan-ly/phieu-giam-gia/9"));
            var form=org.mockito.ArgumentCaptor.forClass(PhieuGiamGiaForm.class);verify(service).luu(isNull(),form.capture());
            assertEquals(ship?1:0,form.getValue().getAp_dung_van_chuyen());assertEquals(ship?1:0,form.getValue().getChi_ap_dung_ha_noi());
        }
    }
    @Test void guiSaiManHinhKhiSuaKhongLamDoiLoaiPhieu() throws Exception {
        var service=mock(QuanLyPhieuGiamGiaService.class);when(service.lay(9)).thenReturn(Map.of("ap_dung_van_chuyen",true));
        MockMvcBuilders.standaloneSetup(new QuanLyPhieuGiamGiaController(service)).build()
                .perform(post("/quan-ly/phieu-giam-gia/9/luu").session(session("Quản trị viên"))
                        .param("ten_phieu_giam_gia","Phiếu A").param("loai_phieu_giam_gia","1").param("gia_tri_giam_gia","30000")
                        .param("ngay_bat_dau","2026-11-01").param("ngay_ket_thuc","2026-12-31").param("so_luong_su_dung","10").param("trang_thai","1"))
                .andExpect(redirectedUrl("/quan-ly/phieu-giam-gia/9")).andExpect(flash().attributeExists("error"));
        verify(service,never()).luu(anyInt(),any());
    }
    @Test void formFreeshipLoiQuayLaiDungManHinhVaGiuDuLieu() throws Exception {
        var service=mock(QuanLyPhieuGiamGiaService.class);
        MockMvcBuilders.standaloneSetup(new QuanLyPhieuGiamGiaController(service)).build()
                .perform(post("/quan-ly/phieu-giam-gia/luu-freeship").session(session("Quản trị viên")).param("ten_phieu_giam_gia","Tên đã nhập"))
                .andExpect(redirectedUrl("/quan-ly/phieu-giam-gia/them-freeship")).andExpect(flash().attributeExists("error","uuDai"));
        verifyNoInteractions(service);
    }
    @Test void nhanVienKhongDuocTaoFreeship() throws Exception {
        var service=mock(QuanLyPhieuGiamGiaService.class);
        MockMvcBuilders.standaloneSetup(new QuanLyPhieuGiamGiaController(service)).build()
                .perform(get("/quan-ly/phieu-giam-gia/them-freeship").session(session("Nhân viên"))).andExpect(status().isForbidden());
        verifyNoInteractions(service);
    }
}
