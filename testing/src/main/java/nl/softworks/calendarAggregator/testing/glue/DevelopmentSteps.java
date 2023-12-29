package nl.softworks.calendarAggregator.testing.glue;

import io.cucumber.java.en.Given;

public class DevelopmentSteps extends Steps {

    @Given("pause")
    public void pause() {
        testContext().page().pause();
    }
}
