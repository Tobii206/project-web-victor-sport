package com.example.victorsport.service.impl;

import com.example.victorsport.Entity.ThuongHieu;
import com.example.victorsport.dto.request.ThuongHieuRequest;
import com.example.victorsport.exception.ResourceNotFoundException;
import com.example.victorsport.repository.ThuongHieuRepository;
import com.example.victorsport.service.ThuongHieuService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ThuongHieuServiceImpl implements ThuongHieuService {

    private final ThuongHieuRepository repository;

    public ThuongHieuServiceImpl(ThuongHieuRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<ThuongHieu> getAll() {
        return repository.findByXoaMemFalse();
    }

    @Override
    public Page<ThuongHieu> getAll(Pageable pageable) {
        return repository.findByXoaMemFalse(pageable);
    }

    @Override
    public Page<ThuongHieu> search(String keyword, Pageable pageable) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return repository.findByXoaMemFalse(pageable);
        }
        return repository.findByTenThuongHieuContainingIgnoreCaseAndXoaMemFalse(keyword.trim(), pageable);
    }

    @Override
    public ThuongHieu getById(Integer id) {
        return repository.findById(id)
                .filter(item -> !Boolean.TRUE.equals(item.getXoaMem()))
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thương hiệu có ID: " + id));
    }

    @Override
    @Transactional
    public ThuongHieu create(ThuongHieuRequest request) {
        ThuongHieu item = new ThuongHieu();
        item.setTenThuongHieu(request.getTenThuongHieu());
        item.setTrangThai(request.getTrangThai() != null ? request.getTrangThai() : true);
        item.setXoaMem(false);
        return repository.save(item);
    }

    @Override
    @Transactional
    public ThuongHieu update(Integer id, ThuongHieuRequest request) {
        ThuongHieu item = getById(id);
        item.setTenThuongHieu(request.getTenThuongHieu());
        if (request.getTrangThai() != null) {
            item.setTrangThai(request.getTrangThai());
        }
        return repository.save(item);
    }

    @Override
    @Transactional
    public void delete(Integer id) {
        ThuongHieu item = getById(id);
        item.setXoaMem(true);
        repository.save(item);
    }
}
