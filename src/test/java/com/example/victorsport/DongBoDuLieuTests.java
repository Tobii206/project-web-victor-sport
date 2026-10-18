package com.example.victorsport;

import com.example.victorsport.Config.DongBoDuLieuConfig;
import com.example.victorsport.Service.ChuongTrucTuyenService;
import org.junit.jupiter.api.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.*;
import org.springframework.web.servlet.config.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.test.web.servlet.*;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class DongBoDuLieuTests {
    @Controller static class XuLy {
        @PostMapping("/quan-ly/san-pham/1/luu") String luu() {return "redirect:/quan-ly/san-pham";}
        @PostMapping("/quan-ly/san-pham/1/loi") String loi(RedirectAttributes f) {f.addFlashAttribute("error","Thông tin sai");return "redirect:/quan-ly/san-pham";}
        @GetMapping("/quan-ly/san-pham") @ResponseBody String xem() {return "Danh sách";}
        @PostMapping("/login") @ResponseBody String dangNhap() {return "ok";}
    }
    ChuongTrucTuyenService live;MockMvc mvc;
    @BeforeEach void tao() {
        live=mock(ChuongTrucTuyenService.class);
        HandlerInterceptor[] interceptor=new HandlerInterceptor[1];
        var registry=new InterceptorRegistry() {
            @Override public InterceptorRegistration addInterceptor(HandlerInterceptor i) {interceptor[0]=i;return super.addInterceptor(i);}
        };
        new DongBoDuLieuConfig(live).addInterceptors(registry);
        mvc=MockMvcBuilders.standaloneSetup(new XuLy()).addInterceptors(interceptor[0]).build();
    }
    @Test void luuThanhCongMoiBaoCacManHinhLienQuan() throws Exception {
        mvc.perform(post("/quan-ly/san-pham/1/luu")).andExpect(status().is3xxRedirection());
        verify(live).duLieuThayDoi("san-pham,ton-kho");
    }
    @Test void loiNhapLieuVaChiXemKhongPhatSuKien() throws Exception {
        mvc.perform(post("/quan-ly/san-pham/1/loi")).andExpect(status().is3xxRedirection());
        mvc.perform(get("/quan-ly/san-pham")).andExpect(status().isOk());
        mvc.perform(post("/login")).andExpect(status().isOk());
        verifyNoInteractions(live);
    }
}
