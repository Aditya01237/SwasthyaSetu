package com.medicine.SwasthyaSetu.service;
import com.medicine.SwasthyaSetu.Entity.Doctor;
import com.medicine.SwasthyaSetu.Entity.Hospital;
import com.medicine.SwasthyaSetu.dto.DoctorRegisterRequest;
import com.medicine.SwasthyaSetu.dto.DoctorResponse;
import com.medicine.SwasthyaSetu.repository.DoctorRepository;
import com.medicine.SwasthyaSetu.repository.HospitalRepository;
import org.springframework.stereotype.Service;

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
        doctorResponse.setHospitalName(doctor.getHospital().getName());

        return doctorResponse;

    }

}
