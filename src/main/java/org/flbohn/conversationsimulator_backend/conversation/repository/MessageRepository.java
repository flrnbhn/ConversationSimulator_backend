package org.flbohn.conversationsimulator_backend.conversation.repository;

import org.flbohn.conversationsimulator_backend.conversation.domain.Message;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageRepository extends JpaRepository<Message, Long> {
}
