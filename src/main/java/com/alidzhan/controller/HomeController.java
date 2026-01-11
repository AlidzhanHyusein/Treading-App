package com.alidzhan.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    @GetMapping
    public ResponseEntity<String> homeController() {
        return ResponseEntity.ok("Welcome to treading platform");
    }
    @GetMapping("/api")
    public ResponseEntity<String> secure() {
        return ResponseEntity.ok("Welcome to treading platform secure");
    }
}
