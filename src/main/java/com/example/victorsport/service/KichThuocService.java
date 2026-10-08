package com.example.victorsport.service;

import com.example.victorsport.Entity.KichThuoc;
import com.example.victorsport.dto.request.KichThuocRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface KichThuocService {
    List<KichThuoc> getAll();
    Page<KichThuoc> getAll(Pageable pageable);
    Page<KichThuoc> search(String keyword, Pageable pageable);
    KichThuoc getById(Integer id);
    KichThuoc create(KichThuocRequest request);
    KichThuoc update(Integer id, KichThuocRequest request);
    void delete(Integer id);
}
