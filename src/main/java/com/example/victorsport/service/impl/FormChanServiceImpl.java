package com.example.victorsport.service.impl;

import com.example.victorsport.Entity.FormChan;
import com.example.victorsport.dto.request.FormChanRequest;
import com.example.victorsport.exception.ResourceNotFoundException;
import com.example.victorsport.repository.FormChanRepository;
import com.example.victorsport.service.FormChanService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FormChanServiceImpl implements FormChanService {

    private final FormChanRepository repository;

    public FormChanServiceImpl(FormChanRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<FormChan> getAll() {
        return repository.findByXoaMemFalse();
    }

    @Override
    public Page<FormChan> getAll(Pageable pageable) {
        return repository.findByXoaMemFalse(pageable);
    }

    @Override
    public Page<FormChan> search(String keyword, Pageable pageable) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return repository.findByXoaMemFalse(pageable);
        }
        return repository.findByTenFormChanContainingIgnoreCaseAndXoaMemFalse(keyword.trim(), pageable);
    }

    @Override
    public FormChan getById(Integer id) {
        return repository.findById(id)
                .filter(item -> !Boolean.TRUE.equals(item.getXoaMem()))
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy form chân có ID: " + id));
    }

    @Override
    @Transactional
    public FormChan create(FormChanRequest request) {
        FormChan item = new FormChan();
        item.setTenFormChan(request.getTenFormChan());
        item.setTrangThai(request.getTrangThai() != null ? request.getTrangThai() : true);
        item.setXoaMem(false);
        return repository.save(item);
    }

    @Override
    @Transactional
    public FormChan update(Integer id, FormChanRequest request) {
        FormChan item = getById(id);
        item.setTenFormChan(request.getTenFormChan());
        if (request.getTrangThai() != null) {
            item.setTrangThai(request.getTrangThai());
        }
        return repository.save(item);
    }

    @Override
    @Transactional
    public void delete(Integer id) {
        FormChan item = getById(id);
        item.setXoaMem(true);
        repository.save(item);
    }
}
