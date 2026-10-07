package com.example.victorsport.Dto;

public class LuaChonKhachHang {
    private final Integer id;
    private final String maKhachHang;
    private final String tenKhachHang;
    private final String soDienThoai;
    private final String email;

    public LuaChonKhachHang(Integer id, String maKhachHang, String tenKhachHang, String soDienThoai, String email) {
        this.id = id;
        this.maKhachHang = maKhachHang;
        this.tenKhachHang = tenKhachHang;
        this.soDienThoai = soDienThoai;
        this.email = email;
    }

    public Integer getId() {
        return id;
    }

    public String getMaKhachHang() {
        return maKhachHang;
    }

    public String getTenKhachHang() {
        return tenKhachHang;
    }

    public String getSoDienThoai() {
        return soDienThoai;
    }

    public String getEmail() {
        return email;
    }
}
