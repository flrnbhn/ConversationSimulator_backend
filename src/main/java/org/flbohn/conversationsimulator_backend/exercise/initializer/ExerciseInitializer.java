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
        if (exerciseRepository.findAll().isEmpty()) {
            createInitialExercises();
        }
    }

    private List<Exercise> createInitialExercises() {
        List<Exercise> exercises = new ArrayList<>();
        exercises.add(createFoodOrderRestaurantExercise());
        exercises.add(createDoktorExercise());
        exercises.add(createHotelCheckInExercise());
        exercises.add(createShoppingExercise());
        exercises.add(createPublicTransportExercise());
        exercises.add(createJobInterviewExercise());
        exercises.add(createHouseRentalExercise());
        return exercises;
    }

    private Exercise createFoodOrderRestaurantExercise() {
        Exercise foodOrderRestaurant = new Exercise("Essensbestellung", "Der Gast sitzt in einem noblen Restaurant und hat ziemlichen Hunger. Zum Glück kommt nun endlich der Kellner um seine Bestellung aufzunehmen.", "In einem Noblen Restaurant", "Gast", "Kellner", 25);
        Task starterTask = new Task("Bestelle eine Vorspeise");
        Task mainTask = new Task("Bestelle eine Hauptspeise");
        Task dessertTask = new Task("Bestelle einen Nachtisch");
        Task goodbyeTask = new Task("Bedanke dich");
        List<Task> tasks = new ArrayList<>(List.of(starterTask, mainTask, dessertTask, goodbyeTask));
        tasks.forEach(task -> task.setExercise(foodOrderRestaurant));
        foodOrderRestaurant.setTasks(tasks);
        exerciseRepository.save(foodOrderRestaurant);
        taskRepository.saveAll(tasks);

        return foodOrderRestaurant;
    }

    private Exercise createDoktorExercise() {
        Exercise doktorDate = new Exercise("Arzt Termin machen", "Der Patient plagen seit einigen Tagen starke Bauchschmerzen. Daher entscheidet er sich, einen Arzttermin zu vereinbaren. Er ruft in der Arztpraxis an, um seinen Gesundheitszustand zu besprechen und einen passenden Termin zu finden.", "", "Patient", "Arzt", 20);
        Task problemTask = new Task("Schilder das Problem");
        Task timeTask = new Task("Mache eine Uhrzeit für einen Termin aus");
        List<Task> tasks = new ArrayList<>(List.of(problemTask, timeTask));
        tasks.forEach(task -> task.setExercise(doktorDate));
        doktorDate.setTasks(tasks);
        exerciseRepository.save(doktorDate);
        taskRepository.saveAll(tasks);
        return doktorDate;
    }


    private Exercise createHotelCheckInExercise() {
        Exercise hotelCheckIn = new Exercise("Hotel Check-In", "Der Gast kommst nach einer langen Reise endlich in deinem Hotel an. Die Lobby ist groß und einladend, und er freut dich darauf, in sein Zimmer zu kommen und sich auszuruhen. Am Empfang wartet bereits der Rezeptionist, um ihm beim Check-In zu helfen.", "Hotel Lobby", "Gast", "Rezeptionist", 20);
        Task greetTask = new Task("Grüße den Rezeptionisten");
        Task reservationTask = new Task("Gib deine Reservierungsdetails an");
        Task roomPreferenceTask = new Task("Teile deine Zimmerpräferenzen mit");
        Task keyReceiveTask = new Task("Erhalte den Zimmerschlüssel");
        Task thankTask = new Task("Bedanke dich beim Rezeptionisten");
        List<Task> tasks = new ArrayList<>(List.of(greetTask, reservationTask, roomPreferenceTask, keyReceiveTask, thankTask));
        tasks.forEach(task -> task.setExercise(hotelCheckIn));
        hotelCheckIn.setTasks(tasks);
        exerciseRepository.save(hotelCheckIn);
        taskRepository.saveAll(tasks);
        return hotelCheckIn;
    }

    private Exercise createShoppingExercise() {
        Exercise shopping = new Exercise("Elektronikgeschäft", "Der Kunde möchtest sich ein neues Smartphone zulegen und betrittst dazu ein Elektronikgeschäft. Dort erwartest er kompetente Beratung vom Verkäufer, um das für sich beste Modell zu finden.", "", "Kunde", "Verkäufer", 20);
        Task greetingTask = new Task("Grüße den Verkäufer");
        Task askProductTask = new Task("Frage nach den aktuell besten Smartphones");
        Task askPriceTask = new Task("Frage nach dem Preisen");
        Task negotiateTask = new Task("Entscheide dich für ein Smartphone");
        Task thankTask = new Task("Bedanke dich beim Verkäufer");
        List<Task> tasks = new ArrayList<>(List.of(greetingTask, askProductTask, askPriceTask, negotiateTask, thankTask));
        tasks.forEach(task -> task.setExercise(shopping));
        shopping.setTasks(tasks);
        exerciseRepository.save(shopping);
        taskRepository.saveAll(tasks);
        return shopping;
    }

    private Exercise createPublicTransportExercise() {
        Exercise publicTransport = new Exercise("Öffentliche Verkehrsmittel", "Der Fahrgast befindest sich an einer Bus- oder Bahnstation und musst nach Hamburg reisen. Allerdings ist er unsicher, welchen Zug er nehmen musst und benötigst außerdem noch ein Ticket. Der Schaffner ist seine Anlaufstelle für diese Informationen.", "Bus- oder Bahnstation", "Fahrgast", "Schaffner", 20);
        Task askTicketTask = new Task("Frage nach der Zugverbindung");
        Task askDepartureTask = new Task("Frage nach der Abfahrtszeit");
        Task validateTicketTask = new Task("Frage nach dem Preis und bezahle das Ticket");
        List<Task> tasks = new ArrayList<>(List.of(askTicketTask, askDepartureTask, validateTicketTask));
        tasks.forEach(task -> task.setExercise(publicTransport));
        publicTransport.setTasks(tasks);
        exerciseRepository.save(publicTransport);
        taskRepository.saveAll(tasks);
        return publicTransport;
    }

    private Exercise createJobInterviewExercise() {
        Exercise jobInterview = new Exercise("Vorstellungsgespräch", "Der Bewerber hat ein Vorstellungsgespräch in einem Callcenter und bereitest sich darauf vor, um sich von deiner besten Seite zu zeigen.", "Büro", "Bewerber", "Interviewer", 40);
        Task greetTask = new Task("Begrüße den Interviewer");
        Task selfIntroductionTask = new Task("Stelle dich vor und gib einen Überblick über deinen Hintergrund");
        Task experienceTask = new Task("Beschreibe deine Berufserfahrung");
        Task skillsTask = new Task("Erkläre deine Fähigkeiten und wie sie zur Position passen");
        Task questionsTask = new Task("Stelle dem Interviewer Fragen zur Firma und zur Position");
        Task closingTask = new Task("Bedanke dich und verabschiede dich");
        List<Task> tasks = new ArrayList<>(List.of(greetTask, selfIntroductionTask, experienceTask, skillsTask, questionsTask, closingTask));
        tasks.forEach(task -> task.setExercise(jobInterview));
        jobInterview.setTasks(tasks);
        exerciseRepository.save(jobInterview);
        taskRepository.saveAll(tasks);
        return jobInterview;
    }

    private Exercise createHouseRentalExercise() {
        Exercise houseRental = new Exercise("Hausmiete", "Der Mieter möchte in ein neues Zuhause umziehen und besuchst dazu das Büro eines Vermieters. Der Mieter ist auf der Suche nach einem passenden Haus und möchte alle wichtigen Details und Bedingungen klären.", "Vermieterbüro", "Mieter", "Vermieter", 30);
        Task greetTask = new Task("Begrüße den Vermieter");
        Task inquireAvailabilityTask = new Task("Erkundige dich nach verfügbaren Häusern");
        Task discussPriceTask = new Task("Diskutiere die Mietpreise und zusätzliche Kosten");
        Task amenitiesTask = new Task("Frage nach den Annehmlichkeiten und Einrichtungen des Hauses");
        Task negotiationTask = new Task("Verhandle über die Mietkonditionen");
        Task closingTask = new Task("Schließe den Mietvertrag ab und bedanke dich");
        List<Task> tasks = new ArrayList<>(List.of(greetTask, inquireAvailabilityTask, discussPriceTask, amenitiesTask, negotiationTask, closingTask));
        tasks.forEach(task -> task.setExercise(houseRental));
        houseRental.setTasks(tasks);
        exerciseRepository.save(houseRental);
        taskRepository.saveAll(tasks);
        return houseRental;
    }
}
