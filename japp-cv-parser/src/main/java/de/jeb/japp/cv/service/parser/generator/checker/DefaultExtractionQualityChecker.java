package de.jeb.japp.cv.service.parser.generator.checker;

import org.springframework.stereotype.Component;

@Component
public class DefaultExtractionQualityChecker implements ExtractionQualityChecker {

    private static final int MIN_CHARACTERS = 20;
    private static final int MIN_TOTAL_WORDS = 100;
    private static final double MIN_ALPHANUMERIC_RATIO = 0.6;
    private static final double MAX_SINGLE_CHARACTER_SHARE = 0.3;

    @Override
    public ExtractionQuality check(String text) {
        if (text == null || text.isBlank() || text.trim().length() < MIN_CHARACTERS) {
            return ExtractionQuality.EMPTY;
        }

        String trimmed = text.trim();
        String[] words = trimmed.split("\\s+");

        if (words.length < MIN_TOTAL_WORDS) {
            return ExtractionQuality.POOR;
        }

        if (alphanumericRatio(trimmed) < MIN_ALPHANUMERIC_RATIO) {
            return ExtractionQuality.POOR;
        }

        if (dominantCharacterShare(trimmed) > MAX_SINGLE_CHARACTER_SHARE) {
            return ExtractionQuality.POOR;
        }

        return ExtractionQuality.GOOD;
    }

    private double alphanumericRatio(String text) {
        long nonWhitespace = 0;
        long alphanumeric = 0;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (Character.isWhitespace(c)) {
                continue;
            }
            nonWhitespace++;
            if (Character.isLetterOrDigit(c)) {
                alphanumeric++;
            }
        }
        return nonWhitespace == 0 ? 0.0 : (double) alphanumeric / nonWhitespace;
    }

    private double dominantCharacterShare(String text) {
        int[] counts = new int[Character.MAX_VALUE + 1];
        int total = 0;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (Character.isWhitespace(c)) {
                continue;
            }
            counts[c]++;
            total++;
        }
        if (total == 0) {
            return 0.0;
        }
        int max = 0;
        for (int count : counts) {
            if (count > max) {
                max = count;
            }
        }
        return (double) max / total;
    }
}
