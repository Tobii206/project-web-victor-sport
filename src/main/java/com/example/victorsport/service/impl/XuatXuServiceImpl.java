package com.example.victorsport.service.impl;

import com.example.victorsport.Entity.XuatXu;
import com.example.victorsport.dto.request.XuatXuRequest;
import com.example.victorsport.exception.ResourceNotFoundException;
import com.example.victorsport.repository.XuatXuRepository;
import com.example.victorsport.service.XuatXuService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class XuatXuServiceImpl implements XuatXuService {

    private final XuatXuRepository repository;

    public XuatXuServiceImpl(XuatXuRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<XuatXu> getAll() {
        return repository.findByXoaMemFalse();
    }

    @Override
    public Page<XuatXu> getAll(Pageable pageable) {
        return repository.findByXoaMemFalse(pageable);
    }

    @Override
    public Page<XuatXu> search(String keyword, Pageable pageable) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return repository.findByXoaMemFalse(pageable);
        }
        return repository.findByTenXuatXuContainingIgnoreCaseAndXoaMemFalse(keyword.trim(), pageable);
    }

    @Override
    public XuatXu getById(Integer id) {
        return repository.findById(id)
                .filter(item -> !Boolean.TRUE.equals(item.getXoaMem()))
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy xuất xứ có ID: " + id));
    }

    @Override
    @Transactional
    public XuatXu create(XuatXuRequest request) {
        XuatXu item = new XuatXu();
        item.setTenXuatXu(request.getTenXuatXu());
        item.setTrangThai(request.getTrangThai() != null ? request.getTrangThai() : true);
        item.setXoaMem(false);
        return repository.save(item);
    }

    @Override
    @Transactional
    public XuatXu update(Integer id, XuatXuRequest request) {
        XuatXu item = getById(id);
        item.setTenXuatXu(request.getTenXuatXu());
        if (request.getTrangThai() != null) {
            item.setTrangThai(request.getTrangThai());
        }
        return repository.save(item);
    }

    @Override
    @Transactional
    public void delete(Integer id) {
        XuatXu item = getById(id);
        item.setXoaMem(true);
        repository.save(item);
    }
}
