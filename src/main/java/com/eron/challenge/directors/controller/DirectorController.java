package com.eron.challenge.directors.controller;


import com.eron.challenge.directors.model.dto.DirectorsResponseDTO;
import com.eron.challenge.directors.service.DirectorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/directors")
public class DirectorController {

    @Autowired
    private DirectorService directorService;

    @GetMapping
    public ResponseEntity<DirectorsResponseDTO> getDirectors(@RequestParam int threshold) {
        log.info("Received request for directors with threshold: {}", threshold);
        DirectorsResponseDTO response = directorService.getDirectorsWithMoreThan(threshold);
        log.info("Found directors above threshold {}", threshold);
        return ResponseEntity.ok(response);
    }
}
