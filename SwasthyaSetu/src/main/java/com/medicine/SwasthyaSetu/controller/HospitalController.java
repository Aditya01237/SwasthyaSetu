package com.medicine.SwasthyaSetu.controller;

import com.medicine.SwasthyaSetu.dto.HospitalDetailsResponse;
import com.medicine.SwasthyaSetu.dto.HospitalRegisterRequest;
import com.medicine.SwasthyaSetu.service.HospitalServices;
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
    public HospitalDetailsResponse registerHospital(@RequestBody HospitalRegisterRequest hospitalRegisterRequest){
        return hospitalServices.addHospital(hospitalRegisterRequest);
    }

    @GetMapping("/all")
    public List<HospitalDetailsResponse> getAllHospital(){
        return hospitalServices.getAllHospitals();
    }

    @GetMapping("/{id}")
    public HospitalDetailsResponse getAllHospital(@PathVariable String id){
        return hospitalServices.getHospitalById(id);
    }

}
