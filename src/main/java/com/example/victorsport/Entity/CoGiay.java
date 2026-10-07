package com.example.victorsport.Entity;

import jakarta.persistence.*;

@Entity
@Table(name = "co_giay")
public class CoGiay {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "ma_co_giay", insertable = false, updatable = false)
    private String maCoGiay;

    @Column(name = "ten_co_giay", nullable = false)
    private String tenCoGiay;

    @Column(name = "trang_thai")
    private Boolean trangThai;

    @Column(name = "xoa_mem")
    private Boolean xoaMem;
}

