package com.example.victorsport.service;

import com.example.victorsport.Entity.XuatXu;
import com.example.victorsport.dto.request.XuatXuRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface XuatXuService {
    List<XuatXu> getAll();
    Page<XuatXu> getAll(Pageable pageable);
    Page<XuatXu> search(String keyword, Pageable pageable);
    XuatXu getById(Integer id);
    XuatXu create(XuatXuRequest request);
    XuatXu update(Integer id, XuatXuRequest request);
    void delete(Integer id);
}
