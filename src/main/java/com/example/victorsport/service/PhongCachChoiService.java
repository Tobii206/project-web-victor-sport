package com.example.victorsport.service;

import com.example.victorsport.Entity.PhongCachChoi;
import com.example.victorsport.dto.request.PhongCachChoiRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PhongCachChoiService {
    List<PhongCachChoi> getAll();
    Page<PhongCachChoi> getAll(Pageable pageable);
    Page<PhongCachChoi> search(String keyword, Pageable pageable);
    PhongCachChoi getById(Integer id);
    PhongCachChoi create(PhongCachChoiRequest request);
    PhongCachChoi update(Integer id, PhongCachChoiRequest request);
    void delete(Integer id);
}
