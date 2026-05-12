package com.medicine.SwasthyaSetu.dto;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class PrescriptionUploadRequest {
    private String patientId;
    private MultipartFile file;
}
