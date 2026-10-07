package com.example.victorsport.Dto;

import java.math.BigDecimal;

public class LuaChonPhieuGiamGia {
    private final Integer id;
    private final String maPhieuGiamGia;
    private final String tenPhieuGiamGia;
    private final Boolean loaiPhieuGiamGia;
    private final BigDecimal giaTriGiamGia;
    private final BigDecimal hoaDonToiThieu;

    public LuaChonPhieuGiamGia(Integer id, String maPhieuGiamGia, String tenPhieuGiamGia,
                               Boolean loaiPhieuGiamGia, BigDecimal giaTriGiamGia, BigDecimal hoaDonToiThieu) {
        this.id = id;
        this.maPhieuGiamGia = maPhieuGiamGia;
        this.tenPhieuGiamGia = tenPhieuGiamGia;
        this.loaiPhieuGiamGia = loaiPhieuGiamGia;
        this.giaTriGiamGia = giaTriGiamGia;
        this.hoaDonToiThieu = hoaDonToiThieu;
    }

    public Integer getId() {
        return id;
    }

    public String getMaPhieuGiamGia() {
        return maPhieuGiamGia;
    }

    public String getTenPhieuGiamGia() {
        return tenPhieuGiamGia;
    }

    public Boolean getLoaiPhieuGiamGia() {
        return loaiPhieuGiamGia;
    }

    public BigDecimal getGiaTriGiamGia() {
        return giaTriGiamGia;
    }

    public BigDecimal getHoaDonToiThieu() {
        return hoaDonToiThieu;
    }
}
