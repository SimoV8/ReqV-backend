package it.sagelab.reqv.projects.tasks;

import com.fasterxml.jackson.annotation.*;
import it.sagelab.reqv.projects.Project;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Entity
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Task {

    public enum Status {
        SUCCESS,
        FAIL,
        RUNNING
    }

    public enum Type {
        TRANSLATE,
        CONSISTENCY_CHECKING,
        MINIMUM_UNSATISFIABLE_CORE
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private String description;

    @NotNull
    @ManyToOne
    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    @JsonIdentityReference(alwaysAsId = true)
    private Project project;

    @NotNull
    private Type type;

    @NotNull
    private Status status;

    @NotNull
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy hh:mm:ss")
    @Temporal(TemporalType.TIMESTAMP)
    private Date timestamp;

    @Column(columnDefinition = "TEXT")
    private String log;

    public Task() { }

    public Task(String description, Project project, Type type, Status status, Date timestamp, String log) {
        this.description = description;
        this.project = project;
        this.type = type;
        this.status = status;
        this.timestamp = timestamp;
        this.log = log;
    }

    public Task(String description, Project project, Type type) {
        this.description = description;
        this.project = project;
        this.type = type;
        this.status = Status.RUNNING;
        this.timestamp = new Date();
        this.log = "";
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String getLog() {
        return log;
    }

    public void setLog(String log) {
        this.log = log;
    }

    public void appendLog(String log) {
        this.log += log + "\n";
    }

    @Override
    public String toString() {
        return "Task[id=" +id + ", pid=" + project.getId() + ", descrption='" + description + "', status=" + status + ", type=" + type + "]";
    }

}
