package it.unige.ReqV.engine;

import it.unige.ReqV.requirements.Requirement;

import java.io.ByteArrayOutputStream;
import java.util.List;

public interface ProjectEngine {

    Requirement validate(Requirement req);

    public ByteArrayOutputStream translate(List<Requirement> reqList);
}
