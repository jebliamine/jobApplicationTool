package de.jeb.japp.model.search.dto;

import de.jeb.japp.model.search.SearchResultType;

import java.util.UUID;

/**
 * One match from GET /api/v1/search — deliberately doesn't carry a route: the frontend already
 * knows its own routing conventions per {@link SearchResultType} (see SearchService for how
 * title/subtitle are built per type).
 */
public class SearchResultResponse {
    private SearchResultType type;
    private UUID id;
    private String title;
    private String subtitle;

    public SearchResultResponse() {
    }

    public SearchResultResponse(SearchResultType type, UUID id, String title, String subtitle) {
        this.type = type;
        this.id = id;
        this.title = title;
        this.subtitle = subtitle;
    }

    public SearchResultType getType() {
        return type;
    }

    public UUID getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getSubtitle() {
        return subtitle;
    }
}
