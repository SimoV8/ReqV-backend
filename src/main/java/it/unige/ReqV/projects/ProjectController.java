package it.unige.ReqV.projects;

import it.unige.ReqV.user.User;
import it.unige.ReqV.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/projects")
public class ProjectController {

    private ProjectRepository projectRepository;
    private ProjectTypeRepository projectTypeRepository;
    private UserService userService;

    @Autowired
    public ProjectController(ProjectTypeRepository projectTypeRepository,
                             ProjectRepository projectRepository,
                             UserService userService
                             ) {
        this.projectTypeRepository = projectTypeRepository;
        this.projectRepository = projectRepository;
        this.userService = userService;
    }

    @GetMapping("/types")
    public List<ProjectType> getTypes() {
        return this.projectTypeRepository.findAll();
    }

    @GetMapping
    public List<Project> getProjects() {
        User owner = userService.getAuthenticatedUser();
        return this.projectRepository.findByOwner(owner);
    }

    @GetMapping("/{id}")
    public Project getProject(@PathVariable("id") Long id) {
        User owner = userService.getAuthenticatedUser();
        Project p = projectRepository.findOne(id);
        if(p != null && p.getOwner().getId().equals(owner.getId())) {
            return p;
        }

        return null;
    }

    @PostMapping
    public ResponseEntity<?> createProject(@Valid @RequestBody Project project) {
        User owner = userService.getAuthenticatedUser();
        project.setOwner(owner);
        project = projectRepository.save(project);
        if (project != null)
            return new ResponseEntity<>(project, HttpStatus.OK);
        else
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
    }
}
