package com.medicine.SwasthyaSetu.dto;
import com.medicine.SwasthyaSetu.Entity.Hospital;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AppointmentDTO {

    private Hospital hospital;
    private LocalDateTime appointmentTime;

}
