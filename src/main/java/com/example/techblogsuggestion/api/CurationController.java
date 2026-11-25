package com.example.techblogsuggestion.api;

import com.example.techblogsuggestion.api.dto.CurationSummaryResponse;
import com.example.techblogsuggestion.service.ArticleCurationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/curate")
@RequiredArgsConstructor
public class CurationController {

    private final ArticleCurationService articleCurationService;

    @PostMapping("/trigger")
    public ResponseEntity<CurationSummaryResponse> triggerCuration() {
        return ResponseEntity.ok(articleCurationService.curateRawArticles());
    }
}