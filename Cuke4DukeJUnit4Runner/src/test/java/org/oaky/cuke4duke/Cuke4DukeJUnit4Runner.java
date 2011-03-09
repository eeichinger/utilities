package org.oaky.cuke4duke;

import com.sun.xml.internal.messaging.saaj.util.ByteInputStream;
import com.sun.xml.internal.messaging.saaj.util.ByteOutputStream;
import cuke4duke.ant.CucumberTask;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.CommandlineJava;
import org.apache.tools.ant.types.Path;
import org.jruby.Main;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;

import java.io.File;
import java.io.PrintStream;
import java.util.Map;

public class Cuke4DukeJUnit4Runner extends Runner {

    public static class InProcessCucumberTask extends CucumberTask {

        public InProcessCucumberTask() {
            Project project = new Project();
            setProject(project);
            Path path = new Path(project, System.getProperty("java.class.path"));
            getProject().addReference("jruby.classpath", path);
        }

//        @Override
//        public void execute() throws BuildException {
//            try {
////                super.execute();    //To change body of overridden methods use File | Settings | File Templates.
//            } catch (Throwable e) {
//                throw new Error(e);
//            }
//        }

        @Override
        protected int executeJava(CommandlineJava commandLine) {
            ByteOutputStream bos = new ByteOutputStream();
            Main main = new Main(new ByteInputStream(), new PrintStream(bos), new PrintStream(bos));
            Main.Status status = main.run(commandLine.getJavaCommand().getArguments());

            int res = status.getStatus(); // super.executeJava(commandLine);
            if (res != 0) {
                throw new Error("[BEGIN]" + bos.toString()+"[END]");
            }
            return res;
        }

        @Override
        protected File getJrubyHome() {
            String jruby_home = System.getenv("JRUBY_HOME");
            if (jruby_home == null) {
                jruby_home = System.getenv("GEM_HOME");
            }
            if (jruby_home == null) {
                throw new BuildException("you must set either JRUBY_HOME or GEM_HOME environment variable");
            }

            return new File(jruby_home);
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
            featureFile = featureFile.substring(0, featureFile.length() - "Feature".length());
        }
        return featureFile + ".feature";
    }

    @Override
    public Description getDescription() {
        return Description.createTestDescription(testClass, getFeatureFile());
    }

    @Override
    public void run(RunNotifier runNotifier) {
        System.out.println("Classpath:" + System.getProperty("java.class.path"));
//        dumpMap(System.getenv());
//        dumpMap(System.getProperties());

        runNotifier.fireTestStarted(getDescription());
        CucumberTask task = new InProcessCucumberTask();
        task.setFork(false);
//        task.setArgs("--require /Users/Shared/workspaces/tmp/cuke-mvn_spike/first/target/test-classes /Users/Shared/workspaces/tmp/cuke-mvn_spike/first/features");
        System.out.println(testClass.getClassLoader().getResource(testClass.getName().replace('.', '/') + ".class"));

        String featurePath = "features/" + getFeatureFile();
//        task.setArgs("--require target/test-classes " + featurePath);
        task.setArgs("--require target/test-classes --require " + featurePath + " --require features/AnotherFeature.feature");
        try {
            task.execute();
        } catch (Throwable e) {
            runNotifier.fireTestFailure(new Failure(getDescription(), e));
        }
        runNotifier.fireTestFinished(getDescription());
//        runNotifier.fireTestRunFinished(result);
    }

    private static void dumpMap(Map map) {
        for (Object key : map.keySet()) {
            System.out.println(key + "=" + map.get(key));
        }
    }
}
