package com.example.victorsport.Entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "kich_thuoc")
public class KichThuoc {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "ma_kich_thuoc", insertable = false, updatable = false)
    private String maKichThuoc;

    @Column(name = "ten_kich_thuoc", nullable = false)
    private String tenKichThuoc;

    @Column(name = "gia_tri_kich_thuoc")
    private BigDecimal giaTriKichThuoc;

    @Column(name = "trang_thai")
    private Boolean trangThai;

    @Column(name = "xoa_mem")
    private Boolean xoaMem;
}

