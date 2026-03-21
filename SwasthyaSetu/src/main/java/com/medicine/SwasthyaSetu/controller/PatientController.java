package com.medicine.SwasthyaSetu.controller;
import com.medicine.SwasthyaSetu.dto.PatientDetailsRequest;
import com.medicine.SwasthyaSetu.dto.PatientDetailsResponse;
import com.medicine.SwasthyaSetu.dto.PatientRegisterRequest;
import com.medicine.SwasthyaSetu.dto.PatientResponse;
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
    public PatientResponse registerPatient(@RequestBody PatientRegisterRequest request){
        return patientService.registerPatient(request);
    }

    @GetMapping("/history/{phone}")
    public PatientDetailsResponse getPatientDetails(@PathVariable("phone") String phone){
        PatientDetailsRequest patientDetailsRequest = new PatientDetailsRequest();
        patientDetailsRequest.setPhone(phone);
        return patientService.getPatientDetails(patientDetailsRequest);
    }

}
