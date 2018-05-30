package it.unige.ReqV.projects.tasks;

import it.unige.ReqV.projects.Project;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByProjectOrderByIdDesc(Project project);
}
