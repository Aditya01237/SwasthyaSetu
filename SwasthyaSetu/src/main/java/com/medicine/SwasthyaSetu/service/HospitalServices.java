package com.medicine.SwasthyaSetu.service;

import com.medicine.SwasthyaSetu.Entity.Hospital;
import com.medicine.SwasthyaSetu.dto.HospitalDetailsResponse;
import com.medicine.SwasthyaSetu.dto.HospitalRegisterRequest;
import com.medicine.SwasthyaSetu.repository.HospitalRepository;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class HospitalServices {

    private final HospitalRepository hospitalRepository;

    public HospitalServices(HospitalRepository hospitalRepository){
        this.hospitalRepository = hospitalRepository;
    }
    private static final Logger log = LoggerFactory.getLogger(HospitalServices.class);

    public HospitalDetailsResponse addHospital(HospitalRegisterRequest request){

        Hospital hospital = new Hospital();
        hospital.setName(request.getName());
        hospital.setLocation(request.getLocation());
        hospital.setCreatedAt(LocalDateTime.now());

        hospitalRepository.save(hospital);

        HospitalDetailsResponse hospitalDetailsResponse = new HospitalDetailsResponse();
        hospitalDetailsResponse.setName(hospital.getName());
        hospitalDetailsResponse.setLocation(hospital.getLocation());

        log.info("Hospital Created Successfully");
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
