package it.sagelab.reqv.engine;


import it.sagelab.reqv.projects.ProjectType;

public class EngineFactory {

    public static ProjectEngine getEngine(ProjectType type) {
        if(type.getName().equals("snl2fl"))
            return new Snl2FlEngine();
        throw new IllegalArgumentException("No engine available for type \""+type.getName()+"\".");
    }
}
