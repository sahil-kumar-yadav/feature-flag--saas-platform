package com.featureflag.featureflagplatform.repository;

import com.featureflag.featureflagplatform.domain.Environment;
import com.featureflag.featureflagplatform.domain.FlagValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FlagValueRepository extends JpaRepository<FlagValue, Long> {
    Optional<FlagValue> findByFlagIdAndEnvironment(Long flagId, Environment environment);
}