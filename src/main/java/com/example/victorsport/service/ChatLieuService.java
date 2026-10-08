package com.example.victorsport.service;

import com.example.victorsport.Entity.ChatLieu;
import com.example.victorsport.dto.request.ChatLieuRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ChatLieuService {
    List<ChatLieu> getAll();
    Page<ChatLieu> getAll(Pageable pageable);
    Page<ChatLieu> search(String keyword, Pageable pageable);
    ChatLieu getById(Integer id);
    ChatLieu create(ChatLieuRequest request);
    ChatLieu update(Integer id, ChatLieuRequest request);
    void delete(Integer id);
}
