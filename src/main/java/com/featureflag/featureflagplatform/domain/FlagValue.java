package com.featureflag.featureflagplatform.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "flag_values")
public class FlagValue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long flagId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Environment environment;

    private Boolean enabled; // for BOOLEAN type

    private Integer percentage; // for PERCENTAGE type

    private String userIds; // comma-separated for USER_TARGETED

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    private String updatedBy;

    // Constructors
    public FlagValue() {}

    public FlagValue(Long flagId, Environment environment, String updatedBy) {
        this.flagId = flagId;
        this.environment = environment;
        this.updatedAt = LocalDateTime.now();
        this.updatedBy = updatedBy;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getFlagId() { return flagId; }
    public void setFlagId(Long flagId) { this.flagId = flagId; }

    public Environment getEnvironment() { return environment; }
    public void setEnvironment(Environment environment) { this.environment = environment; }

    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }

    public Integer getPercentage() { return percentage; }
    public void setPercentage(Integer percentage) { this.percentage = percentage; }

    public String getUserIds() { return userIds; }
    public void setUserIds(String userIds) { this.userIds = userIds; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }
}