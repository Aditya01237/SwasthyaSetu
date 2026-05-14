package com.example.provider_service.service;
import com.example.provider_service.entity.Doctor;
import com.example.provider_service.entity.Hospital;
import com.example.provider_service.dto.DoctorRegisterRequest;
import com.example.provider_service.dto.DoctorResponse;
import com.example.provider_service.repository.DoctorRepository;
import com.example.provider_service.repository.HospitalRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DoctorService {

    private final HospitalRepository hospitalRepository;
    private final DoctorRepository doctorRepository;


    public DoctorService(HospitalRepository hospitalRepository, DoctorRepository doctorRepository){
        this.hospitalRepository = hospitalRepository;
        this.doctorRepository = doctorRepository;
    }

    public DoctorResponse registerDoctor(DoctorRegisterRequest registerRequest){

        Hospital hospital = hospitalRepository.findById(registerRequest.getHospitalId()).orElseThrow(
                ()-> new RuntimeException("Hospital Not Found")
        );

        Doctor doctor = new Doctor();
        doctor.setName(registerRequest.getName());
        doctor.setExperience(registerRequest.getExperience());
        doctor.setSpecialization(registerRequest.getSpecialization());
        doctor.setHospital(hospital);

        Doctor savedDoctor = doctorRepository.save(doctor);

        DoctorResponse doctorResponse = new DoctorResponse();
        doctorResponse.setId(savedDoctor.getId());
        doctorResponse.setName(savedDoctor.getName());
        doctorResponse.setExperience(savedDoctor.getExperience());
        doctorResponse.setSpecialization(savedDoctor.getSpecialization());
        doctorResponse.setHospitalName(savedDoctor.getHospital().getName());

        return doctorResponse;
    }

    public DoctorResponse getDoctorById(Long id){

        Doctor doctor = doctorRepository.findById(id).orElseThrow(
                ()-> new RuntimeException("Doctor not found")
        );

        DoctorResponse doctorResponse = new DoctorResponse();
        doctorResponse.setId(doctor.getId());
        doctorResponse.setName(doctor.getName());
        doctorResponse.setExperience(doctor.getExperience());
        doctorResponse.setSpecialization(doctor.getSpecialization());
        doctorResponse.setFee(doctor.getFee());
        doctorResponse.setHospitalId(doctor.getHospital().getId());
        doctorResponse.setHospitalName(doctor.getHospital().getName());

        return doctorResponse;

    }

    public List<DoctorResponse> getDoctorsByHospital(String hospitalId) {
        List<Doctor> doctors = doctorRepository.findByHospitalId(hospitalId);

        return doctors.stream().map(d -> {
            DoctorResponse res = new DoctorResponse();
            res.setId(d.getId());
            res.setName(d.getName());
            res.setSpecialization(d.getSpecialization());
            res.setExperience(d.getExperience());
            res.setFee(d.getFee());
            return res;
        }).toList();
    }

}
