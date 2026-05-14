package com.example.qr_service.dto;

import lombok.Data;

@Data
public class QrScanRequest {
    private String token;
    private Long doctorId;
}
