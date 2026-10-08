package com.example.victorsport.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
@Getter @Setter
@Entity
@Table(name = "phuong_thuc_thanh_toan")
public class PhuongThucThanhToan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "ma_phuong_thuc_thanh_toan", insertable = false, updatable = false)
    private String maPhuongThucThanhToan;

    @Column(name = "ten_phuong_thuc_thanh_toan", nullable = false)
    private String tenPhuongThucThanhToan;

    @Column(name = "nha_cung_cap")
    private String nhaCungCap;

    @Column(name = "trang_thai")
    private Boolean trangThai;

    @Column(name = "xoa_mem")
    private Boolean xoaMem;
}
