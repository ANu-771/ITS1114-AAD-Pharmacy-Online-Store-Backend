package lk.ijse.pharmacy_backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/test")
public class TestController {

    @GetMapping("/ping")
    public ResponseEntity<String> pingGet() {
        return ResponseEntity.ok("Server connected successfully!");
    }

    @PostMapping("/ping")
    public ResponseEntity<String> pingPost() {
        return ResponseEntity.ok("Server connected successfully!");
    }
}
