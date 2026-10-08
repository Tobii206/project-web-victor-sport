package com.example.victorsport.service;

import com.example.victorsport.dto.request.AnhChiTietSanPhamRequest;
import com.example.victorsport.dto.request.ChiTietSanPhamRequest;
import com.example.victorsport.dto.response.AnhChiTietResponse;
import com.example.victorsport.dto.response.ChiTietSanPhamResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ChiTietSanPhamService {
    Page<ChiTietSanPhamResponse> getAll(Pageable pageable);
    List<ChiTietSanPhamResponse> getBySanPhamId(Integer sanPhamId);
    Page<ChiTietSanPhamResponse> filter(Integer sanPhamId, Integer mauSacId, Integer kichThuocId, Integer formChanId, Boolean trangThai, Pageable pageable);
    ChiTietSanPhamResponse getById(Integer id);
    ChiTietSanPhamResponse create(ChiTietSanPhamRequest request);
    ChiTietSanPhamResponse update(Integer id, ChiTietSanPhamRequest request);
    void delete(Integer id);

    AnhChiTietResponse themAnh(Integer idChiTietSanPham, AnhChiTietSanPhamRequest request);
    void xoaAnh(Integer idAnh);
}
