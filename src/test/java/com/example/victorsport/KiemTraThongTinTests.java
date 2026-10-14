package com.example.victorsport;

import com.example.victorsport.Service.*;
import com.example.victorsport.Controller.DangNhapController;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class KiemTraThongTinTests {
    @Test void chiChapNhanEmailGmailDungDinhDang() {
        for(String email:new String[]{"khach01@gmail.com","ten.ho@gmail.com","TEN@GMAIL.COM","ten+shop@gmail.com"})
            assertTrue(KiemTraThongTin.laEmailGmail(email));
        for(String email:new String[]{"ten@yahoo.com","@gmail.com","ten@gmail.com.vn","ten@gmailcom","ten@@gmail.com","ten..ho@gmail.com",".ten@gmail.com","ten @gmail.com"})
            assertFalse(KiemTraThongTin.laEmailGmail(email));
    }
    @Test void matKhauItNhatSauKyTu() {
        assertThrows(IllegalArgumentException.class,()->KiemTraThongTin.kiemTraMatKhau("12345"));
        assertThrows(IllegalArgumentException.class,()->KiemTraThongTin.kiemTraMatKhau("      "));
        assertDoesNotThrow(()->KiemTraThongTin.kiemTraMatKhau("123456"));
    }
    @Test void thongTinSaiKhongDuocGhiDatabase() {
        var jdbc=mock(JdbcTemplate.class);var nv=new QuanLyNhanVienService(jdbc);var kh=new QuanLyKhachHangService(jdbc);var pos=new BanHangTaiQuayService(jdbc);
        Map<String,String> nhanVien=new HashMap<>(Map.of("ten_nhan_vien","Nhân viên","email","nv@gmail.com","mat_khau","123456","id_quyen_han","2","trang_thai","1"));
        Map<String,String> khachHang=new HashMap<>(Map.of("ten_khach_hang","Khách","ten_tai_khoan","khach01","email","kh@gmail.com","mat_khau","123456","trang_thai","1"));
        for(String sdt:new String[]{"032532760","03253276021","1325327602","0abcdefghi"}) {
            nhanVien.put("so_dien_thoai",sdt);khachHang.put("so_dien_thoai",sdt);
            assertThrows(IllegalArgumentException.class,()->nv.luu(null,nhanVien,1));
            assertThrows(IllegalArgumentException.class,()->kh.luu(null,khachHang,1));
            assertThrows(IllegalArgumentException.class,()->pos.themKhachHangMoi("Khách",sdt,"kh@gmail.com",1));
        }
        nhanVien.put("so_dien_thoai","0325327602");khachHang.put("so_dien_thoai","0325327602");
        nhanVien.put("email","nv@yahoo.com");khachHang.put("email","kh@yahoo.com");
        assertThrows(IllegalArgumentException.class,()->nv.luu(null,nhanVien,1));
        assertThrows(IllegalArgumentException.class,()->kh.luu(null,khachHang,1));
        assertThrows(IllegalArgumentException.class,()->pos.themKhachHangMoi("Khách","0325327602","kh@yahoo.com",1));
        verifyNoInteractions(jdbc);
    }
    @Test void dangNhapSaiDinhDangKhongTruyVanDatabase() throws Exception {
        var nv=mock(DangNhapService.class);var kh=mock(TaiKhoanService.class);var sp=mock(SanPhamOnlineService.class);
        var mvc=MockMvcBuilders.standaloneSetup(new DangNhapController(nv,kh,sp)).build();
        mvc.perform(post("/login").param("taiKhoan","khach01").param("matKhau","12345"))
                .andExpect(flash().attribute("error","Mật khẩu phải có ít nhất 6 ký tự."));
        mvc.perform(post("/login").param("taiKhoan","khach@yahoo.com").param("matKhau","123456"))
                .andExpect(flash().attributeExists("error"));
        verifyNoInteractions(nv,kh,sp);
    }
}
