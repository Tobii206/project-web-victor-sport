package com.example.victorsport.service;

import com.example.victorsport.Entity.MauSac;
import com.example.victorsport.dto.request.MauSacRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface MauSacService {
    List<MauSac> getAll();
    Page<MauSac> getAll(Pageable pageable);
    Page<MauSac> search(String keyword, Pageable pageable);
    MauSac getById(Integer id);
    MauSac create(MauSacRequest request);
    MauSac update(Integer id, MauSacRequest request);
    void delete(Integer id);
}
