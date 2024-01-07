package nl.softworks.calendarAggregator.testing.impl.webPage;


import nl.softworks.calendarAggregator.testing.TestContext;

public class LoginPage extends Page {

    static public LoginPage get() {
        return TestContext.get(LoginPage.class);
    }

    public void visit() {
        page().navigate(baseUrl() + "/login");
        assertOnPage();
    }

    public void assertOnPage() {
        page().waitForSelector(String.format("xpath=//input[@id='username']"));
    }

    public void setUsername(String username) {
        page().fill("#username", username);
    }
}
