package com.example.victorsport.controller;

import com.example.victorsport.common.ApiResponse;
import com.example.victorsport.common.PageResponse;
import com.example.victorsport.dto.request.SanPhamRequest;
import com.example.victorsport.dto.response.SanPhamResponse;
import com.example.victorsport.service.SanPhamService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/san-pham")
public class SanPhamController {

    private final SanPhamService service;

    public SanPhamController(SanPhamService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<SanPhamResponse>>> getPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer thuongHieuId,
            @RequestParam(required = false) Boolean trangThai) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(service.filter(keyword, thuongHieuId, trangThai, pageable))));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SanPhamResponse>> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponse.success(service.getById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<SanPhamResponse>> create(@Valid @RequestBody SanPhamRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Thêm sản phẩm thành công", service.create(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SanPhamResponse>> update(@PathVariable Integer id, @Valid @RequestBody SanPhamRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Cập nhật sản phẩm thành công", service.update(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa sản phẩm thành công", null));
    }
}
