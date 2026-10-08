package com.example.victorsport.service.impl;

import com.example.victorsport.Entity.MauSac;
import com.example.victorsport.dto.request.MauSacRequest;
import com.example.victorsport.exception.ResourceNotFoundException;
import com.example.victorsport.repository.MauSacRepository;
import com.example.victorsport.service.MauSacService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MauSacServiceImpl implements MauSacService {

    private final MauSacRepository repository;

    public MauSacServiceImpl(MauSacRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<MauSac> getAll() {
        return repository.findByXoaMemFalse();
    }

    @Override
    public Page<MauSac> getAll(Pageable pageable) {
        return repository.findByXoaMemFalse(pageable);
    }

    @Override
    public Page<MauSac> search(String keyword, Pageable pageable) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return repository.findByXoaMemFalse(pageable);
        }
        return repository.findByTenMauSacContainingIgnoreCaseAndXoaMemFalse(keyword.trim(), pageable);
    }

    @Override
    public MauSac getById(Integer id) {
        return repository.findById(id)
                .filter(item -> !Boolean.TRUE.equals(item.getXoaMem()))
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy màu sắc có ID: " + id));
    }

    @Override
    @Transactional
    public MauSac create(MauSacRequest request) {
        MauSac item = new MauSac();
        item.setTenMauSac(request.getTenMauSac());
        item.setMaMauHex(request.getMaMauHex());
        item.setTrangThai(request.getTrangThai() != null ? request.getTrangThai() : true);
        item.setXoaMem(false);
        return repository.save(item);
    }

    @Override
    @Transactional
    public MauSac update(Integer id, MauSacRequest request) {
        MauSac item = getById(id);
        item.setTenMauSac(request.getTenMauSac());
        item.setMaMauHex(request.getMaMauHex());
        if (request.getTrangThai() != null) {
            item.setTrangThai(request.getTrangThai());
        }
        return repository.save(item);
    }

    @Override
    @Transactional
    public void delete(Integer id) {
        MauSac item = getById(id);
        item.setXoaMem(true);
        repository.save(item);
    }
}
