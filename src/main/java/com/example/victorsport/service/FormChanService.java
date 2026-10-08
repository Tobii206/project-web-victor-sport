package com.example.victorsport.service;

import com.example.victorsport.Entity.FormChan;
import com.example.victorsport.dto.request.FormChanRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface FormChanService {
    List<FormChan> getAll();
    Page<FormChan> getAll(Pageable pageable);
    Page<FormChan> search(String keyword, Pageable pageable);
    FormChan getById(Integer id);
    FormChan create(FormChanRequest request);
    FormChan update(Integer id, FormChanRequest request);
    void delete(Integer id);
}
