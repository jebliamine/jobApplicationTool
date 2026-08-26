package de.jeb.japp.jobsearch.service.provider.jooble;

/** Request body for Jooble's {@code POST /api/{key}} endpoint. */
record JoobleSearchRequest(String keywords, String location, int page) {
}
