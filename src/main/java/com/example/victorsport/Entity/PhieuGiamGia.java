package com.example.victorsport.Entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "phieu_giam_gia")
public class PhieuGiamGia {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "ma_phieu_giam_gia", insertable = false, updatable = false)
    private String maPhieuGiamGia;

    @Column(name = "ten_phieu_giam_gia", nullable = false)
    private String tenPhieuGiamGia;

    @Column(name = "loai_phieu_giam_gia")
    private Boolean loaiPhieuGiamGia;

    @Column(name = "gia_tri_giam_gia")
    private BigDecimal giaTriGiamGia;

    @Column(name = "so_tien_giam_toi_da")
    private BigDecimal soTienGiamToiDa;

    @Column(name = "hoa_don_toi_thieu")
    private BigDecimal hoaDonToiThieu;

    @Column(name = "so_luong_su_dung")
    private Integer soLuongSuDung;

    @Column(name = "ngay_bat_dau")
    private LocalDate ngayBatDau;

    @Column(name = "ngay_ket_thuc")
    private LocalDate ngayKetThuc;

    @Column(name = "trang_thai")
    private Boolean trangThai;

    @Column(name = "mo_ta")
    private String moTa;

    @Column(name = "xoa_mem")
    private Boolean xoaMem;
}

