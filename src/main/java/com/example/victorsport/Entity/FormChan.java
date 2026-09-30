package com.example.victorsport.Entity;

import jakarta.persistence.*;

@Entity
@Table(name = "form_chan")
public class FormChan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "ma_form_chan", insertable = false, updatable = false)
    private String maFormChan;

    @Column(name = "ten_form_chan", nullable = false)
    private String tenFormChan;

    @Column(name = "trang_thai")
    private Boolean trangThai;

    @Column(name = "xoa_mem")
    private Boolean xoaMem;
}
