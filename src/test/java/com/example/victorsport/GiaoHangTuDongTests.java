package com.example.victorsport;

import com.example.victorsport.Config.DongBoGhnConfig;
import com.example.victorsport.Controller.*;
import com.example.victorsport.Dto.NguoiDungDangNhap;
import com.example.victorsport.Service.*;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class GiaoHangTuDongTests {
    @Test void lichSuCuTachBanGiaoVaVanChuyenGiuNguyenThoiGian() {
        var jdbc=mock(JdbcTemplate.class);var luc=java.sql.Timestamp.valueOf("2026-11-02 17:43:00");
        var cu=Map.<String,Object>of("trang_thai",4,"thoi_gian",luc,"ghi_chu","Shipper đã lấy kiện hàng; đơn chuyển sang đang vận chuyển.");
        var moi=Map.<String,Object>of("trang_thai",4,"thoi_gian",luc,"ghi_chu","Đơn hàng đang vận chuyển.");
        when(jdbc.queryForList(contains("FROM lich_su_hoa_don"),eq(10))).thenReturn(List.of(cu));
        var result=new HoaDonService(jdbc).layLichSu(10);
        assertEquals(List.of("Đã gửi hàng cho shipper.","Đơn hàng đang vận chuyển."),result.stream().map(r->r.get("ghi_chu")).toList());
        assertTrue(result.stream().allMatch(r->luc.equals(r.get("thoi_gian"))));
        assertEquals("Shipper đã lấy kiện hàng; đơn chuyển sang đang vận chuyển.",cu.get("ghi_chu"));
        when(jdbc.queryForList(contains("FROM lich_su_hoa_don"),eq(10))).thenReturn(List.of(moi));
        assertEquals(1,new HoaDonService(jdbc).layLichSu(10).size());
        verify(jdbc,never()).update(anyString(),any(Object[].class));
    }
    @Test void quanLyVanChuyenChiDocPhiKhongGoiGhnHayDoiDon() throws Exception {
        var service=mock(VanChuyenService.class);var ghn=mock(GhnService.class);var hoaDon=mock(HoaDonService.class);
        var fees=Map.<String,Object>of("tongQuan",Map.of("so_don",1),"donHang",List.of(Map.of("phi_van_chuyen",15000,"phi_thuc_te",30000)),"trang",1,"soTrang",1);
        when(service.danhSach("HD00010",4,1)).thenReturn(fees);
        var session=new MockHttpSession();session.setAttribute(DangNhapController.KHOA_NGUOI_DUNG_DANG_NHAP,new NguoiDungDangNhap(2,"a@gmail.com","A","Quản trị viên"));
        MockMvcBuilders.standaloneSetup(new VanChuyenController(service,ghn,hoaDon)).build()
                .perform(get("/quan-ly/van-chuyen").session(session).param("tuKhoa","HD00010").param("trangThai","4"))
                .andExpect(view().name("admin/shipping/show")).andExpect(model().attribute("activeMenu","van-chuyen"))
                .andExpect(model().attribute("donHang",fees.get("donHang")));
        verify(service).danhSach("HD00010",4,1);verifyNoInteractions(ghn,hoaDon);
    }
    JdbcTemplate jdbc(int state) {
        var jdbc=mock(JdbcTemplate.class);
        when(jdbc.queryForList(contains("JOIN quyen_han"),eq(2))).thenReturn(List.of(Map.of("id",2)));
        when(jdbc.queryForList(contains("WITH (UPDLOCK,HOLDLOCK)"),eq(10))).thenReturn(List.of(Map.of("trang_thai_hien_tai",state)));
        when(jdbc.queryForList(contains("SELECT id_hoa_don FROM van_don_ghn"),eq(10))).thenReturn(List.of(Map.of("id_hoa_don",10)));
        when(jdbc.update(contains("SET trang_thai_hien_tai=4"),eq(2),eq(10))).thenReturn(1);
        return jdbc;
    }
    @Test void guiHangTuTaoVanDonTruocKhiChuyenTrangThai() {
        var jdbc=jdbc(3);var ghn=mock(GiaoHangGhnService.class);var service=new HoaDonService(jdbc);
        ReflectionTestUtils.setField(service,"giaoHangGhn",ghn);
        service.banGiaoShipper(10,2);
        var order=inOrder(ghn,jdbc);
        order.verify(ghn).taoVanDon(10);
        order.verify(jdbc).update(contains("SET trang_thai_hien_tai=4"),eq(2),eq(10));
        verify(jdbc,never()).update(contains("chi_tiet_san_pham"),any(Object[].class));
    }
    @Test void ghnLoiHoacSaiBuocKhongGhiNhanDaGui() {
        var jdbc=jdbc(3);var ghn=mock(GiaoHangGhnService.class);var service=new HoaDonService(jdbc);
        ReflectionTestUtils.setField(service,"giaoHangGhn",ghn);
        when(ghn.taoVanDon(10)).thenThrow(new IllegalStateException("Không kết nối GHN"));
        assertThrows(IllegalStateException.class,()->service.banGiaoShipper(10,2));
        verify(jdbc,never()).update(anyString(),any(Object[].class));
        when(jdbc.queryForList(contains("WITH (UPDLOCK,HOLDLOCK)"),eq(10))).thenReturn(List.of(Map.of("trang_thai_hien_tai",4)));
        assertThrows(IllegalArgumentException.class,()->service.banGiaoShipper(10,2));
        verify(ghn,times(1)).taoVanDon(10);
    }
    @Test void donCuKhongQuaGhnVanGuiDuoc() {
        var jdbc=jdbc(3);var ghn=mock(GiaoHangGhnService.class);var service=new HoaDonService(jdbc);
        ReflectionTestUtils.setField(service,"giaoHangGhn",ghn);
        when(jdbc.queryForList(contains("SELECT id_hoa_don FROM van_don_ghn"),eq(10))).thenReturn(List.of());
        service.banGiaoShipper(10,2);verifyNoInteractions(ghn);
    }
    @Test void khachVangLaiNhanDonQrKhongGhiNhanThemThanhToan() {
        var jdbc=jdbc(4);var service=new HoaDonService(jdbc);
        when(jdbc.queryForList(contains("WITH (UPDLOCK,HOLDLOCK)"),eq(10))).thenReturn(List.of(Map.of("trang_thai_hien_tai",5,"ghi_chu","QR")));
        when(jdbc.update(contains("SET trang_thai_hien_tai=5"),eq(10))).thenReturn(1,0);
        assertThrows(IllegalArgumentException.class,()->service.nhanDonVangLai(10,false));
        service.nhanDonVangLai(10,true);
        verify(jdbc).update(argThat(sql -> sql.contains("SET trang_thai_hien_tai=5") && sql.contains("trang_thai_hien_tai=4") && sql.contains("id_khach_hang IS NULL")),eq(10));
        assertThrows(IllegalArgumentException.class,()->service.nhanDonVangLai(10,true));
        verify(jdbc,never()).update(contains("giao_dich_thanh_toan"),any(Object[].class));
    }
    @Test void adminChiXacNhanHoKhachVangLai() throws Exception {
        var service=mock(HoaDonService.class);
        var mvc=MockMvcBuilders.standaloneSetup(new HoaDonController(service)).build();
        var staff=new MockHttpSession();staff.setAttribute(DangNhapController.KHOA_NGUOI_DUNG_DANG_NHAP,new NguoiDungDangNhap(2,"a@gmail.com","A","Nhân viên"));
        for(var action:List.of("gui-hang","giao-khach-vang-lai")) {
            mvc.perform(post("/quan-ly/hoa-don/10/"+action)).andExpect(redirectedUrl("/login"));
            mvc.perform(post("/quan-ly/hoa-don/10/"+action).session(staff)).andExpect(status().isForbidden());
        }
        verifyNoInteractions(service);
        staff.setAttribute(DangNhapController.KHOA_NGUOI_DUNG_DANG_NHAP,new NguoiDungDangNhap(2,"a@gmail.com","A","Quản trị viên"));
        mvc.perform(post("/quan-ly/hoa-don/10/gui-hang").session(staff)).andExpect(redirectedUrl("/quan-ly/hoa-don/10"));
        mvc.perform(post("/quan-ly/hoa-don/10/giao-thanh-cong").session(staff)).andExpect(status().isNotFound());
        mvc.perform(post("/quan-ly/hoa-don/10/giao-khach-vang-lai").session(staff)).andExpect(redirectedUrl("/quan-ly/hoa-don/10"));
        verify(service).banGiaoShipper(10,2);verify(service).xacNhanGiaoKhachVangLai(10,2);verifyNoMoreInteractions(service);
    }
    @Test void xacNhanHoDonQrVangLaiKhongThuThemTien() {
        var jdbc=jdbc(4);var service=new HoaDonService(jdbc);
        when(jdbc.queryForList(contains("WITH (UPDLOCK,HOLDLOCK)"),eq(10))).thenReturn(List.of(Map.of("trang_thai_hien_tai",5,"ghi_chu","QR")));
        when(jdbc.update(contains("SET trang_thai_hien_tai=5"),eq(2),eq(10))).thenReturn(1,0);
        service.xacNhanGiaoKhachVangLai(10,2);
        verify(jdbc).update(argThat(sql -> sql.contains("id_khach_hang IS NULL") && sql.contains("trang_thai_hien_tai=4") && sql.contains("loai_don IN (1,2)")),eq(2),eq(10));
        verify(jdbc).update(contains("INSERT INTO lich_su_hoa_don"),eq(10),eq(5),contains("Quản trị viên"),eq(2),eq(2));
        assertThrows(IllegalArgumentException.class,()->service.xacNhanGiaoKhachVangLai(10,2));
        verify(jdbc,never()).update(contains("giao_dich_thanh_toan"),any(Object[].class));
    }
    @Test void dongBoTuDongBoQuaKhiChuaCauHinhVaThuTiepDonKhacKhiLoi() {
        var jdbc=mock(JdbcTemplate.class);var ghn=mock(GhnService.class);var giaoHang=mock(GiaoHangGhnService.class);
        var task=new DongBoGhnConfig(jdbc,ghn,giaoHang);
        task.dongBo();verifyNoInteractions(jdbc,giaoHang);
        when(ghn.sanSang()).thenReturn(true);
        when(jdbc.queryForList(contains("SELECT TOP 20"))).thenReturn(List.of(Map.of("id_hoa_don",10),Map.of("id_hoa_don",11)));
        doThrow(new IllegalStateException()).when(giaoHang).dongBo(10);
        assertDoesNotThrow(task::dongBo);
        verify(giaoHang).dongBo(10);verify(giaoHang).dongBo(11);
        verify(jdbc,never()).update(anyString(),any(Object[].class));
    }
}
