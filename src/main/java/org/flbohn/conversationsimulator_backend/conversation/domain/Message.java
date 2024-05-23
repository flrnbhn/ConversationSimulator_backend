package org.flbohn.conversationsimulator_backend.conversation.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.flbohn.conversationsimulator_backend.conversation.types.ConversationMember;
import org.flbohn.conversationsimulator_backend.evaluation.domain.Mistake;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Getter
@Setter
public class Message {

    @Id
    @GeneratedValue
    private Long id;

    @Version
    private long version;

    @Column(length = 1000)
    private String message;

    private ConversationMember conversationMember;

    @ManyToOne
    private Conversation conversationOfMessage;

    @OneToMany(mappedBy = "messageOfTheMistake", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Mistake> mistakes;

    public Message(String message, ConversationMember conversationMember) {
        this.conversationMember = conversationMember;
        this.message = message;
        this.mistakes = new ArrayList<>();
    }

    public Message() {
        this.mistakes = new ArrayList<>();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Message message1 = (Message) o;
        return Objects.equals(id, message1.id) && conversationMember == message1.conversationMember && Objects.equals(message, message1.message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, conversationMember, message);
    }
}
