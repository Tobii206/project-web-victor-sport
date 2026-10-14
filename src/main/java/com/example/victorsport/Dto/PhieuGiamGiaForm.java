package com.example.victorsport.Dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;

public class PhieuGiamGiaForm {
    @Min(value = 0, message = "Số lượt sử dụng cũ không được âm.")
    private Integer so_luong_cu;

    public Integer getSo_luong_cu() { return so_luong_cu; }
    public void setSo_luong_cu(Integer so_luong_cu) { this.so_luong_cu = so_luong_cu; }

    @NotBlank(message = "Vui lòng nhập tên phiếu giảm giá.")
    @Size(max = 255, message = "Tên phiếu tối đa 255 ký tự.")
    private String ten_phieu_giam_gia;

    @NotNull(message = "Vui lòng chọn loại hoặc trạng thái.")
    @Min(value = 0, message = "Lựa chọn không hợp lệ.")
    @Max(value = 1, message = "Lựa chọn không hợp lệ.")
    private Integer loai_phieu_giam_gia;

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

    @DecimalMin(value = "0", inclusive = true, message = "Số tiền không được âm.")
    @Digits(integer = 16, fraction = 2, message = "Số tiền tối đa 16 chữ số và 2 chữ số thập phân.")
    private BigDecimal so_tien_giam_toi_da;

    @DecimalMin(value = "0", inclusive = true, message = "Số tiền không được âm.")
    @Digits(integer = 16, fraction = 2, message = "Số tiền tối đa 16 chữ số và 2 chữ số thập phân.")
    private BigDecimal hoa_don_toi_thieu;

    @NotNull(message = "Vui lòng nhập số lượt sử dụng còn lại.")
    @Min(value = 0, message = "Số lượt sử dụng không được âm.")
    private Integer so_luong_su_dung;

    @Size(max = 255, message = "Mô tả tối đa 255 ký tự.")
    private String mo_ta;

    public String getTen_phieu_giam_gia() { return ten_phieu_giam_gia; }
    public void setTen_phieu_giam_gia(String ten_phieu_giam_gia) {
        this.ten_phieu_giam_gia = ten_phieu_giam_gia == null ? null : ten_phieu_giam_gia.trim();
    }

    public Integer getLoai_phieu_giam_gia() { return loai_phieu_giam_gia; }
    public void setLoai_phieu_giam_gia(Integer loai_phieu_giam_gia) {
        this.loai_phieu_giam_gia = loai_phieu_giam_gia;
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

    public BigDecimal getSo_tien_giam_toi_da() { return so_tien_giam_toi_da; }
    public void setSo_tien_giam_toi_da(BigDecimal so_tien_giam_toi_da) {
        this.so_tien_giam_toi_da = so_tien_giam_toi_da;
    }

    public BigDecimal getHoa_don_toi_thieu() { return hoa_don_toi_thieu; }
    public void setHoa_don_toi_thieu(BigDecimal hoa_don_toi_thieu) {
        this.hoa_don_toi_thieu = hoa_don_toi_thieu;
    }

    public Integer getSo_luong_su_dung() { return so_luong_su_dung; }
    public void setSo_luong_su_dung(Integer so_luong_su_dung) {
        this.so_luong_su_dung = so_luong_su_dung;
    }

    public String getMo_ta() { return mo_ta; }
    public void setMo_ta(String mo_ta) {
        this.mo_ta = mo_ta == null ? null : mo_ta.trim();
    }

}
