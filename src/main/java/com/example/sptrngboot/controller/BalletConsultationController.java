package com.example.sptrngboot.controller;

import com.example.sptrngboot.model.BalletConsultation;
import com.example.sptrngboot.repository.BalletConsultationRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class BalletConsultationController {
    private final BalletConsultationRepository balletConsultationRepository;
    private final ObjectMapper objectMapper;

    public BalletConsultationController(BalletConsultationRepository balletConsultationRepository, ObjectMapper objectMapper) {
        this.balletConsultationRepository = balletConsultationRepository;
        this.objectMapper = objectMapper;
    }

    @PostMapping("/ballet/consultations")
    public ResponseEntity<Map<String, Object>> saveConsultation(@RequestBody Map<String, Object> payload) {
        if (payload == null || payload.isEmpty()) {
            return ResponseEntity.badRequest().body(errorResponse("リクエストが空です。"));
        }

        String sourcePage = firstNonBlank(asString(payload.get("sourcePage")), asString(payload.get("source_page")), "unknown");
        String preferredArea = firstNonBlank(asString(payload.get("preferredArea")), asString(payload.get("preferred_area")), asString(payload.get("area")));
        String ageGroup = firstNonBlank(asString(payload.get("ageGroup")), asString(payload.get("age_group")), asString(payload.get("age")));
        String level = firstNonBlank(asString(payload.get("level")));
        String customerName = firstNonBlank(
            asString(payload.get("customerName")),
            asString(payload.get("name")),
            joinName(asString(payload.get("sei")), asString(payload.get("mei")))
        );
        String phone = firstNonBlank(asString(payload.get("phone")), asString(payload.get("tel")));
        String postalCode = firstNonBlank(asString(payload.get("postalCode")), asString(payload.get("zip")));
        String prefecture = firstNonBlank(asString(payload.get("prefecture")), asString(payload.get("pref")));
        String referralSource = firstNonBlank(asString(payload.get("referralSource")), asString(payload.get("ref")));
        String address = firstNonBlank(asString(payload.get("address")));

        if (customerName.isBlank() && (payload.get("sei") != null || payload.get("mei") != null)) {
            customerName = joinName(asString(payload.get("sei")), asString(payload.get("mei")));
        }

        if (customerName.isBlank() && (phone.isBlank() && preferredArea.isBlank() && ageGroup.isBlank() && level.isBlank())) {
            return ResponseEntity.badRequest().body(errorResponse("保存に必要なデータがありません。"));
        }

        BalletConsultation consultation = new BalletConsultation();
        consultation.setSourcePage(sourcePage);
        consultation.setPreferredArea(preferredArea);
        consultation.setAgeGroup(ageGroup);
        consultation.setLevel(level);
        consultation.setCustomerName(customerName);
        consultation.setPhone(phone);
        consultation.setPostalCode(postalCode);
        consultation.setPrefecture(prefecture);
        consultation.setReferralSource(referralSource);
        consultation.setAddress(address);
        try {
            consultation.setRawPayload(objectMapper.writeValueAsString(payload));
        } catch (JsonProcessingException e) {
            consultation.setRawPayload(payload.toString());
        }

        BalletConsultation saved = balletConsultationRepository.save(consultation);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "相談内容をDBに登録しました。");
        response.put("id", saved.getId());
        response.put("sourcePage", saved.getSourcePage());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    private String joinName(String sei, String mei) {
        StringBuilder builder = new StringBuilder();
        if (!sei.isBlank()) {
            builder.append(sei.trim());
        }
        if (!mei.isBlank()) {
            if (!builder.isEmpty()) {
                builder.append(" ");
            }
            builder.append(mei.trim());
        }
        return builder.toString();
    }

    private String firstNonBlank(String... values) {
        if (values == null) {
            return "";
        }
        for (String value : values) {
            if (value != null && !value.trim().isEmpty()) {
                return value.trim();
            }
        }
        return "";
    }

    private String asString(Object value) {
        if (value == null) {
            return "";
        }
        return String.valueOf(value).trim();
    }

    private Map<String, Object> errorResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", message);
        return response;
    }
}
