package de.jeb.japp.cv.service.parser.support;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

/** Builds small, realistic fixture documents in-memory using the same libraries the generators exercise. */
public final class FixtureFiles {

    public static final List<String> CV_LINES = List.of(
            "John Doe",
            "Senior Software Engineer",
            "",
            "Summary",
            "Experienced backend engineer with a decade of experience building distributed systems,",
            "REST APIs, and cloud native platforms using Java, Spring Boot, and PostgreSQL.",
            "",
            "Experience",
            "Acme Corp - Senior Software Engineer (2019 - Present)",
            "Designed and implemented microservices handling millions of requests per day.",
            "Led migration from a monolithic architecture to a service oriented architecture.",
            "Mentored junior engineers and conducted code reviews across multiple teams.",
            "",
            "Globex Inc - Software Engineer (2015 - 2019)",
            "Built internal tooling for continuous integration and deployment pipelines.",
            "Collaborated with product managers to define and ship new features.",
            "",
            "Education",
            "Bachelor of Science in Computer Science, State University, 2015",
            "",
            "Skills",
            "Java, Spring Boot, PostgreSQL, Docker, Kubernetes, AWS, REST APIs, Git"
    );

    private FixtureFiles() {
    }

    public static byte[] goodPdf() throws IOException {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.LETTER);
            document.addPage(page);

            try (PDPageContentStream stream = new PDPageContentStream(document, page)) {
                stream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 11);
                stream.beginText();
                stream.setLeading(14f);
                stream.newLineAtOffset(50, 740);
                for (String line : CV_LINES) {
                    stream.showText(line.isBlank() ? " " : line);
                    stream.newLine();
                }
                stream.endText();
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            document.save(out);
            return out.toByteArray();
        }
    }

    public static byte[] emptyPdf() throws IOException {
        try (PDDocument document = new PDDocument()) {
            document.addPage(new PDPage(PDRectangle.LETTER));
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            document.save(out);
            return out.toByteArray();
        }
    }

    public static byte[] goodDocx() throws IOException {
        try (XWPFDocument document = new XWPFDocument()) {
            for (String line : CV_LINES) {
                XWPFParagraph paragraph = document.createParagraph();
                XWPFRun run = paragraph.createRun();
                run.setText(line);
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            document.write(out);
            return out.toByteArray();
        }
    }

    public static byte[] emptyDocx() throws IOException {
        try (XWPFDocument document = new XWPFDocument()) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            document.write(out);
            return out.toByteArray();
        }
    }

    public static byte[] truncate(byte[] content, int keepBytes) {
        byte[] truncated = new byte[Math.min(keepBytes, content.length)];
        System.arraycopy(content, 0, truncated, 0, truncated.length);
        return truncated;
    }
}
