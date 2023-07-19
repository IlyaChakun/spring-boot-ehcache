package ch.chakun.testweb.controller;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
@AllArgsConstructor
public class HealthCheckController {

    @GetMapping
    public ResponseEntity<String> getHealth() {
        return ResponseEntity.ok("Alive.");
    }

}
