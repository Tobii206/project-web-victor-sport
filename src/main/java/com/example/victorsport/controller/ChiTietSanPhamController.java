package com.example.victorsport.controller;

import com.example.victorsport.common.ApiResponse;
import com.example.victorsport.common.PageResponse;
import com.example.victorsport.dto.request.AnhChiTietSanPhamRequest;
import com.example.victorsport.dto.request.ChiTietSanPhamRequest;
import com.example.victorsport.dto.response.AnhChiTietResponse;
import com.example.victorsport.dto.response.ChiTietSanPhamResponse;
import com.example.victorsport.service.ChiTietSanPhamService;
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
@RequestMapping("/api/chi-tiet-san-pham")
public class ChiTietSanPhamController {

    private final ChiTietSanPhamService service;

    public ChiTietSanPhamController(ChiTietSanPhamService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ChiTietSanPhamResponse>>> getPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer sanPhamId,
            @RequestParam(required = false) Integer mauSacId,
            @RequestParam(required = false) Integer kichThuocId,
            @RequestParam(required = false) Integer formChanId,
            @RequestParam(required = false) Boolean trangThai) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        return ResponseEntity.ok(ApiResponse.success(
                PageResponse.from(service.filter(sanPhamId, mauSacId, kichThuocId, formChanId, trangThai, pageable))));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ChiTietSanPhamResponse>> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponse.success(service.getById(id)));
    }

    @GetMapping("/by-san-pham/{sanPhamId}")
    public ResponseEntity<ApiResponse<List<ChiTietSanPhamResponse>>> getBySanPhamId(@PathVariable Integer sanPhamId) {
        return ResponseEntity.ok(ApiResponse.success(service.getBySanPhamId(sanPhamId)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ChiTietSanPhamResponse>> create(@Valid @RequestBody ChiTietSanPhamRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Thêm biến thể sản phẩm thành công", service.create(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ChiTietSanPhamResponse>> update(@PathVariable Integer id, @Valid @RequestBody ChiTietSanPhamRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Cập nhật biến thể sản phẩm thành công", service.update(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa biến thể sản phẩm thành công", null));
    }

    @PostMapping("/{id}/anh")
    public ResponseEntity<ApiResponse<AnhChiTietResponse>> themAnh(
            @PathVariable Integer id,
            @Valid @RequestBody AnhChiTietSanPhamRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Thêm ảnh cho biến thể thành công", service.themAnh(id, request)));
    }

    @DeleteMapping("/anh/{idAnh}")
    public ResponseEntity<ApiResponse<Void>> xoaAnh(@PathVariable Integer idAnh) {
        service.xoaAnh(idAnh);
        return ResponseEntity.ok(ApiResponse.success("Xóa ảnh thành công", null));
    }
}
