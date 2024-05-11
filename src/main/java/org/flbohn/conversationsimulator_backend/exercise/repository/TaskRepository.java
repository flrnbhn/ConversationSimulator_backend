package org.flbohn.conversationsimulator_backend.exercise.repository;

import org.flbohn.conversationsimulator_backend.exercise.domain.Task;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepository extends JpaRepository<Task, Long> {

}
