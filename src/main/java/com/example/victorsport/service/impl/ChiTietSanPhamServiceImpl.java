package com.example.victorsport.service.impl;

import com.example.victorsport.Entity.*;
import com.example.victorsport.dto.request.AnhChiTietSanPhamRequest;
import com.example.victorsport.dto.request.ChiTietSanPhamRequest;
import com.example.victorsport.dto.response.AnhChiTietResponse;
import com.example.victorsport.dto.response.ChiTietSanPhamResponse;
import com.example.victorsport.exception.ResourceNotFoundException;
import com.example.victorsport.repository.*;
import com.example.victorsport.service.ChiTietSanPhamService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ChiTietSanPhamServiceImpl implements ChiTietSanPhamService {

    private final ChiTietSanPhamRepository chiTietSanPhamRepository;
    private final SanPhamRepository sanPhamRepository;
    private final MauSacRepository mauSacRepository;
    private final KichThuocRepository kichThuocRepository;
    private final FormChanRepository formChanRepository;
    private final AnhChiTietSanPhamRepository anhChiTietSanPhamRepository;

    public ChiTietSanPhamServiceImpl(ChiTietSanPhamRepository chiTietSanPhamRepository,
                                   SanPhamRepository sanPhamRepository,
                                   MauSacRepository mauSacRepository,
                                   KichThuocRepository kichThuocRepository,
                                   FormChanRepository formChanRepository,
                                   AnhChiTietSanPhamRepository anhChiTietSanPhamRepository) {
        this.chiTietSanPhamRepository = chiTietSanPhamRepository;
        this.sanPhamRepository = sanPhamRepository;
        this.mauSacRepository = mauSacRepository;
        this.kichThuocRepository = kichThuocRepository;
        this.formChanRepository = formChanRepository;
        this.anhChiTietSanPhamRepository = anhChiTietSanPhamRepository;
    }

    @Override
    public Page<ChiTietSanPhamResponse> getAll(Pageable pageable) {
        return chiTietSanPhamRepository.findByXoaMemFalse(pageable).map(this::toResponse);
    }

    @Override
    public List<ChiTietSanPhamResponse> getBySanPhamId(Integer sanPhamId) {
        return chiTietSanPhamRepository.findBySanPhamIdAndXoaMemFalse(sanPhamId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public Page<ChiTietSanPhamResponse> filter(Integer sanPhamId, Integer mauSacId, Integer kichThuocId, Integer formChanId, Boolean trangThai, Pageable pageable) {
        return chiTietSanPhamRepository.filterChiTietSanPham(sanPhamId, mauSacId, kichThuocId, formChanId, trangThai, pageable)
                .map(this::toResponse);
    }

    @Override
    public ChiTietSanPhamResponse getById(Integer id) {
        ChiTietSanPham ct = chiTietSanPhamRepository.findByIdAndXoaMemFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chi tiết sản phẩm có ID: " + id));
        return toResponse(ct);
    }

    @Override
    @Transactional
    public ChiTietSanPhamResponse create(ChiTietSanPhamRequest request) {
        ChiTietSanPham ct = new ChiTietSanPham();
        mapRequestToEntity(request, ct);
        ct.setXoaMem(false);
        ct.setNgayTao(LocalDateTime.now());
        ct.setNguoiTao(request.getNguoiThaoTac());
        ChiTietSanPham saved = chiTietSanPhamRepository.save(ct);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public ChiTietSanPhamResponse update(Integer id, ChiTietSanPhamRequest request) {
        ChiTietSanPham ct = chiTietSanPhamRepository.findByIdAndXoaMemFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chi tiết sản phẩm có ID: " + id));
        mapRequestToEntity(request, ct);
        ct.setNgayCapNhat(LocalDateTime.now());
        ct.setNguoiCapNhat(request.getNguoiThaoTac());
        ChiTietSanPham saved = chiTietSanPhamRepository.save(ct);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public void delete(Integer id) {
        ChiTietSanPham ct = chiTietSanPhamRepository.findByIdAndXoaMemFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chi tiết sản phẩm có ID: " + id));
        ct.setXoaMem(true);
        chiTietSanPhamRepository.save(ct);
    }

    @Override
    @Transactional
    public AnhChiTietResponse themAnh(Integer idChiTietSanPham, AnhChiTietSanPhamRequest request) {
        ChiTietSanPham ct = chiTietSanPhamRepository.findByIdAndXoaMemFalse(idChiTietSanPham)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chi tiết sản phẩm có ID: " + idChiTietSanPham));

        AnhChiTietSanPham anh = new AnhChiTietSanPham();
        anh.setChiTietSanPham(ct);
        anh.setDuongDanAnh(request.getDuongDanAnh());
        anh.setLaAnhDaiDien(Boolean.TRUE.equals(request.getLaAnhDaiDien()));
        anh.setMoTa(request.getMoTa());
        anh.setXoaMem(false);

        AnhChiTietSanPham saved = anhChiTietSanPhamRepository.save(anh);
        return AnhChiTietResponse.builder()
                .id(saved.getId())
                .duongDanAnh(saved.getDuongDanAnh())
                .laAnhDaiDien(saved.getLaAnhDaiDien())
                .moTa(saved.getMoTa())
                .build();
    }

    @Override
    @Transactional
    public void xoaAnh(Integer idAnh) {
        AnhChiTietSanPham anh = anhChiTietSanPhamRepository.findById(idAnh)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy ảnh có ID: " + idAnh));
        anh.setXoaMem(true);
        anhChiTietSanPhamRepository.save(anh);
    }

    private void mapRequestToEntity(ChiTietSanPhamRequest request, ChiTietSanPham ct) {
        SanPham sp = sanPhamRepository.findByIdAndXoaMemFalse(request.getIdSanPham())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm ID: " + request.getIdSanPham()));
        MauSac ms = mauSacRepository.findById(request.getIdMauSac())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy màu sắc ID: " + request.getIdMauSac()));
        KichThuoc kt = kichThuocRepository.findById(request.getIdKichThuoc())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy kích thước ID: " + request.getIdKichThuoc()));
        FormChan fc = formChanRepository.findById(request.getIdFormChan())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy form chân ID: " + request.getIdFormChan()));

        ct.setSanPham(sp);
        ct.setMauSac(ms);
        ct.setKichThuoc(kt);
        ct.setFormChan(fc);
        ct.setSoLuong(request.getSoLuong());
        ct.setGiaNiemYet(request.getGiaNiemYet());
        ct.setGiaBan(request.getGiaBan() != null ? request.getGiaBan() : request.getGiaNiemYet());
        ct.setTrangThai(request.getTrangThai() != null ? request.getTrangThai() : true);
        ct.setGhiChu(request.getGhiChu());
    }

    private ChiTietSanPhamResponse toResponse(ChiTietSanPham ct) {
        List<AnhChiTietResponse> danhSachAnh = anhChiTietSanPhamRepository.findByChiTietSanPhamIdAndXoaMemFalse(ct.getId())
                .stream()
                .map(anh -> AnhChiTietResponse.builder()
                        .id(anh.getId())
                        .duongDanAnh(anh.getDuongDanAnh())
                        .laAnhDaiDien(anh.getLaAnhDaiDien())
                        .moTa(anh.getMoTa())
                        .build())
                .toList();

        return ChiTietSanPhamResponse.builder()
                .id(ct.getId())
                .maChiTietSanPham(ct.getMaChiTietSanPham())
                .idSanPham(ct.getSanPham() != null ? ct.getSanPham().getId() : null)
                .tenSanPham(ct.getSanPham() != null ? ct.getSanPham().getTenSanPham() : null)
                .idMauSac(ct.getMauSac() != null ? ct.getMauSac().getId() : null)
                .tenMauSac(ct.getMauSac() != null ? ct.getMauSac().getTenMauSac() : null)
                .maMauHex(ct.getMauSac() != null ? ct.getMauSac().getMaMauHex() : null)
                .idKichThuoc(ct.getKichThuoc() != null ? ct.getKichThuoc().getId() : null)
                .tenKichThuoc(ct.getKichThuoc() != null ? ct.getKichThuoc().getTenKichThuoc() : null)
                .giaTriKichThuoc(ct.getKichThuoc() != null ? ct.getKichThuoc().getGiaTriKichThuoc() : null)
                .idFormChan(ct.getFormChan() != null ? ct.getFormChan().getId() : null)
                .tenFormChan(ct.getFormChan() != null ? ct.getFormChan().getTenFormChan() : null)
                .soLuong(ct.getSoLuong())
                .giaNiemYet(ct.getGiaNiemYet())
                .giaBan(ct.getGiaBan())
                .trangThai(ct.getTrangThai())
                .ghiChu(ct.getGhiChu())
                .ngayTao(ct.getNgayTao())
                .ngayCapNhat(ct.getNgayCapNhat())
                .danhSachAnh(danhSachAnh)
                .build();
    }
}
