package com.example.victorsport.Dto;

import java.math.BigDecimal;

public class TongKetBanHang {
    private final BigDecimal tamTinh;
    private final BigDecimal tienGiam;
    private final BigDecimal tongThanhToan;
    private final String tenUuDai;
    private final Integer idPhieuGiamGia;

    public TongKetBanHang(BigDecimal tamTinh, BigDecimal tienGiam, BigDecimal tongThanhToan,
                          String tenUuDai, Integer idPhieuGiamGia) {
        this.tamTinh = tamTinh;
        this.tienGiam = tienGiam;
        this.tongThanhToan = tongThanhToan;
        this.tenUuDai = tenUuDai;
        this.idPhieuGiamGia = idPhieuGiamGia;
    }

    public BigDecimal getTamTinh() {
        return tamTinh;
    }

    public BigDecimal getTienGiam() {
        return tienGiam;
    }

    public BigDecimal getTongThanhToan() {
        return tongThanhToan;
    }

    public String getTenUuDai() {
        return tenUuDai;
    }

    public Integer getIdPhieuGiamGia() {
        return idPhieuGiamGia;
    }
}
