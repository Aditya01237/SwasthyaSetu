package com.medicine.hospital.service;

import com.medicine.hospital.dto.HospitalDetailsResponse;
import com.medicine.hospital.dto.HospitalRegisterRequest;
import com.medicine.hospital.dto.HospitalResponse;
import com.medicine.hospital.entity.Hospital;
import com.medicine.hospital.repository.HospitalRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HospitalService {

    private final HospitalRepository hospitalRepository;

    public HospitalService(HospitalRepository hospitalRepository) {
        this.hospitalRepository = hospitalRepository;
    }

    @Transactional
    public HospitalDetailsResponse addHospital(HospitalRegisterRequest request) {
        Hospital hospital = new Hospital();
        hospital.setName(request.getName());
        hospital.setCity(request.getCity());
        hospital.setAddress(request.getAddress());
        hospital.setPhone(request.getPhone());
        hospital.setEmail(request.getEmail());
        hospital.setImageUrls(request.getImageUrls() != null ? request.getImageUrls() : List.of());
        hospital.setServices(request.getServices() != null ? request.getServices() : List.of());
        hospital.setSpecializations(request.getSpecializations() != null ? request.getSpecializations() : List.of());
        hospital.setRating(request.getRating() != null ? request.getRating() : 4.0);
        hospital.setTotalReviews(request.getTotalReviews() != null ? request.getTotalReviews() : 0);
        hospital.setIsOpen24x7(request.getIsOpen24x7() != null ? request.getIsOpen24x7() : true);

        return mapToHospitalDetailsResponse(hospitalRepository.save(hospital));
    }

    public List<HospitalDetailsResponse> getAllHospitals() {
        return hospitalRepository.findAll()
                .stream()
                .map(this::mapToHospitalDetailsResponse)
                .toList();
    }

    public HospitalDetailsResponse getHospitalById(String id) {
        Hospital hospital = hospitalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Hospital Not Found"));
        return mapToHospitalDetailsResponse(hospital);
    }

    public List<HospitalResponse> getHospitals(String city, String name) {
        List<Hospital> hospitals;

        if (city != null && !city.isBlank() && name != null && !name.isBlank()) {
            hospitals = hospitalRepository.findByCityContainingIgnoreCaseAndNameContainingIgnoreCase(city, name);
        } else if (city != null && !city.isBlank()) {
            hospitals = hospitalRepository.findByCityContainingIgnoreCase(city);
        } else if (name != null && !name.isBlank()) {
            hospitals = hospitalRepository.findByNameContainingIgnoreCase(name);
        } else {
            hospitals = hospitalRepository.findAll();
        }

        return hospitals.stream().map(this::mapToHospitalResponse).toList();
    }

    private HospitalDetailsResponse mapToHospitalDetailsResponse(Hospital hospital) {
        HospitalDetailsResponse response = new HospitalDetailsResponse();
        response.setId(hospital.getId());
        response.setName(hospital.getName());
        response.setCity(hospital.getCity());
        response.setAddress(hospital.getAddress());
        response.setPhone(hospital.getPhone());
        response.setEmail(hospital.getEmail());
        response.setImageUrls(hospital.getImageUrls());
        response.setRating(hospital.getRating());
        response.setTotalReviews(hospital.getTotalReviews());
        response.setServices(hospital.getServices());
        response.setSpecializations(hospital.getSpecializations());
        response.setIsOpen24x7(hospital.getIsOpen24x7());
        return response;
    }

    private HospitalResponse mapToHospitalResponse(Hospital hospital) {
        HospitalResponse response = new HospitalResponse();
        response.setId(hospital.getId());
        response.setName(hospital.getName());
        response.setCity(hospital.getCity());
        response.setAddress(hospital.getAddress());
        response.setImageUrls(hospital.getImageUrls());
        response.setRating(hospital.getRating());
        response.setTotalReviews(hospital.getTotalReviews());
        response.setIsOpen24x7(hospital.getIsOpen24x7());
        response.setServices(hospital.getServices());
        response.setSpecializations(hospital.getSpecializations());
        return response;
    }
}
