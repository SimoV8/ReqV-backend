package it.sagelab.reqv.projects.tasks;

import it.sagelab.reqv.requirements.Requirement;
import it.sagelab.reqv.requirements.RequirementService;
import it.sagelab.reqv.projects.Project;
import it.sagelab.reqv.projects.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import java.io.ByteArrayOutputStream;
import java.util.List;

@Service
public class TaskService {

    private ProjectService projectService;
    private RequirementService requirementService;
    private TaskRepository taskRepository;

    @Autowired
    public TaskService(ProjectService projectService,
                RequirementService requirementService,
                TaskRepository taskRepository) {
        this.projectService = projectService;
        this.requirementService = requirementService;
        this.taskRepository = taskRepository;
    }

    public Task getTask(Long projectId, Long taskId) {
        Project project = projectService.getProjectOfAuthUser(projectId);
        Task task = taskRepository.getOne(taskId);

        if(task == null || !task.getProject().getId().equals(project.getId()))
            throw new EntityNotFoundException("Task with id " + taskId + " not found");
        return task;
    }

    public List<Task> getTasks(Long projectId) {
        Project project = projectService.getProjectOfAuthUser(projectId);
        return taskRepository.findByProjectOrderByIdDesc(project);
    }

    public ByteArrayOutputStream translate(Long projectId) {
        Project project = projectService.getProjectOfAuthUser(projectId);
        List<Requirement> reqList = requirementService.getProjectRequirementsEnabled(project);

        if(reqList == null || reqList.isEmpty())
            return null;

        ByteArrayOutputStream stream = new TaskExecutor(project.getType()).translate(reqList);

        // TODO: save Task in db

        return stream;
    }

    public Task consistencyChecking(Long projectId) {
        Project project = projectService.getProjectOfAuthUser(projectId);
        List<Requirement> reqList = requirementService.getProjectRequirementsEnabled(project);

        if(reqList == null || reqList.isEmpty())
            return null;

        String taskDescription = "Consistency checking " + reqList.size() + " requirements";
        Task task = new Task(taskDescription, project, Task.Type.CONSISTENCY_CHECKING);
        task = taskRepository.save(task);

        new TaskExecutor(project.getType()).runConsistencyCheck(taskRepository, task, reqList);

        return task;
    }

    public Task computeMUC(Long projectId) {
        Project project = projectService.getProjectOfAuthUser(projectId);
        List<Requirement> reqList = requirementService.getProjectRequirementsEnabled(project);

        if(reqList == null || reqList.isEmpty())
            return null;

        String taskDescription = "Computing Minimal Unsatisfiable Core of " + reqList.size() + " requirements";
        Task task = new Task(taskDescription, project, Task.Type.MINIMUM_UNSATISFIABLE_CORE);
        task = taskRepository.save(task);

        new TaskExecutor(project.getType()).runInconsistencyExplanation(taskRepository, task, reqList);

        return task;
    }

}
