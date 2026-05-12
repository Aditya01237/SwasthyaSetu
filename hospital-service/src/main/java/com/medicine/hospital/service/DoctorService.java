package com.medicine.hospital.service;

import com.medicine.hospital.dto.DoctorRegisterRequest;
import com.medicine.hospital.dto.DoctorResponse;
import com.medicine.hospital.entity.Doctor;
import com.medicine.hospital.entity.Hospital;
import com.medicine.hospital.repository.DoctorRepository;
import com.medicine.hospital.repository.HospitalRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DoctorService {

    private final HospitalRepository hospitalRepository;
    private final DoctorRepository doctorRepository;

    public DoctorService(HospitalRepository hospitalRepository, DoctorRepository doctorRepository) {
        this.hospitalRepository = hospitalRepository;
        this.doctorRepository = doctorRepository;
    }

    @Transactional
    public DoctorResponse registerDoctor(DoctorRegisterRequest request) {
        if (doctorRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Doctor already exists");
        }

        Hospital hospital = hospitalRepository.findById(request.getHospitalId())
                .orElseThrow(() -> new RuntimeException("Hospital Not Found"));

        Doctor doctor = new Doctor();
        doctor.setName(request.getName());
        doctor.setExperience(request.getExperience());
        doctor.setSpecialization(request.getSpecialization());
        doctor.setFee(request.getFee());
        doctor.setEmail(request.getEmail());
        doctor.setPassword(request.getPassword());
        doctor.setHospital(hospital);

        return mapToDoctorResponse(doctorRepository.save(doctor));
    }

    public DoctorResponse getDoctorById(Long id) {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));
        return mapToDoctorResponse(doctor);
    }

    public List<DoctorResponse> getDoctorsByHospital(String hospitalId) {
        return doctorRepository.findByHospitalId(hospitalId)
                .stream()
                .map(this::mapToDoctorResponse)
                .toList();
    }

    private DoctorResponse mapToDoctorResponse(Doctor doctor) {
        DoctorResponse response = new DoctorResponse();
        response.setId(doctor.getId());
        response.setName(doctor.getName());
        response.setExperience(doctor.getExperience());
        response.setSpecialization(doctor.getSpecialization());
        response.setFee(doctor.getFee());

        Hospital hospital = doctor.getHospital();
        if (hospital != null) {
            response.setHospitalId(hospital.getId());
            response.setHospitalName(hospital.getName());
        }

        return response;
    }
}
