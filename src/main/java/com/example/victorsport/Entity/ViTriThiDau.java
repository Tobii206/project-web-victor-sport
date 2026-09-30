package com.example.victorsport.Entity;

import jakarta.persistence.*;

@Entity
@Table(name = "vi_tri_thi_dau")
public class ViTriThiDau {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "ma_vi_tri", insertable = false, updatable = false)
    private String maViTri;

    @Column(name = "ten_vi_tri", nullable = false)
    private String tenViTri;

    @Column(name = "trang_thai")
    private Boolean trangThai;

    @Column(name = "xoa_mem")
    private Boolean xoaMem;
}
