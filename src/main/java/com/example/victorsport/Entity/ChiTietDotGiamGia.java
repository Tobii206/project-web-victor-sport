package com.example.victorsport.Entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "chi_tiet_dot_giam_gia")
public class ChiTietDotGiamGia {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_dot_giam_gia", nullable = false)
    private DotGiamGia dotGiamGia;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_chi_tiet_san_pham", nullable = false)
    private ChiTietSanPham chiTietSanPham;

    @Column(name = "so_luong_ap_dung")
    private Integer soLuongApDung;

    @Column(name = "gia_tri_giam_rieng")
    private BigDecimal giaTriGiamRieng;

    @Column(name = "so_tien_giam_toi_da_rieng")
    private BigDecimal soTienGiamToiDaRieng;

    @Column(name = "trang_thai")
    private Boolean trangThai;

    @Column(name = "ghi_chu")
    private String ghiChu;

    @Column(name = "xoa_mem")
    private Boolean xoaMem;

    @Column(name = "ngay_tao")
    private LocalDateTime ngayTao;

    @Column(name = "nguoi_tao")
    private Integer nguoiTao;

    @Column(name = "ngay_cap_nhat")
    private LocalDateTime ngayCapNhat;

    @Column(name = "nguoi_cap_nhat")
    private Integer nguoiCapNhat;
}
