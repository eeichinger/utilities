package helloworld;

import cuke4duke.annotation.After;
import cuke4duke.annotation.Before;
import cuke4duke.annotation.I18n.EN.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import static org.junit.Assert.assertEquals;

public class HelloWorldSteps {

    public static class HelloWorldContext {
        String currentGreeting;
        String currentSubject;
    }

    private final HelloWorldContext helloWorldContext;

    private HelloWorldContext getContext() {
        return helloWorldContext;
    }

    @Autowired
    private HelloWorldService helloWorldService;

    public HelloWorldSteps() {
        helloWorldContext = new HelloWorldContext();
    }

    @Given("^The Greeting is (.*)$")
    public void setGreeting(String greeting) {
        getContext().currentGreeting = greeting;
    }

    @When("^The Subject is (.*)$")
    public void setSubject(String subject) {
        getContext().currentSubject = subject;
    }

    @Then("^The Message is (.*)$")
    public void assertMessageEquals(String expectedGreeting) {
        String actualMsg = helloWorldService.getMessage(getContext().currentGreeting, getContext().currentSubject);
        assertEquals(expectedGreeting, actualMsg);
    }
}
