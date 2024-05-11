package org.flbohn.conversationsimulator_backend.exercise.repository;

import org.flbohn.conversationsimulator_backend.exercise.domain.Exercise;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExerciseRepository extends JpaRepository<Exercise, Long> {
}
