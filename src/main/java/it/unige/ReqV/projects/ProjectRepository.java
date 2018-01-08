package it.unige.ReqV.projects;

import it.unige.ReqV.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
interface ProjectRepository extends JpaRepository<Project, Long> {

    List<Project> findByOwner(User owner);

}

