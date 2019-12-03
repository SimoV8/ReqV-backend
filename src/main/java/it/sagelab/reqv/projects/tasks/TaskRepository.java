package it.sagelab.reqv.projects.tasks;

import it.sagelab.reqv.projects.Project;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByProjectOrderByIdDesc(Project project);

    Optional<Task> findById(Long id);
}
