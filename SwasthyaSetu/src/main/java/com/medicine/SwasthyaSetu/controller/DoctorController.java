package com.medicine.SwasthyaSetu.controller;

import com.medicine.SwasthyaSetu.dto.CommonResponse;
import com.medicine.SwasthyaSetu.dto.DoctorRegisterRequest;
import com.medicine.SwasthyaSetu.dto.DoctorResponse;
import com.medicine.SwasthyaSetu.service.DoctorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/doctor")
public class DoctorController {

    private final DoctorService doctorService;

    public DoctorController(DoctorService doctorService){
        this.doctorService = doctorService;
    }

    @PostMapping("/register")
    public ResponseEntity<CommonResponse<DoctorResponse>> registerDoctor(@RequestBody DoctorRegisterRequest registerRequest){
        DoctorResponse response = doctorService.registerDoctor(registerRequest);
        return ResponseEntity.ok(
                new CommonResponse<>("Doctor registered successfully", response, 200)
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<CommonResponse<DoctorResponse>> getDoctorById(@PathVariable("id") Long id){
        DoctorResponse response = doctorService.getDoctorById(id);
        return ResponseEntity.ok(
                new CommonResponse<>("Doctor Find successfully", response, 200)
        );
    }

}
