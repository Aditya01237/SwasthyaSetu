package com.medicine.SwasthyaSetu.service;

import com.medicine.SwasthyaSetu.Entity.Hospital;
import com.medicine.SwasthyaSetu.dto.HospitalDetailsResponse;
import com.medicine.SwasthyaSetu.dto.HospitalRegisterRequest;
import com.medicine.SwasthyaSetu.dto.HospitalResponse;
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

    // ✅ Add Hospital
    public HospitalDetailsResponse addHospital(HospitalRegisterRequest request){

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

        hospital.setCreatedAt(LocalDateTime.now());
        hospital.setUpdatedAt(LocalDateTime.now());

        hospitalRepository.save(hospital);

        log.info("✅ Hospital Created Successfully: {}", hospital.getName());

        return mapToHospitalDetailsResponse(hospital);
    }

    // ✅ Get All Hospitals (NOW FULL DATA)
    public List<HospitalDetailsResponse> getAllHospitals(){
        return hospitalRepository.findAll()
                .stream()
                .map(this::mapToHospitalDetailsResponse)
                .toList();
    }

    // ✅ Get Hospital By ID (FULL DETAILS)
    public HospitalDetailsResponse getHospitalById(String id){
        Hospital h = hospitalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Hospital Not Found"));

        return mapToHospitalDetailsResponse(h);
    }

    // ✅ Filter Hospitals (lighter response if needed)
    public List<HospitalResponse> getHospitals(String city, String name) {

        List<Hospital> hospitals;

        if (city != null && !city.isBlank() && name != null && !name.isBlank()) {
            hospitals = hospitalRepository
                    .findByCityContainingIgnoreCaseAndNameContainingIgnoreCase(city, name);

        } else if (city != null && !city.isBlank()) {
            hospitals = hospitalRepository.findByCityContainingIgnoreCase(city);

        } else if (name != null && !name.isBlank()) {
            hospitals = hospitalRepository.findByNameContainingIgnoreCase(name);

        } else {
            hospitals = hospitalRepository.findAll();
        }

        return hospitals.stream()
                .map(this::mapToHospitalResponse)
                .toList();
    }

    // 🔥 ✅ FULL DETAILS MAPPER (MATCHES YOUR DTO)
    private HospitalDetailsResponse mapToHospitalDetailsResponse(Hospital h){

        HospitalDetailsResponse res = new HospitalDetailsResponse();

        res.setId(h.getId());
        res.setName(h.getName());
        res.setCity(h.getCity());
        res.setAddress(h.getAddress());
        res.setPhone(h.getPhone());
        res.setEmail(h.getEmail());

        res.setImageUrls(h.getImageUrls());
        res.setRating(h.getRating());
        res.setTotalReviews(h.getTotalReviews());

        res.setServices(h.getServices());
        res.setSpecializations(h.getSpecializations());

        res.setIsOpen24x7(h.getIsOpen24x7());

        return res;
    }

    // 🔥 ✅ LIGHT RESPONSE (for listing cards)
    private HospitalResponse mapToHospitalResponse(Hospital h){

        HospitalResponse res = new HospitalResponse();

        res.setId(h.getId());
        res.setName(h.getName());
        res.setCity(h.getCity());
        res.setAddress(h.getAddress());

        res.setImageUrls(h.getImageUrls());
        res.setRating(h.getRating());
        res.setIsOpen24x7(h.getIsOpen24x7());

        return res;
    }
}