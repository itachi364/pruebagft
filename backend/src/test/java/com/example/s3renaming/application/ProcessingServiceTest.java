package com.example.s3renaming.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.s3renaming.domain.DateNormalizer;
import com.example.s3renaming.domain.DateStrategy;
import com.example.s3renaming.domain.MappingRule;
import com.example.s3renaming.domain.ProcessingStatus;
import com.example.s3renaming.domain.RuleEngine;
import com.example.s3renaming.infrastructure.memory.InMemoryProcessingRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class ProcessingServiceTest {

    @Test
    void processesProvidedFilesAndBuildsSummary() {
        InMemoryProcessingRepository processingRepository = new InMemoryProcessingRepository();
        ProcessingService service = new ProcessingService(
                (bucket, prefix) -> List.of("not-used"),
                new FakeRuleRepository(),
                processingRepository,
                new RuleEngine(new DateNormalizer()),
                "bucket"
        );

        BatchProcessingResponse response = service.process(new ProcessBatchCommand(
                "bucket",
                "",
                List.of("PHO_CD_DES_20260430", "unknown.txt")
        ));

        assertThat(response.batch().total()).isEqualTo(2);
        assertThat(response.batch().transformed()).isEqualTo(1);
        assertThat(response.batch().unmapped()).isEqualTo(1);
        assertThat(processingRepository.findResultsByBatchId(response.batch().batchId())).hasSize(2);
    }

    @Test
    void listsFilesFromStorageWhenNoFilesAreProvided() {
        ProcessingService service = new ProcessingService(
                (bucket, prefix) -> List.of(prefix + "PHO_CD_DES_20260430"),
                new FakeRuleRepository(),
                new InMemoryProcessingRepository(),
                new RuleEngine(new DateNormalizer()),
                "bucket"
        );

        assertThat(service.listFiles(null, "in/")).containsExactly("in/PHO_CD_DES_20260430");
    }

    @Test
    void reprocessesExistingBatchUsingCurrentRules() {
        InMemoryProcessingRepository processingRepository = new InMemoryProcessingRepository();
        ProcessingService service = new ProcessingService(
                (bucket, prefix) -> List.of(),
                new FakeRuleRepository(),
                processingRepository,
                new RuleEngine(new DateNormalizer()),
                "bucket"
        );
        BatchProcessingResponse first = service.process(new ProcessBatchCommand(
                "bucket",
                "",
                List.of("PHO_CD_DES_20260430")
        ));

        BatchProcessingResponse second = service.reprocess(first.batch().batchId());

        assertThat(second.batch().batchId()).isNotEqualTo(first.batch().batchId());
        assertThat(second.results()).extracting(result -> result.status()).containsExactly(ProcessingStatus.TRANSFORMADO);
    }

    @Test
    void rejectsReprocessWhenBatchDoesNotExist() {
        ProcessingService service = new ProcessingService(
                (bucket, prefix) -> List.of(),
                new FakeRuleRepository(),
                new InMemoryProcessingRepository(),
                new RuleEngine(new DateNormalizer()),
                "bucket"
        );

        assertThatThrownBy(() -> service.reprocess("missing"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("No existen resultados");
    }

    private static class FakeRuleRepository implements RuleRepository {
        private final List<MappingRule> rules = new ArrayList<>(List.of(
                new MappingRule("cdt", 1, "CDT", "PHO_CD_DES_*",
                        "01_Estructura CDT Desmaterializado_{date}", true, DateStrategy.AUTO, 10, true)
        ));

        @Override
        public List<MappingRule> findAll() {
            return List.copyOf(rules);
        }

        @Override
        public List<MappingRule> findActive() {
            return rules.stream().filter(MappingRule::active).toList();
        }

        @Override
        public Optional<MappingRule> findLatestById(String ruleId) {
            return rules.stream().filter(rule -> rule.ruleId().equals(ruleId)).findFirst();
        }

        @Override
        public MappingRule save(MappingRule rule) {
            rules.add(rule);
            return rule;
        }
    }
}

