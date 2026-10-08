package com.example.victorsport.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
@Getter @Setter
@Entity
@Table(name = "dia_chi_khach_hang")
public class DiaChiKhachHang {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_khach_hang", nullable = false)
    private KhachHang khachHang;

    @Column(name = "ma_dia_chi", insertable = false, updatable = false)
    private String maDiaChi;

    @Column(name = "ten_dia_chi", nullable = false)
    private String tenDiaChi;

    @Column(name = "thanh_pho")
    private String thanhPho;

    private String quan;
    private String phuong;

    @Column(name = "dia_chi_cu_the")
    private String diaChiCuThe;

    @Column(name = "mac_dinh")
    private Boolean macDinh;

    @Column(name = "xoa_mem")
    private Boolean xoaMem;
}
