package com.example.s3renaming.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import org.junit.jupiter.api.Test;

class DateNormalizerTest {

    private final DateNormalizer dateNormalizer = new DateNormalizer();

    @Test
    void extractsFirstEightDigitCandidate() {
        Optional<String> candidate = dateNormalizer.extractCandidate("PHO_CD_DES_20260430");

        assertThat(candidate).contains("20260430");
    }

    @Test
    void returnsEmptyWhenNoCandidateExists() {
        assertThat(dateNormalizer.extractCandidate("PrendasPajaro")).isEmpty();
        assertThat(dateNormalizer.extractCandidate(" ")).isEmpty();
        assertThat(dateNormalizer.extractCandidate(null)).isEmpty();
    }

    @Test
    void normalizesYearMonthDay() {
        DateNormalizationResult result = dateNormalizer.normalize("20260430", DateStrategy.YYYYMMDD);

        assertThat(result.valid()).isTrue();
        assertThat(result.normalizedDate()).isEqualTo("20260430");
        assertThat(result.strategyUsed()).isEqualTo(DateStrategy.YYYYMMDD);
    }

    @Test
    void normalizesYearDayMonth() {
        DateNormalizationResult result = dateNormalizer.normalize("20263004", DateStrategy.YYYYDDMM);

        assertThat(result.valid()).isTrue();
        assertThat(result.normalizedDate()).isEqualTo("20260430");
        assertThat(result.strategyUsed()).isEqualTo(DateStrategy.YYYYDDMM);
    }

    @Test
    void autoFallsBackToYearDayMonth() {
        DateNormalizationResult result = dateNormalizer.normalize("20263004", DateStrategy.AUTO);

        assertThat(result.valid()).isTrue();
        assertThat(result.normalizedDate()).isEqualTo("20260430");
    }

    @Test
    void rejectsInvalidCandidates() {
        assertThat(dateNormalizer.normalize(null, DateStrategy.AUTO).valid()).isFalse();
        assertThat(dateNormalizer.normalize("abc", DateStrategy.AUTO).valid()).isFalse();
        assertThat(dateNormalizer.normalize("20269999", DateStrategy.YYYYMMDD).valid()).isFalse();
        assertThat(dateNormalizer.normalize("20269999", DateStrategy.YYYYDDMM).valid()).isFalse();
        assertThat(dateNormalizer.normalize("20269999", DateStrategy.AUTO).valid()).isFalse();
        assertThat(dateNormalizer.normalize("20260430", DateStrategy.NONE).valid()).isFalse();
    }
}
