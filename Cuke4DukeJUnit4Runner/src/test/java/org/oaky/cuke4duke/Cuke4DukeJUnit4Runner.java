package org.oaky.cuke4duke;

import cuke4duke.ant.CucumberTask;
import org.apache.tools.ant.BuildEvent;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.BuildListener;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.CommandlineJava;
import org.apache.tools.ant.types.Path;
import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.Runner;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;

import java.io.File;
import java.util.Map;

public class Cuke4DukeJUnit4Runner extends Runner {

    public static class MyCucumberTask extends CucumberTask {

        public MyCucumberTask() {
            Project project = new Project();
            setProject(project);
            Path path = new Path(project, System.getProperty("java.class.path"));
            getProject().addReference("jruby.classpath", path);
//            String jrubyHome = System.getenv("JRUBY_HOME");
//            System.getenv().put("GEM_HOME", jrubyHome);
//            System.getenv().put("GEM_PATH", jrubyHome);
        }

        @Override
        protected int executeJava(CommandlineJava commandLine) {
            int res = super.executeJava(commandLine);
            if (res != 0) {
                throw new Error("" + res);
            }
            return res;
        }

        @Override
        protected File getJrubyHome() {
            return new File(System.getenv("JRUBY_HOME"));
        }
    }

    private Class testClass;

    public Cuke4DukeJUnit4Runner(Class clazz) {
        testClass = clazz;
    }

    public String getFeatureFile() {
        Feature featureDefinition = (Feature) testClass.getAnnotation(Feature.class);
        if (featureDefinition != null) {
            String featureFile = featureDefinition.value();
            if (!featureFile.endsWith(".feature")) {
                featureFile = featureFile + ".feature";
            }
            return featureFile;
        }

        String featureFile = testClass.getSimpleName();
        if (featureFile.endsWith("Feature")) {
            featureFile = featureFile.substring(0, featureFile.length()-"Feature".length());
        }
        return featureFile + ".feature";
    }

    @Override
    public Description getDescription() {
        return Description.createTestDescription(testClass, getFeatureFile());
    }

    @Override
    public void run(RunNotifier runNotifier) {
        dumpMap(System.getenv());
        dumpMap(System.getProperties());
        
//        runNotifier.fireTestRunStarted(getDescription());
        runNotifier.fireTestStarted(getDescription());
        CucumberTask task = new MyCucumberTask();
        task.setFork(false);
//        task.setArgs("--require /Users/Shared/workspaces/tmp/cuke-mvn_spike/first/target/test-classes /Users/Shared/workspaces/tmp/cuke-mvn_spike/first/features");
        System.out.println(testClass.getClassLoader().getResource(testClass.getName().replace('.', '/') + ".class"));

        String featurePath = "features/" + getFeatureFile();
        task.setArgs("--require target/test-classes " + featurePath);
        try {
            task.execute();
        } catch (Throwable e) {
            runNotifier.fireTestFailure(new Failure(getDescription(), e));
        }
        runNotifier.fireTestFinished(getDescription());
//        runNotifier.fireTestRunFinished(result);
    }

    private static void dumpMap(Map map) {
        for(Object key:map.keySet()) {
            System.out.println(key + "=" + map.get(key));
        }
    }
}
