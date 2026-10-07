package com.example.victorsport.Entity;

import jakarta.persistence.*;

@Entity
@Table(name = "thuong_hieu")
public class ThuongHieu {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "ma_thuong_hieu", insertable = false, updatable = false)
    private String maThuongHieu;

    @Column(name = "ten_thuong_hieu", nullable = false)
    private String tenThuongHieu;

    @Column(name = "trang_thai")
    private Boolean trangThai;

    @Column(name = "xoa_mem")
    private Boolean xoaMem;
}

