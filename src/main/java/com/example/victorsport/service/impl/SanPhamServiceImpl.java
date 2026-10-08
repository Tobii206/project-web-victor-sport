package com.example.victorsport.service.impl;

import com.example.victorsport.Entity.*;
import com.example.victorsport.dto.request.SanPhamRequest;
import com.example.victorsport.dto.response.SanPhamResponse;
import com.example.victorsport.exception.ResourceNotFoundException;
import com.example.victorsport.repository.*;
import com.example.victorsport.service.SanPhamService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class SanPhamServiceImpl implements SanPhamService {

    private final SanPhamRepository sanPhamRepository;
    private final ThuongHieuRepository thuongHieuRepository;
    private final XuatXuRepository xuatXuRepository;
    private final ViTriThiDauRepository viTriThiDauRepository;
    private final PhongCachChoiRepository phongCachChoiRepository;
    private final CoGiayRepository coGiayRepository;
    private final ChatLieuRepository chatLieuRepository;

    public SanPhamServiceImpl(SanPhamRepository sanPhamRepository,
                             ThuongHieuRepository thuongHieuRepository,
                             XuatXuRepository xuatXuRepository,
                             ViTriThiDauRepository viTriThiDauRepository,
                             PhongCachChoiRepository phongCachChoiRepository,
                             CoGiayRepository coGiayRepository,
                             ChatLieuRepository chatLieuRepository) {
        this.sanPhamRepository = sanPhamRepository;
        this.thuongHieuRepository = thuongHieuRepository;
        this.xuatXuRepository = xuatXuRepository;
        this.viTriThiDauRepository = viTriThiDauRepository;
        this.phongCachChoiRepository = phongCachChoiRepository;
        this.coGiayRepository = coGiayRepository;
        this.chatLieuRepository = chatLieuRepository;
    }

    @Override
    public Page<SanPhamResponse> getAll(Pageable pageable) {
        return sanPhamRepository.findByXoaMemFalse(pageable).map(this::toResponse);
    }

    @Override
    public Page<SanPhamResponse> filter(String keyword, Integer thuongHieuId, Boolean trangThai, Pageable pageable) {
        return sanPhamRepository.filterSanPham(keyword, thuongHieuId, trangThai, pageable).map(this::toResponse);
    }

    @Override
    public SanPhamResponse getById(Integer id) {
        SanPham sp = sanPhamRepository.findByIdAndXoaMemFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm có ID: " + id));
        return toResponse(sp);
    }

    @Override
    @Transactional
    public SanPhamResponse create(SanPhamRequest request) {
        SanPham sp = new SanPham();
        mapRequestToEntity(request, sp);
        sp.setXoaMem(false);
        sp.setNgayTao(LocalDateTime.now());
        sp.setNguoiTao(request.getNguoiThaoTac());
        SanPham saved = sanPhamRepository.save(sp);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public SanPhamResponse update(Integer id, SanPhamRequest request) {
        SanPham sp = sanPhamRepository.findByIdAndXoaMemFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm có ID: " + id));
        mapRequestToEntity(request, sp);
        sp.setNgayCapNhat(LocalDateTime.now());
        sp.setNguoiCapNhat(request.getNguoiThaoTac());
        SanPham saved = sanPhamRepository.save(sp);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public void delete(Integer id) {
        SanPham sp = sanPhamRepository.findByIdAndXoaMemFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm có ID: " + id));
        sp.setXoaMem(true);
        sanPhamRepository.save(sp);
    }

    private void mapRequestToEntity(SanPhamRequest request, SanPham sp) {
        sp.setTenSanPham(request.getTenSanPham());
        sp.setMoTaNgan(request.getMoTaNgan());
        sp.setMoTaChiTiet(request.getMoTaChiTiet());
        sp.setTrangThaiKinhDoanh(request.getTrangThaiKinhDoanh() != null ? request.getTrangThaiKinhDoanh() : true);

        if (request.getIdThuongHieu() != null) {
            ThuongHieu th = thuongHieuRepository.findById(request.getIdThuongHieu())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thương hiệu ID: " + request.getIdThuongHieu()));
            sp.setThuongHieu(th);
        }

        if (request.getIdXuatXu() != null) {
            sp.setXuatXu(xuatXuRepository.findById(request.getIdXuatXu()).orElse(null));
        } else {
            sp.setXuatXu(null);
        }

        if (request.getIdViTriThiDau() != null) {
            sp.setViTriThiDau(viTriThiDauRepository.findById(request.getIdViTriThiDau()).orElse(null));
        } else {
            sp.setViTriThiDau(null);
        }

        if (request.getIdPhongCachChoi() != null) {
            sp.setPhongCachChoi(phongCachChoiRepository.findById(request.getIdPhongCachChoi()).orElse(null));
        } else {
            sp.setPhongCachChoi(null);
        }

        if (request.getIdCoGiay() != null) {
            sp.setCoGiay(coGiayRepository.findById(request.getIdCoGiay()).orElse(null));
        } else {
            sp.setCoGiay(null);
        }

        if (request.getIdChatLieu() != null) {
            sp.setChatLieu(chatLieuRepository.findById(request.getIdChatLieu()).orElse(null));
        } else {
            sp.setChatLieu(null);
        }
    }

    private SanPhamResponse toResponse(SanPham sp) {
        return SanPhamResponse.builder()
                .id(sp.getId())
                .maSanPham(sp.getMaSanPham())
                .tenSanPham(sp.getTenSanPham())
                .idThuongHieu(sp.getThuongHieu() != null ? sp.getThuongHieu().getId() : null)
                .tenThuongHieu(sp.getThuongHieu() != null ? sp.getThuongHieu().getTenThuongHieu() : null)
                .idXuatXu(sp.getXuatXu() != null ? sp.getXuatXu().getId() : null)
                .tenXuatXu(sp.getXuatXu() != null ? sp.getXuatXu().getTenXuatXu() : null)
                .idViTriThiDau(sp.getViTriThiDau() != null ? sp.getViTriThiDau().getId() : null)
                .tenViTriThiDau(sp.getViTriThiDau() != null ? sp.getViTriThiDau().getTenViTri() : null)
                .idPhongCachChoi(sp.getPhongCachChoi() != null ? sp.getPhongCachChoi().getId() : null)
                .tenPhongCachChoi(sp.getPhongCachChoi() != null ? sp.getPhongCachChoi().getTenPhongCach() : null)
                .idCoGiay(sp.getCoGiay() != null ? sp.getCoGiay().getId() : null)
                .tenCoGiay(sp.getCoGiay() != null ? sp.getCoGiay().getTenCoGiay() : null)
                .idChatLieu(sp.getChatLieu() != null ? sp.getChatLieu().getId() : null)
                .tenChatLieu(sp.getChatLieu() != null ? sp.getChatLieu().getTenChatLieu() : null)
                .moTaNgan(sp.getMoTaNgan())
                .moTaChiTiet(sp.getMoTaChiTiet())
                .trangThaiKinhDoanh(sp.getTrangThaiKinhDoanh())
                .ngayTao(sp.getNgayTao())
                .ngayCapNhat(sp.getNgayCapNhat())
                .build();
    }
}
