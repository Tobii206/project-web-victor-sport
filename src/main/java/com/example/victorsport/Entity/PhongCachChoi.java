package com.example.victorsport.Entity;

import jakarta.persistence.*;

@Entity
@Table(name = "phong_cach_choi")
public class PhongCachChoi {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "ma_phong_cach", insertable = false, updatable = false)
    private String maPhongCach;

    @Column(name = "ten_phong_cach", nullable = false)
    private String tenPhongCach;

    @Column(name = "trang_thai")
    private Boolean trangThai;

    @Column(name = "xoa_mem")
    private Boolean xoaMem;
}

