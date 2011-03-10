package helloworld;

import org.junit.runner.RunWith;
import org.oaky.cuke4duke.Cuke4DukeJUnit4Runner;
import org.oaky.cuke4duke.FeatureConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.transaction.AfterTransaction;
import org.springframework.test.context.transaction.BeforeTransaction;
import org.springframework.transaction.annotation.Transactional;

/**
 */
@RunWith(Cuke4DukeJUnit4Runner.class)
@FeatureConfiguration(name="HelloWorld")
@ContextConfiguration("HelloWorldFeature-context.xml")
@Transactional
public class HelloWorldFeature {

    @BeforeTransaction
    public void setupDb() {

    }

    @AfterTransaction
    public void tearDownDb() {

    }
}
