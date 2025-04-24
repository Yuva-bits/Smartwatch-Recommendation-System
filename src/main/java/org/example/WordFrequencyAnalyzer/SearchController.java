package org.example.WordFrequencyAnalyzer;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class SearchController {
    private final WordFrequencyAnalyzer2 analyzer;

    public SearchController(WordFrequencyAnalyzer2 analyzer) {
        this.analyzer = analyzer;
    }

    @GetMapping("/search")
    public String search(@RequestParam String query) {
        return analyzer.search(query);
    }

    @GetMapping("/suggestions")
    public List<String> getSuggestions(@RequestParam String input) {
        return analyzer.getSuggestions(input);
    }
}