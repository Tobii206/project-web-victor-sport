package com.example.victorsport.repository;

import com.example.victorsport.Entity.PhongCachChoi;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PhongCachChoiRepository extends JpaRepository<PhongCachChoi, Integer> {
    List<PhongCachChoi> findByXoaMemFalse();
    Page<PhongCachChoi> findByXoaMemFalse(Pageable pageable);
    Page<PhongCachChoi> findByTenPhongCachContainingIgnoreCaseAndXoaMemFalse(String ten, Pageable pageable);
}
