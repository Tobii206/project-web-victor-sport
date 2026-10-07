package com.example.victorsport.Entity;

import jakarta.persistence.*;

@Entity
@Table(name = "quyen_han_chuc_nang")
public class QuyenHanChucNang {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_quyen_han", nullable = false)
    private QuyenHan quyenHan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_chuc_nang", nullable = false)
    private ChucNang chucNang;

    @Column(name = "xoa_mem")
    private Boolean xoaMem;
}

