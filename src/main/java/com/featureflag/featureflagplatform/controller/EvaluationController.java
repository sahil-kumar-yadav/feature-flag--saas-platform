package com.featureflag.featureflagplatform.controller;

import com.featureflag.featureflagplatform.domain.Environment;
import com.featureflag.featureflagplatform.service.FeatureFlagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/evaluate")
public class EvaluationController {

    @Autowired
    private FeatureFlagService featureFlagService;

    @GetMapping("/{key}")
    public ResponseEntity<EvaluationResponse> evaluate(@PathVariable String key,
                                                       @RequestParam Environment environment,
                                                       @RequestParam String userId) {
        boolean enabled = featureFlagService.evaluateFlag(key, environment, userId);
        return ResponseEntity.ok(new EvaluationResponse(enabled));
    }

    public static class EvaluationResponse {
        private boolean enabled;

        public EvaluationResponse(boolean enabled) {
            this.enabled = enabled;
        }

        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
    }
}