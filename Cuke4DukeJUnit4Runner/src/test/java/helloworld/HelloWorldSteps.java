package helloworld;

import cuke4duke.annotation.Before;
import cuke4duke.annotation.I18n.EN.*;

import static org.junit.Assert.assertEquals;

public class HelloWorldSteps {

    private String currentAction;
    private String actualGreeting;

    @Before
    public void setUp() {
        currentAction = null;
        actualGreeting = null;
    }

    @Given("^The Action is (.*)$")
    public void setAction(String action) {
        currentAction = action;
    }

    @When("^The Subject is (.*)$")
    public void setSubject(String subject) {
        actualGreeting = currentAction + ", " + subject;
    }

    @Then("^The Greeting is (.*)$")
    public void assertGreetingEquals(String expectedGreeting) {
        assertEquals(expectedGreeting, actualGreeting);
    }
}
