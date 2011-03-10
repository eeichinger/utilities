package org.oaky.cuke4duke;

import cuke4duke.internal.jvmclass.PicoFactory;
import org.picocontainer.PicoContainer;
import org.springframework.util.StringUtils;

/**
 * Created by IntelliJ IDEA.
 * User: b1exe04
 * Date: 3/9/11
 * Time: 3:22 PM
 * To change this template use File | Settings | File Templates.
 */
public class FeatureConfigurationAttribute {

    private final Class featureClass;
    private String featureName;
    private Class objectFactoryClass;

    public FeatureConfigurationAttribute(Class clazz) {
        AssertUtils.notNull(clazz, "clazz is a mandatory argument");
        featureClass = clazz;
        featureName = featureClass.getSimpleName();
        objectFactoryClass = PicoFactory.class;
        FeatureConfiguration cfg = (FeatureConfiguration) clazz.getAnnotation(FeatureConfiguration.class);
        if (cfg != null) {
            setFeatureConfiguration(cfg);
        }
    }

    public void setFeatureConfiguration(FeatureConfiguration cfg) {
        if (StringUtils.hasText(cfg.name())) {
            this.featureName = cfg.name();
        }
        if (cfg.objectFactory() != Object.class) {
            this.objectFactoryClass = cfg.objectFactory();
        }
    }

    public Class getFeatureClass() {
        return featureClass;
    }

    public Class getObjectFactoryClass() {
        return objectFactoryClass;
    }

    public String getFeatureFilename() {
        String fn = featureName;
        if (featureName.endsWith("Feature")) {
            fn = featureName.substring(0, featureName.length() - "Feature".length());
        }

        return "features/" + fn + ".feature";
    }
}
