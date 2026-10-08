package com.example.victorsport.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "thuong_hieu")
@Getter
@Setter
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

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getMaThuongHieu() {
        return maThuongHieu;
    }

    public void setMaThuongHieu(String maThuongHieu) {
        this.maThuongHieu = maThuongHieu;
    }

    public String getTenThuongHieu() {
        return tenThuongHieu;
    }

    public void setTenThuongHieu(String tenThuongHieu) {
        this.tenThuongHieu = tenThuongHieu;
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
