package it.sagelab.reqv.projects.tasks;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.util.List;

@RestController
@RequestMapping("/projects/{pid}/tasks")
public class TaskController {

    private TaskService taskService;

    @Autowired
    TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping("")
    public List<Task> getTasks(@PathVariable("pid") Long projectId) {
        return taskService.getTasks(projectId);
    }

    @GetMapping("/{tid}")
    public Task getTask(@PathVariable("pid") Long projectId, @PathVariable("tid") Long taskId) {
        return taskService.getTask(projectId, taskId);
    }


    @GetMapping("/translate")
    public ResponseEntity<?> translateRequirements(@PathVariable("pid") Long projectId) {

        ByteArrayOutputStream stream = taskService.translate(projectId);

        if(stream == null || stream.toByteArray().length == 0)
            return new ResponseEntity<>("Impossible to translate, check that all requirements are compliant", HttpStatus.BAD_REQUEST);
        HttpHeaders header = new HttpHeaders();
        header.setContentType(MediaType.APPLICATION_OCTET_STREAM);

        return new ResponseEntity<>(stream.toByteArray(), header, HttpStatus.OK);
    }

    @GetMapping("/consistencyCheck")
    public Task consistencyCheck(@PathVariable("pid") Long projectId) {
        return taskService.consistencyChecking(projectId);
    }

    @GetMapping("/findInconsistency")
    public Task computeMuc(@PathVariable("pid") Long projectId) {
        return taskService.computeMUC(projectId);
    }

}
