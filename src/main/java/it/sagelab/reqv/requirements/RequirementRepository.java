package it.sagelab.reqv.requirements;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RequirementRepository extends JpaRepository<Requirement, Long> {

    List<Requirement> findByProjectIdOrderById(Long id);

    List<Requirement> findByProjectIdAndDisabledOrderById(Long id, boolean disabled);

    Optional<Requirement> findById(Long id);

}
