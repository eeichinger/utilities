package org.oaky.cuke4duke;

import org.apache.tools.ant.BuildException;

import java.io.InputStream;
import java.util.Properties;

public class FeatureConfigurationAttribute {

    private static final String GLOBAL_RUNTIMEPROPERTIES_FILENAME = "cuke4dukejunitrunner.properties";

    private final Class featureClass;
    private final TypedProperties runtimeProperties;

    private final String featureName;
    private final String gemHome;

    private final Class objectFactoryClass;
    private final boolean strict;
    private final String tags;
    private final String customArguments;

    public FeatureConfigurationAttribute(Class clazz) {
        Check.assertNotNull(clazz, "clazz is a mandatory argument");
        featureClass = clazz;

        FeatureConfiguration cfg = (FeatureConfiguration) clazz.getAnnotation(FeatureConfiguration.class);
        Properties annotationProperties = new PropertyAnnotationProperties(cfg, loadRuntimeProperties(clazz));
        
        runtimeProperties = new TypedProperties(annotationProperties);

        featureName = runtimeProperties.getString("cuke4duke.featureName", calculateFeatureFileFromClass(clazz));
        gemHome = runtimeProperties.getString("jruby.gem.home", calculateJGemHome());
        objectFactoryClass = runtimeProperties.getClass("cuke4duke.objectFactory", null);
        strict = runtimeProperties.getBoolean("cuke4duke.strict", true);
        tags = runtimeProperties.getString("cuke4duke.tags", null);
        customArguments = runtimeProperties.getString("cuke4duke.customArguments", null);
    }

    public Class getFeatureClass() {
        return featureClass;
    }

    public Class getObjectFactoryClass() {
        return objectFactoryClass;
    }

    public String getFeatureFilename() {
        return featureName;
    }

    public boolean isStrict() {
        return strict;
    }

    public String getTags() {
        return tags;
    }

    public String getGemHome() {
        return gemHome;
    }

    public String getCustomArguments() {
        return customArguments;
    }

    protected String calculateFeatureFileFromClass(Class clazz) {
        String featureName = clazz.getSimpleName();
        if (featureName.endsWith("Feature")) {
            featureName =  featureName.substring(0, featureName.length() - "Feature".length());
        }
        return "features/" + featureName + ".feature";
    }

    protected String calculateJGemHome() {
        String jruby_home = coalesce(System.getenv("GEM_HOME"), System.getenv("JRUBY_HOME"));
        return jruby_home;
    }

    protected Properties loadRuntimeProperties(Class clazz) {
        Properties props = new Properties();
        ClassLoader classLoader = clazz.getClassLoader();
        if (classLoader.getResource(GLOBAL_RUNTIMEPROPERTIES_FILENAME) != null) {
            loadPropertiesFromResource(props, classLoader, GLOBAL_RUNTIMEPROPERTIES_FILENAME);
        }
        String propertyFilename = clazz.getName().replace('.', '/') + "-cuke4dukejunitrunner.properties";
        if (classLoader.getResource(propertyFilename)!=null) {
            loadPropertiesFromResource(props, classLoader, propertyFilename);
        }
        return props;
    }

    private void loadPropertiesFromResource(Properties props, ClassLoader classLoader, String propertyFilename) {
        try {
            InputStream propsFile = classLoader.getResourceAsStream(propertyFilename);
            props.load(propsFile);
        } catch (Exception e) {
            throw new BuildException("Failed loading properties from " + propertyFilename, e);
        }
    }

    private static <T> T coalesce(T... values) {
        for(T val:values) {
            if (val != null) {
                return val;
            }
        }
        return null;
    }
}
