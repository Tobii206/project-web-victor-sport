package com.example.victorsport.repository;

import com.example.victorsport.Entity.CoGiay;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CoGiayRepository extends JpaRepository<CoGiay, Integer> {
    List<CoGiay> findByXoaMemFalse();
    Page<CoGiay> findByXoaMemFalse(Pageable pageable);
    Page<CoGiay> findByTenCoGiayContainingIgnoreCaseAndXoaMemFalse(String ten, Pageable pageable);
}
