package de.jeb.japp.application.service;

import de.jeb.japp.model.application.Application;
import de.jeb.japp.model.job.Job;
import de.jeb.japp.model.tag.Tag;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Renders a list of Applications as RFC-4180-ish CSV text — hand-rolled rather than pulling in a
 * CSV library, since the only requirement is quoting fields that contain a comma, quote, or
 * newline, which is a handful of lines. No authorization, no persistence: the caller (
 * {@link ApplicationService#exportCsv}) is responsible for passing in an already-scoped list.
 */
final class ApplicationCsvExporter {

    private static final String[] HEADER = {
            "Company", "Job Title", "Status", "Applied Date", "Deadline", "Follow-up Date",
            "Contact Person", "Tags", "Notes"
    };

    private ApplicationCsvExporter() {
    }

    static String toCsv(List<Application> applications) {
        StringBuilder csv = new StringBuilder();
        writeRow(csv, HEADER);
        for (Application application : applications) {
            writeRow(csv, rowFor(application));
        }
        return csv.toString();
    }

    private static String[] rowFor(Application application) {
        Job job = application.getJob();
        return new String[]{
                job != null && job.getCompany() != null ? job.getCompany().getName() : "",
                job != null ? job.getTitle() : "",
                application.getStatus() != null ? application.getStatus().name() : "",
                orEmpty(application.getAppliedAt()),
                orEmpty(application.getDeadline()),
                orEmpty(application.getFollowUpDate()),
                orEmpty(application.getContactPerson()),
                tagsOf(application),
                orEmpty(application.getNotes()),
        };
    }

    private static String tagsOf(Application application) {
        if (application.getTags() == null || application.getTags().isEmpty()) {
            return "";
        }
        return application.getTags().stream()
                .map(Tag::getName)
                .sorted(Comparator.naturalOrder())
                .collect(Collectors.joining("; "));
    }

    private static String orEmpty(Object value) {
        return value != null ? value.toString() : "";
    }

    private static void writeRow(StringBuilder csv, String[] fields) {
        for (int i = 0; i < fields.length; i++) {
            if (i > 0) {
                csv.append(',');
            }
            csv.append(quote(fields[i]));
        }
        csv.append("\r\n");
    }

    private static String quote(String field) {
        String safe = field != null ? field : "";
        return "\"" + safe.replace("\"", "\"\"") + "\"";
    }
}
