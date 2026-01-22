package com.featureflag.featureflagplatform.controller;

import com.featureflag.featureflagplatform.domain.*;
import com.featureflag.featureflagplatform.service.FeatureFlagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/flags")
public class FeatureFlagController {

    @Autowired
    private FeatureFlagService featureFlagService;

    @PostMapping
    public ResponseEntity<FeatureFlag> createFlag(@RequestBody CreateFlagRequest request) {
        FeatureFlag flag = featureFlagService.createFlag(request.getKey(), request.getName(),
                request.getDescription(), request.getType(), request.getCreatedBy());
        return ResponseEntity.ok(flag);
    }

    @GetMapping
    public ResponseEntity<List<FeatureFlag>> getAllFlags() {
        return ResponseEntity.ok(featureFlagService.getAllFlags());
    }

    @PutMapping("/{flagId}/environments/{environment}")
    public ResponseEntity<Void> updateFlagValue(@PathVariable Long flagId,
                                                @PathVariable Environment environment,
                                                @RequestBody UpdateFlagRequest request) {
        featureFlagService.updateFlagValue(flagId, environment, request.getValue(), request.getUpdatedBy());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{flagId}/audit")
    public ResponseEntity<List<AuditLog>> getAuditLogs(@PathVariable Long flagId) {
        return ResponseEntity.ok(featureFlagService.getAuditLogs(flagId));
    }

    // DTOs
    public static class CreateFlagRequest {
        private String key;
        private String name;
        private String description;
        private FlagType type;
        private String createdBy;

        // getters and setters
        public String getKey() { return key; }
        public void setKey(String key) { this.key = key; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public FlagType getType() { return type; }
        public void setType(FlagType type) { this.type = type; }
        public String getCreatedBy() { return createdBy; }
        public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    }

    public static class UpdateFlagRequest {
        private Object value;
        private String updatedBy;

        public Object getValue() { return value; }
        public void setValue(Object value) { this.value = value; }
        public String getUpdatedBy() { return updatedBy; }
        public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }
    }
}