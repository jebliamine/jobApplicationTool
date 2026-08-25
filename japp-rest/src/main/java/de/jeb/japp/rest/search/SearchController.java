package de.jeb.japp.rest.search;

import de.jeb.japp.model.search.dto.SearchResultResponse;
import de.jeb.japp.model.user.User;
import de.jeb.japp.search.service.SearchService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("api/v1/search")
public class SearchController {

    private final SearchService searchService;

    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    @GetMapping
    public List<SearchResultResponse> search(@RequestParam(required = false) String q, @AuthenticationPrincipal User user) {
        return searchService.search(q, user);
    }
}
