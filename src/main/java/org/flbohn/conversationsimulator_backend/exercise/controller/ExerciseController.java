package org.flbohn.conversationsimulator_backend.exercise.controller;

import io.swagger.v3.oas.annotations.Operation;
import org.flbohn.conversationsimulator_backend.exercise.dto.exercise.ExerciseResponseDTO;
import org.flbohn.conversationsimulator_backend.exercise.service.ExerciseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/exercise")
public class ExerciseController {

    private final ExerciseService exerciseService;

    @Autowired
    public ExerciseController(ExerciseService exerciseService) {
        this.exerciseService = exerciseService;
    }

    @Operation(summary = "Get all Exercises")
    @GetMapping("")
    public ResponseEntity<List<ExerciseResponseDTO>> getAllExercises() {
        List<ExerciseResponseDTO> exerciseResponseDTOS = exerciseService.getAllExercises().stream().map(ExerciseResponseDTO::from).toList();
        return new ResponseEntity<>(exerciseResponseDTOS, HttpStatus.OK);
    }
}
