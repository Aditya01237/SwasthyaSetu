package com.medicine.appointment.controller;

import com.medicine.appointment.dto.CommonResponse;
import com.medicine.appointment.dto.QrScanRequest;
import com.medicine.appointment.dto.QrScanResponse;
import com.medicine.appointment.service.QrService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/qr")
public class QrController {

    private final QrService qrService;

    public QrController(QrService qrService) {
        this.qrService = qrService;
    }

    @PostMapping("/scan")
    public ResponseEntity<CommonResponse<QrScanResponse>> scan(@RequestBody QrScanRequest request) {
        return ResponseEntity.ok(new CommonResponse<>("Qr Scan", qrService.scan(request), 200));
    }
}
