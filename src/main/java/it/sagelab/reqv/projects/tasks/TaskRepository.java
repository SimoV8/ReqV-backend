package it.sagelab.reqv.projects.tasks;

import it.sagelab.reqv.projects.Project;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByProjectOrderByIdDesc(Project project);
}
