package com.example.victorsport.controller;

import com.example.victorsport.Entity.PhieuGiamGia;
import com.example.victorsport.Entity.PhuongThucThanhToan;
import com.example.victorsport.common.ApiResponse;
import com.example.victorsport.dto.request.DatHangRequest;
import com.example.victorsport.dto.response.DatHangResponse;
import com.example.victorsport.dto.response.TraCuuDonHangResponse;
import com.example.victorsport.repository.PhieuGiamGiaRepository;
import com.example.victorsport.repository.PhuongThucThanhToanRepository;
import com.example.victorsport.service.DonHangOnlineService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/don-hang")
public class DonHangOnlineController {

    private final DonHangOnlineService donHangOnlineService;
    private final PhuongThucThanhToanRepository phuongThucThanhToanRepository;
    private final PhieuGiamGiaRepository phieuGiamGiaRepository;

    public DonHangOnlineController(DonHangOnlineService donHangOnlineService,
                                  PhuongThucThanhToanRepository phuongThucThanhToanRepository,
                                  PhieuGiamGiaRepository phieuGiamGiaRepository) {
        this.donHangOnlineService = donHangOnlineService;
        this.phuongThucThanhToanRepository = phuongThucThanhToanRepository;
        this.phieuGiamGiaRepository = phieuGiamGiaRepository;
    }

    @PostMapping("/dat-hang")
    public ResponseEntity<ApiResponse<DatHangResponse>> datHang(@Valid @RequestBody DatHangRequest request) {
        DatHangResponse response = donHangOnlineService.datHang(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Đặt hàng thành công", response));
    }

    @GetMapping("/tra-cuu/{maHoaDon}")
    public ResponseEntity<ApiResponse<TraCuuDonHangResponse>> traCuu(@PathVariable String maHoaDon) {
        TraCuuDonHangResponse response = donHangOnlineService.traCuuDonHang(maHoaDon);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/kiem-tra-voucher")
    public ResponseEntity<ApiResponse<Map<String, Object>>> kiemTraVoucher(
            @RequestParam String maVoucher,
            @RequestParam BigDecimal tongTien) {
        Map<String, Object> result = donHangOnlineService.kiemTraVoucher(maVoucher, tongTien);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/phuong-thuc-thanh-toan")
    public ResponseEntity<ApiResponse<List<PhuongThucThanhToan>>> getPhuongThucThanhToan() {
        return ResponseEntity.ok(ApiResponse.success(phuongThucThanhToanRepository.findByTrangThaiTrueAndXoaMemFalse()));
    }

    @GetMapping("/voucher-kha-dung")
    public ResponseEntity<ApiResponse<List<PhieuGiamGia>>> getVoucherKhaDung() {
        return ResponseEntity.ok(ApiResponse.success(phieuGiamGiaRepository.findByTrangThaiTrueAndXoaMemFalse()));
    }
}
