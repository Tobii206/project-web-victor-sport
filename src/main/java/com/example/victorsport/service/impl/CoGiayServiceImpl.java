package com.example.victorsport.service.impl;

import com.example.victorsport.Entity.CoGiay;
import com.example.victorsport.dto.request.CoGiayRequest;
import com.example.victorsport.exception.ResourceNotFoundException;
import com.example.victorsport.repository.CoGiayRepository;
import com.example.victorsport.service.CoGiayService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CoGiayServiceImpl implements CoGiayService {

    private final CoGiayRepository repository;

    public CoGiayServiceImpl(CoGiayRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<CoGiay> getAll() {
        return repository.findByXoaMemFalse();
    }

    @Override
    public Page<CoGiay> getAll(Pageable pageable) {
        return repository.findByXoaMemFalse(pageable);
    }

    @Override
    public Page<CoGiay> search(String keyword, Pageable pageable) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return repository.findByXoaMemFalse(pageable);
        }
        return repository.findByTenCoGiayContainingIgnoreCaseAndXoaMemFalse(keyword.trim(), pageable);
    }

    @Override
    public CoGiay getById(Integer id) {
        return repository.findById(id)
                .filter(item -> !Boolean.TRUE.equals(item.getXoaMem()))
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy cổ giày có ID: " + id));
    }

    @Override
    @Transactional
    public CoGiay create(CoGiayRequest request) {
        CoGiay item = new CoGiay();
        item.setTenCoGiay(request.getTenCoGiay());
        item.setTrangThai(request.getTrangThai() != null ? request.getTrangThai() : true);
        item.setXoaMem(false);
        return repository.save(item);
    }

    @Override
    @Transactional
    public CoGiay update(Integer id, CoGiayRequest request) {
        CoGiay item = getById(id);
        item.setTenCoGiay(request.getTenCoGiay());
        if (request.getTrangThai() != null) {
            item.setTrangThai(request.getTrangThai());
        }
        return repository.save(item);
    }

    @Override
    @Transactional
    public void delete(Integer id) {
        CoGiay item = getById(id);
        item.setXoaMem(true);
        repository.save(item);
    }
}
