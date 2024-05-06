package org.flbohn.conversationsimulator_backend.conversation.repository;

import org.flbohn.conversationsimulator_backend.conversation.domain.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {
}
