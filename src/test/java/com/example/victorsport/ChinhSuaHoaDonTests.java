package com.example.victorsport;

import com.example.victorsport.Service.HoaDonService;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class ChinhSuaHoaDonTests {
    @Test
    void khachKhongSuaDuocDonKhongThuocMinhHoacDaGuiHang() {
        var jdbc = mock(JdbcTemplate.class);
        when(jdbc.queryForList(contains("SELECT id FROM khach_hang"), eq(7))).thenReturn(List.of(Map.of("id", 7)));
        var service = new HoaDonService(jdbc);
        assertThrows(IllegalArgumentException.class,
                () -> service.suaDiaChiCuaKhach(19, 7, "Người nhận", "0912345678", "12 Phố Mới", null, null));
        verify(jdbc).queryForList(argThat(sql -> sql.contains("UPDLOCK") && sql.contains("id_khach_hang=?")
                && sql.contains("trang_thai_hien_tai IN (1,2,3)")), eq(19), eq(7));
        verify(jdbc, never()).update(anyString(), any(Object[].class));
    }

    @Test
    void khachDoiKhuVucLamTangPhiThiKhongGhiDiaChiMoi() {
        var jdbc = mock(JdbcTemplate.class);
        var ghn = mock(com.example.victorsport.Service.GiaoHangGhnService.class);
        var service = new HoaDonService(jdbc);
        org.springframework.test.util.ReflectionTestUtils.setField(service, "giaoHangGhn", ghn);
        when(jdbc.queryForList(contains("SELECT id FROM khach_hang"), eq(7))).thenReturn(List.of(Map.of("id", 7)));
        when(jdbc.queryForList(contains("id_khach_hang=?"), eq(19), eq(7))).thenReturn(List.of(Map.of("trang_thai_hien_tai", 2)));
        when(jdbc.queryForList(contains("SELECT ma_van_don,ten_tinh,ten_xa"), eq(19)))
                .thenReturn(List.of(Map.of("ten_tinh", "Hà Nội", "ten_xa", "Phường Cầu Giấy", "phi_bao_gia", new BigDecimal("20000"))));
        when(jdbc.queryForMap(contains("SELECT tong_tien,phi_van_chuyen"), eq(19)))
                .thenReturn(Map.of("tong_tien", new BigDecimal("200000"), "phi_van_chuyen", new BigDecimal("20000")));
        when(jdbc.queryForList(contains("SUM(so_luong) AS so_luong"), eq(19)))
                .thenReturn(List.of(Map.of("id_chi_tiet_san_pham", 31, "so_luong", 1)));
        when(ghn.layKhuVuc(1, 2)).thenReturn(new com.example.victorsport.Service.GiaoHangGhnService.KhuVuc("Hà Nội", "Phường Tây Hồ"));
        var baoGia = mock(com.example.victorsport.Dto.BaoGiaGhn.class);
        when(baoGia.phi()).thenReturn(new BigDecimal("30000"));
        when(ghn.baoGia(Map.of(31, 1), "Người nhận", "0912345678", "12 Phố Mới", "COD", new BigDecimal("200000"), 1, 2))
                .thenReturn(baoGia);

        assertThrows(IllegalArgumentException.class,
                () -> service.suaDiaChiCuaKhach(19, 7, "Người nhận", "0912345678", "12 Phố Mới", 1, 2));
        verify(jdbc, never()).update(anyString(), any(Object[].class));
    }

    @Test
    void suaDiaChiDonDaThanhToanTruocKhiTaoVanDon() {
        var jdbc = mock(JdbcTemplate.class);
        when(jdbc.queryForList(contains("SELECT trang_thai_hien_tai FROM hoa_don"), eq(19)))
                .thenReturn(List.of(Map.of("trang_thai_hien_tai", 2, "ngay_thanh_toan", java.time.LocalDateTime.now())));
        when(jdbc.queryForList(contains("SELECT ma_van_don,ten_tinh,ten_xa"), eq(19)))
                .thenReturn(List.of(Map.of("ten_tinh", "Hà Nội", "ten_xa", "Phường Cầu Giấy")));

        new HoaDonService(jdbc).suaDiaChiGiaoHang(19, 2, "Nguyễn Văn A", "0912345678", "12 Phố Mới", null, null);

        verify(jdbc).update(startsWith("UPDATE hoa_don SET ten_khach_hang"),
                eq("Nguyễn Văn A"), eq("0912345678"), eq("12 Phố Mới, Phường Cầu Giấy, Hà Nội"), eq(2), eq(19));
        verify(jdbc, never()).update(contains("tong_tien_sau_giam"), any(Object[].class));
    }

    @Test
    void chanSuaDiaChiKhiDaCoVanDon() {
        var jdbc = mock(JdbcTemplate.class);
        when(jdbc.queryForList(contains("SELECT trang_thai_hien_tai FROM hoa_don"), eq(19)))
                .thenReturn(List.of(Map.of("trang_thai_hien_tai", 3)));
        when(jdbc.queryForList(contains("SELECT ma_van_don,ten_tinh,ten_xa"), eq(19)))
                .thenReturn(List.of(Map.of("ma_van_don", "GHN123")));
        var service = new HoaDonService(jdbc);

        assertThrows(IllegalArgumentException.class,
                () -> service.suaDiaChiGiaoHang(19, 2, "Nguyễn Văn A", "0912345678", "12 Phố Mới", null, null));
        verify(jdbc, never()).update(anyString(), any(Object[].class));
    }

    @Test
    void doiSoLuongDonCodCapNhatTonKhoTongTienVaKhoanThuHo() {
        var jdbc = mock(JdbcTemplate.class);
        Map<String, Object> don = new HashMap<>(Map.of("id", 19, "loai_don", 1, "trang_thai_hien_tai", 2,
                "tong_tien_sau_giam", new BigDecimal("220000"), "tong_tien_giam", BigDecimal.ZERO,
                "phi_van_chuyen", new BigDecimal("20000")));
        when(jdbc.queryForList(contains("SELECT * FROM hoa_don WITH"), eq(19))).thenReturn(new ArrayList<>(List.of(don)));
        when(jdbc.queryForList(contains("SELECT id_chi_tiet_san_pham,so_luong"), eq(44), eq(19)))
                .thenReturn(List.of(Map.of("id_chi_tiet_san_pham", 31, "so_luong", 2)));
        when(jdbc.update(startsWith("UPDATE ct SET so_luong=ct.so_luong-?"), eq(1), eq(2), eq(31), eq(1), eq(1))).thenReturn(1);
        when(jdbc.queryForMap(startsWith("SELECT SUM(thanh_tien)"), eq(19)))
                .thenReturn(Map.of("tien_hang", new BigDecimal("300000"), "so_doi", 3L));

        new HoaDonService(jdbc).suaSoLuongDonCod(19, 44, 2, 3, 2);

        verify(jdbc).update("UPDATE hoa_don_chi_tiet SET so_luong=? WHERE id=? AND id_hoa_don=?", 3, 44, 19);
        verify(jdbc).update(startsWith("UPDATE hoa_don SET tong_tien="), eq(new BigDecimal("300000")), eq(new BigDecimal("320000")), eq(2), eq(19));
        verify(jdbc).update(startsWith("UPDATE gd SET so_tien="), eq(new BigDecimal("320000")), eq(19));
    }
}
