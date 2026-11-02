package com.example.victorsport.Dto;

import jakarta.validation.constraints.*;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;

public record TaiKhoanKhachForm(
        @NotBlank(message="Vui lòng nhập họ tên.") @Size(max=100,message="Họ tên tối đa 100 ký tự.") String hoTen,
        @NotBlank(message="Vui lòng nhập email.") @Email(message="Email không hợp lệ.")
        @Pattern(regexp="(?i)[a-z0-9]+([._%+\\-][a-z0-9]+)*@gmail\\.com",message="Email phải có dạng ten@gmail.com.")
        @Size(max=100,message="Email tối đa 100 ký tự.") String email,
        @NotBlank(message="Vui lòng nhập số điện thoại.") @Pattern(regexp="0[0-9]{9}",message="Số điện thoại phải đủ 10 số, bắt đầu bằng 0.") String sdt,
        @PastOrPresent(message="Ngày sinh không được ở tương lai.") @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate ngaySinh,
        @Min(value=0,message="Chọn giới tính hợp lệ.") @Max(value=1,message="Chọn giới tính hợp lệ.") Integer gioiTinh) {}
