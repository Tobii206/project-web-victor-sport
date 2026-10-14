package com.example.victorsport.Dto;

import jakarta.validation.constraints.*;

// Nhận dữ liệu từ form và kiểm tra trước khi lưu.
public class DangNhapForm {
    @NotBlank(message = "Vui lòng nhập tài khoản.")
    @Size(max = 100, message = "Tài khoản tối đa 100 ký tự.")
    @Pattern(regexp = "(?i)([^@]+|[a-z0-9]+([._%+\\-][a-z0-9]+)*@gmail\\.com)", message = "Email phải đúng định dạng, ví dụ: ten@gmail.com.")
    private String taiKhoan;

    @NotBlank(message = "Vui lòng nhập mật khẩu.")
    @Size(min = 6, message = "Mật khẩu phải có ít nhất 6 ký tự.")
    private String matKhau;

    public String getTaiKhoan() { return taiKhoan; }

    public void setTaiKhoan(String taiKhoan) {
        this.taiKhoan = taiKhoan == null ? null : taiKhoan.trim();
    }

    public String getMatKhau() { return matKhau; }

    public void setMatKhau(String matKhau) {
        this.matKhau = matKhau;
    }

}
