package com.example.victorsport.Entity;

import jakarta.persistence.*;

@Entity
@Table(name = "chuc_nang")
public class ChucNang {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "ma_chuc_nang")
    private String maChucNang;

    @Column(name = "ten_chuc_nang")
    private String tenChucNang;

    @Column(name = "mo_ta")
    private String moTa;

    @Column(name = "trang_thai")
    private Boolean trangThai;

    @Column(name = "xoa_mem")
    private Boolean xoaMem;
}

