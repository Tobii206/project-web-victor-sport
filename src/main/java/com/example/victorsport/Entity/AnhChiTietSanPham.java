package com.example.victorsport.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "anh_chi_tiet_san_pham")
@Getter
@Setter
public class AnhChiTietSanPham {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_chi_tiet_san_pham", nullable = false)
    private ChiTietSanPham chiTietSanPham;

    @Column(name = "duong_dan_anh", nullable = false)
    private String duongDanAnh;

    @Column(name = "la_anh_dai_dien")
    private Boolean laAnhDaiDien;

    @Column(name = "mo_ta")
    private String moTa;

    @Column(name = "xoa_mem")
    private Boolean xoaMem;
}
