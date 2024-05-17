package org.flbohn.conversationsimulator_backend.exercise.controller;

import io.swagger.v3.oas.annotations.Operation;
import org.flbohn.conversationsimulator_backend.exercise.dto.exercise.ExerciseRequestDTO;
import org.flbohn.conversationsimulator_backend.exercise.dto.exercise.ExerciseResponseDTO;
import org.flbohn.conversationsimulator_backend.exercise.dto.task.TaskResponseDTO;
import org.flbohn.conversationsimulator_backend.exercise.service.ExerciseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @Operation(summary = "Get all Tasks from one Exercise")
    @GetMapping("/tasks/{exerciseId}")
    public ResponseEntity<List<TaskResponseDTO>> getAllTasksForExercise(@PathVariable Long exerciseId) {
        List<TaskResponseDTO> taskResponseDTOs = exerciseService.getAllTasksForExercise(exerciseId).stream().map(TaskResponseDTO::from).toList();
        return new ResponseEntity<>(taskResponseDTOs, HttpStatus.OK);
    }

    @Operation(summary = "Get Exercise by Id")
    @GetMapping("/{exerciseId}")
    public ResponseEntity<ExerciseResponseDTO> getExerciseById(@PathVariable Long exerciseId) {
        ExerciseResponseDTO exerciseResponseDTO = ExerciseResponseDTO.from(exerciseService.getExerciseById(exerciseId));
        return new ResponseEntity<>(exerciseResponseDTO, HttpStatus.OK);
    }

    @Operation(summary = "Post new Exercise")
    @PostMapping("")
    public ResponseEntity<Long> postExercise(@RequestBody ExerciseRequestDTO exerciseRequestDTO) {
        Long exerciseId = exerciseService.createNewExercise(exerciseRequestDTO.title(),
                exerciseRequestDTO.szenario(),
                exerciseRequestDTO.furtherInformation(),
                exerciseRequestDTO.roleUser(),
                exerciseRequestDTO.roleSystem(),
                exerciseRequestDTO.numberOfMessagesTillFailure(),
                exerciseRequestDTO.taskRequestDTO());

        return new ResponseEntity<>(exerciseId, HttpStatus.OK);
    }
}
