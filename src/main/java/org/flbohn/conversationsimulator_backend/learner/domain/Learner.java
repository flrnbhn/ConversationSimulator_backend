package org.flbohn.conversationsimulator_backend.learner.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.flbohn.conversationsimulator_backend.conversation.domain.Conversation;

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

    private String learningLanguage;

    @OneToOne
    private Conversation conversation;

    public Learner() {
    }

    public Learner(String name, String learningLanguage) {
        this.name = name;
        this.learningLanguage = learningLanguage;
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
