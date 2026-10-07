package com.example.victorsport.Entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "giao_dich_thanh_toan")
public class GiaoDichThanhToan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_hoa_don", nullable = false)
    private HoaDon hoaDon;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_phuong_thuc_thanh_toan", nullable = false)
    private PhuongThucThanhToan phuongThucThanhToan;

    @Column(name = "ma_giao_dich_thanh_toan", insertable = false, updatable = false)
    private String maGiaoDichThanhToan;

    @Column(name = "so_tien")
    private BigDecimal soTien;

    @Column(name = "trang_thai")
    private String trangThai;

    @Column(name = "ma_yeu_cau")
    private String maYeuCau;

    @Column(name = "ma_giao_dich_ngoai")
    private String maGiaoDichNgoai;

    @Column(name = "ma_tham_chieu")
    private String maThamChieu;

    @Column(name = "duong_dan_thanh_toan")
    private String duongDanThanhToan;

    @Column(name = "du_lieu_qr")
    private String duLieuQr;

    @Column(name = "thoi_gian_het_han")
    private LocalDateTime thoiGianHetHan;

    @Column(name = "du_lieu_phan_hoi")
    private String duLieuPhanHoi;

    @Column(name = "thoi_gian_tao")
    private LocalDateTime thoiGianTao;

    @Column(name = "thoi_gian_cap_nhat")
    private LocalDateTime thoiGianCapNhat;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nguoi_cap_nhat")
    private NhanVien nguoiCapNhat;

    @Column(name = "xoa_mem")
    private Boolean xoaMem;

    @Column(name = "ghi_chu")
    private String ghiChu;
}

