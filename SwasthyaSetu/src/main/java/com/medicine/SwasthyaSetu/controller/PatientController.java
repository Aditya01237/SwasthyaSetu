package com.medicine.SwasthyaSetu.controller;
import com.medicine.SwasthyaSetu.Entity.Patient;
import com.medicine.SwasthyaSetu.service.PatientService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/patient")
public class PatientController {

    private final PatientService patientService;

    public PatientController(PatientService patientService){
        this.patientService = patientService;
    }

    @PostMapping("/register")
    public Patient registerPatient(@RequestBody Patient patient){
        return patientService.registerPatient(patient);
    }


}
