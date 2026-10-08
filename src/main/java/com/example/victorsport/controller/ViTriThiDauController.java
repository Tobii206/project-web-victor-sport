package com.example.victorsport.controller;

import com.example.victorsport.Entity.ViTriThiDau;
import com.example.victorsport.common.ApiResponse;
import com.example.victorsport.common.PageResponse;
import com.example.victorsport.dto.request.ViTriThiDauRequest;
import com.example.victorsport.service.ViTriThiDauService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vi-tri-thi-dau")
public class ViTriThiDauController {

    private final ViTriThiDauService service;

    public ViTriThiDauController(ViTriThiDauService service) {
        this.service = service;
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<ViTriThiDau>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(service.getAll()));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ViTriThiDau>>> getPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(service.search(keyword, pageable))));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ViTriThiDau>> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponse.success(service.getById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ViTriThiDau>> create(@Valid @RequestBody ViTriThiDauRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Thêm vị trí thi đấu thành công", service.create(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ViTriThiDau>> update(@PathVariable Integer id, @Valid @RequestBody ViTriThiDauRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Cập nhật vị trí thi đấu thành công", service.update(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa vị trí thi đấu thành công", null));
    }
}
