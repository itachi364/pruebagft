package com.example.s3renaming.infrastructure.memory;

import com.example.s3renaming.application.RuleRepository;
import com.example.s3renaming.domain.DateStrategy;
import com.example.s3renaming.domain.MappingRule;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

@Repository
@ConditionalOnProperty(prefix = "app.adapters", name = "persistence", havingValue = "memory", matchIfMissing = true)
public class InMemoryRuleRepository implements RuleRepository {

    private final List<MappingRule> rules = new ArrayList<>();

    public InMemoryRuleRepository() {
        rules.add(new MappingRule("rule-pho-cd-des", 1, "CDT Desmaterializado", "PHO_CD_DES_*",
                "01_Estructura CDT Desmaterializado_{date}", true, DateStrategy.AUTO, 10, true));
        rules.add(new MappingRule("rule-pho-sv", 1, "Cuenta Ahorros", "PHO_SV_*",
                "03_Estructura Cuenta Ahorros_{date}", true, DateStrategy.AUTO, 20, true));
        rules.add(new MappingRule("rule-pho-ck", 1, "Cuenta Corriente", "PHO_CK_*",
                "04_Estructura Cuenta Corriente_{date}", true, DateStrategy.AUTO, 30, true));
        rules.add(new MappingRule("rule-pho-ml-util", 1, "Creditos Utilizacion", "PHO_ML_UTIL_*",
                "13_CREDITOS UTILIZACION_{date}", true, DateStrategy.AUTO, 40, true));
        rules.add(new MappingRule("rule-cuotas-bdb", 1, "Cuotas Activos", "cuotas_bdb_*",
                "13_CUOTAS Activos", false, DateStrategy.NONE, 50, true));
        rules.add(new MappingRule("rule-garantias-solo-firma", 1, "Garantias Solo Firma", "garantias_solo_firma_*",
                "14_Solo Firma_{date}", true, DateStrategy.YYYYDDMM, 60, true));
        rules.add(new MappingRule("rule-activos-vehiculo", 1, "Leasing Vehiculo", "activos_vehiculo_*",
                "37_Leasing_Vehiculo_{date}", true, DateStrategy.AUTO, 70, true));
    }

    @Override
    public List<MappingRule> findAll() {
        return List.copyOf(rules);
    }

    @Override
    public List<MappingRule> findActive() {
        return rules.stream()
                .filter(MappingRule::active)
                .sorted(Comparator.comparingInt(MappingRule::priority))
                .toList();
    }

    @Override
    public Optional<MappingRule> findLatestById(String ruleId) {
        return rules.stream()
                .filter(rule -> rule.ruleId().equals(ruleId))
                .max(Comparator.comparingInt(MappingRule::version));
    }

    @Override
    public MappingRule save(MappingRule rule) {
        rules.add(rule);
        return rule;
    }
}

