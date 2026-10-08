package com.example.victorsport.repository;

import com.example.victorsport.Entity.ThuongHieu;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ThuongHieuRepository extends JpaRepository<ThuongHieu, Integer> {
    List<ThuongHieu> findByXoaMemFalse();
    Page<ThuongHieu> findByXoaMemFalse(Pageable pageable);
    Page<ThuongHieu> findByTenThuongHieuContainingIgnoreCaseAndXoaMemFalse(String ten, Pageable pageable);
}
