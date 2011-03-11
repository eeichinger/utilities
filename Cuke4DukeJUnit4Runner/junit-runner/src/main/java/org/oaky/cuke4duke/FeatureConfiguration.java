package org.oaky.cuke4duke;

@java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
@java.lang.annotation.Target({java.lang.annotation.ElementType.TYPE})
public @interface FeatureConfiguration {
    String name() default "";
    Class objectFactory() default Cuke4DukeJUnit4SpringFactory.class;
}
