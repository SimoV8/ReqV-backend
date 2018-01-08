package it.unige.ReqV.projects;

import com.fasterxml.jackson.annotation.JsonIgnore;
import it.unige.ReqV.user.User;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity
public class Project {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @NotNull
    private String name;

    private String description;

    @JsonIgnore
    @ManyToOne
    private User owner;

    @ManyToOne
    @NotNull
    private ProjectType type;

    public Project() { }

    public Project(String name, String  description, User owner, ProjectType projectType) {
        this.name = name;
        this.description = description;
        this.owner = owner;
        this.type = projectType;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() { return description; }

    public void setDescription(String description) { this.description = description; }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public ProjectType getType() {
        return type;
    }

    public void setType(ProjectType type) {
        this.type = type;
    }
}