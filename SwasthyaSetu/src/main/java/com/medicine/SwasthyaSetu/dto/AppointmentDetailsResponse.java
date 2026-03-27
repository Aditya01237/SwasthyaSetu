package com.medicine.SwasthyaSetu.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AppointmentDetailsResponse {
    private Long id;
    private String doctorName;
    private String hospitalName;
    private LocalDateTime time;
    private String qrToken;

    // ✅ ADD THESE
    private LocalDateTime validFrom;
    private LocalDateTime validTo;
}