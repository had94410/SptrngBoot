package com.example.sptrngboot.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "ballet_consultations")
public class BalletConsultation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "source_page", nullable = false, length = 100)
    private String sourcePage;

    @Column(name = "preferred_area", length = 100)
    private String preferredArea;

    @Column(name = "age_group", length = 50)
    private String ageGroup;

    @Column(name = "level", length = 100)
    private String level;

    @Column(name = "customer_name", length = 255)
    private String customerName;

    @Column(name = "phone", length = 50)
    private String phone;

    @Column(name = "postal_code", length = 20)
    private String postalCode;

    @Column(name = "prefecture", length = 100)
    private String prefecture;

    @Column(name = "referral_source", length = 100)
    private String referralSource;

    @Column(name = "address", length = 500)
    private String address;

    @Column(name = "raw_payload", columnDefinition = "TEXT")
    private String rawPayload;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public BalletConsultation() {
    }

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSourcePage() {
        return sourcePage;
    }

    public void setSourcePage(String sourcePage) {
        this.sourcePage = sourcePage;
    }

    public String getPreferredArea() {
        return preferredArea;
    }

    public void setPreferredArea(String preferredArea) {
        this.preferredArea = preferredArea;
    }

    public String getAgeGroup() {
        return ageGroup;
    }

    public void setAgeGroup(String ageGroup) {
        this.ageGroup = ageGroup;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getPrefecture() {
        return prefecture;
    }

    public void setPrefecture(String prefecture) {
        this.prefecture = prefecture;
    }

    public String getReferralSource() {
        return referralSource;
    }

    public void setReferralSource(String referralSource) {
        this.referralSource = referralSource;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getRawPayload() {
        return rawPayload;
    }

    public void setRawPayload(String rawPayload) {
        this.rawPayload = rawPayload;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
