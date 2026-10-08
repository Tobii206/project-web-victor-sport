package com.example.victorsport.repository;

import com.example.victorsport.Entity.SanPham;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SanPhamRepository extends JpaRepository<SanPham, Integer> {
    Optional<SanPham> findByIdAndXoaMemFalse(Integer id);
    Page<SanPham> findByXoaMemFalse(Pageable pageable);

    @Query("SELECT sp FROM SanPham sp WHERE sp.xoaMem = false " +
           "AND (:keyword IS NULL OR LOWER(sp.tenSanPham) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "     OR LOWER(sp.maSanPham) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (:thuongHieuId IS NULL OR sp.thuongHieu.id = :thuongHieuId) " +
           "AND (:trangThai IS NULL OR sp.trangThaiKinhDoanh = :trangThai)")
    Page<SanPham> filterSanPham(@Param("keyword") String keyword,
                               @Param("thuongHieuId") Integer thuongHieuId,
                               @Param("trangThai") Boolean trangThai,
                               Pageable pageable);
}
