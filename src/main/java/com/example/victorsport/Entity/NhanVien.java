package com.example.victorsport.Entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
@Getter @Setter
@Entity
@Table(name = "nhan_vien")
public class NhanVien {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_quyen_han", nullable = false)
    private QuyenHan quyenHan;

    @Column(name = "ma_nhan_vien", insertable = false, updatable = false)
    private String maNhanVien;

    @Column(name = "ten_nhan_vien", nullable = false)
    private String tenNhanVien;

    @Column(name = "ten_tai_khoan", nullable = false)
    private String tenTaiKhoan;

    @Column(name = "mat_khau", nullable = false)
    private String matKhau;

    private String email;

    @Column(name = "so_dien_thoai")
    private String soDienThoai;

    @Column(name = "anh_nhan_vien")
    private String anhNhanVien;

    @Column(name = "ngay_sinh")
    private LocalDate ngaySinh;

    @Column(name = "ghi_chu")
    private String ghiChu;

    @Column(name = "thanh_pho")
    private String thanhPho;

    private String quan;
    private String phuong;

    @Column(name = "dia_chi_cu_the")
    private String diaChiCuThe;

    @Column(name = "trang_thai")
    private Boolean trangThai;

    @Column(name = "xoa_mem")
    private Boolean xoaMem;

    @Column(name = "ngay_tao")
    private LocalDateTime ngayTao;

    @Column(name = "nguoi_tao")
    private Integer nguoiTao;

    @Column(name = "ngay_cap_nhat")
    private LocalDateTime ngayCapNhat;

    @Column(name = "nguoi_cap_nhat")
    private Integer nguoiCapNhat;
}
