package org.flbohn.conversationsimulator_backend.exercise.service;

import org.flbohn.conversationsimulator_backend.conversation.domain.Conversation;
import org.flbohn.conversationsimulator_backend.exercise.domain.Exercise;
import org.flbohn.conversationsimulator_backend.exercise.domain.Task;
import org.flbohn.conversationsimulator_backend.exercise.dto.task.TaskRequestDTO;
import org.flbohn.conversationsimulator_backend.exercise.repository.ExerciseRepository;
import org.flbohn.conversationsimulator_backend.exercise.repository.TaskRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ExerciseServiceImpl implements ExerciseService {

    private final ExerciseRepository exerciseRepository;

    private final TaskRepository taskRepository;

    public ExerciseServiceImpl(ExerciseRepository exerciseRepository, TaskRepository taskRepository) {
        this.exerciseRepository = exerciseRepository;
        this.taskRepository = taskRepository;
    }

    @Override
    public List<Exercise> getAllExercises() {
        return exerciseRepository.findAll();
    }

    @Override
    public Exercise getExerciseById(long id) {
        return exerciseRepository.findById(id).orElse(null);
    }

    @Override
    public void setConversationInExercise(long id, Conversation conversation) {
        exerciseRepository.findById(id).orElseThrow().getConversations().add(conversation);
    }

    @Override
    public List<Task> getAllTasksForExercise(long exerciseId) {
        return exerciseRepository.findById(exerciseId).orElseThrow().getTasks();
    }

    @Override
    public Long createNewExercise(String title, String szenario, String furtherInformation, String roleUser, String roleSystem, Integer numberOfMessagesTillFailure, List<TaskRequestDTO> taskDTOList) {
        List<Task> taskList = taskDTOList.stream().map(taskResponseDTO -> new Task(taskResponseDTO.description())).toList();
        Exercise newExercise = new Exercise(title, szenario, furtherInformation, roleUser, roleSystem, numberOfMessagesTillFailure);
        newExercise.getTasks().addAll(taskList);
        taskList.forEach(task -> task.setExercise(newExercise));
        Long exerciseId = exerciseRepository.save(newExercise).getId();
        taskRepository.saveAll(taskList);
        return exerciseId;
    }
}
