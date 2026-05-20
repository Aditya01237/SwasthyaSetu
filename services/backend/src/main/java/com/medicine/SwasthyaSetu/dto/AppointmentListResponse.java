package com.medicine.SwasthyaSetu.dto;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AppointmentListResponse {
    Long id;
    String doctorName;
    LocalDateTime time;
}
