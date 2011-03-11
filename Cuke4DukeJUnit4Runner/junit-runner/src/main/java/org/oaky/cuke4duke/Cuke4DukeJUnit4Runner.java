package org.oaky.cuke4duke;

import cuke4duke.ant.CucumberTask;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.CommandlineJava;
import org.apache.tools.ant.types.Environment;
import org.apache.tools.ant.types.Path;
import org.jruby.Ruby;
import org.jruby.RubyInstanceConfig;
import org.jruby.exceptions.RaiseException;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;

import java.io.*;
import java.util.Properties;

public class Cuke4DukeJUnit4Runner extends Runner {

    public static class InProcessCucumberTask extends CucumberTask {

        public InProcessCucumberTask() {
            Project project = new Project();
            setProject(project);
            Path path = new Path(project, System.getProperty("java.class.path"));
            getProject().addReference("jruby.classpath", path);
        }

        public void setObjectFactory(Class clazz) {
            Environment.Variable objectFactory = new Environment.Variable();
            objectFactory.setKey("cuke4duke.objectFactory");
            objectFactory.setValue(clazz.getName());
            this.addSysproperty(objectFactory);
        }

        @Override
        protected int executeJava(CommandlineJava commandLine) {
            OutputStream bos = new ByteArrayOutputStream();
            RubyInstanceConfig config = new RubyInstanceConfig();
            config.setOutput(new PrintStream(bos));
            config.setError(new PrintStream(bos));
//            config.setHardExit(false);
            String[] args = commandLine.getJavaCommand().getArguments();
            config.processArguments(args);

            Properties systemProperties = System.getProperties();
            Properties cucumberSystemProperties = new Properties(systemProperties);

            for (Object o : commandLine.getSystemProperties().getVariablesVector()) {
                Environment.Variable var = (Environment.Variable) o;
                cucumberSystemProperties.setProperty(var.getKey(), var.getValue());
            }
            System.setProperties(cucumberSystemProperties);
            Ruby runtime = Ruby.newInstance(config);
            try {
                doSetContextClassLoader(runtime);
                runtime.runFromMain(config.getScriptSource(), config.displayedFileName());
            } catch (RaiseException rex) {
                throw new RuntimeException(bos.toString());
            } finally {
                System.setProperties(systemProperties);
                runtime.tearDown(true);
            }
            return 0;
        }

        private void doSetContextClassLoader(Ruby runtime) {
            // set thread context JRuby classloader here, for the main thread
            try {
                Thread.currentThread().setContextClassLoader(runtime.getJRubyClassLoader());
            } catch (SecurityException se) {
                // can't set TC classloader
/*
                if (runtime.getInstanceConfig().isVerbose()) {
                    config.getError().println("WARNING: Security restrictions disallowed setting context classloader for main thread.");
                }
*/
            }
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

    private final FeatureConfigurationAttribute fca;

    public Cuke4DukeJUnit4Runner(Class clazz) {
        fca = new FeatureConfigurationAttribute(clazz);
    }

    @Override
    public Description getDescription() {
        return Description.createTestDescription(fca.getFeatureClass(), fca.getFeatureFilename());
    }

    @Override
    public void run(RunNotifier runNotifier) {
        runNotifier.fireTestStarted(getDescription());
        InProcessCucumberTask task = new InProcessCucumberTask();
        task.setFork(false);

        System.out.println("working dir:" + new File(".").getAbsolutePath());

        String classpath = System.getProperty("java.class.path");
        String[] classpathElements = classpath.split(";");

        StringBuffer argsBuffer = new StringBuffer();
        for (String classpathElement : classpathElements) {
            argsBuffer.append(" --require '").append(classpathElement).append("' ");
        }
        argsBuffer.append(" --strict");
        argsBuffer.append(" --require " + fca.getFeatureFilename());

        task.setArgs(argsBuffer.toString());
        task.setObjectFactory(fca.getObjectFactoryClass());

        try {
            Cuke4DukeTestContextManager tcm = new Cuke4DukeTestContextManager(fca.getFeatureClass());
            testContextManagerHolder.set(tcm);
            task.execute();
        } catch (Throwable e) {
            runNotifier.fireTestFailure(new Failure(getDescription(), e));
        } finally {
            testContextManagerHolder.remove();
        }
        runNotifier.fireTestFinished(getDescription());
    }

    private static ThreadLocal<Cuke4DukeTestContextManager> testContextManagerHolder =
            new ThreadLocal<Cuke4DukeTestContextManager>();

    public static Cuke4DukeTestContextManager getTestContextManager() {
        return testContextManagerHolder.get();
    }
}
