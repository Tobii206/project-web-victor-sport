package com.example.victorsport.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
@Getter @Setter
@Entity
@Table(name = "quyen_han")
public class QuyenHan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "ma_quyen_han", insertable = false, updatable = false)
    private String maQuyenHan;

    @Column(name = "ten_quyen_han", nullable = false)
    private String tenQuyenHan;

    @Column(name = "trang_thai")
    private Boolean trangThai;

    @Column(name = "xoa_mem")
    private Boolean xoaMem;
}
