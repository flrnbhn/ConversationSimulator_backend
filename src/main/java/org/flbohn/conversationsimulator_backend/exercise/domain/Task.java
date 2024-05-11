package org.flbohn.conversationsimulator_backend.exercise.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@Getter
@Setter
@Entity
public class Task {

    @Id
    @GeneratedValue
    private Long id;

    @Version
    private Long version;

    @ManyToOne
    private Exercise exercise;

    private String description;

    public Task() {
    }

    public Task(String description) {
        this.description = description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return Objects.equals(id, task.id) && Objects.equals(version, task.version) && Objects.equals(exercise, task.exercise) && Objects.equals(description, task.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, version, exercise, description);
    }

    @Override
    public String toString() {
        return "Task{" +
                "id=" + id +
                ", version=" + version +
                ", exercise=" + exercise +
                ", description='" + description + '\'' +
                '}';
    }
}
