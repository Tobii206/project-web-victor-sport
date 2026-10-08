package com.example.victorsport.repository;

import com.example.victorsport.Entity.GiaoDichThanhToan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GiaoDichThanhToanRepository extends JpaRepository<GiaoDichThanhToan, Integer> {
    List<GiaoDichThanhToan> findByHoaDonId(Integer hoaDonId);
}
