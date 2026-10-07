package com.example.victorsport.Dto;

public class NguoiDungDangNhap {
    private final Integer id;
    private final String email;
    private final String hoTen;
    private final String tenQuyen;

    public NguoiDungDangNhap(Integer id, String email, String hoTen, String tenQuyen) {
        this.id = id;
        this.email = email;
        this.hoTen = hoTen;
        this.tenQuyen = tenQuyen;
    }

    public Integer getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getHoTen() {
        return hoTen;
    }

    public String getTenQuyen() {
        return tenQuyen;
    }
}
