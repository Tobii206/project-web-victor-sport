package com.example.victorsport.repository;

import com.example.victorsport.Entity.MauSac;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MauSacRepository extends JpaRepository<MauSac, Integer> {
    List<MauSac> findByXoaMemFalse();
    Page<MauSac> findByXoaMemFalse(Pageable pageable);
    Page<MauSac> findByTenMauSacContainingIgnoreCaseAndXoaMemFalse(String ten, Pageable pageable);
}
