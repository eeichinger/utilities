package org.oaky.cuke4duke;

import cuke4duke.ant.CucumberTask;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.CommandlineJava;
import org.apache.tools.ant.types.Environment;
import org.apache.tools.ant.types.Path;
import org.jruby.Main;
import org.jruby.Ruby;
import org.jruby.RubyInstanceConfig;
import org.jruby.exceptions.RaiseException;
import org.junit.Assert;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
import org.springframework.core.io.ClassPathResource;

import java.io.*;
import java.util.Properties;
import java.util.Vector;

public class Cuke4DukeJUnit4Runner extends Runner {

    public static class InProcessCucumberTask extends CucumberTask {

        public InProcessCucumberTask() {
            Project project = new Project();
            setProject(project);
            Path path = new Path(project, System.getProperty("java.class.path"));
            getProject().addReference("jruby.classpath", path);
            setFork(false);
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
            PrintStream ps = new PrintStream(bos);

            Properties systemProperties = System.getProperties();
            Properties cucumberSystemProperties = new Properties(systemProperties);

            for (Object o : commandLine.getSystemProperties().getVariablesVector()) {
                Environment.Variable var = (Environment.Variable) o;
                cucumberSystemProperties.setProperty(var.getKey(), var.getValue());
            }
            System.setProperties(cucumberSystemProperties);
            cucumberSystemProperties.setProperty("jruby.gem.home", calculateJRubyHome());

            Main.Status status;
            try {
                Main ruby = new Main(new ByteArrayInputStream(new byte[0]), ps, ps);
                String[] args = commandLine.getJavaCommand().getArguments();
                status = ruby.run(args);
            } finally {
                System.setProperties(systemProperties);
            }
            if (status.getStatus() != 0) {
                Assert.fail(bos.toString());
            }
            System.out.println(bos.toString());
            return 0;
        }

        @Override
        protected File getJrubyHome() {
            String jruby_home = calculateJRubyHome();
            return new File(jruby_home);
        }

        private String calculateJRubyHome() {
            String jruby_home = System.getenv("JRUBY_HOME");
            if (jruby_home == null) {
                jruby_home = System.getenv("GEM_HOME");
            }
            if (jruby_home == null) {
                ClassPathResource propsFile = new ClassPathResource("/cuke4dukejunitrunner.properties");
                if (propsFile.exists()) {
                    Properties props = new Properties();
                    try {
                        props.load(propsFile.getInputStream());
                        jruby_home = props.getProperty("jruby.home");
//                        System.out.println("loaded jruby_home from props file: " + jruby_home);
                    } catch (IOException e) {
                        log("Failed loading cuke4dukejunitrunner.properties", e, 0);
                    }
                }
            }
            if (jruby_home == null) {
                throw new BuildException("you must set either JRUBY_HOME environment variable or provide a cuke4dukejunitrunner.properties file containing a jruby.home property pointing to JRUBY_HOME");
            }
            return jruby_home;
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
//        task.setFork(false);

//        System.out.println("working dir:" + new File(".").getAbsolutePath());

        StringBuffer argsBuffer = new StringBuffer();
        requireClasspath(argsBuffer);
        argsBuffer.append(" --require features --strict");
        argsBuffer.append(" " + fca.getFeatureFilename());

        task.setArgs(argsBuffer.toString());
        task.setObjectFactory(fca.getObjectFactoryClass());

        System.out.println("args:" + argsBuffer.toString());
        
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

    private void requireClasspath(StringBuffer argsBuffer) {
        String classpath = System.getProperty("java.class.path");
        String[] classpathElements = classpath.split(File.pathSeparator);

        for (String classpathElement : classpathElements) {
            if(!classpathElement.endsWith(".jar"))
                argsBuffer.append(" --require '").append(classpathElement).append("' ");
        }
    }

    private static ThreadLocal<Cuke4DukeTestContextManager> testContextManagerHolder =
            new ThreadLocal<Cuke4DukeTestContextManager>();

    public static Cuke4DukeTestContextManager getTestContextManager() {
        return testContextManagerHolder.get();
    }
}
