package nl.softworks.calendarAggregator.testing.glue;

import io.cucumber.java.en.Given;
import nl.softworks.calendarAggregator.testing.TestUtil;
import nl.softworks.calendarAggregator.testing.impl.webPage.LoginPage;

public class LoginSteps extends Steps {

    @Given("user {string} is logged in")
    public void isLoggedIn(String username) {
        LoginPage.get().visit();
        LoginPage.get().setUsername(username);
        TestUtil.sleep(3000);
    }
}
