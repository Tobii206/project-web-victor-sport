package com.example.victorsport.repository;

import com.example.victorsport.Entity.PhuongThucThanhToan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PhuongThucThanhToanRepository extends JpaRepository<PhuongThucThanhToan, Integer> {
    List<PhuongThucThanhToan> findByTrangThaiTrueAndXoaMemFalse();
}
