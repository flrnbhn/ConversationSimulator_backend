package org.flbohn.conversationsimulator_backend.conversation.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.flbohn.conversationsimulator_backend.conversation.types.ConversationStatus;
import org.flbohn.conversationsimulator_backend.conversation.types.Grade;
import org.flbohn.conversationsimulator_backend.evaluation.domain.Mistake;
import org.flbohn.conversationsimulator_backend.exercise.domain.Exercise;
import org.flbohn.conversationsimulator_backend.exercise.domain.Task;
import org.flbohn.conversationsimulator_backend.learner.domain.Learner;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Entity
@Getter
@Setter
public class Conversation {

    @Id
    @GeneratedValue
    private Long id;

    @Version
    private long version;

    private Date conversationStartDate;

    private ConversationStatus conversationStatus;

    @OneToMany(mappedBy = "conversationOfMessage", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Message> messagesOfConversation;

    @ManyToOne
    private Exercise exercise;

    @ManyToMany(mappedBy = "conversationsWhereTaskCompleted", cascade = CascadeType.ALL)
    private List<Task> completedTasks;

    @OneToMany(mappedBy = "conversationOfTheMistake", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Mistake> mistakes;

    @ManyToOne
    private Learner learner;

    private Grade gradeOfConversation;

    private Integer pointsOfConversation;

    public Conversation(Date conversationStartDate) {
        this.conversationStartDate = conversationStartDate;
        messagesOfConversation = new ArrayList<>();
        completedTasks = new ArrayList<>();
        mistakes = new ArrayList<>();
        conversationStatus = ConversationStatus.NOT_STARTED;
        gradeOfConversation = Grade.UNRATED;
        pointsOfConversation = 0;
    }

    public Conversation() {
        messagesOfConversation = new ArrayList<>();
        completedTasks = new ArrayList<>();
        mistakes = new ArrayList<>();
        conversationStatus = ConversationStatus.NOT_STARTED;
        gradeOfConversation = Grade.UNRATED;
        pointsOfConversation = 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Conversation that = (Conversation) o;
        return Objects.equals(id, that.id) && Objects.equals(messagesOfConversation, that.messagesOfConversation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, messagesOfConversation);
    }

}
