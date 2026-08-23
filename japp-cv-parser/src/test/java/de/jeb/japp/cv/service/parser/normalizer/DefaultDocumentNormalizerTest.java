package de.jeb.japp.cv.service.parser.normalizer;

import de.jeb.japp.cv.service.parser.generator.DocumentType;
import de.jeb.japp.cv.service.parser.generator.ExtractedDocument;
import de.jeb.japp.cv.service.parser.generator.ExtractionMethod;
import de.jeb.japp.cv.service.parser.generator.checker.ExtractionQuality;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DefaultDocumentNormalizerTest {

    private final DefaultDocumentNormalizer normalizer = new DefaultDocumentNormalizer();

    private ExtractedDocument extracted(String text) {
        return new ExtractedDocument(text, "application/pdf", "cv.pdf", text == null ? 0 : text.length(),
                1, DocumentType.PDF, ExtractionMethod.TIKA, ExtractionQuality.GOOD);
    }

    @Test
    void normalizesWindowsAndMacLineEndings() {
        NormalizedDocument result = normalizer.normalize(extracted("line one\r\nline two\rline three"));
        assertThat(result.text()).isEqualTo("line one\nline two\nline three");
    }

    @Test
    void collapsesExcessiveBlankLines() {
        NormalizedDocument result = normalizer.normalize(extracted("para one\n\n\n\n\npara two"));
        assertThat(result.text()).isEqualTo("para one\n\npara two");
    }

    @Test
    void collapsesInteriorWhitespaceButKeepsIndentation() {
        NormalizedDocument result = normalizer.normalize(extracted("  Title:      Senior Engineer   "));
        assertThat(result.text()).isEqualTo("Title: Senior Engineer");
    }

    @Test
    void normalizesBulletGlyphsToASingleMarker() {
        NormalizedDocument result = normalizer.normalize(extracted("• first bullet\n● second bullet\n▪ third bullet"));
        assertThat(result.text()).isEqualTo("- first bullet\n- second bullet\n- third bullet");
    }

    @Test
    void normalizesCurlyQuotesDashesAndLigatures() {
        NormalizedDocument result = normalizer.normalize(extracted("It’s a “great” role – not just a job — for a proficient ﬁnance ofﬁcer"));
        assertThat(result.text()).isEqualTo("It's a \"great\" role - not just a job - for a proficient finance officer");
    }

    @Test
    void removesRepeatedShortHeaderFooterLines() {
        String text = """
                John Doe - Confidential
                Summary line one about experience
                John Doe - Confidential
                Summary line two about education
                John Doe - Confidential
                """;
        NormalizedDocument result = normalizer.normalize(extracted(text));
        long occurrences = result.text().lines().filter(line -> line.equals("John Doe - Confidential")).count();
        assertThat(occurrences).isEqualTo(1);
        assertThat(result.text()).contains("Summary line one about experience");
        assertThat(result.text()).contains("Summary line two about education");
    }

    @Test
    void doesNotTreatARepeatedRealContentLineAsAnArtifactWhenItsLong() {
        String longLine = "This exact sentence intentionally repeats because it genuinely appears twice in the CV content";
        String text = longLine + "\n" + longLine;
        NormalizedDocument result = normalizer.normalize(extracted(text));
        long occurrences = result.text().lines().filter(line -> line.equals(longLine)).count();
        assertThat(occurrences).isEqualTo(2);
    }

    @Test
    void handlesNullTextGracefully() {
        NormalizedDocument result = normalizer.normalize(extracted(null));
        assertThat(result.text()).isEmpty();
    }
}
