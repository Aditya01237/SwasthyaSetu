package com.medicine.SwasthyaSetu.controller;

import com.medicine.SwasthyaSetu.dto.CommonResponse;
import com.medicine.SwasthyaSetu.dto.HospitalDetailsResponse;
import com.medicine.SwasthyaSetu.dto.HospitalRegisterRequest;
import com.medicine.SwasthyaSetu.service.HospitalServices;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hospital")
public class HospitalController {

    private final HospitalServices hospitalServices;

    public HospitalController(HospitalServices hospitalServices) {
        this.hospitalServices = hospitalServices;
    }

    @PostMapping("/add")
    public ResponseEntity<CommonResponse<HospitalDetailsResponse>> registerHospital(@RequestBody HospitalRegisterRequest hospitalRegisterRequest){
        HospitalDetailsResponse response = hospitalServices.addHospital(hospitalRegisterRequest);
        return ResponseEntity.ok(
                new CommonResponse<>("Hospital Registered Successfully", response, 200)
        );
    }

    @GetMapping("/all")
    public ResponseEntity<CommonResponse<List<HospitalDetailsResponse>>> getAllHospital(){
        List<HospitalDetailsResponse> response = hospitalServices.getAllHospitals();
        return ResponseEntity.ok(
                new CommonResponse<>("All Hospital Data", response, 200)
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<CommonResponse<HospitalDetailsResponse>> getHospitalById(@PathVariable("id") String id){
        HospitalDetailsResponse response = hospitalServices.getHospitalById(id);
        return ResponseEntity.ok(
                new CommonResponse<>("Hospital Available", response, 200)
        );
    }

}
