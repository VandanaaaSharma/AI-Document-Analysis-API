package com.docai.document_analyzer.service;

import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class KeywordService {

    // Basic stopwords list (can be expanded)
    private static final Set<String> STOPWORDS = Set.of(
            "the", "and", "is", "to", "in", "for", "of", "on", "a", "an", "this",
            "that", "it", "with", "as", "by", "be", "are", "was", "were", "from"
    );

    private static final Pattern WORD_PATTERN = Pattern.compile("[a-zA-Z]+")

    public List<String> extractKeywords(String text) {

        // Convert to lower-case & split into words
        List<String> words = WORD_PATTERN.matcher(text.toLowerCase())
                .results()
                .map(match -> match.group())
                .filter(word -> !STOPWORDS.contains(word))
                .collect(Collectors.toList());

        // Count frequency
        Map<String, Long> frequencyMap = words.stream()
                .collect(Collectors.groupingBy(w -> w, Collectors.counting()));

        // Sort by frequency (desc) and return top 10 keywords
        return frequencyMap.entrySet()
                .stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(10)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }
}

