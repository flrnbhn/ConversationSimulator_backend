package org.flbohn.conversationsimulator_backend.conversation.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

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

    @OneToMany(mappedBy = "conversationOfMessage", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Message> messagesOfConversation;

    public Conversation(Date conversationStartDate) {
        this.conversationStartDate = conversationStartDate;
        messagesOfConversation = new ArrayList<>();
    }

    public Conversation() {
        messagesOfConversation = new ArrayList<>();
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

    @Override
    public String toString() {
        return "Conversation{" +
                "id=" + id +
                ", messagesOfConversation=" + messagesOfConversation +
                '}';
    }
}
