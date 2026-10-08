package com.example.victorsport.service.impl;

import com.example.victorsport.Entity.ChatLieu;
import com.example.victorsport.dto.request.ChatLieuRequest;
import com.example.victorsport.exception.ResourceNotFoundException;
import com.example.victorsport.repository.ChatLieuRepository;
import com.example.victorsport.service.ChatLieuService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ChatLieuServiceImpl implements ChatLieuService {

    private final ChatLieuRepository repository;

    public ChatLieuServiceImpl(ChatLieuRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<ChatLieu> getAll() {
        return repository.findByXoaMemFalse();
    }

    @Override
    public Page<ChatLieu> getAll(Pageable pageable) {
        return repository.findByXoaMemFalse(pageable);
    }

    @Override
    public Page<ChatLieu> search(String keyword, Pageable pageable) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return repository.findByXoaMemFalse(pageable);
        }
        return repository.findByTenChatLieuContainingIgnoreCaseAndXoaMemFalse(keyword.trim(), pageable);
    }

    @Override
    public ChatLieu getById(Integer id) {
        return repository.findById(id)
                .filter(item -> !Boolean.TRUE.equals(item.getXoaMem()))
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chất liệu có ID: " + id));
    }

    @Override
    @Transactional
    public ChatLieu create(ChatLieuRequest request) {
        ChatLieu item = new ChatLieu();
        item.setTenChatLieu(request.getTenChatLieu());
        item.setTrangThai(request.getTrangThai() != null ? request.getTrangThai() : true);
        item.setXoaMem(false);
        return repository.save(item);
    }

    @Override
    @Transactional
    public ChatLieu update(Integer id, ChatLieuRequest request) {
        ChatLieu item = getById(id);
        item.setTenChatLieu(request.getTenChatLieu());
        if (request.getTrangThai() != null) {
            item.setTrangThai(request.getTrangThai());
        }
        return repository.save(item);
    }

    @Override
    @Transactional
    public void delete(Integer id) {
        ChatLieu item = getById(id);
        item.setXoaMem(true);
        repository.save(item);
    }
}
