package it.unige.ReqV.projects;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
interface ProjectTypeRepository extends JpaRepository<ProjectType, Integer> {

}