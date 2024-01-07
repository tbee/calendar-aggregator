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

    public void fillUsername(String username) {
        page().fill("#username", username);
    }

    public void fillPassword(String username) {
        page().fill("#password", username);
    }

    public void clickLogin() {
        page().click("xpath=//button");
    }
}
