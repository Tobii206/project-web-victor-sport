package com.example.victorsport.Dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;

public class DotGiamGiaForm {
    @DecimalMin(value = "0", message = "Tối đa được giảm không được âm.")
    @Digits(integer = 16, fraction = 2, message = "Số tiền tối đa 16 chữ số và 2 chữ số thập phân.")
    private BigDecimal so_tien_giam_toi_da;

    public BigDecimal getSo_tien_giam_toi_da() { return so_tien_giam_toi_da; }
    public void setSo_tien_giam_toi_da(BigDecimal so_tien_giam_toi_da) { this.so_tien_giam_toi_da = so_tien_giam_toi_da; }

    @NotBlank(message = "Vui lòng nhập tên đợt giảm giá.")
    @Size(max = 255, message = "Tên đợt giảm giá tối đa 255 ký tự.")
    private String ten_dot_giam_gia;

    @NotNull(message = "Vui lòng chọn loại hoặc trạng thái.")
    @Min(value = 0, message = "Lựa chọn không hợp lệ.")
    @Max(value = 1, message = "Lựa chọn không hợp lệ.")
    private Integer loai_giam_gia;

    @NotNull(message = "Vui lòng nhập giá trị giảm.")
    @DecimalMin(value = "0", inclusive = false, message = "Giá trị giảm phải lớn hơn 0.")
    @Digits(integer = 16, fraction = 2, message = "Số tiền tối đa 16 chữ số và 2 chữ số thập phân.")
    private BigDecimal gia_tri_giam_gia;

    @NotNull(message = "Vui lòng chọn ngày bắt đầu.")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate ngay_bat_dau;

    @NotNull(message = "Vui lòng chọn ngày kết thúc.")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate ngay_ket_thuc;

    @NotNull(message = "Vui lòng chọn loại hoặc trạng thái.")
    @Min(value = 0, message = "Lựa chọn không hợp lệ.")
    @Max(value = 1, message = "Lựa chọn không hợp lệ.")
    private Integer trang_thai;

    @NotNull(message = "Vui lòng nhập mức ưu tiên.")
    @Min(value = 0, message = "Mức ưu tiên không được âm.")
    private Integer muc_uu_tien;

    public String getTen_dot_giam_gia() { return ten_dot_giam_gia; }
    public void setTen_dot_giam_gia(String ten_dot_giam_gia) {
        this.ten_dot_giam_gia = ten_dot_giam_gia == null ? null : ten_dot_giam_gia.trim();
    }

    public Integer getLoai_giam_gia() { return loai_giam_gia; }
    public void setLoai_giam_gia(Integer loai_giam_gia) {
        this.loai_giam_gia = loai_giam_gia;
    }

    public BigDecimal getGia_tri_giam_gia() { return gia_tri_giam_gia; }
    public void setGia_tri_giam_gia(BigDecimal gia_tri_giam_gia) {
        this.gia_tri_giam_gia = gia_tri_giam_gia;
    }

    public LocalDate getNgay_bat_dau() { return ngay_bat_dau; }
    public void setNgay_bat_dau(LocalDate ngay_bat_dau) {
        this.ngay_bat_dau = ngay_bat_dau;
    }

    public LocalDate getNgay_ket_thuc() { return ngay_ket_thuc; }
    public void setNgay_ket_thuc(LocalDate ngay_ket_thuc) {
        this.ngay_ket_thuc = ngay_ket_thuc;
    }

    public Integer getTrang_thai() { return trang_thai; }
    public void setTrang_thai(Integer trang_thai) {
        this.trang_thai = trang_thai;
    }

    public Integer getMuc_uu_tien() { return muc_uu_tien; }
    public void setMuc_uu_tien(Integer muc_uu_tien) {
        this.muc_uu_tien = muc_uu_tien;
    }

}
