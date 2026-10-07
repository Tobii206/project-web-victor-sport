package com.example.victorsport.Entity;

import jakarta.persistence.*;

@Entity
@Table(name = "xuat_xu")
public class XuatXu {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "ma_xuat_xu", insertable = false, updatable = false)
    private String maXuatXu;

    @Column(name = "ten_xuat_xu", nullable = false)
    private String tenXuatXu;

    @Column(name = "trang_thai")
    private Boolean trangThai;

    @Column(name = "xoa_mem")
    private Boolean xoaMem;
}

