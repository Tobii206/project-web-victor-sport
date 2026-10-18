package com.example.victorsport;

import com.example.victorsport.Controller.ChuongThongBaoController;
import com.example.victorsport.Dto.NguoiDungDangNhap;
import com.example.victorsport.Service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.util.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ChuongThongBaoTests {
    ChuongThongBaoService service;PhanQuyenService quyen;JdbcTemplate jdbc;MockMvc mvc;
    @BeforeEach void tao() {
        service=mock(ChuongThongBaoService.class);quyen=mock(PhanQuyenService.class);jdbc=mock(JdbcTemplate.class);
        mvc=MockMvcBuilders.standaloneSetup(new ChuongThongBaoController(service,quyen,jdbc)).build();
        when(service.danhSach(anyBoolean(),anyInt())).thenReturn(Map.of("danhSach",List.of(),"chuaDoc",0));
        when(jdbc.queryForList(anyString(),eq(7))).thenReturn(List.of(Map.of("id",7)));
    }
    MockHttpSession khach() { var s=new MockHttpSession();s.setAttribute("khachHangOnlineDangNhap",7);return s; }
    @Test void chuaDangNhapKhongDuocXem() throws Exception {
        mvc.perform(get("/thong-bao-don-hang").param("kenh","khach")).andExpect(status().isUnauthorized());
        mvc.perform(get("/thong-bao-don-hang/truc-tuyen").param("kenh","khach")).andExpect(status().isUnauthorized());
        verifyNoInteractions(service);
    }
    @Test void khongLayIdNguoiKhacTuTrinhDuyet() throws Exception {
        mvc.perform(get("/thong-bao-don-hang").session(khach()).param("kenh","khach").param("idKhachHang","99"))
            .andExpect(status().isOk()).andExpect(header().string("Cache-Control","no-store")).andExpect(jsonPath("$.csrf").isNotEmpty());
        verify(service).danhSach(false,7);
    }
    @Test void khachKhongDuocDocChuongAdmin() throws Exception {
        mvc.perform(get("/thong-bao-don-hang").session(khach()).param("kenh","admin")).andExpect(status().isUnauthorized());
        verifyNoInteractions(service);
    }
    @Test void thuHoiQuyenAdminCoHieuLucNgay() throws Exception {
        var s=new MockHttpSession();s.setAttribute("nguoiDungDangNhap",new NguoiDungDangNhap(3,"a","A","Quản trị viên"));
        when(quyen.layQuyen(3)).thenReturn(null);
        mvc.perform(get("/thong-bao-don-hang").session(s).param("kenh","admin")).andExpect(status().isForbidden());
        verifyNoInteractions(service);
    }
    @Test void haiLoaiTaiKhoanKhongDungCheoChuong() throws Exception {
        var s=khach();var admin=new NguoiDungDangNhap(3,"a","A","Quản trị viên");s.setAttribute("nguoiDungDangNhap",admin);
        when(quyen.layQuyen(3)).thenReturn(admin);
        mvc.perform(get("/thong-bao-don-hang").session(s).param("kenh","admin")).andExpect(status().isOk());
        mvc.perform(get("/thong-bao-don-hang").session(s).param("kenh","khach")).andExpect(status().isOk());
        verify(service).danhSach(true,3);verify(service).danhSach(false,7);
    }
    @Test void danhDauDocCanMaBaoVeVaDungChu() throws Exception {
        var s=khach();s.setAttribute("csrfChuong","ma-bao-ve");
        mvc.perform(post("/thong-bao-don-hang/da-doc").session(s).param("kenh","khach").param("id","20"))
            .andExpect(status().isForbidden());
        verifyNoInteractions(service);
        mvc.perform(post("/thong-bao-don-hang/da-doc").session(s).param("kenh","khach").param("id","20").param("tatCa","true").header("X-CSRF-Chuong","ma-bao-ve"))
            .andExpect(status().isNoContent());
        verify(service).danhDau(false,7,20,true);
    }
    @Test void taiKhoanNgungHoatDongKhongDuocDoc() throws Exception {
        when(jdbc.queryForList(anyString(),eq(7))).thenReturn(List.of());
        mvc.perform(get("/thong-bao-don-hang").session(khach()).param("kenh","khach")).andExpect(status().isForbidden());
        verifyNoInteractions(service);
    }
    @Test void kenhCongKhaiVaNhanVienKhongDocDuocDanhSachThongBao() throws Exception {
        for(String kenh:List.of("cong-khai","noi-bo")) {
            mvc.perform(get("/thong-bao-don-hang").param("kenh",kenh)).andExpect(status().isBadRequest());
        }
        verifyNoInteractions(service);
    }
    @Test void ketNoiSseTraDungSuKienVaKieuNoiDung() throws Exception {
        var live=new ChuongTrucTuyenService(jdbc);
        var controller=new ChuongThongBaoController(service,quyen,jdbc);
        org.springframework.test.util.ReflectionTestUtils.setField(controller,"trucTuyen",live);
        var m=MockMvcBuilders.standaloneSetup(controller).build();
        try {
            var result=m.perform(get("/thong-bao-don-hang/truc-tuyen").session(khach()).param("kenh","khach"))
                .andExpect(request().asyncStarted()).andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("text/event-stream")).andReturn();
            live.dong();
            m.perform(asyncDispatch(result)).andExpect(content().string(org.hamcrest.Matchers.containsString("event:cap-nhat")));
        }finally{live.dong();}
    }
}
