package com.example.victorsport.repository;

import com.example.victorsport.Entity.AnhChiTietSanPham;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnhChiTietSanPhamRepository extends JpaRepository<AnhChiTietSanPham, Integer> {
    List<AnhChiTietSanPham> findByChiTietSanPhamIdAndXoaMemFalse(Integer chiTietSanPhamId);
}
