package com.example.victorsport.repository;

import com.example.victorsport.Entity.ChiTietSanPham;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChiTietSanPhamRepository extends JpaRepository<ChiTietSanPham, Integer> {
    Optional<ChiTietSanPham> findByIdAndXoaMemFalse(Integer id);
    List<ChiTietSanPham> findBySanPhamIdAndXoaMemFalse(Integer sanPhamId);
    Page<ChiTietSanPham> findByXoaMemFalse(Pageable pageable);

    @Query("SELECT ct FROM ChiTietSanPham ct WHERE ct.xoaMem = false " +
           "AND (:sanPhamId IS NULL OR ct.sanPham.id = :sanPhamId) " +
           "AND (:mauSacId IS NULL OR ct.mauSac.id = :mauSacId) " +
           "AND (:kichThuocId IS NULL OR ct.kichThuoc.id = :kichThuocId) " +
           "AND (:formChanId IS NULL OR ct.formChan.id = :formChanId) " +
           "AND (:trangThai IS NULL OR ct.trangThai = :trangThai)")
    Page<ChiTietSanPham> filterChiTietSanPham(@Param("sanPhamId") Integer sanPhamId,
                                             @Param("mauSacId") Integer mauSacId,
                                             @Param("kichThuocId") Integer kichThuocId,
                                             @Param("formChanId") Integer formChanId,
                                             @Param("trangThai") Boolean trangThai,
                                             Pageable pageable);
}
