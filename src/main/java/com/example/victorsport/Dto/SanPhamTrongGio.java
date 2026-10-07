package com.example.victorsport.Dto;

import java.math.BigDecimal;

public class SanPhamTrongGio {
    private Integer idChiTietSanPham;
    private String maSanPham;
    private String tenSanPham;
    private String tenMauSac;
    private String tenKichThuoc;
    private Integer soLuongMua;
    private Integer soLuongTon;
    private BigDecimal donGia;

    public SanPhamTrongGio(Integer idChiTietSanPham, String maSanPham, String tenSanPham, String tenMauSac,
                           String tenKichThuoc, Integer soLuongMua, Integer soLuongTon, BigDecimal donGia) {
        this.idChiTietSanPham = idChiTietSanPham;
        this.maSanPham = maSanPham;
        this.tenSanPham = tenSanPham;
        this.tenMauSac = tenMauSac;
        this.tenKichThuoc = tenKichThuoc;
        this.soLuongMua = soLuongMua;
        this.soLuongTon = soLuongTon;
        this.donGia = donGia;
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

    public String getTenMauSac() {
        return tenMauSac;
    }

    public String getTenKichThuoc() {
        return tenKichThuoc;
    }

    public Integer getSoLuongMua() {
        return soLuongMua;
    }

    public void setSoLuongMua(Integer soLuongMua) {
        this.soLuongMua = soLuongMua;
    }

    public Integer getSoLuongTon() {
        return soLuongTon;
    }

    public BigDecimal getDonGia() {
        return donGia;
    }

    public BigDecimal getThanhTien() {
        return donGia.multiply(BigDecimal.valueOf(soLuongMua));
    }
}
