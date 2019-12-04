package it.sagelab.reqv.engine;


import it.sagelab.reqv.projects.tasks.Task;
import it.sagelab.reqv.projects.tasks.TaskRepository;
import it.sagelab.reqv.requirements.Requirement;
import it.sagelab.specpro.consistency.BinaryInconsistencyFinder;
import it.sagelab.specpro.consistency.ConsistencyChecker;
import it.sagelab.specpro.consistency.InconsistencyFinder;
import it.sagelab.specpro.fe.PSPFrontEnd;
import it.sagelab.specpro.fe.ParseException;
import it.sagelab.specpro.models.InputRequirement;
import it.sagelab.specpro.models.ltl.LTLSpec;
import it.sagelab.specpro.reasoners.NuSMV;
import it.sagelab.specpro.reasoners.translators.NuSMVTranslator;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

public class Snl2FlEngine implements ProjectEngine {


    public Requirement validate(Requirement req) {
        PSPFrontEnd fe = new PSPFrontEnd();
        try {
            fe.parseString(req.getText());
            req.setState(Requirement.State.COMPLIANT);
        } catch (ParseException e) {
            req.setState(Requirement.State.ERROR);
            req.setErrorDescription(e.getMessage());
        }

        return req;
    }

    public ByteArrayOutputStream translate(List<Requirement> reqList) {


        try {
            LTLSpec spec = parseReqList(reqList);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            NuSMVTranslator translator = new NuSMVTranslator();
            translator.translate(new PrintStream(stream), spec);
            return stream;

        } catch (ParseException e) {
            return null;
        }
    }

    public void runConsistencyCheck(TaskRepository taskRepository, Task task, List<Requirement> reqList) {

        try {
            LTLSpec spec = parseReqList(reqList);


            File tempFile = File.createTempFile("/tmp/out_" + task.getProject().getId(), ".nusmv");
            ConsistencyChecker cc = new ConsistencyChecker(new NuSMV(300), spec, tempFile.getAbsolutePath());

            final long taskId = task.getId();
            cc.runAsync((res) -> {
                Optional<Task> runningTask = taskRepository.findById(taskId);
                if(runningTask.isPresent()) {
                    if (res == ConsistencyChecker.Result.CONSISTENT)
                        runningTask.get().setStatus(Task.Status.SUCCESS);
                    else
                        runningTask.get().setStatus(Task.Status.FAIL);
                    runningTask.get().appendLog(cc.getMc().getMessage());
                    taskRepository.save(runningTask.get());
                }

                tempFile.delete();
            });
        } catch (ParseException | IOException e) {
            task.appendLog("[ERROR] " + e.getMessage());
        }
    }

    public void runInconsistencyExplanation(TaskRepository taskRepository, Task task, List<Requirement> reqList) {
        try {
            LTLSpec spec = parseReqList(reqList);


            File tempFile = File.createTempFile("/tmp/out_" + task.getProject().getId(), ".nusmv");
            ConsistencyChecker cc = new ConsistencyChecker(new NuSMV(300), spec, tempFile.getAbsolutePath());
            InconsistencyFinder inconsistencyFinder = new BinaryInconsistencyFinder(cc);

            AtomicInteger counter = new AtomicInteger();
            final long taskId = task.getId();
            final int reqSize = reqList.size();


            inconsistencyFinder.runAsync(
                (inconsistentReqs) -> {
                Optional<Task> runningTaskOpt = taskRepository.findById(taskId);

                if(runningTaskOpt.isPresent()) {
                    Task runningTask = runningTaskOpt.get();
                    runningTask.appendLog("\n\n##################################################################");
                    runningTask.appendLog("Minimal Unsatisfiable core of " + inconsistentReqs.size() + " requirements found:");
                    for (InputRequirement r : inconsistentReqs)
                        runningTask.appendLog(r.getText());

                    runningTask.setStatus(Task.Status.SUCCESS);

                    taskRepository.save(runningTask);
                }
                tempFile.delete();
            });
        } catch (ParseException | IOException e) {
            task.appendLog("[ERROR] " + e.getMessage());
        }
    }

    private LTLSpec parseReqList(List<Requirement> reqList) {
        PSPFrontEnd fe = new PSPFrontEnd();
        StringBuilder builder = new StringBuilder();
        for(Requirement req: reqList) {
            builder.append(req.getText());
            builder.append("\n");
        }
        return fe.parseString(builder.toString());
    }

}
