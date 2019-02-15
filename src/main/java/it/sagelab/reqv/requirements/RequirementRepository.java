package it.sagelab.reqv.requirements;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RequirementRepository extends JpaRepository<Requirement, Long> {

    List<Requirement> findByProjectIdOrderById(Long id);

    List<Requirement> findByProjectIdAndDisabledOrderById(Long id, boolean disabled);

}
