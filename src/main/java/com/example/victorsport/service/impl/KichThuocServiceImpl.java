package com.example.victorsport.service.impl;

import com.example.victorsport.Entity.KichThuoc;
import com.example.victorsport.dto.request.KichThuocRequest;
import com.example.victorsport.exception.ResourceNotFoundException;
import com.example.victorsport.repository.KichThuocRepository;
import com.example.victorsport.service.KichThuocService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class KichThuocServiceImpl implements KichThuocService {

    private final KichThuocRepository repository;

    public KichThuocServiceImpl(KichThuocRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<KichThuoc> getAll() {
        return repository.findByXoaMemFalse();
    }

    @Override
    public Page<KichThuoc> getAll(Pageable pageable) {
        return repository.findByXoaMemFalse(pageable);
    }

    @Override
    public Page<KichThuoc> search(String keyword, Pageable pageable) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return repository.findByXoaMemFalse(pageable);
        }
        return repository.findByTenKichThuocContainingIgnoreCaseAndXoaMemFalse(keyword.trim(), pageable);
    }

    @Override
    public KichThuoc getById(Integer id) {
        return repository.findById(id)
                .filter(item -> !Boolean.TRUE.equals(item.getXoaMem()))
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy kích thước có ID: " + id));
    }

    @Override
    @Transactional
    public KichThuoc create(KichThuocRequest request) {
        KichThuoc item = new KichThuoc();
        item.setTenKichThuoc(request.getTenKichThuoc());
        item.setGiaTriKichThuoc(request.getGiaTriKichThuoc());
        item.setTrangThai(request.getTrangThai() != null ? request.getTrangThai() : true);
        item.setXoaMem(false);
        return repository.save(item);
    }

    @Override
    @Transactional
    public KichThuoc update(Integer id, KichThuocRequest request) {
        KichThuoc item = getById(id);
        item.setTenKichThuoc(request.getTenKichThuoc());
        item.setGiaTriKichThuoc(request.getGiaTriKichThuoc());
        if (request.getTrangThai() != null) {
            item.setTrangThai(request.getTrangThai());
        }
        return repository.save(item);
    }

    @Override
    @Transactional
    public void delete(Integer id) {
        KichThuoc item = getById(id);
        item.setXoaMem(true);
        repository.save(item);
    }
}
