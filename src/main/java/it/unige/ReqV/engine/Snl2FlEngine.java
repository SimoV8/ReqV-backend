package it.unige.ReqV.engine;

import it.unige.ReqV.requirements.Requirement;
import snl2fl.Snl2FlException;
import snl2fl.Snl2FlParser;
import snl2fl.ltl.nusmv.NuSMVTranslator;

import java.io.ByteArrayOutputStream;
import java.util.List;

public class Snl2FlEngine implements ProjectEngine {


    public Requirement validate(Requirement req) {
        Snl2FlParser parser = new Snl2FlParser();
        try {
            parser.parseString(req.getText());
            req.setState(Requirement.State.COMPLIANT);
        } catch (Snl2FlException e) {
            req.setState(Requirement.State.ERROR);
            req.setErrorDescription(e.getMessage());
        }

        return req;
    }

    public ByteArrayOutputStream translate(List<Requirement> reqList) {
        Snl2FlParser parser = new Snl2FlParser();
        try {
            for(Requirement req: reqList) {
                parser.parseString(req.getText());
            }
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            parser.translate(new NuSMVTranslator(), stream);

            return stream;

        } catch (Snl2FlException e) {
            return null;
        }

    }
}
