package de.jeb.japp.cv.service.parser.validator;

import de.jeb.japp.commons.exceptions.cv.CVUnsupportedDocumentTypeException;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * Pre-extraction validation. Mirrors the extension/content-type/size limits already enforced
 * at the upload boundary in CVServiceImpl - this is a defensive second check at the
 * extraction-pipeline boundary, for any caller that doesn't go through the upload flow.
 */
@Component
public class DefaultDocumentValidator implements DocumentValidator {

    private static final long MAX_FILE_SIZE_BYTES = 10L * 1024 * 1024;
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("pdf", "doc", "docx");
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    );

    @Override
    public void validate(String filename, String contentType, long size) {
        if (size <= 0) {
            throw new CVUnsupportedDocumentTypeException("The document is empty.");
        }
        if (size > MAX_FILE_SIZE_BYTES) {
            throw new CVUnsupportedDocumentTypeException("The document exceeds the 10 MB limit.");
        }

        String extension = extractExtension(filename);
        boolean extensionOk = ALLOWED_EXTENSIONS.contains(extension);
        boolean contentTypeOk = contentType != null && ALLOWED_CONTENT_TYPES.contains(contentType);

        if (!extensionOk || !contentTypeOk) {
            throw new CVUnsupportedDocumentTypeException(
                    "Unsupported document type. Allowed formats: PDF, DOC, DOCX.");
        }
    }

    private String extractExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    }
}
