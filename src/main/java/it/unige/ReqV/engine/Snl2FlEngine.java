package it.unige.ReqV.engine;

import it.unige.ReqV.projects.tasks.Task;
import it.unige.ReqV.projects.tasks.TaskRepository;
import it.unige.ReqV.requirements.Requirement;
import rcc.ConsistencyChecker;
import rcc.InconsistencyFinder;
import rcc.mc.NuSMV;
import snl2fl.Snl2FlException;
import snl2fl.Snl2FlParser;
import snl2fl.ltl.nusmv.NuSMVTranslator;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

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

    public void runConsistencyCheck(TaskRepository taskRepository, Task task, List<Requirement> reqList) {

        try {
            Snl2FlParser parser = new Snl2FlParser();
            parseReqList(parser, reqList);


            File tempFile = File.createTempFile("/tmp/out_" + task.getProject().getId(), ".nusmv");
            ConsistencyChecker cc = new ConsistencyChecker(new NuSMV(300), parser, tempFile.getAbsolutePath());

            final long taskId = task.getId();
            cc.runAsync((res) -> {
                Task runningTask = taskRepository.findOne(taskId);
                if (res == ConsistencyChecker.Result.CONSISTENT)
                    runningTask.setStatus(Task.Status.SUCCESS);
                else
                    runningTask.setStatus(Task.Status.FAIL);
                runningTask.appendLog(cc.getMc().getMessage());
                taskRepository.save(runningTask);
                tempFile.delete();
            });
        } catch (Snl2FlException | IOException e) {
            task.appendLog("[ERROR] " + e.getMessage());
        }
    }

    public void runInconsistencyExplanation(TaskRepository taskRepository, Task task, List<Requirement> reqList) {
        try {
            Snl2FlParser parser = new Snl2FlParser();
            parseReqList(parser, reqList);


            File tempFile = File.createTempFile("/tmp/out_" + task.getProject().getId(), ".nusmv");
            ConsistencyChecker cc = new ConsistencyChecker(new NuSMV(300), parser, tempFile.getAbsolutePath());
            InconsistencyFinder inconsistencyFinder = new InconsistencyFinder(cc);

            AtomicInteger counter = new AtomicInteger();
            final long taskId = task.getId();
            final int reqSize = reqList.size();
            inconsistencyFinder.runAsync(
                (requirement, result) -> {
                    Task runningTask = taskRepository.findOne(taskId);

                    if(result == ConsistencyChecker.Result.FAIL) {
                        runningTask.appendLog("[ERROR] Error occured during consistency check");
                        runningTask.setStatus(Task.Status.FAIL);
                    }
                    else
                        runningTask.appendLog(counter.incrementAndGet() + "/" + reqSize);
                    taskRepository.save(runningTask);
                },
                (inconsistentReqs) -> {
                Task runningTask = taskRepository.findOne(taskId);

                runningTask.appendLog("\n\n##################################################################");
                runningTask.appendLog("Minimum Unsatisfiable core of " + inconsistentReqs.size() + " requirements found:");
                for(snl2fl.req.requirements.Requirement r : inconsistentReqs)
                    runningTask.appendLog(r.getText());

                runningTask.setStatus(Task.Status.SUCCESS);

                taskRepository.save(runningTask);
                tempFile.delete();
            });
        } catch (Snl2FlException | IOException e) {
            task.appendLog("[ERROR] " + e.getMessage());
        }
    }

    private void parseReqList(Snl2FlParser parser, List<Requirement> reqList) {
        for (Requirement req : reqList) {
            parser.parseString(req.getText());
        }
    }

}
