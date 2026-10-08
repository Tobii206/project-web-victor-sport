package com.example.victorsport.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "co_giay")
@Getter
@Setter
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

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getMaCoGiay() {
        return maCoGiay;
    }

    public void setMaCoGiay(String maCoGiay) {
        this.maCoGiay = maCoGiay;
    }

    public String getTenCoGiay() {
        return tenCoGiay;
    }

    public void setTenCoGiay(String tenCoGiay) {
        this.tenCoGiay = tenCoGiay;
    }

    public Boolean getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(Boolean trangThai) {
        this.trangThai = trangThai;
    }

    public Boolean getXoaMem() {
        return xoaMem;
    }

    public void setXoaMem(Boolean xoaMem) {
        this.xoaMem = xoaMem;
    }
}
