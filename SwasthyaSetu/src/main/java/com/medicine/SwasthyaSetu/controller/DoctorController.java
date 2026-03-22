package com.medicine.SwasthyaSetu.controller;

import com.medicine.SwasthyaSetu.dto.DoctorRegisterRequest;
import com.medicine.SwasthyaSetu.dto.DoctorResponse;
import com.medicine.SwasthyaSetu.service.DoctorService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/doctor")
public class DoctorController {

    private final DoctorService doctorService;

    public DoctorController(DoctorService doctorService){
        this.doctorService = doctorService;
    }

    @PostMapping("/register")
    public DoctorResponse registerDoctor(@RequestBody DoctorRegisterRequest registerRequest){
        return doctorService.registerDoctor(registerRequest);
    }

    @GetMapping("/{id}")
    public DoctorResponse getDoctorById(@PathVariable("id") Long id){
        return doctorService.getDoctorById(id);
    }

}
