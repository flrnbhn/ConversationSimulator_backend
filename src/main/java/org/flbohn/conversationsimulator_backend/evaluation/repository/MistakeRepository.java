package org.flbohn.conversationsimulator_backend.evaluation.repository;

import org.flbohn.conversationsimulator_backend.evaluation.domain.Mistake;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MistakeRepository extends JpaRepository<Mistake, Long> {

}
