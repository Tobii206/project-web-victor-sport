package com.example.victorsport.service;

import com.example.victorsport.dto.request.SanPhamRequest;
import com.example.victorsport.dto.response.SanPhamResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SanPhamService {
    Page<SanPhamResponse> getAll(Pageable pageable);
    Page<SanPhamResponse> filter(String keyword, Integer thuongHieuId, Boolean trangThai, Pageable pageable);
    SanPhamResponse getById(Integer id);
    SanPhamResponse create(SanPhamRequest request);
    SanPhamResponse update(Integer id, SanPhamRequest request);
    void delete(Integer id);
}
