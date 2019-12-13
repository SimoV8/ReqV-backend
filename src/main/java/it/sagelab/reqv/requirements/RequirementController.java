package it.sagelab.reqv.requirements;

import it.sagelab.reqv.projects.Project;
import it.sagelab.reqv.projects.tasks.TaskExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.ByteArrayOutputStream;
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

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteRequirement(@PathVariable("id") Long id) {
        if(requirementService.delete(id))
            return new ResponseEntity<>(HttpStatus.OK);
        else
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @PostMapping
    public ResponseEntity<?> createRequirement(@Valid @RequestBody Requirement requirement) {
        requirement = requirementService.create(requirement);
        if (requirement != null)
            return new ResponseEntity<>(requirement, HttpStatus.OK);
        else
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
    }

    @PutMapping
    public Requirement updateRequirement(@Valid @RequestBody Requirement requirement) {
        return requirementService.update(requirement);
    }

    @PostMapping("/file")
    public ResponseEntity<?> importFile(@RequestParam("pId") Long projectId,
                                        @RequestParam("file") MultipartFile file,
                                        @RequestParam("format") String format) {
        List<Requirement> requirements = requirementService.importFile(file, projectId, format);
        if(requirements != null)
            return new ResponseEntity<>(requirements, HttpStatus.OK);
        else
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
    }

    @GetMapping("/file")
    public ResponseEntity<?> exportFile(@RequestParam("pId") Long projectId, @RequestParam("format") String format) {
        ByteArrayOutputStream stream = requirementService.exportFile(projectId, format);
        if(stream == null || stream.toByteArray().length == 0)
            return new ResponseEntity<>("Impossible to export, check that all requirements are compliant", HttpStatus.BAD_REQUEST);
        HttpHeaders header = new HttpHeaders();
        header.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        header.add("Content-Description", format);

        return new ResponseEntity<>(stream.toByteArray(), header, HttpStatus.OK);
    }

}
