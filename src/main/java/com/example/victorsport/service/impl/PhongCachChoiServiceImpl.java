package com.example.victorsport.service.impl;

import com.example.victorsport.Entity.PhongCachChoi;
import com.example.victorsport.dto.request.PhongCachChoiRequest;
import com.example.victorsport.exception.ResourceNotFoundException;
import com.example.victorsport.repository.PhongCachChoiRepository;
import com.example.victorsport.service.PhongCachChoiService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PhongCachChoiServiceImpl implements PhongCachChoiService {

    private final PhongCachChoiRepository repository;

    public PhongCachChoiServiceImpl(PhongCachChoiRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<PhongCachChoi> getAll() {
        return repository.findByXoaMemFalse();
    }

    @Override
    public Page<PhongCachChoi> getAll(Pageable pageable) {
        return repository.findByXoaMemFalse(pageable);
    }

    @Override
    public Page<PhongCachChoi> search(String keyword, Pageable pageable) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return repository.findByXoaMemFalse(pageable);
        }
        return repository.findByTenPhongCachContainingIgnoreCaseAndXoaMemFalse(keyword.trim(), pageable);
    }

    @Override
    public PhongCachChoi getById(Integer id) {
        return repository.findById(id)
                .filter(item -> !Boolean.TRUE.equals(item.getXoaMem()))
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phong cách chơi có ID: " + id));
    }

    @Override
    @Transactional
    public PhongCachChoi create(PhongCachChoiRequest request) {
        PhongCachChoi item = new PhongCachChoi();
        item.setTenPhongCach(request.getTenPhongCach());
        item.setTrangThai(request.getTrangThai() != null ? request.getTrangThai() : true);
        item.setXoaMem(false);
        return repository.save(item);
    }

    @Override
    @Transactional
    public PhongCachChoi update(Integer id, PhongCachChoiRequest request) {
        PhongCachChoi item = getById(id);
        item.setTenPhongCach(request.getTenPhongCach());
        if (request.getTrangThai() != null) {
            item.setTrangThai(request.getTrangThai());
        }
        return repository.save(item);
    }

    @Override
    @Transactional
    public void delete(Integer id) {
        PhongCachChoi item = getById(id);
        item.setXoaMem(true);
        repository.save(item);
    }
}
