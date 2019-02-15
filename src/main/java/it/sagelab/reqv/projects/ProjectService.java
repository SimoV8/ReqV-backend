package it.sagelab.reqv.projects;

import it.sagelab.reqv.user.User;
import it.sagelab.reqv.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import java.util.List;

@Service
public class ProjectService {

    private ProjectRepository projectRepository;
    private ProjectTypeRepository projectTypeRepository;
    private UserService userService;

    @Autowired
    public ProjectService(ProjectRepository projectRepository,
                          ProjectTypeRepository projectTypeRepository,
                          UserService userService) {
        this.projectRepository = projectRepository;
        this.projectTypeRepository = projectTypeRepository;
        this.userService = userService;
    }

    /**
     * Retrieves the project with the specified id and checks that it belongs
     * to the authenticated user, returns null otherwise
     * @param id Project id
     * @return The requested project or null
     */
    public Project getProjectOfAuthUser(Long id) throws EntityNotFoundException {
        User user = userService.getAuthenticatedUser();
        Project p = projectRepository.findOne(id);
        if (p != null && p.getOwner().getId().equals(user.getId()))
            return p;
        else
            throw new EntityNotFoundException("Project with id " + id + " does not exist or you don't have the authorization to access it");
    }

    /**
     * Retrieves the list of all projects belonging to the authenticated user
     * @return The list of projects
     */
    public List<Project> getProjectsOfAuthUser() {
        return getProjects(userService.getAuthenticatedUser());
    }

    /**
     * Retrieves the list of all projects belonging to the specified user
     * @param user The owner of the projects
     * @return The list of projects
     */
    List<Project> getProjects(User user) {
        return this.projectRepository.findByOwner(user);
    }

    /**
     * Stores the project and set the owner equal to the authenticated user
     * @param project The project to store
     * @return The stored project
     */
    public Project save(Project project) {
        User owner = userService.getAuthenticatedUser();
        project.setOwner(owner);
        return projectRepository.save(project);
    }

    /**
     * Retrieves all the project types
     * @return The list of project types
     */
    public List<ProjectType> getTypes() {
        return projectTypeRepository.findAll();
    }
}
