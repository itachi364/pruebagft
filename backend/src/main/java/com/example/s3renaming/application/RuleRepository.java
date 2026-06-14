package com.example.s3renaming.application;

import com.example.s3renaming.domain.MappingRule;
import java.util.List;
import java.util.Optional;

public interface RuleRepository {

    List<MappingRule> findAll();

    List<MappingRule> findActive();

    Optional<MappingRule> findLatestById(String ruleId);

    MappingRule save(MappingRule rule);
}

