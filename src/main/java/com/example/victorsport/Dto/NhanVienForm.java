package com.example.victorsport.Dto;

import jakarta.validation.constraints.*;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;

// Nhận dữ liệu từ form và kiểm tra trước khi lưu.
public class NhanVienForm {
    @NotBlank(message = "Vui lòng nhập tên nhân viên.")
    @Size(max = 100, message = "Tên nhân viên tối đa 100 ký tự.")
    private String ten_nhan_vien;

    @NotBlank(message = "Vui lòng nhập email.")
    @Email(message = "Email không hợp lệ.")
    @Pattern(regexp = "(?i)[a-z0-9]+([._%+\\-][a-z0-9]+)*@gmail\\.com", message = "Email phải đúng định dạng, ví dụ: ten@gmail.com.")
    @Size(max = 100, message = "Email tối đa 100 ký tự.")
    private String email;

    @Size(min = 6, max = 255, message = "Mật khẩu phải có ít nhất 6 ký tự và tối đa 255 ký tự.")
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

    @NotNull(message = "Vui lòng chọn quyền hạn.")
    @Positive(message = "Vui lòng chọn quyền hạn.")
    private Integer id_quyen_han;

    @NotNull(message = "Vui lòng chọn trạng thái.")
    @Min(value = 0, message = "Trạng thái không hợp lệ.")
    @Max(value = 1, message = "Trạng thái không hợp lệ.")
    private Integer trang_thai;

    @Size(max = 255, message = "Ghi chú tối đa 255 ký tự.")
    private String ghi_chu;

    public String getTen_nhan_vien() { return ten_nhan_vien; }

    public void setTen_nhan_vien(String ten_nhan_vien) {
        this.ten_nhan_vien = ten_nhan_vien == null ? null : ten_nhan_vien.trim();
    }

    public String getEmail() { return email; }

    public void setEmail(String email) {
        this.email = email == null ? null : email.trim();
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

    public Integer getId_quyen_han() { return id_quyen_han; }

    public void setId_quyen_han(Integer id_quyen_han) {
        this.id_quyen_han = id_quyen_han;
    }

    public Integer getTrang_thai() { return trang_thai; }

    public void setTrang_thai(Integer trang_thai) {
        this.trang_thai = trang_thai;
    }

    public String getGhi_chu() { return ghi_chu; }

    public void setGhi_chu(String ghi_chu) {
        this.ghi_chu = ghi_chu == null ? null : ghi_chu.trim();
    }

}
