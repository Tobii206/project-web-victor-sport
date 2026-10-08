package com.example.victorsport.controller;

import com.example.victorsport.Entity.FormChan;
import com.example.victorsport.common.ApiResponse;
import com.example.victorsport.common.PageResponse;
import com.example.victorsport.dto.request.FormChanRequest;
import com.example.victorsport.service.FormChanService;
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
@RequestMapping("/api/form-chan")
public class FormChanController {

    private final FormChanService service;

    public FormChanController(FormChanService service) {
        this.service = service;
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<FormChan>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(service.getAll()));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<FormChan>>> getPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(service.search(keyword, pageable))));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<FormChan>> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponse.success(service.getById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<FormChan>> create(@Valid @RequestBody FormChanRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Thêm form chân thành công", service.create(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<FormChan>> update(@PathVariable Integer id, @Valid @RequestBody FormChanRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Cập nhật form chân thành công", service.update(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa form chân thành công", null));
    }
}
