package de.jeb.japp.cv.service.parser.normalizer;

import de.jeb.japp.cv.service.parser.generator.ExtractedDocument;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Pure text hygiene: whitespace, line endings, bullets, encoding artifacts and repeated
 * header/footer lines. Deliberately has no notion of CV sections, dates, or semantics -
 * that belongs to the future LLM parsing layer.
 */
@Component
public class DefaultDocumentNormalizer implements DocumentNormalizer {

    // Curly quotes
    private static final char CURLY_QUOTE_LEFT_SINGLE = '‘';
    private static final char CURLY_QUOTE_RIGHT_SINGLE = '’';
    private static final char CURLY_QUOTE_LEFT_DOUBLE = '“';
    private static final char CURLY_QUOTE_RIGHT_DOUBLE = '”';
    // Dashes
    private static final char EN_DASH = '–';
    private static final char EM_DASH = '—';
    // Non-breaking space
    private static final char NON_BREAKING_SPACE = ' ';
    // Unicode replacement character (mojibake artifact)
    private static final char REPLACEMENT_CHARACTER = '�';
    // Ligatures
    private static final String LIGATURE_FI = "ﬁ";
    private static final String LIGATURE_FL = "ﬂ";
    // Bullet glyphs: bullet, black circle, black small square, triangular bullet, bullet operator, white bullet
    private static final String BULLET_CHAR_CLASS =
            "[•●▪‣∙◦*-]";

    private static final Pattern BULLET_PREFIX = Pattern.compile("^[\\s]*" + BULLET_CHAR_CLASS + "\\s+");
    private static final Pattern INTERIOR_WHITESPACE_RUN = Pattern.compile("[ \\t]{2,}");
    private static final Pattern BLANK_LINE_RUN = Pattern.compile("\\n{3,}");
    private static final int MAX_HEADER_FOOTER_LINE_LENGTH = 60;
    private static final int REPEATED_LINE_THRESHOLD = 3;

    @Override
    public NormalizedDocument normalize(ExtractedDocument document) {
        String text = document.text() == null ? "" : document.text();

        text = normalizeLineEndings(text);
        text = normalizeEncodingArtifacts(text);
        text = normalizeLines(text);
        text = removeRepeatedHeaderFooterLines(text);
        text = collapseBlankLineRuns(text);

        return new NormalizedDocument(text.trim(), document.filename(), document.contentType());
    }

    private String normalizeLineEndings(String text) {
        return text.replace("\r\n", "\n").replace('\r', '\n');
    }

    private String normalizeEncodingArtifacts(String text) {
        return text
                .replace(CURLY_QUOTE_LEFT_SINGLE, '\'').replace(CURLY_QUOTE_RIGHT_SINGLE, '\'')
                .replace(CURLY_QUOTE_LEFT_DOUBLE, '"').replace(CURLY_QUOTE_RIGHT_DOUBLE, '"')
                .replace(EN_DASH, '-').replace(EM_DASH, '-')
                .replace(NON_BREAKING_SPACE, ' ')
                .replace(LIGATURE_FI, "fi").replace(LIGATURE_FL, "fl")
                .replace(String.valueOf(REPLACEMENT_CHARACTER), "");
    }

    private String normalizeLines(String text) {
        String[] lines = text.split("\n", -1);
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < lines.length; i++) {
            result.append(normalizeLine(lines[i]));
            if (i < lines.length - 1) {
                result.append('\n');
            }
        }
        return result.toString();
    }

    private String normalizeLine(String line) {
        int leadingWhitespace = 0;
        while (leadingWhitespace < line.length() && Character.isWhitespace(line.charAt(leadingWhitespace))) {
            leadingWhitespace++;
        }
        String leading = line.substring(0, leadingWhitespace);
        String rest = line.substring(leadingWhitespace);

        rest = INTERIOR_WHITESPACE_RUN.matcher(rest).replaceAll(" ");

        String normalized = leading + rest;
        normalized = BULLET_PREFIX.matcher(normalized).replaceFirst(leading + "- ");

        return stripTrailingWhitespace(normalized);
    }

    private String stripTrailingWhitespace(String line) {
        int end = line.length();
        while (end > 0 && Character.isWhitespace(line.charAt(end - 1))) {
            end--;
        }
        return line.substring(0, end);
    }

    private String removeRepeatedHeaderFooterLines(String text) {
        String[] lines = text.split("\n", -1);
        Map<String, Integer> counts = new HashMap<>();
        for (String line : lines) {
            String trimmed = line.trim();
            if (!trimmed.isEmpty() && trimmed.length() <= MAX_HEADER_FOOTER_LINE_LENGTH) {
                counts.merge(trimmed, 1, Integer::sum);
            }
        }

        Map<String, Boolean> seen = new HashMap<>();
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < lines.length; i++) {
            String trimmed = lines[i].trim();
            Integer count = counts.get(trimmed);
            boolean isRepeatedArtifact = count != null && count >= REPEATED_LINE_THRESHOLD;

            if (isRepeatedArtifact) {
                if (Boolean.TRUE.equals(seen.get(trimmed))) {
                    continue;
                }
                seen.put(trimmed, true);
            }

            result.append(lines[i]);
            if (i < lines.length - 1) {
                result.append('\n');
            }
        }
        return result.toString();
    }

    private String collapseBlankLineRuns(String text) {
        return BLANK_LINE_RUN.matcher(text).replaceAll("\n\n");
    }
}
