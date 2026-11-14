package com.example.victorsport;

import com.example.victorsport.Controller.*;
import com.example.victorsport.Dto.*;
import com.example.victorsport.Service.BanHangTaiQuayService;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DiaChiKhachQuenTaiQuayTests {
    private MockHttpSession session() {
        var s=new MockHttpSession();s.setAttribute(DangNhapController.KHOA_NGUOI_DUNG_DANG_NHAP,new NguoiDungDangNhap(2,"nv@example.com","Nhân viên","Bán hàng"));return s;
    }
    private BanHangTaiQuayService service() {
        var s=mock(BanHangTaiQuayService.class);
        when(s.layKhachHang(32)).thenReturn(new LuaChonKhachHang(32,"KH00032","Long","0912345678",null));
        when(s.layDiaChiGiaoHangMacDinh(32)).thenReturn(Map.of("thanh_pho","Hà Nội","phuong","Phường B","dia_chi_cu_the","12 Đường A"));
        return s;
    }
    private BaoGiaGhn quote(){return new BaoGiaGhn(Map.of(1,1),"Tên tự nhập","0911111111","5 Đường C, Phường D, Hà Nội","QR",BigDecimal.TEN,BigDecimal.ONE,1,2,"Hà Nội","Phường D",1000,30,20,15,Instant.now().plusSeconds(300));}
    @Test void chonKhachLayTenDienThoaiVaDiaChiMacDinh() {
        var s=service();var session=session();session.setAttribute("idKhachHangTaiQuay",32);var model=new ExtendedModelMap();
        new BanHangTaiQuayController(s).hienThiBanHangTaiQuay(null,null,null,session,model);
        var dc=(Map<?,?>)model.get("thongTinNhanHang");
        assertEquals("Long",dc.get("ten"));assertEquals("0912345678",dc.get("sdt"));assertEquals("12 Đường A",dc.get("diaChi"));
        assertEquals("Hà Nội",dc.get("tenTinh"));assertEquals("Phường B",dc.get("tenXa"));
    }
    @Test void chuaCoDiaChiVanDienTenVaSoDienThoai() {
        var s=service();when(s.layDiaChiGiaoHangMacDinh(32)).thenReturn(Map.of());var session=session();session.setAttribute("idKhachHangTaiQuay",32);var model=new ExtendedModelMap();
        new BanHangTaiQuayController(s).hienThiBanHangTaiQuay(null,null,null,session,model);
        var dc=(Map<?,?>)model.get("thongTinNhanHang");assertEquals("Long",dc.get("ten"));assertNull(dc.get("diaChi"));
    }
    @Test void doiKhachXoaBaoGiaVaChonGuestKhongGiuThongTinKhachCu() {
        var s=service();var session=session();session.setAttribute("idKhachHangTaiQuay",1);session.setAttribute("baoGiaGhnTaiQuay",quote());session.setAttribute("maBaoGiaTaiQuay","old");
        var c=new BanHangTaiQuayController(s);var flash=new RedirectAttributesModelMap();c.chonKhachHang(32,session,flash);
        assertNull(session.getAttribute("baoGiaGhnTaiQuay"));assertNull(session.getAttribute("maBaoGiaTaiQuay"));assertEquals(true,flash.getFlashAttributes().get("khachHangVuaDoi"));
        c.chonKhachHang(null,session,new RedirectAttributesModelMap());var model=new ExtendedModelMap();c.hienThiBanHangTaiQuay(null,null,null,session,model);
        assertTrue(((Map<?,?>)model.get("thongTinNhanHang")).isEmpty());
    }
    @Test void khachKhongHopLeKhongDoiLuaChonVaBaoGia() {
        var s=service();when(s.layKhachHang(99)).thenThrow(new IllegalArgumentException("Không tồn tại"));var session=session();session.setAttribute("idKhachHangTaiQuay",32);var q=quote();session.setAttribute("baoGiaGhnTaiQuay",q);
        new BanHangTaiQuayController(s).chonKhachHang(99,session,new RedirectAttributesModelMap());
        assertEquals(32,session.getAttribute("idKhachHangTaiQuay"));assertSame(q,session.getAttribute("baoGiaGhnTaiQuay"));
    }
    @Test void khongGhiDeDiaChiDaNhapKhiVanCungKhach() {
        var s=service();var session=session();session.setAttribute("idKhachHangTaiQuay",32);session.setAttribute("baoGiaGhnTaiQuay",quote());var model=new ExtendedModelMap();
        new BanHangTaiQuayController(s).hienThiBanHangTaiQuay(null,null,null,session,model);
        assertEquals("5 Đường C",((Map<?,?>)model.get("thongTinNhanHang")).get("diaChi"));
    }
    @Test void donChoKhachCuKhongGhiDeDiaChiKhachMoiKeCaFlashDaDuocDoc() {
        var s=service();var session=session();session.setAttribute("idKhachHangTaiQuay",32);session.setAttribute("idDonChoTaiQuay",10);
        when(s.layDonCho(10,2)).thenReturn(Map.of("loai_don",2,"id_khach_hang",1,"ten_khach_hang","Khách cũ","dia_chi_chi_tiet","Địa chỉ cũ"));
        var model=new ExtendedModelMap();new BanHangTaiQuayController(s).hienThiBanHangTaiQuay(null,null,null,session,model);
        assertEquals("12 Đường A",((Map<?,?>)model.get("thongTinNhanHang")).get("diaChi"));
    }
    @Test void truyVanChiLayDiaChiConHieuLucCuaDungKhachUuTienMacDinh() {
        var j=mock(JdbcTemplate.class);new BanHangTaiQuayService(j).layDiaChiGiaoHangMacDinh(32);
        verify(j).queryForList(argThat(sql->sql.contains("SELECT TOP 1") && sql.contains("dc.id_khach_hang=?") && sql.contains("ISNULL(dc.xoa_mem,0)=0")
                && sql.contains("ISNULL(kh.trang_thai,1)=1") && sql.contains("ORDER BY ISNULL(dc.mac_dinh,0) DESC,dc.id DESC")),eq(32));
    }
}
