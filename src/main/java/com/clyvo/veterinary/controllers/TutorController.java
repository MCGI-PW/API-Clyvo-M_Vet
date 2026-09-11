package com.clyvo.veterinary.controllers;

import com.clyvo.veterinary.models.Tutor;
import com.clyvo.veterinary.services.TutorService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tutors")
public class TutorController {

    private final TutorService tutorService;

    public TutorController(TutorService tutorService) {
        this.tutorService = tutorService;
    }
    
    @GetMapping
    public List<Tutor> listAll() {
        return tutorService.listAll();
    }
}
