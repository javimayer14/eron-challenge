package com.eron.challenge.directors.controller;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("api/directors")
public class DirectorController {


    @GetMapping
    public void getDirectors() {

    }
}
