package org.oaky.cuke4duke;

import cuke4duke.ant.CucumberTask;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.CommandlineJava;
import org.apache.tools.ant.types.Path;
import org.jruby.Main;
import org.junit.Assert;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class JUnitRunnerCucumberTask extends CucumberTask {

    private final Map<String,String> envVars = new HashMap<String,String>();

    public JUnitRunnerCucumberTask() {
        Project project = new Project();
        setProject(project);
        setFork(false);
    }

    public void setClasspath(String classpath) {
        Path path = new Path(getProject(), classpath);
        getProject().addReference("jruby.classpath", path);
    }

    public void setObjectFactory(Class clazz) {
        if (clazz != null) {
            envVars.put("cuke4duke.objectFactory", clazz.getName());
        }
    }

    public void setGemHome(String gemHome) {
        envVars.put("jruby.gem.home", gemHome);
    }

    @Override
    protected int executeJava(CommandlineJava commandLine) {
        OutputStream bos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(bos);

        Properties systemProperties = System.getProperties();
        Properties cucumberSystemProperties = new Properties(systemProperties);

        for (Map.Entry<String,String> o : envVars.entrySet()) {
            cucumberSystemProperties.setProperty(o.getKey(), o.getValue());
        }
        System.setProperties(cucumberSystemProperties);

        Main.Status status;
        try {
            Main ruby = new Main(new ByteArrayInputStream(new byte[0]), ps, ps);
            String[] args = commandLine.getJavaCommand().getArguments();
            status = ruby.run(args);
        } finally {
            System.setProperties(systemProperties);
        }
        if (status == null || status.getStatus() != 0) {
            Assert.fail(bos.toString());
        }
        System.out.println(bos.toString());
        return 0;
    }

    @Override
    protected File getJrubyHome() {
        String jruby_home = envVars.get("jruby.gem.home");
        return new File(jruby_home);
    }
}
