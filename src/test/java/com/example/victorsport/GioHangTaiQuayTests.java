package com.example.victorsport;

import com.example.victorsport.Dto.GioHangTaiQuay;
import com.example.victorsport.Dto.SanPhamTrongGio;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class GioHangTaiQuayTests {
    @Test
    void themTrungSanPhamKhongVuotTonKho() {
        GioHangTaiQuay gioHang = new GioHangTaiQuay();
        gioHang.themHoacCapNhat(taoSanPham(1, 2, 3, "100000"));
        gioHang.themHoacCapNhat(taoSanPham(1, 2, 3, "100000"));

        assertEquals(1, gioHang.getDanhSachSanPham().size());
        assertEquals(3, gioHang.timSanPham(1).getSoLuongMua().intValue());
        assertEquals(new BigDecimal("300000.00"), gioHang.getTamTinh());
    }

    @Test
    void capNhatSoLuongVaXoaSanPhamKhongLamMatSanPhamKhac() {
        GioHangTaiQuay gioHang = new GioHangTaiQuay();
        gioHang.themHoacCapNhat(taoSanPham(1, 1, 5, "100000"));
        gioHang.themHoacCapNhat(taoSanPham(2, 2, 4, "150000"));

        gioHang.capNhatSoLuong(1, 10);
        assertEquals(5, gioHang.timSanPham(1).getSoLuongMua().intValue());
        assertEquals(new BigDecimal("800000.00"), gioHang.getTamTinh());

        gioHang.capNhatSoLuong(1, 0);
        assertNull(gioHang.timSanPham(1));
        assertEquals(new BigDecimal("300000.00"), gioHang.getTamTinh());

        gioHang.xoaSanPham(2);
        assertTrue(gioHang.isRong());
        assertEquals(new BigDecimal("0.00"), gioHang.getTamTinh());
    }

    @Test
    void sanPhamKhongCoTrongGioKhongLamThayDoiTongTien() {
        GioHangTaiQuay gioHang = new GioHangTaiQuay();
        gioHang.themHoacCapNhat(taoSanPham(1, 2, 5, "100000.50"));

        gioHang.capNhatSoLuong(99, 3);
        gioHang.xoaSanPham(99);
        assertNull(gioHang.timSanPham(99));
        assertEquals(new BigDecimal("200001.00"), gioHang.getTamTinh());

        gioHang.xoaTatCa();
        assertTrue(gioHang.isRong());
    }

    private SanPhamTrongGio taoSanPham(int id, int soLuongMua, int soLuongTon, String donGia) {
        return new SanPhamTrongGio(id, "SP" + id, "Giày Victor", "Trắng", "40",
                soLuongMua, soLuongTon, new BigDecimal(donGia));
    }
}
