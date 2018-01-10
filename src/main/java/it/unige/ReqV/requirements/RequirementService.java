package it.unige.ReqV.requirements;

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
        if(project == null)
            return null;
        else
            return requirementRepository.findByProjectIdOrderById(projectId);
    }

    public Requirement getRequirement(Long id) {
        Requirement requirement = requirementRepository.findOne(id);
        if(isValid(requirement))
            return requirement;
        else
            return null;
    }

    public Requirement create(Requirement req) {
        if(req.getId() == null && isValid(req))
            return requirementRepository.save(req);
        else
            return null;
    }

    public Requirement update(Requirement req) {
        Requirement oldReq = getRequirement(req.getId());
        if(isValid(req) && oldReq != null && oldReq.getProject().getId().equals(req.getProject().getId())) {
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
        return req != null && req.getProject() != null
                && projectService.getProjectOfAuthUser(req.getProject().getId()) != null;
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
                Requirement req = new Requirement(line, project, Requirement.State.NOT_VALIDATED, null);
                create(req);
                requirements.add(req);
            }

            return requirements;
        } catch (IOException e) {
            return null;
        }
    }



}