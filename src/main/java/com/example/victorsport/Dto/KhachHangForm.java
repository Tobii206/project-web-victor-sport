package com.example.victorsport.Dto;

import jakarta.validation.constraints.*;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;

// Nhận dữ liệu từ form và kiểm tra trước khi lưu.
public class KhachHangForm {
    @NotBlank(message = "Vui lòng nhập tên khách hàng.")
    @Size(max = 100, message = "Tên khách hàng tối đa 100 ký tự.")
    private String ten_khach_hang;

    @NotBlank(message = "Vui lòng nhập tên tài khoản.")
    @Size(max = 50, message = "Tên tài khoản tối đa 50 ký tự.")
    @Pattern(regexp = "(?i)[a-z0-9._@-]+", message = "Tên tài khoản chỉ gồm chữ không dấu, số và . _ @ -.")
    private String ten_tai_khoan;

    @Email(message = "Email không hợp lệ.")
    @Pattern(regexp = "(?i)[a-z0-9]+([._%+\\-][a-z0-9]+)*@gmail\\.com", message = "Email phải đúng định dạng, ví dụ: ten@gmail.com.")
    @Size(max = 100, message = "Email tối đa 100 ký tự.")
    private String email;

    @Size(min = 6, max = 72, message = "Mật khẩu phải có ít nhất 6 ký tự và tối đa 72 ký tự.")
    @Pattern(regexp = ".*\\S.*", message = "Mật khẩu không được chỉ chứa khoảng trắng.")
    private String mat_khau;

    @Pattern(regexp = "0[0-9]{9}", message = "Số điện thoại phải đủ 10 số và bắt đầu bằng số 0.")
    private String so_dien_thoai;

    @PastOrPresent(message = "Ngày sinh không được ở tương lai.")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate ngay_sinh;

    @Size(max = 100, message = "Tỉnh / Thành phố tối đa 100 ký tự.")
    private String thanh_pho;

    @Size(max = 100, message = "Quận / Huyện tối đa 100 ký tự.")
    private String quan;

    @Size(max = 100, message = "Phường / Xã tối đa 100 ký tự.")
    private String phuong;

    @Size(max = 255, message = "Địa chỉ chi tiết tối đa 255 ký tự.")
    private String dia_chi_cu_the;

    @Min(value = 0, message = "Giới tính không hợp lệ.")
    @Max(value = 1, message = "Giới tính không hợp lệ.")
    private Integer gioi_tinh;

    @NotNull(message = "Vui lòng chọn trạng thái.")
    @Min(value = 0, message = "Trạng thái không hợp lệ.")
    @Max(value = 1, message = "Trạng thái không hợp lệ.")
    private Integer trang_thai;

    @Size(max = 100, message = "Tên địa chỉ tối đa 100 ký tự.")
    private String ten_dia_chi;

    public String getTen_khach_hang() { return ten_khach_hang; }

    public void setTen_khach_hang(String ten_khach_hang) {
        this.ten_khach_hang = ten_khach_hang == null ? null : ten_khach_hang.trim();
    }

    public String getTen_tai_khoan() { return ten_tai_khoan; }

    public void setTen_tai_khoan(String ten_tai_khoan) {
        this.ten_tai_khoan = ten_tai_khoan == null ? null : ten_tai_khoan.trim();
    }

    public String getEmail() { return email; }

    public void setEmail(String email) {
        this.email = email == null || email.isBlank() ? null : email.trim();
    }

    public String getMat_khau() { return mat_khau; }

    public void setMat_khau(String mat_khau) {
        this.mat_khau = mat_khau == null || mat_khau.isEmpty() ? null : mat_khau;
    }

    public String getSo_dien_thoai() { return so_dien_thoai; }

    public void setSo_dien_thoai(String so_dien_thoai) {
        this.so_dien_thoai = so_dien_thoai == null || so_dien_thoai.isBlank() ? null : so_dien_thoai.trim();
    }

    public LocalDate getNgay_sinh() { return ngay_sinh; }

    public void setNgay_sinh(LocalDate ngay_sinh) {
        this.ngay_sinh = ngay_sinh;
    }

    public String getThanh_pho() { return thanh_pho; }

    public void setThanh_pho(String thanh_pho) {
        this.thanh_pho = thanh_pho == null ? null : thanh_pho.trim();
    }

    public String getQuan() { return quan; }

    public void setQuan(String quan) {
        this.quan = quan == null ? null : quan.trim();
    }

    public String getPhuong() { return phuong; }

    public void setPhuong(String phuong) {
        this.phuong = phuong == null ? null : phuong.trim();
    }

    public String getDia_chi_cu_the() { return dia_chi_cu_the; }

    public void setDia_chi_cu_the(String dia_chi_cu_the) {
        this.dia_chi_cu_the = dia_chi_cu_the == null ? null : dia_chi_cu_the.trim();
    }

    public Integer getGioi_tinh() { return gioi_tinh; }

    public void setGioi_tinh(Integer gioi_tinh) {
        this.gioi_tinh = gioi_tinh;
    }

    public Integer getTrang_thai() { return trang_thai; }

    public void setTrang_thai(Integer trang_thai) {
        this.trang_thai = trang_thai;
    }

    public String getTen_dia_chi() { return ten_dia_chi; }

    public void setTen_dia_chi(String ten_dia_chi) {
        this.ten_dia_chi = ten_dia_chi == null ? null : ten_dia_chi.trim();
    }

}
