package com.example.victorsport.repository;

import com.example.victorsport.Entity.ChatLieu;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatLieuRepository extends JpaRepository<ChatLieu, Integer> {
    List<ChatLieu> findByXoaMemFalse();
    Page<ChatLieu> findByXoaMemFalse(Pageable pageable);
    Page<ChatLieu> findByTenChatLieuContainingIgnoreCaseAndXoaMemFalse(String ten, Pageable pageable);
}
