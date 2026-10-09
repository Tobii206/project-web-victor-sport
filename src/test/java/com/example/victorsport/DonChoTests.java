package com.example.victorsport;

import com.example.victorsport.Dto.GioHangTaiQuay;
import com.example.victorsport.Dto.NguoiDungDangNhap;
import com.example.victorsport.Dto.SanPhamTrongGio;
import com.example.victorsport.Dto.TongKetBanHang;
import com.example.victorsport.Service.BanHangTaiQuayService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class DonChoTests {
    private JdbcTemplate jdbc;
    private BanHangTaiQuayService service;
    private GioHangTaiQuay gioHang;
    private NguoiDungDangNhap nhanVien;
    private BanHangTaiQuayService.YeuCauThanhToan yeuCau;

    @BeforeEach
    void chuanBi() {
        jdbc = mock(JdbcTemplate.class);
        service = spy(new BanHangTaiQuayService(jdbc));
        gioHang = new GioHangTaiQuay();
        nhanVien = new NguoiDungDangNhap(2, "nv@example.com", "Nhân viên", "Bán hàng");
        yeuCau = new BanHangTaiQuayService.YeuCauThanhToan(null, null, null, null, null, 1, null, null);
        doReturn(new TongKetBanHang(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, "Không áp dụng", null))
                .when(service).tinhTongKet(any(GioHangTaiQuay.class), nullable(Integer.class));
    }

    @Test
    void daCoMuoiDonThiKhongTaoThem() {
        when(jdbc.queryForObject(contains("FROM nhan_vien"), eq(Integer.class), eq(2))).thenReturn(2);
        when(jdbc.queryForObject(contains("COUNT(*)"), eq(Integer.class), eq(2))).thenReturn(10);

        IllegalArgumentException loi = assertThrows(IllegalArgumentException.class,
                () -> service.luuDonCho(gioHang, yeuCau, nhanVien, null));
        assertTrue(loi.getMessage().contains("10 đơn chờ"));
        verify(jdbc, never()).queryForObject(contains("INSERT INTO hoa_don"), eq(Integer.class), eq(2), eq(2));
        verify(jdbc, never()).update(anyString(), any(Object[].class));
    }

    @Test
    void taoDuocDonThuMuoiVaChoPhepDonTrong() {
        when(jdbc.queryForObject(contains("FROM nhan_vien"), eq(Integer.class), eq(2))).thenReturn(2);
        when(jdbc.queryForObject(contains("COUNT(*)"), eq(Integer.class), eq(2))).thenReturn(9);
        when(jdbc.queryForObject(contains("INSERT INTO hoa_don"), eq(Integer.class), eq(2), eq(2))).thenReturn(20);

        assertEquals(20, service.luuDonCho(gioHang, yeuCau, nhanVien, null).intValue());
        verify(jdbc, never()).update(contains("UPDATE chi_tiet_san_pham"), any(Object[].class));
        verify(jdbc, never()).update(contains("UPDATE phieu_giam_gia"), any(Object[].class));
    }

    @Test
    void luuLaiDonCuKhongTinhThanhDonMoiVaKhongTruTon() {
        when(jdbc.queryForList(contains("WITH (UPDLOCK, HOLDLOCK)"), eq(20), eq(2)))
                .thenReturn(List.of(Map.of("id", 20)));
        gioHang.themHoacCapNhat(new SanPhamTrongGio(1, "SP1", "Giày", "Trắng", "40", 1, 5, BigDecimal.TEN));

        assertEquals(20, service.luuDonCho(gioHang, yeuCau, nhanVien, 20).intValue());
        verify(jdbc, never()).queryForObject(contains("COUNT(*)"), eq(Integer.class), eq(2));
        verify(jdbc, never()).update(contains("UPDATE chi_tiet_san_pham"), any(Object[].class));
        verify(jdbc, never()).update(contains("UPDATE phieu_giam_gia"), any(Object[].class));
    }

    @Test
    void khongHuyDuocDonCuaNhanVienKhacHoacDonDaThanhToan() {
        when(jdbc.queryForList(contains("WITH (UPDLOCK, HOLDLOCK)"), eq(20), eq(2))).thenReturn(List.of());

        assertThrows(IllegalArgumentException.class, () -> service.huyDonCho(20, 2));
        verify(jdbc, never()).update(anyString(), any(Object[].class));
    }

    @Test
    void huyDonChoXoaChiTietVaHoaDon() {
        when(jdbc.queryForList(contains("WITH (UPDLOCK, HOLDLOCK)"), eq(20), eq(2)))
                .thenReturn(List.of(Map.of("id", 20)));

        service.huyDonCho(20, 2);
        verify(jdbc).update("DELETE FROM hoa_don_chi_tiet WHERE id_hoa_don = ?", 20);
        verify(jdbc).update("DELETE FROM hoa_don WHERE id = ?", 20);
        verify(jdbc, never()).update(contains("UPDATE chi_tiet_san_pham"), any(Object[].class));
    }

    @Test
    void thanhToanDonChoGiuNguyenMaVaTruTonMotLan() {
        when(jdbc.queryForList(contains("WITH (UPDLOCK, HOLDLOCK)"), eq(20), eq(2)))
                .thenReturn(List.of(Map.of("id", 20)));
        when(jdbc.queryForList(contains("FROM chi_tiet_san_pham"), eq(Integer.class), eq(1)))
                .thenReturn(List.of(5));
        gioHang.themHoacCapNhat(new SanPhamTrongGio(1, "SP1", "Giày", "Trắng", "40", 2, 5, BigDecimal.TEN));

        assertEquals(20, service.thanhToan(gioHang, yeuCau, nhanVien, 20).intValue());
        verify(jdbc).update(contains("trang_thai_hien_tai = 5"), eq(20));
        verify(jdbc, times(1)).update(contains("UPDATE chi_tiet_san_pham"), eq(2), eq(2), eq(1));
        verify(jdbc, never()).queryForObject(contains("INSERT INTO hoa_don"), eq(Integer.class), eq(2), eq(2));
    }

    @Test
    void donChoKhongDuTonKhoThiChuaDuocThanhToan() {
        when(jdbc.queryForList(contains("WITH (UPDLOCK, HOLDLOCK)"), eq(20), eq(2)))
                .thenReturn(List.of(Map.of("id", 20)));
        when(jdbc.queryForList(contains("FROM chi_tiet_san_pham"), eq(Integer.class), eq(1)))
                .thenReturn(List.of(1));
        gioHang.themHoacCapNhat(new SanPhamTrongGio(1, "SP1", "Giày", "Trắng", "40", 2, 5, BigDecimal.TEN));

        assertThrows(IllegalArgumentException.class, () -> service.thanhToan(gioHang, yeuCau, nhanVien, 20));
        verify(jdbc, never()).update(anyString(), any(Object[].class));
    }
}
