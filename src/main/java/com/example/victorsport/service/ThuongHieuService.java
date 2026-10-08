package com.example.victorsport.service;

import com.example.victorsport.Entity.ThuongHieu;
import com.example.victorsport.dto.request.ThuongHieuRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ThuongHieuService {
    List<ThuongHieu> getAll();
    Page<ThuongHieu> getAll(Pageable pageable);
    Page<ThuongHieu> search(String keyword, Pageable pageable);
    ThuongHieu getById(Integer id);
    ThuongHieu create(ThuongHieuRequest request);
    ThuongHieu update(Integer id, ThuongHieuRequest request);
    void delete(Integer id);
}
