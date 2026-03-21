package com.medicine.SwasthyaSetu.controller;

import com.medicine.SwasthyaSetu.dto.QrScanRequest;
import com.medicine.SwasthyaSetu.dto.QrScanResponse;
import com.medicine.SwasthyaSetu.service.QrService;
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
    public QrScanResponse scan(@RequestBody QrScanRequest request){
        return qrService.scan(request);
    }
}
