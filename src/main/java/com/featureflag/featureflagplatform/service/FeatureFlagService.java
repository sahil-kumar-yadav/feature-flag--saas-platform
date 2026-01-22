package com.featureflag.featureflagplatform.service;

import com.featureflag.featureflagplatform.domain.*;
import com.featureflag.featureflagplatform.repository.AuditLogRepository;
import com.featureflag.featureflagplatform.repository.FeatureFlagRepository;
import com.featureflag.featureflagplatform.repository.FlagValueRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
public class FeatureFlagService {

    @Autowired
    private FeatureFlagRepository featureFlagRepository;

    @Autowired
    private FlagValueRepository flagValueRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired(required = false)
    private RedisTemplate<String, Object> redisTemplate;

    private static final String FLAG_CACHE_PREFIX = "flag:";

    @Transactional
    public FeatureFlag createFlag(String key, String name, String description, FlagType type, String createdBy) {
        if (featureFlagRepository.findByKey(key).isPresent()) {
            throw new IllegalArgumentException("Flag key already exists");
        }
        FeatureFlag flag = new FeatureFlag(key, name, description, type, createdBy);
        flag = featureFlagRepository.save(flag);

        // Create default values for all environments
        for (Environment env : Environment.values()) {
            FlagValue value = new FlagValue(flag.getId(), env, createdBy);
            flagValueRepository.save(value);
        }

        auditLogRepository.save(new AuditLog(flag.getId(), null, "CREATE", null, "Flag created", createdBy));
        return flag;
    }

    public Optional<FeatureFlag> getFlag(String key) {
        return featureFlagRepository.findByKey(key);
    }

    public List<FeatureFlag> getAllFlags() {
        return featureFlagRepository.findAll();
    }

    @Transactional
    public void updateFlagValue(Long flagId, Environment environment, Object value, String updatedBy) {
        Optional<FeatureFlag> flagOpt = featureFlagRepository.findById(flagId);
        if (flagOpt.isEmpty()) {
            throw new IllegalArgumentException("Flag not found");
        }
        FeatureFlag flag = flagOpt.get();

        Optional<FlagValue> valueOpt = flagValueRepository.findByFlagIdAndEnvironment(flagId, environment);
        FlagValue flagValue = valueOpt.orElse(new FlagValue(flagId, environment, updatedBy));

        String oldValue = serializeValue(flagValue);

        switch (flag.getType()) {
            case BOOLEAN:
                flagValue.setEnabled((Boolean) value);
                break;
            case PERCENTAGE:
                flagValue.setPercentage((Integer) value);
                break;
            case USER_TARGETED:
                flagValue.setUserIds((String) value);
                break;
        }

        flagValue.setUpdatedAt(java.time.LocalDateTime.now());
        flagValue.setUpdatedBy(updatedBy);
        flagValueRepository.save(flagValue);

        String newValue = serializeValue(flagValue);
        auditLogRepository.save(new AuditLog(flagId, environment, "UPDATE", oldValue, newValue, updatedBy));

        // Invalidate cache
        if (redisTemplate != null) {
            redisTemplate.delete(FLAG_CACHE_PREFIX + flag.getKey() + ":" + environment);
        }
    }

    public boolean evaluateFlag(String key, Environment environment, String userId) {
        String cacheKey = FLAG_CACHE_PREFIX + key + ":" + environment;
        if (redisTemplate != null) {
            Boolean cached = (Boolean) redisTemplate.opsForValue().get(cacheKey);
            if (cached != null) {
                return cached;
            }
        }

        Optional<FeatureFlag> flagOpt = featureFlagRepository.findByKey(key);
        if (flagOpt.isEmpty()) {
            return false; // Default off
        }
        FeatureFlag flag = flagOpt.get();

        Optional<FlagValue> valueOpt = flagValueRepository.findByFlagIdAndEnvironment(flag.getId(), environment);
        if (valueOpt.isEmpty()) {
            return false;
        }
        FlagValue value = valueOpt.get();

        boolean result = evaluate(flag, value, userId);
        if (redisTemplate != null) {
            redisTemplate.opsForValue().set(cacheKey, result, 5, TimeUnit.MINUTES); // Cache for 5 min
        }
        return result;
    }

    private boolean evaluate(FeatureFlag flag, FlagValue value, String userId) {
        switch (flag.getType()) {
            case BOOLEAN:
                return value.getEnabled() != null ? value.getEnabled() : false;
            case PERCENTAGE:
                if (value.getPercentage() == null || value.getPercentage() == 0) return false;
                if (value.getPercentage() == 100) return true;
                int hash = Math.abs(userId.hashCode()) % 100;
                return hash < value.getPercentage();
            case USER_TARGETED:
                if (value.getUserIds() == null) return false;
                return value.getUserIds().contains(userId);
            default:
                return false;
        }
    }

    private String serializeValue(FlagValue value) {
        if (value.getEnabled() != null) return value.getEnabled().toString();
        if (value.getPercentage() != null) return value.getPercentage().toString();
        return value.getUserIds();
    }

    public List<AuditLog> getAuditLogs(Long flagId) {
        return auditLogRepository.findByFlagIdOrderByTimestampDesc(flagId);
    }
}