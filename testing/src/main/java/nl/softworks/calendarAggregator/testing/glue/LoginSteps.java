package nl.softworks.calendarAggregator.testing.glue;

import io.cucumber.java.en.Given;
import nl.softworks.calendarAggregator.testing.TestContext;
import nl.softworks.calendarAggregator.testing.TestUtil;
import nl.softworks.calendarAggregator.testing.impl.webPage.LoginPage;

public class LoginSteps extends Steps {

    @Given("user {string} is logged in")
    public void isLoggedIn(String username) {
        LoginPage loginPage = LoginPage.get();
        loginPage.visit();
        loginPage.fillUsername(username);
        loginPage.fillPassword(TestContext.get().getPasswordFor(username));
        loginPage.clickLogin();
        TestUtil.sleep(3000);
    }
}
