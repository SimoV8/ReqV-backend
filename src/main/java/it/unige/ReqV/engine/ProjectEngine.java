package it.unige.ReqV.engine;

import it.unige.ReqV.requirements.Requirement;

public interface ProjectEngine {

    Requirement validate(Requirement req);
}
