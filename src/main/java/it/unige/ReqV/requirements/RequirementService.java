package it.unige.ReqV.requirements;

import it.unige.ReqV.engine.EngineFactory;
import it.unige.ReqV.projects.Project;
import it.unige.ReqV.projects.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

@Service
public class RequirementService {

    private RequirementRepository requirementRepository;
    private ProjectService projectService;

    @Autowired
    RequirementService(RequirementRepository requirementRepository, ProjectService projectService) {
        this.requirementRepository = requirementRepository;
        this.projectService = projectService;
    }

    public List<Requirement> getProjectRequirements(Long projectId) {
        //Check if the authenticated user is the owner of the project
        Project project = projectService.getProjectOfAuthUser(projectId);
        return getProjectRequirements(project);
    }

    public List<Requirement> getProjectRequirements(Project project) {
        if(project == null)
            return null;
        return requirementRepository.findByProjectIdOrderById(project.getId());
    }

    public Requirement getRequirement(Long id) {
        Requirement requirement = requirementRepository.findOne(id);
        if(isValid(requirement))
            return requirement;
        else
            return null;
    }

    public Requirement create(Requirement req) {
        if(req.getId() == null && isValid(req)) {
            req = EngineFactory.getEngine(req.getProject().getType()).validate(req);
            return requirementRepository.save(req);
        } else {
            return null;
        }
    }

    public boolean delete(Long id) {
        Requirement req = getRequirement(id);
        if(req == null)
            return false;
        requirementRepository.delete(req);
        return true;
    }

    public Requirement update(Requirement req) {
        Requirement oldReq = getRequirement(req.getId());
        if(isValid(req) && oldReq != null && oldReq.getProject().getId().equals(req.getProject().getId())) {
            req = EngineFactory.getEngine(req.getProject().getType()).validate(req);
            return requirementRepository.save(req);
        }
        else {
            return null;
        }
    }

    /**
     * Checks that the requirement exists and it belongs to authenticated user
     * @param req The requirement to validate
     * @return True if the requirement is valid, false otherwise
     */
    public boolean isValid(Requirement req) {
        if(req != null && req.getProject() != null) {
            Project project = projectService.getProjectOfAuthUser(req.getProject().getId());
            if(project != null) {
                req.setProject(project); // Can be useful later on
                return true;
            }
        }
        return false;
    }

    public List<Requirement> parseFile(MultipartFile file, Long projectId) {
        Project project = projectService.getProjectOfAuthUser(projectId);
        if(project == null)
            return null;
        try {
            List<Requirement> requirements = new ArrayList<>();
            Scanner scanner = new Scanner(file.getInputStream());

            while(scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                // Skip empty lines and comments
                if(line.isEmpty() || line.contains("#"))
                    continue;
                Requirement req = new Requirement(line, project, Requirement.State.NOT_CHECKED, null, false);
                create(req);
                requirements.add(req);
            }

            return requirements;
        } catch (IOException e) {
            return null;
        }
    }



}
