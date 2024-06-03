package org.flbohn.conversationsimulator_backend.learner.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.flbohn.conversationsimulator_backend.conversation.domain.Conversation;
import org.flbohn.conversationsimulator_backend.conversation.types.Grade;
import org.flbohn.conversationsimulator_backend.learner.types.LearningLanguage;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Getter
@Setter
public class Learner {

    @Id
    @GeneratedValue
    private Long id;

    @Version
    private Long version;

    private String name;

    private LearningLanguage learningLanguage;

    private Integer totalPoints;

    private Float gradeAverage;

    @ElementCollection
    private List<Grade> allGrades;

    @OneToMany(mappedBy = "learner", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Conversation> conversations;

    public Learner() {
        allGrades = new ArrayList<>();
        conversations = new ArrayList<>();
        totalPoints = 0;
        gradeAverage = 0.0F;
    }

    public Learner(String name, LearningLanguage learningLanguage) {
        this.name = name;
        this.learningLanguage = learningLanguage;
        allGrades = new ArrayList<>();
        conversations = new ArrayList<>();
        totalPoints = 0;
        gradeAverage = 0.0F;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Learner learner = (Learner) o;
        return Objects.equals(id, learner.id) && Objects.equals(version, learner.version) && Objects.equals(name, learner.name) && Objects.equals(learningLanguage, learner.learningLanguage);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, version, name, learningLanguage);
    }
}
