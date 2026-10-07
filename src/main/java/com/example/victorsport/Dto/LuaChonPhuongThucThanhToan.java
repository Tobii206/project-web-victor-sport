package com.example.victorsport.Dto;

public class LuaChonPhuongThucThanhToan {
    private final Integer id;
    private final String tenPhuongThuc;
    private final String nhaCungCap;

    public LuaChonPhuongThucThanhToan(Integer id, String tenPhuongThuc, String nhaCungCap) {
        this.id = id;
        this.tenPhuongThuc = tenPhuongThuc;
        this.nhaCungCap = nhaCungCap;
    }

    public Integer getId() {
        return id;
    }

    public String getTenPhuongThuc() {
        return tenPhuongThuc;
    }

    public String getNhaCungCap() {
        return nhaCungCap;
    }
}
