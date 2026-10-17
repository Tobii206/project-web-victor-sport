package com.example.victorsport;

import com.example.victorsport.Controller.*;
import com.example.victorsport.Dto.NguoiDungDangNhap;
import com.example.victorsport.Service.*;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class FormValidationTests {
    private MockHttpSession admin() {
        var session = new MockHttpSession();
        session.setAttribute(DangNhapController.KHOA_NGUOI_DUNG_DANG_NHAP,
                new NguoiDungDangNhap(2, "admin@gmail.com", "Quản lý", "Quản trị viên"));
        return session;
    }

    @Test void emailVaSoDienThoaiSaiKhongDuocLuuKhachHang() throws Exception {
        var service = mock(QuanLyKhachHangService.class);
        var mvc = MockMvcBuilders.standaloneSetup(new QuanLyKhachHangController(service, mock(TaiKhoanService.class))).build();
        for (String email : new String[]{"kh@yahoo.com", "kh@@gmail.com", "kh..ho@gmail.com"}) {
            mvc.perform(post("/quan-ly/khach-hang/1/luu").session(admin())
                    .param("ten_khach_hang", "Khách").param("ten_tai_khoan", "khach01")
                    .param("trang_thai", "1").param("email", email))
                    .andExpect(redirectedUrl("/quan-ly/khach-hang/1")).andExpect(flash().attributeExists("error"));
        }
        for (String sdt : new String[]{"032532760", "1325327602", "0abcdefghi", "03253276021"}) {
            mvc.perform(post("/quan-ly/khach-hang/1/luu").session(admin())
                    .param("ten_khach_hang", "Khách").param("ten_tai_khoan", "khach01")
                    .param("trang_thai", "1").param("so_dien_thoai", sdt))
                    .andExpect(flash().attributeExists("error"));
        }
        verifyNoInteractions(service);
    }

    @Test void suaKhachHangDuocBoTrongMatKhauVaLienHe() throws Exception {
        var service = mock(QuanLyKhachHangService.class);
        var mvc = MockMvcBuilders.standaloneSetup(new QuanLyKhachHangController(service, mock(TaiKhoanService.class))).build();
        mvc.perform(post("/quan-ly/khach-hang/1/luu").session(admin())
                .param("ten_khach_hang", "Khách").param("ten_tai_khoan", "khach01")
                .param("trang_thai", "1").param("email", "").param("so_dien_thoai", "")
                .param("mat_khau", "").param("ngay_sinh", ""))
                .andExpect(redirectedUrl("/quan-ly/khach-hang")).andExpect(flash().attributeExists("success"));
        verify(service).luu(eq(1), anyMap(), eq(2));
    }

    @Test void ngaySinhSaiHoacTuongLaiKhongDuocLuuNhanVien() throws Exception {
        var service = mock(QuanLyNhanVienService.class);
        var mvc = MockMvcBuilders.standaloneSetup(new QuanLyNhanVienController(service)).build();
        for (String ngay : new String[]{"2026-02-30", "ngay-sai", "2999-01-01"}) {
            mvc.perform(post("/quan-ly/nhan-vien/1/luu").session(admin())
                    .param("ten_nhan_vien", "Nhân viên").param("email", "nv@gmail.com")
                    .param("id_quyen_han", "2").param("trang_thai", "1").param("ngay_sinh", ngay))
                    .andExpect(redirectedUrl("/quan-ly/nhan-vien/1")).andExpect(flash().attributeExists("error"));
        }
        verifyNoInteractions(service);
    }

    @Test void matKhauNganHoacChiKhoangTrangKhongDuocLuu() throws Exception {
        var service = mock(QuanLyNhanVienService.class);
        var mvc = MockMvcBuilders.standaloneSetup(new QuanLyNhanVienController(service)).build();
        for (String matKhau : new String[]{"12345", "      "}) {
            mvc.perform(post("/quan-ly/nhan-vien/1/luu").session(admin())
                    .param("ten_nhan_vien", "Nhân viên").param("email", "nv@gmail.com")
                    .param("id_quyen_han", "2").param("trang_thai", "1").param("mat_khau", matKhau))
                    .andExpect(flash().attributeExists("error"));
        }
        verifyNoInteractions(service);
    }

    @Test void tenSanPhamRongKhongDuocLuu() throws Exception {
        var service = mock(QuanLySanPhamService.class);
        var mvc = MockMvcBuilders.standaloneSetup(new QuanLySanPhamController(service)).build();
        mvc.perform(post("/quan-ly/san-pham/luu").session(admin()).param("ten_san_pham", "   ")
                .param("id_thuong_hieu", "1").param("trang_thai_kinh_doanh", "1"))
                .andExpect(redirectedUrl("/quan-ly/san-pham/them"))
                .andExpect(flash().attribute("error", "Vui lòng nhập tên sản phẩm."));
        verifyNoInteractions(service);
    }

    @Test void giaVaTonKhoSaiKhongDuocLuuMau() throws Exception {
        var service = mock(QuanLySanPhamService.class);
        var mvc = MockMvcBuilders.standaloneSetup(new QuanLySanPhamController(service)).build();
        String[][] duLieuSai = {{"-1", "1"}, {"10.123", "1"}, {"10000000000000000", "1"},
                {"abc", "1"}, {"100", "-1"}, {"100", "1.5"}, {"100", "2147483648"}};
        for (String[] duLieu : duLieuSai) {
            mvc.perform(post("/quan-ly/san-pham/1/mau/luu").session(admin())
                    .param("id_mau_sac", "1").param("id_kich_thuoc", "1").param("id_form_chan", "1")
                    .param("trang_thai", "1").param("gia_ban", duLieu[0]).param("so_luong", duLieu[1]))
                    .andExpect(redirectedUrl("/quan-ly/san-pham/1#mau-giay"))
                    .andExpect(flash().attributeExists("error", "mauForm"));
        }
        verifyNoInteractions(service);
    }

    @Test void thongTinNguoiNhanSaiKhongDuocDatHang() throws Exception {
        var service = mock(SanPhamOnlineService.class);
        var mvc = MockMvcBuilders.standaloneSetup(new SanPhamOnlineController(service, mock(com.example.victorsport.Service.DiaChiKhachHangService.class))).build();
        mvc.perform(post("/thanh-toan").param("ten", "Khách").param("sdt", "123")
                .param("diaChi", "Cầu Giấy"))
                .andExpect(redirectedUrl("/thanh-toan?loai=gio"))
                .andExpect(flash().attribute("sdt", "123")).andExpect(flash().attributeExists("error"));
        mvc.perform(post("/thanh-toan").param("ten", " ").param("sdt", "0325327602")
                .param("diaChi", "Cầu Giấy")).andExpect(flash().attributeExists("error"));
        verifyNoInteractions(service);
    }

    @Test void themKhachTaiQuaySaiEmailKhongDuocGhi() throws Exception {
        var service = mock(BanHangTaiQuayService.class);
        var mvc = MockMvcBuilders.standaloneSetup(new BanHangTaiQuayController(service)).build();
        mvc.perform(post("/pos/khach-hang/them").session(admin())
                .param("tenKhachHangMoi", "Khách").param("emailMoi", "kh@yahoo.com"))
                .andExpect(redirectedUrl("/pos")).andExpect(flash().attributeExists("loiThemKhachHang"));
        verifyNoInteractions(service);
    }
}
