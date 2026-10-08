package com.example.victorsport.repository;

import com.example.victorsport.Entity.FormChan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FormChanRepository extends JpaRepository<FormChan, Integer> {
    List<FormChan> findByXoaMemFalse();
    Page<FormChan> findByXoaMemFalse(Pageable pageable);
    Page<FormChan> findByTenFormChanContainingIgnoreCaseAndXoaMemFalse(String ten, Pageable pageable);
}
