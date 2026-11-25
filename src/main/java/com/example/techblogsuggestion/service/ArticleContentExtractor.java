package com.example.techblogsuggestion.service;

public interface ArticleContentExtractor {
    String extractCleanText(String url, String existingHtmlSnippet);
}