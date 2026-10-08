package com.example.victorsport.repository;

import com.example.victorsport.Entity.HoaDon;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface HoaDonRepository extends JpaRepository<HoaDon, Integer> {
    Optional<HoaDon> findByMaHoaDon(String maHoaDon);
    Page<HoaDon> findBySoDienThoaiKhachHangAndXoaMemFalse(String sdt, Pageable pageable);
    Page<HoaDon> findByXoaMemFalse(Pageable pageable);
}
