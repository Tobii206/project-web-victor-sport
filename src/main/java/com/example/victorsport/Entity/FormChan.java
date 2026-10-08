package com.example.victorsport.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "form_chan")
@Getter
@Setter
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

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getMaFormChan() {
        return maFormChan;
    }

    public void setMaFormChan(String maFormChan) {
        this.maFormChan = maFormChan;
    }

    public String getTenFormChan() {
        return tenFormChan;
    }

    public void setTenFormChan(String tenFormChan) {
        this.tenFormChan = tenFormChan;
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
