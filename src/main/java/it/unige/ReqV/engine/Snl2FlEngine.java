package it.unige.ReqV.engine;

import it.unige.ReqV.requirements.Requirement;
import snl2fl.req.validator.RequirementValidator;

public class Snl2FlEngine implements ProjectEngine {


    public Requirement validate(Requirement req) {
        RequirementValidator rv = new RequirementValidator();
        if(rv.isValid(req.getText())) {
            req.setState(Requirement.State.COMPLIANT);
        } else {
            req.setState(Requirement.State.ERROR);
            req.setErrorDescription(rv.getErrorMessage());
        }

        return req;
    }
}
