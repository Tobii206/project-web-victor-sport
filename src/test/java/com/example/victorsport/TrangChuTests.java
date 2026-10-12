package com.example.victorsport;

import com.example.victorsport.Controller.DangNhapController;
import com.example.victorsport.Controller.TrangChuController;
import com.example.victorsport.Service.DangNhapService;
import com.example.victorsport.Service.TrangChuService;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class TrangChuTests {
    @Test
    void locKetHopGiuLaiSizeMauVaCacDieuKienDaChon() throws Exception {
        TrangChuService service = mock(TrangChuService.class);
        taoTrang(service).perform(get("/").param("thuongHieu", "2").param("nhuCau", "6")
                        .param("chatLieu", "1").param("xuatXu", "2").param("kichThuoc", "2")
                        .param("mauSac", "2").param("conHang", "true").param("sapXep", "gia-tang"))
                .andExpect(status().isOk()).andExpect(model().attribute("kichThuocDangChon", 2))
                .andExpect(model().attribute("mauSacDangChon", 2)).andExpect(model().attribute("conHang", true));
        verify(service).timSanPham("", 2, 6, 1, 2, 2, 2, true, "gia-tang");
    }

    @Test
    void trangChuVaDangNhapNhanVienCoDuongDanRieng() throws Exception {
        TrangChuService service = mock(TrangChuService.class);
        when(service.layThuongHieu()).thenReturn(List.of(Map.of("id", 1, "ten_thuong_hieu", "Nike")));
        when(service.timSanPham("Nike", 1, null, null, null, null, null, false, "gia-tang")).thenReturn(List.of(Map.of("ten_san_pham", "Giày Nike")));
        MockMvc mvc = taoTrang(service);

        mvc.perform(get("/").param("tuKhoa", "Nike").param("thuongHieu", "1").param("sapXep", "gia-tang"))
                .andExpect(status().isOk()).andExpect(view().name("KhachHang/trang-chu"))
                .andExpect(model().attributeExists("sanPham", "thuongHieu"));
        verify(service).timSanPham("Nike", 1, null, null, null, null, null, false, "gia-tang");
        mvc.perform(get("/login")).andExpect(status().isOk()).andExpect(view().name("KhachHang/login"));
    }

    @Test
    void loiDatabaseVanHienThiTrangChuVaThongBao() throws Exception {
        TrangChuService service = mock(TrangChuService.class);
        when(service.layThuongHieu()).thenThrow(new DataAccessResourceFailureException("Không kết nối được"));
        taoTrang(service).perform(get("/"))
                .andExpect(status().isOk()).andExpect(view().name("KhachHang/trang-chu"))
                .andExpect(model().attributeExists("loiDuLieu"))
                .andExpect(model().attribute("sanPham", List.of()));
        verify(service, never()).timSanPham(anyString(), nullable(Integer.class), nullable(Integer.class), nullable(Integer.class), nullable(Integer.class), nullable(Integer.class), nullable(Integer.class), anyBoolean(), anyString());
    }

    private MockMvc taoTrang(TrangChuService service) {
        InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
        viewResolver.setPrefix("/WEB-INF/views/");
        viewResolver.setSuffix(".jsp");
        return MockMvcBuilders.standaloneSetup(new TrangChuController(service),
                new DangNhapController(mock(DangNhapService.class), mock(com.example.victorsport.Service.TaiKhoanService.class)))
                .setViewResolvers(viewResolver).build();
    }
}
