package com.example.victorsport.service;

import com.example.victorsport.Entity.CoGiay;
import com.example.victorsport.dto.request.CoGiayRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CoGiayService {
    List<CoGiay> getAll();
    Page<CoGiay> getAll(Pageable pageable);
    Page<CoGiay> search(String keyword, Pageable pageable);
    CoGiay getById(Integer id);
    CoGiay create(CoGiayRequest request);
    CoGiay update(Integer id, CoGiayRequest request);
    void delete(Integer id);
}
