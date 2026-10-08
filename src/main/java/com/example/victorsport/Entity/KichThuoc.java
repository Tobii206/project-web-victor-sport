package com.example.victorsport.Entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "kich_thuoc")
@Getter
@Setter
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

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getMaKichThuoc() {
        return maKichThuoc;
    }

    public void setMaKichThuoc(String maKichThuoc) {
        this.maKichThuoc = maKichThuoc;
    }

    public String getTenKichThuoc() {
        return tenKichThuoc;
    }

    public void setTenKichThuoc(String tenKichThuoc) {
        this.tenKichThuoc = tenKichThuoc;
    }

    public BigDecimal getGiaTriKichThuoc() {
        return giaTriKichThuoc;
    }

    public void setGiaTriKichThuoc(BigDecimal giaTriKichThuoc) {
        this.giaTriKichThuoc = giaTriKichThuoc;
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
