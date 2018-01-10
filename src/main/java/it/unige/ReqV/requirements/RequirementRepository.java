package it.unige.ReqV.requirements;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RequirementRepository extends JpaRepository<Requirement, Long> {

    List<Requirement> findByProjectId(Long id);
}
