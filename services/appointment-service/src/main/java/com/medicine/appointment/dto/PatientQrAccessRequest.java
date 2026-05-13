package com.medicine.appointment.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PatientQrAccessRequest {
    private Long doctorId;
    private Long appointmentId;
}
