package com.medicine.auth.config;

import com.medicine.auth.service.AdminAuthService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class AdminBootstrapConfig implements ApplicationRunner {

    private final AdminAuthService adminAuthService;
    private final String bootstrapEmail;
    private final String bootstrapPassword;

    public AdminBootstrapConfig(
            AdminAuthService adminAuthService,
            @Value("${app.bootstrap-admin.email:}") String bootstrapEmail,
            @Value("${app.bootstrap-admin.password:}") String bootstrapPassword) {
        this.adminAuthService = adminAuthService;
        this.bootstrapEmail = bootstrapEmail;
        this.bootstrapPassword = bootstrapPassword;
    }

    @Override
    public void run(ApplicationArguments args) {
        adminAuthService.ensureBootstrapAdmin(bootstrapEmail, bootstrapPassword);
    }
}
