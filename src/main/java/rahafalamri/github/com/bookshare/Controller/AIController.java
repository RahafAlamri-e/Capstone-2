package rahafalamri.github.com.bookshare.Controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rahafalamri.github.com.bookshare.Service.AIService;

@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
public class AIController {

    private final AIService aiService;

    @GetMapping("/recommend/{userId}")
    public ResponseEntity<?> recommendBooks(@PathVariable Integer userId) {
        return ResponseEntity.status(200).body(aiService.recommendBooks(userId));
    }
}