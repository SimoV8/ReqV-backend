package it.sagelab.reqv.requirements;

import com.fasterxml.jackson.annotation.*;
import it.sagelab.reqv.projects.Project;
import org.hibernate.annotations.ColumnDefault;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity
public class Requirement {

    public enum State {
        COMPLIANT,
        WARNING,
        ERROR,
        NOT_CHECKED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotNull
    private String text;

    @NotNull
    @ManyToOne
    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    @JsonIdentityReference(alwaysAsId = true)
    private Project project;

    @JsonIgnore
    @Enumerated(EnumType.STRING)
    private State state = State.NOT_CHECKED;

    @JsonIgnore
    private String errorDescription;

    @ColumnDefault("false")
    private boolean disabled;

    public Requirement() { }

    public Requirement(String text, Project project, State state, String errorDescription, boolean disabled) {
        this.text = text;
        this.project = project;
        this.state = state;
        this.errorDescription = errorDescription;
        this.disabled = disabled;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    @JsonProperty("project")
    public void setProject(Long id) {
        project = new Project();
        project.setId(id);
    }

    @JsonProperty
    public State getState() {
        return state;
    }

    @JsonIgnore
    public void setState(State state) {
        this.state = state;
    }

    @JsonProperty
    public String getErrorDescription() {
        return errorDescription;
    }

    @JsonIgnore
    public void setErrorDescription(String errorDescription) {
        this.errorDescription = errorDescription;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }
}
