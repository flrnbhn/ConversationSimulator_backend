package org.flbohn.conversationsimulator_backend.exercise.initializer;

import org.flbohn.conversationsimulator_backend.exercise.domain.Exercise;
import org.flbohn.conversationsimulator_backend.exercise.domain.Task;
import org.flbohn.conversationsimulator_backend.exercise.repository.ExerciseRepository;
import org.flbohn.conversationsimulator_backend.exercise.repository.TaskRepository;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ExerciseInitializer implements InitializingBean {

    private final ExerciseRepository exerciseRepository;

    private final TaskRepository taskRepository;


    public ExerciseInitializer(ExerciseRepository exerciseRepository, TaskRepository taskRepository) {
        this.exerciseRepository = exerciseRepository;
        this.taskRepository = taskRepository;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        // Initial Exercises
        List<Exercise> initialExercises = createInitialExercises();
    }

    private List<Exercise> createInitialExercises() {
        List<Exercise> exercises = new ArrayList<>();
        exercises.add(createFoodOrderRestaurantExercise());
        exercises.add(createDoktorExercise());


        return exercises;
    }

    private Exercise createFoodOrderRestaurantExercise() {
        Exercise foodOrderRestaurant = new Exercise("Meal order", "Ordering food in a restaurant", "In einem Noblen Restaurant", "guest of the restaurant", "waiter of the restaurant");
        Task greetingTask = new Task("Greet the waiter");
        Task starterTask = new Task("Order starter");
        Task mainTask = new Task("Order main course");
        Task dessertTask = new Task("Order dessert");
        Task goodbyeTask = new Task("Say goodbye");
        List<Task> tasks = new ArrayList<>(List.of(greetingTask, starterTask, mainTask, dessertTask, goodbyeTask));
        tasks.forEach(task -> task.setExercise(foodOrderRestaurant));
        foodOrderRestaurant.setTasks(tasks);
        exerciseRepository.save(foodOrderRestaurant);
        taskRepository.saveAll(tasks);

        return foodOrderRestaurant;
    }

    private Exercise createDoktorExercise() {
        Exercise doktorDate = new Exercise("Arzt Termin machen", "Du machst einen Arzt Termin", "Zahnarzt", "patient", "arzt");
        Task problemTask = new Task("Schilder das Problem");
        Task timeTask = new Task("Mache eine Uhrzeit aus");
        List<Task> tasks = new ArrayList<>(List.of(problemTask, timeTask));
        tasks.forEach(task -> task.setExercise(doktorDate));
        doktorDate.setTasks(tasks);
        exerciseRepository.save(doktorDate);
        taskRepository.saveAll(tasks);
        return doktorDate;
    }
}
