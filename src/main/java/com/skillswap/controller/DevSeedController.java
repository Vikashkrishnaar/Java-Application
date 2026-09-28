package com.skillswap.controller;

import com.skillswap.service.DataSeederService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/dev")
public class DevSeedController {

    private final DataSeederService dataSeederService;

    public DevSeedController(DataSeederService dataSeederService) {
        this.dataSeederService = dataSeederService;
    }

    @PostMapping("/seed")
    public ResponseEntity<Map<String, Object>> seedDemoData(
            @RequestParam(defaultValue = "false") boolean force) {

        Map<String, Object> result = dataSeederService.seedDemoData(force);
        return ResponseEntity.ok(result);
    }
}
