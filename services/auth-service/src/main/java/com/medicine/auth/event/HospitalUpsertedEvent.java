package com.medicine.auth.event;

import java.util.List;

public record HospitalUpsertedEvent(
        String id,
        String name,
        String city,
        String address,
        String phone,
        String email,
        List<String> imageUrls,
        Double rating,
        Integer totalReviews,
        List<String> services,
        List<String> specializations,
        Boolean isOpen24x7
) {
}
