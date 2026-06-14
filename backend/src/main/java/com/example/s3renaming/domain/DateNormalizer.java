package com.example.s3renaming.domain;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DateNormalizer {

    private static final Pattern EIGHT_DIGITS = Pattern.compile("(\\d{8})");
    private static final DateTimeFormatter OUTPUT = DateTimeFormatter.BASIC_ISO_DATE;

    public Optional<String> extractCandidate(String fileNameWithoutExtension) {
        if (fileNameWithoutExtension == null || fileNameWithoutExtension.isBlank()) {
            return Optional.empty();
        }
        Matcher matcher = EIGHT_DIGITS.matcher(fileNameWithoutExtension);
        if (matcher.find()) {
            return Optional.of(matcher.group(1));
        }
        return Optional.empty();
    }

    public DateNormalizationResult normalize(String candidate, DateStrategy strategy) {
        if (candidate == null || !candidate.matches("\\d{8}")) {
            return DateNormalizationResult.invalid("No se encontro una fecha de 8 digitos valida.");
        }
        return switch (strategy) {
            case YYYYMMDD -> normalizeAsYearMonthDay(candidate);
            case YYYYDDMM -> normalizeAsYearDayMonth(candidate);
            case AUTO -> normalizeAutomatically(candidate);
            case NONE -> DateNormalizationResult.invalid("La regla no requiere fecha.");
        };
    }

    private DateNormalizationResult normalizeAutomatically(String candidate) {
        DateNormalizationResult yearMonthDay = normalizeAsYearMonthDay(candidate);
        if (yearMonthDay.valid()) {
            return yearMonthDay;
        }
        DateNormalizationResult yearDayMonth = normalizeAsYearDayMonth(candidate);
        if (yearDayMonth.valid()) {
            return yearDayMonth;
        }
        return DateNormalizationResult.invalid("La fecha no coincide con YYYYMMDD ni YYYYDDMM.");
    }

    private DateNormalizationResult normalizeAsYearMonthDay(String candidate) {
        int year = Integer.parseInt(candidate.substring(0, 4));
        int month = Integer.parseInt(candidate.substring(4, 6));
        int day = Integer.parseInt(candidate.substring(6, 8));
        return buildDate(year, month, day, DateStrategy.YYYYMMDD);
    }

    private DateNormalizationResult normalizeAsYearDayMonth(String candidate) {
        int year = Integer.parseInt(candidate.substring(0, 4));
        int day = Integer.parseInt(candidate.substring(4, 6));
        int month = Integer.parseInt(candidate.substring(6, 8));
        return buildDate(year, month, day, DateStrategy.YYYYDDMM);
    }

    private DateNormalizationResult buildDate(int year, int month, int day, DateStrategy strategy) {
        try {
            return DateNormalizationResult.valid(LocalDate.of(year, month, day).format(OUTPUT), strategy);
        } catch (DateTimeException exception) {
            return DateNormalizationResult.invalid("Fecha invalida para estrategia " + strategy + ".");
        }
    }
}
