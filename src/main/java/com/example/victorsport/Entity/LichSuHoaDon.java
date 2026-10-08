package com.example.victorsport.Entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
@Getter @Setter
@Entity
@Table(name = "lich_su_hoa_don")
public class LichSuHoaDon {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_hoa_don", nullable = false)
    private HoaDon hoaDon;

    @Column(name = "trang_thai")
    private Integer trangThai;

    @Column(name = "thoi_gian")
    private LocalDateTime thoiGian;

    @Column(name = "ghi_chu")
    private String ghiChu;

    @Column(name = "xoa_mem")
    private Boolean xoaMem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nguoi_cap_nhat")
    private NhanVien nguoiCapNhat;

    @Column(name = "nguoi_thuc_hien")
    private Integer nguoiThucHien;

    @Column(name = "loai_nguoi_thuc_hien")
    private String loaiNguoiThucHien;
}
