package com.example.victorsport.repository;

import com.example.victorsport.Entity.ViTriThiDau;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ViTriThiDauRepository extends JpaRepository<ViTriThiDau, Integer> {
    List<ViTriThiDau> findByXoaMemFalse();
    Page<ViTriThiDau> findByXoaMemFalse(Pageable pageable);
    Page<ViTriThiDau> findByTenViTriContainingIgnoreCaseAndXoaMemFalse(String ten, Pageable pageable);
}
