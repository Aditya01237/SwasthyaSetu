package com.medicine.SwasthyaSetu.service;

import com.medicine.SwasthyaSetu.Entity.Hospital;
import com.medicine.SwasthyaSetu.dto.HospitalDetailsResponse;
import com.medicine.SwasthyaSetu.dto.HospitalRegisterRequest;
import com.medicine.SwasthyaSetu.repository.HospitalRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class HospitalServices {

    private final HospitalRepository hospitalRepository;

    public HospitalServices(HospitalRepository hospitalRepository){
        this.hospitalRepository = hospitalRepository;
    }
    public HospitalDetailsResponse addHospital(HospitalRegisterRequest request){

        System.out.println("This is request\n");
        System.out.println(request);

        Hospital hospital = new Hospital();
        hospital.setName(request.getName());
        hospital.setLocation(request.getLocation());
        hospital.setCreatedAt(LocalDateTime.now());

        System.out.println(hospital);

        hospitalRepository.save(hospital);

        HospitalDetailsResponse hospitalDetailsResponse = new HospitalDetailsResponse();
        hospitalDetailsResponse.setName(hospital.getName());
        hospitalDetailsResponse.setLocation(hospital.getLocation());

        System.out.println(hospitalDetailsResponse);

        System.out.println("Hospital Created Successfully");
        return hospitalDetailsResponse;
    }

    public List<HospitalDetailsResponse> getAllHospitals(){
       return hospitalRepository.findAll().stream().map(
                r->{
                    HospitalDetailsResponse dto = new HospitalDetailsResponse();
                    dto.setName(r.getName());
                    dto.setLocation(r.getLocation());
                    return dto;
                }
        ).toList();
    }

    public HospitalDetailsResponse getHospitalById(String id){
        Hospital hospital = hospitalRepository.findById(id).orElseThrow(
                ()-> new RuntimeException("Hospital Not Found")
        );

        HospitalDetailsResponse hospitalDetailsResponse = new HospitalDetailsResponse();
        hospitalDetailsResponse.setName(hospital.getName());
        hospitalDetailsResponse.setLocation(hospital.getLocation());

        return hospitalDetailsResponse;
    }

}
