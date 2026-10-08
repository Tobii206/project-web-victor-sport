package com.example.victorsport.repository;

import com.example.victorsport.Entity.KichThuoc;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KichThuocRepository extends JpaRepository<KichThuoc, Integer> {
    List<KichThuoc> findByXoaMemFalse();
    Page<KichThuoc> findByXoaMemFalse(Pageable pageable);
    Page<KichThuoc> findByTenKichThuocContainingIgnoreCaseAndXoaMemFalse(String ten, Pageable pageable);
}
