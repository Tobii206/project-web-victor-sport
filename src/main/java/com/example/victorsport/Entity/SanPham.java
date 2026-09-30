package com.example.victorsport.Entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "san_pham")
public class SanPham {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_thuong_hieu", nullable = false)
    private ThuongHieu thuongHieu;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_xuat_xu")
    private XuatXu xuatXu;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_vi_tri_thi_dau")
    private ViTriThiDau viTriThiDau;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_phong_cach_choi")
    private PhongCachChoi phongCachChoi;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_co_giay")
    private CoGiay coGiay;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_chat_lieu")
    private ChatLieu chatLieu;

    @Column(name = "ma_san_pham", insertable = false, updatable = false)
    private String maSanPham;

    @Column(name = "ten_san_pham", nullable = false)
    private String tenSanPham;

    @Column(name = "mo_ta_ngan")
    private String moTaNgan;

    @Column(name = "mo_ta_chi_tiet")
    private String moTaChiTiet;

    @Column(name = "trang_thai_kinh_doanh")
    private Boolean trangThaiKinhDoanh;

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
