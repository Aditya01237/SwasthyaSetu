package com.example.qr_service.controller;

import com.example.qr_service.dto.CommonResponse;
import com.example.qr_service.dto.QrScanRequest;
import com.example.qr_service.dto.QrScanResponse;
import com.example.qr_service.service.QrService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/qr")
public class QrController {
    private final QrService qrService;
    public QrController(QrService qrService){
        this.qrService = qrService;
    }

    @PostMapping("/scan")
    public ResponseEntity<CommonResponse<QrScanResponse>> scan(@RequestBody QrScanRequest request){
        QrScanResponse response = qrService.scan(request);
        return ResponseEntity.ok(
                new CommonResponse<>("Qr Scan", response, 200)
        );
    }
}
