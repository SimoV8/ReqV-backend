package it.sagelab.reqv.requirements;

import it.sagelab.reqv.projects.Project;
import it.sagelab.reqv.projects.ProjectService;
import it.sagelab.reqv.projects.tasks.TaskExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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

    public List<Requirement> getProjectRequirementsEnabled(Project project) {
        if(project == null)
            return null;
        return requirementRepository.findByProjectIdAndDisabledOrderById(project.getId(), false);
    }

    public Requirement getRequirement(Long id) {
        Optional<Requirement> requirement = requirementRepository.findById(id);
        if(requirement.isPresent() && isValid(requirement.get()))
            return requirement.get();
        else
            return null;
    }

    public Requirement create(Requirement req) {
        if(req.getId() == null && isValid(req)) {
            TaskExecutor executor = new TaskExecutor(req.getProject().getType());
            req = executor.validate(req);
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
            req = new TaskExecutor(req.getProject().getType()).validate(req);
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

    public List<Requirement> importFile(MultipartFile file, Long projectId, String format) {
        Project project = projectService.getProjectOfAuthUser(projectId);
        List<Requirement> requirements = null;
        if(project == null)
            return null;
        try {
            switch (format) {
                case "text":
                    requirements = parseTextFile(file, project);
                    break;
                case "csv":
                    requirements = parseCsvFile(file, project);
                    break;
                default:
                    return null;
            }

            for(Requirement req: requirements) {
                create(req);
            }

            return requirements;
        } catch (IOException e) {
            return null;
        }
    }

    private List<Requirement> parseCsvFile(MultipartFile file, Project project) {
        List<Requirement> requirements = new ArrayList<>();

        return requirements;
    }

    private List<Requirement> parseTextFile(MultipartFile file, Project project) throws IOException {
        List<Requirement> requirements = new ArrayList<>();
        Scanner scanner = new Scanner(file.getInputStream());

        while(scanner.hasNextLine()) {
            String line = scanner.nextLine().trim();
            // Skip empty lines and comments
            if(line.isEmpty() || line.contains("#"))
                continue;
            Requirement req = new Requirement(line, project, Requirement.State.NOT_CHECKED, null, false);
            requirements.add(req);
        }

        return requirements;
    }

    public ByteArrayOutputStream exportFile(Long projectId, String format) {
        List<Requirement> reqList = getProjectRequirements(projectId);

        if(reqList == null || reqList.isEmpty())
            return null;

        RequirementsExporter exporter = new RequirementsExporter(reqList.get(0).getProject().getType());

        switch (format) {
            case "text":
                return exporter.exportTextFile(reqList);
            case "csv":
                return exporter.exportCSVFile(reqList);
            case "nusmv":
            case "aalta":
                return exporter.exportSpecification(reqList, format);
            default:
                return null;
        }
    }


}
