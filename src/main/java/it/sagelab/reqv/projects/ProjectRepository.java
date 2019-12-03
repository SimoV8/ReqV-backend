package it.sagelab.reqv.projects;

import it.sagelab.reqv.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
interface ProjectRepository extends JpaRepository<Project, Long> {

    List<Project> findByOwner(User owner);

    Optional<Project> findById(Long id);

}

