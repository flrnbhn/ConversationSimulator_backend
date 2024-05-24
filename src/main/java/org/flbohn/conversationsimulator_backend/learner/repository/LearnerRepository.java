package org.flbohn.conversationsimulator_backend.learner.repository;

import org.flbohn.conversationsimulator_backend.learner.domain.Learner;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LearnerRepository extends JpaRepository<Learner, Long> {

    Optional<Learner> findByName(String name);


}
