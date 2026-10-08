package com.example.victorsport.service.impl;

import com.example.victorsport.Entity.ViTriThiDau;
import com.example.victorsport.dto.request.ViTriThiDauRequest;
import com.example.victorsport.exception.ResourceNotFoundException;
import com.example.victorsport.repository.ViTriThiDauRepository;
import com.example.victorsport.service.ViTriThiDauService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ViTriThiDauServiceImpl implements ViTriThiDauService {

    private final ViTriThiDauRepository repository;

    public ViTriThiDauServiceImpl(ViTriThiDauRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<ViTriThiDau> getAll() {
        return repository.findByXoaMemFalse();
    }

    @Override
    public Page<ViTriThiDau> getAll(Pageable pageable) {
        return repository.findByXoaMemFalse(pageable);
    }

    @Override
    public Page<ViTriThiDau> search(String keyword, Pageable pageable) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return repository.findByXoaMemFalse(pageable);
        }
        return repository.findByTenViTriContainingIgnoreCaseAndXoaMemFalse(keyword.trim(), pageable);
    }

    @Override
    public ViTriThiDau getById(Integer id) {
        return repository.findById(id)
                .filter(item -> !Boolean.TRUE.equals(item.getXoaMem()))
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy vị trí thi đấu có ID: " + id));
    }

    @Override
    @Transactional
    public ViTriThiDau create(ViTriThiDauRequest request) {
        ViTriThiDau item = new ViTriThiDau();
        item.setTenViTri(request.getTenViTri());
        item.setTrangThai(request.getTrangThai() != null ? request.getTrangThai() : true);
        item.setXoaMem(false);
        return repository.save(item);
    }

    @Override
    @Transactional
    public ViTriThiDau update(Integer id, ViTriThiDauRequest request) {
        ViTriThiDau item = getById(id);
        item.setTenViTri(request.getTenViTri());
        if (request.getTrangThai() != null) {
            item.setTrangThai(request.getTrangThai());
        }
        return repository.save(item);
    }

    @Override
    @Transactional
    public void delete(Integer id) {
        ViTriThiDau item = getById(id);
        item.setXoaMem(true);
        repository.save(item);
    }
}
