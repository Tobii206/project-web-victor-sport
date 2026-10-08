package com.example.victorsport.service;

import com.example.victorsport.Entity.ViTriThiDau;
import com.example.victorsport.dto.request.ViTriThiDauRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ViTriThiDauService {
    List<ViTriThiDau> getAll();
    Page<ViTriThiDau> getAll(Pageable pageable);
    Page<ViTriThiDau> search(String keyword, Pageable pageable);
    ViTriThiDau getById(Integer id);
    ViTriThiDau create(ViTriThiDauRequest request);
    ViTriThiDau update(Integer id, ViTriThiDauRequest request);
    void delete(Integer id);
}
