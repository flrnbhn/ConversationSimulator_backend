package org.flbohn.conversationsimulator_backend.exercise.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.flbohn.conversationsimulator_backend.conversation.domain.Conversation;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Getter
@Setter
@Entity
public class Exercise {
    @Id
    @GeneratedValue
    private Long id;

    @Version
    private Long version;

    private String title;

    private String szenario;

    private String furtherInformation;

    private String roleUser;

    private String roleSystem;

    private int numberOfMessagesTillFailure;

    @OneToMany(mappedBy = "exercise", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Task> tasks;

    @OneToMany(mappedBy = "exercise", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Conversation> conversations;

    public Exercise() {
        this.tasks = new ArrayList<>();
        this.conversations = new ArrayList<>();
    }

    public Exercise(String title, String szenario, String furtherInformation) {
        this.title = title;
        this.szenario = szenario;
        this.furtherInformation = furtherInformation;
        this.tasks = new ArrayList<>();
        this.conversations = new ArrayList<>();

    }

    public Exercise(String title, String szenario, String furtherInformation, String roleUser, String roleSystem, int numberOfMessagesTillFailure) {
        this.title = title;
        this.szenario = szenario;
        this.furtherInformation = furtherInformation;
        this.roleUser = roleUser;
        this.roleSystem = roleSystem;
        this.numberOfMessagesTillFailure = numberOfMessagesTillFailure;
        this.tasks = new ArrayList<>();
        this.conversations = new ArrayList<>();
    }

    public Exercise(String title, String szenario, String furtherInformation, List<Task> tasks) {
        this.title = title;
        this.szenario = szenario;
        this.furtherInformation = furtherInformation;
        this.tasks = tasks;
        this.conversations = new ArrayList<>();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Exercise exercise = (Exercise) o;
        return Objects.equals(id, exercise.id) && Objects.equals(version, exercise.version) && Objects.equals(title, exercise.title) && Objects.equals(szenario, exercise.szenario) && Objects.equals(furtherInformation, exercise.furtherInformation) && Objects.equals(tasks, exercise.tasks);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, version, title, szenario, furtherInformation, tasks);
    }


}
