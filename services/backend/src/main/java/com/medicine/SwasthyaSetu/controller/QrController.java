package com.medicine.SwasthyaSetu.controller;

import com.medicine.SwasthyaSetu.dto.CommonResponse;
import com.medicine.SwasthyaSetu.dto.QrScanRequest;
import com.medicine.SwasthyaSetu.dto.QrScanResponse;
import com.medicine.SwasthyaSetu.service.QrService;
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
