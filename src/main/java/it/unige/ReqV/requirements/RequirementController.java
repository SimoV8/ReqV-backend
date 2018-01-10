package it.unige.ReqV.requirements;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/requirements")
public class RequirementController {

    private RequirementService requirementService;

    @Autowired
    RequirementController(RequirementService requirementService) {
        this.requirementService = requirementService;
    }

    @GetMapping
    public List<Requirement> getProjectRequirements(@RequestParam("pId") Long projectId) {
        return requirementService.getProjectRequirements(projectId);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getRequirement(@PathVariable("id") Long id) {
        Requirement req = requirementService.getRequirement(id);
        if(req != null)
            return new ResponseEntity<>(req, HttpStatus.OK);
        else
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
    }

    @PostMapping
    public ResponseEntity<?> createRequirement(@Valid @RequestBody Requirement requirement) {
        requirement = requirementService.save(requirement);
        if (requirement != null)
            return new ResponseEntity<>(requirement, HttpStatus.OK);
        else
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
    }

    @PostMapping("/file")
    public ResponseEntity<?> singleFileUpload(@RequestParam("pId") Long projectId,
                                   @RequestParam("file") MultipartFile file,
                                   RedirectAttributes redirectAttributes) {
        List<Requirement> requirements = requirementService.parseFile(file, projectId);
        if(requirements != null)
            return new ResponseEntity<>(requirements, HttpStatus.OK);
        else
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
    }
    
}
