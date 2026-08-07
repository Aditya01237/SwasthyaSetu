package com.medicine.auth.service;

import com.medicine.auth.dto.AdminLoginRequest;
import com.medicine.auth.dto.AdminLoginResponse;
import com.medicine.auth.dto.CreateHospitalAdminRequest;
import com.medicine.auth.entity.AdminAccount;
import com.medicine.auth.repository.AdminAccountRepository;
import com.medicine.auth.repository.HospitalRepository;
import com.medicine.auth.security.JwtUtil;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class AdminAuthService {

    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_HOSPITAL_ADMIN = "HOSPITAL_ADMIN";

    private final AdminAccountRepository adminAccountRepository;
    private final HospitalRepository hospitalRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AdminAuthService(AdminAccountRepository adminAccountRepository,
                            HospitalRepository hospitalRepository,
                            PasswordEncoder passwordEncoder,
                            JwtUtil jwtUtil) {
        this.adminAccountRepository = adminAccountRepository;
        this.hospitalRepository = hospitalRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @Transactional
    public AdminLoginResponse login(AdminLoginRequest request) {
        String email = normalizeEmail(request.getEmail());
        AdminAccount account = adminAccountRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Invalid admin credentials"));

        if (!account.isActive() || !passwordEncoder.matches(request.getPassword(), account.getPassword())) {
            throw new RuntimeException("Invalid admin credentials");
        }

        String token = jwtUtil.generateToken(
                String.valueOf(account.getId()),
                account.getRole(),
                account.getHospitalId()
        );

        return new AdminLoginResponse(
                token,
                account.getId(),
                account.getEmail(),
                account.getRole(),
                account.getHospitalId()
        );
    }

    @Transactional
    public Long createHospitalAdmin(CreateHospitalAdminRequest request) {
        String email = normalizeEmail(request.getEmail());
        validatePassword(request.getPassword());

        if (request.getHospitalId() == null || request.getHospitalId().isBlank()) {
            throw new RuntimeException("Hospital ID is required");
        }
        if (!hospitalRepository.existsById(request.getHospitalId())) {
            throw new RuntimeException("Hospital not found");
        }
        if (adminAccountRepository.existsByEmail(email)) {
            throw new RuntimeException("Admin account already exists");
        }

        AdminAccount account = new AdminAccount();
        account.setEmail(email);
        account.setPassword(passwordEncoder.encode(request.getPassword()));
        account.setRole(ROLE_HOSPITAL_ADMIN);
        account.setHospitalId(request.getHospitalId());
        account.setActive(true);
        return adminAccountRepository.save(account).getId();
    }

    @Transactional
    public void ensureBootstrapAdmin(String emailValue, String passwordValue) {
        if (emailValue == null || emailValue.isBlank() || passwordValue == null || passwordValue.isBlank()) {
            return;
        }

        String email = normalizeEmail(emailValue);
        if (adminAccountRepository.existsByEmail(email)) {
            return;
        }

        validatePassword(passwordValue);
        AdminAccount account = new AdminAccount();
        account.setEmail(email);
        account.setPassword(passwordEncoder.encode(passwordValue));
        account.setRole(ROLE_ADMIN);
        account.setHospitalId(null);
        account.setActive(true);
        adminAccountRepository.save(account);
    }

    private String normalizeEmail(String value) {
        if (value == null || value.isBlank()) {
            throw new RuntimeException("Email is required");
        }
        return value.trim().toLowerCase(Locale.ROOT);
    }

    private void validatePassword(String password) {
        if (password == null || password.length() < 10) {
            throw new RuntimeException("Admin password must be at least 10 characters");
        }
    }
}
