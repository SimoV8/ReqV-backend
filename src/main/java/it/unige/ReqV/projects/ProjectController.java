package it.unige.ReqV.projects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/projects")
public class ProjectController {

    private ProjectService projectService;

    @Autowired
    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @GetMapping("/types")
    public List<ProjectType> getTypes() {
        return projectService.getTypes();
    }

    @GetMapping
    public List<Project> getProjects() {
        return projectService.getProjectsOfAuthUser();
    }

    @GetMapping("/{id}")
    public Project getProject(@PathVariable("id") Long id) {
       return projectService.getProjectOfAuthUser(id);
    }

    @PostMapping
    public ResponseEntity<?> createProject(@Valid @RequestBody Project project) {
        project = projectService.save(project);
        if (project != null)
            return new ResponseEntity<>(project, HttpStatus.OK);
        else
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
    }
}
