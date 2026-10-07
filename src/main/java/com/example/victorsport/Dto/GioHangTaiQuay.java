package com.example.victorsport.Dto;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public class GioHangTaiQuay {
    private final List<SanPhamTrongGio> danhSachSanPham = new ArrayList<>();

    public List<SanPhamTrongGio> getDanhSachSanPham() {
        return danhSachSanPham;
    }

    public boolean isRong() {
        return danhSachSanPham.isEmpty();
    }

    public SanPhamTrongGio timSanPham(Integer idChiTietSanPham) {
        for (SanPhamTrongGio sanPham : danhSachSanPham) {
            if (sanPham.getIdChiTietSanPham().equals(idChiTietSanPham)) {
                return sanPham;
            }
        }
        return null;
    }

    public void themHoacCapNhat(SanPhamTrongGio sanPhamMoi) {
        SanPhamTrongGio sanPham = timSanPham(sanPhamMoi.getIdChiTietSanPham());
        if (sanPham != null) {
            int soLuongMoi = Math.min(
                    sanPham.getSoLuongMua() + sanPhamMoi.getSoLuongMua(),
                    sanPham.getSoLuongTon()
            );
            sanPham.setSoLuongMua(soLuongMoi);
            return;
        }
        danhSachSanPham.add(sanPhamMoi);
    }

    public void capNhatSoLuong(Integer idChiTietSanPham, Integer soLuong) {
        SanPhamTrongGio sanPham = timSanPham(idChiTietSanPham);
        if (sanPham == null) {
            return;
        }
        if (soLuong == null || soLuong <= 0) {
            xoaSanPham(idChiTietSanPham);
        } else {
            sanPham.setSoLuongMua(Math.min(soLuong, sanPham.getSoLuongTon()));
        }
    }

    public void xoaSanPham(Integer idChiTietSanPham) {
        for (int i = 0; i < danhSachSanPham.size(); i++) {
            SanPhamTrongGio sanPham = danhSachSanPham.get(i);
            if (sanPham.getIdChiTietSanPham().equals(idChiTietSanPham)) {
                danhSachSanPham.remove(i);
                return;
            }
        }
    }

    public void xoaTatCa() {
        danhSachSanPham.clear();
    }

    public BigDecimal getTamTinh() {
        BigDecimal tamTinh = BigDecimal.ZERO;
        for (SanPhamTrongGio sanPham : danhSachSanPham) {
            tamTinh = tamTinh.add(sanPham.getThanhTien());
        }
        return tamTinh.setScale(2, RoundingMode.HALF_UP);
    }
}
