package org.flbohn.conversationsimulator_backend.evaluation.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.flbohn.conversationsimulator_backend.conversation.domain.Conversation;
import org.flbohn.conversationsimulator_backend.conversation.domain.Message;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Getter
@Setter
public class Mistake {
    @Id
    @GeneratedValue
    private Long id;

    @Version
    private long version;

    @Column(length = 1000)
    private String message;

    private String shortMessage;

    @ElementCollection
    private List<String> replacements = new ArrayList<>();

    private int mistakeLocation;

    private int length;

    @Column(length = 1000)
    private String sentence;

    @ManyToOne
    private Conversation conversationOfTheMistake;

    @ManyToOne
    private Message messageOfTheMistake;

    public Mistake(String message, String shortMessage, List<String> replacements, int mistakeLocation, int length, String sentence) {
        this.message = message;
        this.shortMessage = shortMessage;
        this.replacements = replacements;
        this.mistakeLocation = mistakeLocation;
        this.length = length;
        this.sentence = sentence;
    }

    public Mistake() {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Mistake mistake = (Mistake) o;
        return version == mistake.version && mistakeLocation == mistake.mistakeLocation && length == mistake.length && Objects.equals(id, mistake.id) && Objects.equals(message, mistake.message) && Objects.equals(shortMessage, mistake.shortMessage) && Objects.equals(replacements, mistake.replacements) && Objects.equals(sentence, mistake.sentence);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, version, message, shortMessage, replacements, mistakeLocation, length, sentence);
    }
}
