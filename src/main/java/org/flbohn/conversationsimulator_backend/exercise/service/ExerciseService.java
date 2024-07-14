package org.flbohn.conversationsimulator_backend.exercise.service;

import org.flbohn.conversationsimulator_backend.conversation.domain.Conversation;
import org.flbohn.conversationsimulator_backend.exercise.domain.Exercise;
import org.flbohn.conversationsimulator_backend.exercise.domain.Task;
import org.flbohn.conversationsimulator_backend.exercise.dto.task.TaskRequestDTO;
import org.springframework.stereotype.Service;

import java.util.List;


/**
 * Service for managing exercises
 */
@Service
public interface ExerciseService {

    List<Exercise> getAllExercises();

    Exercise getExerciseById(long id);

    /**
     * Assigns a conversation to the exercise
     */
    void setConversationInExercise(long id, Conversation conversation);

    List<Task> getAllTasksForExercise(long id);

    Long createNewExercise(String title, String szenario, String furtherInformation, String roleUser, String roleSystem, Integer numberOfMessagesTillFailure, List<TaskRequestDTO> taskDTOList);

    void deleteExercise(long id);
}
