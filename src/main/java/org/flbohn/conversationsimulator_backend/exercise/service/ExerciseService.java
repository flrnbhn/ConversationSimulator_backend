package org.flbohn.conversationsimulator_backend.exercise.service;

import org.flbohn.conversationsimulator_backend.conversation.domain.Conversation;
import org.flbohn.conversationsimulator_backend.exercise.domain.Exercise;
import org.flbohn.conversationsimulator_backend.exercise.domain.Task;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface ExerciseService {

    List<Exercise> getAllExercises();

    Exercise getExerciseById(long id);

    void setConversationInExercise(long id, Conversation conversation);

    List<Task> getAllTasksForExercise(long id);

}
