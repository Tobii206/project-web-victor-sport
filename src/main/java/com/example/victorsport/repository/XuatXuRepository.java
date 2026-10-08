package com.example.victorsport.repository;

import com.example.victorsport.Entity.XuatXu;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface XuatXuRepository extends JpaRepository<XuatXu, Integer> {
    List<XuatXu> findByXoaMemFalse();
    Page<XuatXu> findByXoaMemFalse(Pageable pageable);
    Page<XuatXu> findByTenXuatXuContainingIgnoreCaseAndXoaMemFalse(String ten, Pageable pageable);
}
