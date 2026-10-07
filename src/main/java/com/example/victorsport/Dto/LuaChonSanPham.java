package com.example.victorsport.Dto;

import java.math.BigDecimal;

public class LuaChonSanPham {
    private final Integer idChiTietSanPham;
    private final String maSanPham;
    private final String tenSanPham;
    private final String tenThuongHieu;
    private final String tenMauSac;
    private final String tenKichThuoc;
    private final Integer soLuongTon;
    private final BigDecimal giaBan;

    public LuaChonSanPham(Integer idChiTietSanPham, String maSanPham, String tenSanPham, String tenThuongHieu,
                          String tenMauSac, String tenKichThuoc, Integer soLuongTon, BigDecimal giaBan) {
        this.idChiTietSanPham = idChiTietSanPham;
        this.maSanPham = maSanPham;
        this.tenSanPham = tenSanPham;
        this.tenThuongHieu = tenThuongHieu;
        this.tenMauSac = tenMauSac;
        this.tenKichThuoc = tenKichThuoc;
        this.soLuongTon = soLuongTon;
        this.giaBan = giaBan;
    }

    public Integer getIdChiTietSanPham() {
        return idChiTietSanPham;
    }

    public String getMaSanPham() {
        return maSanPham;
    }

    public String getTenSanPham() {
        return tenSanPham;
    }

    public String getTenThuongHieu() {
        return tenThuongHieu;
    }

    public String getTenMauSac() {
        return tenMauSac;
    }

    public String getTenKichThuoc() {
        return tenKichThuoc;
    }

    public Integer getSoLuongTon() {
        return soLuongTon;
    }

    public BigDecimal getGiaBan() {
        return giaBan;
    }
}
