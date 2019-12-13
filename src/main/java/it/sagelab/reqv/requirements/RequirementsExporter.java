package it.sagelab.reqv.requirements;

import it.sagelab.reqv.projects.Project;
import it.sagelab.specpro.fe.AbstractLTLFrontEnd;
import it.sagelab.specpro.fe.LTLFrontEnd;
import it.sagelab.specpro.fe.PSPFrontEnd;
import it.sagelab.specpro.models.ltl.LTLSpec;
import it.sagelab.specpro.reasoners.translators.AALTATranslator;
import it.sagelab.specpro.reasoners.translators.LTLToolTranslator;
import it.sagelab.specpro.reasoners.translators.NuSMVTranslator;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;

public class RequirementsExporter {

   private final AbstractLTLFrontEnd fe;


   public RequirementsExporter(Project.Type type) {
       switch (type) {
           case PSP:
               fe = new PSPFrontEnd();
               break;
           case LTL:
               fe = new LTLFrontEnd();
               break;
           default:
               fe = null;
       }
   }

    public ByteArrayOutputStream exportTextFile(List<Requirement> requirements) {
       ByteArrayOutputStream stream = new ByteArrayOutputStream();
       PrintStream printStream = new PrintStream(stream);

       for(Requirement r: requirements) {
           printStream.println(r.getText());
       }

       return stream;
    }

    public ByteArrayOutputStream exportCSVFile(List<Requirement> requirements) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        PrintStream printStream = new PrintStream(stream);
        printStream.print("id,Primary Text,Artifact Type");

        for(Requirement r: requirements) {
            printStream.println(r.getId() + ",\"" + r.getText() + "\",Requirement");
        }

        return stream;
    }

    public ByteArrayOutputStream exportSpecification(List<Requirement> requirements, String format) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        PrintStream printStream = new PrintStream(stream);
        LTLSpec spec = parseReqList(requirements);

        LTLToolTranslator translator;
        switch (format) {
            case "nusmv":
                translator = new NuSMVTranslator();
                break;
            case "aalta":
                translator = new AALTATranslator();
                break;
            default:
                return null;
        }
        translator.translate(printStream, spec);
        return stream;
    }

    private LTLSpec parseReqList(List<Requirement> reqList) {
        StringBuilder builder = new StringBuilder();
        for(Requirement req: reqList) {
            builder.append(req.getText());
            builder.append("\n");
        }
        return fe.parseString(builder.toString());
    }


}
