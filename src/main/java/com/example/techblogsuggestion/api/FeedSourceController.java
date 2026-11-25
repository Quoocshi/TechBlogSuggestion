package com.example.techblogsuggestion.api;

import com.example.techblogsuggestion.api.dto.FeedSourceRequest;
import com.example.techblogsuggestion.api.dto.FeedSourceResponse;
import com.example.techblogsuggestion.domain.model.FeedSource;
import com.example.techblogsuggestion.service.FeedSourceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/sources")
public class FeedSourceController {
    private final FeedSourceService feedSourceService;

    @PostMapping
    public ResponseEntity<FeedSourceResponse> create(@Valid @RequestBody FeedSourceRequest request){
        FeedSourceResponse response = feedSourceService.createSource(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<FeedSourceResponse>> getAll(){
        return ResponseEntity.ok(feedSourceService.getAllSources());
    }

    @PatchMapping("/{id}/toggle")
    public ResponseEntity<Void> toggle(@PathVariable Long id){
        feedSourceService.toggleActive(id);
        return ResponseEntity.noContent().build();
    }
}
