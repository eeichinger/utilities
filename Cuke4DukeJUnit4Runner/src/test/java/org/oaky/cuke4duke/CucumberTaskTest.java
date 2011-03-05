package org.oaky.cuke4duke;

import cuke4duke.ant.CucumberTask;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.Path;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.File;
import java.util.Map;

//@RunWith(SpringJUnit4ClassRunner.class)
public class CucumberTaskTest {

    public static class MyCucumberTask extends CucumberTask {

        public MyCucumberTask() {
            Project project = new Project();
            setProject(project);
            Path path = new Path(project, System.getProperty("java.class.path"));
            getProject().addReference("jruby.classpath", path);
        }

        @Override
        protected File getJrubyHome() {
            return new File(System.getenv("JRUBY_HOME"));
//            return new File("/Users/Shared/tools/apache-maven-repository/.jruby");
        }
    }

//    @Test
    public void runFeature() {
        dumpMap(System.getenv());
//        dumpMap(System.getProperties());
//        org.jruby.Main.main(new String[] {"-S","cuke4duke","--jars", "lib", "--require", "bin", "features"});
        CucumberTask task = new MyCucumberTask();
        task.setFork(false);
        task.setArgs("--require /Users/Shared/workspaces/tmp/cuke-mvn_spike/first/target/test-classes /Users/Shared/workspaces/tmp/cuke-mvn_spike/first/features");
        task.execute();
    }

    private static void dumpMap(Map map) {
        for(Object key:map.keySet()) {
            System.out.println(key + "=" + map.get(key));
        }
    }
}
