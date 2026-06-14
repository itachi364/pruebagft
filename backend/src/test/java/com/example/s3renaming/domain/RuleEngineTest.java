package com.example.s3renaming.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RuleEngineTest {

    private RuleEngine ruleEngine;

    @BeforeEach
    void setUp() {
        ruleEngine = new RuleEngine(new DateNormalizer());
    }

    @Test
    void transformsKnownPatternsAndNormalizesDate() {
        List<MappingRule> rules = baseRules();

        assertThat(ruleEngine.transform("batch", "PHO_CD_DES_20260430", rules).targetFileName())
                .isEqualTo("01_Estructura CDT Desmaterializado_20260430");
        assertThat(ruleEngine.transform("batch", "PHO_SV_20260430", rules).targetFileName())
                .isEqualTo("03_Estructura Cuenta Ahorros_20260430");
        assertThat(ruleEngine.transform("batch", "PHO_CK_20260430", rules).targetFileName())
                .isEqualTo("04_Estructura Cuenta Corriente_20260430");
    }

    @Test
    void ignoresTxtExtensionAndUsesSubtypeRule() {
        TransformationResult result = ruleEngine.transform("batch", "PHO_ML_UTIL_20260430.txt", baseRules());

        assertThat(result.status()).isEqualTo(ProcessingStatus.TRANSFORMADO);
        assertThat(result.targetFileName()).isEqualTo("13_CREDITOS UTILIZACION_20260430");
        assertThat(result.ruleId()).isEqualTo("ml");
    }

    @Test
    void transformsRuleThatDoesNotRequireDate() {
        TransformationResult result = ruleEngine.transform("batch", "cuotas_bdb_20260430.txt", baseRules());

        assertThat(result.status()).isEqualTo(ProcessingStatus.TRANSFORMADO);
        assertThat(result.targetFileName()).isEqualTo("13_CUOTAS Activos");
    }

    @Test
    void convertsYearDayMonthWhenRuleRequiresIt() {
        TransformationResult result = ruleEngine.transform("batch", "garantias_solo_firma_20263004.txt", baseRules());

        assertThat(result.status()).isEqualTo(ProcessingStatus.TRANSFORMADO);
        assertThat(result.targetFileName()).isEqualTo("14_Solo Firma_20260430");
    }

    @Test
    void marksFileAsUnmappedWhenNoActiveRuleMatches() {
        TransformationResult result = ruleEngine.transform("batch", "PrendasPajaro.txt", baseRules());

        assertThat(result.status()).isEqualTo(ProcessingStatus.NO_MAPEADO);
        assertThat(result.message()).contains("No existe");
    }

    @Test
    void ignoresInactiveRules() {
        MappingRule inactive = new MappingRule("inactive", 1, "Inactive", "PHO_CD_DES_*",
                "x_{date}", true, DateStrategy.AUTO, 1, false);

        TransformationResult result = ruleEngine.transform("batch", "PHO_CD_DES_20260430", List.of(inactive));

        assertThat(result.status()).isEqualTo(ProcessingStatus.NO_MAPEADO);
    }

    @Test
    void returnsErrorWhenDateIsRequiredButMissing() {
        TransformationResult result = ruleEngine.transform("batch", "PHO_CD_DES_", baseRules());

        assertThat(result.status()).isEqualTo(ProcessingStatus.ERROR);
        assertThat(result.message()).contains("requiere fecha");
    }

    @Test
    void returnsErrorWhenEmbeddedDateIsInvalid() {
        TransformationResult result = ruleEngine.transform("batch", "PHO_CD_DES_20269999", baseRules());

        assertThat(result.status()).isEqualTo(ProcessingStatus.ERROR);
        assertThat(result.message()).contains("no coincide").contains("YYYYMMDD");
    }

    @Test
    void returnsErrorForBlankFileName() {
        TransformationResult result = ruleEngine.transform("batch", " ", baseRules());

        assertThat(result.status()).isEqualTo(ProcessingStatus.ERROR);
        assertThat(result.message()).contains("obligatorio");

        TransformationResult nullResult = ruleEngine.transform("batch", null, baseRules());
        assertThat(nullResult.status()).isEqualTo(ProcessingStatus.ERROR);
    }

    @Test
    void returnsUnmappedWhenThereAreNoRules() {
        TransformationResult result = ruleEngine.transform("batch", "PHO_CD_DES_20260430", List.of());

        assertThat(result.status()).isEqualTo(ProcessingStatus.NO_MAPEADO);
        assertThat(result.message()).contains("No hay reglas");

        TransformationResult nullRulesResult = ruleEngine.transform("batch", "PHO_CD_DES_20260430", null);
        assertThat(nullRulesResult.status()).isEqualTo(ProcessingStatus.NO_MAPEADO);
    }

    @Test
    void utilityBranchesHandleNullsAndPaths() {
        assertThat(FileNameNormalizer.removeExtension(null)).isEmpty();
        assertThat(FileNameNormalizer.removeExtension(" ")).isEmpty();
        assertThat(FileNameNormalizer.removeExtension("folder.name/PHO_CD_DES_20260430")).isEqualTo("folder.name/PHO_CD_DES_20260430");
        assertThat(WildcardPatternMatcher.matches(null, "value")).isFalse();
        assertThat(WildcardPatternMatcher.matches(" ", "value")).isFalse();
        assertThat(WildcardPatternMatcher.matches("PHO_*", null)).isFalse();
        assertThat(WildcardPatternMatcher.matches("PHO_SV_*", "PHO_CD_DES_20260430")).isFalse();
    }

    @Test
    void choosesHighestPriorityActiveRule() {
        List<MappingRule> rules = List.of(
                new MappingRule("late", 1, "Late", "PHO_CD_DES_*", "late_{date}", true, DateStrategy.AUTO, 20, true),
                new MappingRule("early", 1, "Early", "PHO_CD_DES_*", "early_{date}", true, DateStrategy.AUTO, 1, true)
        );

        TransformationResult result = ruleEngine.transform("batch", "PHO_CD_DES_20260430", rules);

        assertThat(result.ruleId()).isEqualTo("early");
        assertThat(result.targetFileName()).isEqualTo("early_20260430");
    }

    private List<MappingRule> baseRules() {
        return List.of(
                new MappingRule("cdt", 1, "CDT", "PHO_CD_DES_*",
                        "01_Estructura CDT Desmaterializado_{date}", true, DateStrategy.AUTO, 10, true),
                new MappingRule("sv", 1, "SV", "PHO_SV_*",
                        "03_Estructura Cuenta Ahorros_{date}", true, DateStrategy.AUTO, 20, true),
                new MappingRule("ck", 1, "CK", "PHO_CK_*",
                        "04_Estructura Cuenta Corriente_{date}", true, DateStrategy.AUTO, 30, true),
                new MappingRule("ml", 1, "ML", "PHO_ML_UTIL_*",
                        "13_CREDITOS UTILIZACION_{date}", true, DateStrategy.AUTO, 40, true),
                new MappingRule("cuotas", 1, "Cuotas", "cuotas_bdb_*",
                        "13_CUOTAS Activos", false, DateStrategy.NONE, 50, true),
                new MappingRule("garantias", 1, "Garantias", "garantias_solo_firma_*",
                        "14_Solo Firma_{date}", true, DateStrategy.YYYYDDMM, 60, true)
        );
    }
}
