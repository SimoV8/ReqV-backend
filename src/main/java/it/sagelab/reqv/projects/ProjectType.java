package it.sagelab.reqv.projects;

import javax.persistence.*;

@Entity
public class ProjectType {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    private String name;

    public ProjectType() { }

    public ProjectType(String name) {
        this.id = null;
        this.name = name;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
