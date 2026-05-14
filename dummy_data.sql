-- Insert Hospital
INSERT INTO hospitals (id, address, city, name, is_open24x7, rating, total_reviews)
VALUES ('HOSP123', '123 Health Street', 'Metropolis', 'City General Hospital', true, 4.5, 120)
ON CONFLICT DO NOTHING;

-- Insert Doctor (Password is 'password123')
INSERT INTO doctors (email, experience, fee, name, password, specialization, hospital_id)
VALUES ('adityapareek874@gmail.com', 15, 500, 'Dr. John Smith', '$2a$10$p0M9M5m9xK.zS5Vl/jM2bOfGq0K2d.o1w/z4t/1x31j40J9kP.V3K', 'Cardiologist', 'HOSP123')
ON CONFLICT DO NOTHING;

-- Insert Patient (Login via OTP using phone '1234567890')
INSERT INTO patients (age, email, gender, name, phone, uhid)
VALUES (35, 'adityapareek874@gmail.com', 'Female', 'Jane Doe', '1234567890', 'UHID-987654321')
ON CONFLICT DO NOTHING;

-- Insert Appointment (Scheduled for Tomorrow)
INSERT INTO appointments (appointment_time, doctor_id, hospital_id, patient_id)
SELECT NOW() + INTERVAL '1 day', d.id, 'HOSP123', p.id
FROM doctors d, patients p
WHERE d.email = 'adityapareek874@gmail.com' AND p.uhid = 'UHID-987654321'
ON CONFLICT DO NOTHING;

-- Insert Appointment (Completed yesterday)
INSERT INTO appointments (appointment_time, doctor_id, hospital_id, patient_id)
SELECT NOW() - INTERVAL '1 day', d.id, 'HOSP123', p.id
FROM doctors d, patients p
WHERE d.email = 'adityapareek874@gmail.com' AND p.uhid = 'UHID-987654321'
ON CONFLICT DO NOTHING;

-- Insert QR Token for the completed appointment
INSERT INTO qr_tokens (token, used, valid_from, valid_to, appointment_id, patient_id)
SELECT 'DUMMY-QR-TOKEN-OLD', true, NOW() - INTERVAL '25 hours', NOW() - INTERVAL '22 hours', a.id, a.patient_id
FROM appointments a
JOIN patients p ON a.patient_id = p.id
WHERE p.uhid = 'UHID-987654321' AND a.appointment_time < NOW()
ON CONFLICT DO NOTHING;

-- Insert QR Token for the upcoming appointment
INSERT INTO qr_tokens (token, used, valid_from, valid_to, appointment_id, patient_id)
SELECT 'DUMMY-QR-TOKEN-NEW', false, NOW() + INTERVAL '23 hours', NOW() + INTERVAL '27 hours', a.id, a.patient_id
FROM appointments a
JOIN patients p ON a.patient_id = p.id
WHERE p.uhid = 'UHID-987654321' AND a.appointment_time > NOW()
ON CONFLICT DO NOTHING;
