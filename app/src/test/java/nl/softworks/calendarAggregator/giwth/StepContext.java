package nl.softworks.calendarAggregator.giwth;

public class StepContext {

    final public com.microsoft.playwright.Page page;
    final public String baseUrl;

    public StepContext(int port, com.microsoft.playwright.Page page) {
        this.baseUrl = "http://localhost:"  + port + "/";
        this.page = page;
    }

    static protected void sleep(int ms) {
        try {
            Thread.sleep(ms);
        }
        catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
    static protected void sleepForALongTime() {
        sleep(Integer.MAX_VALUE);
    }

}
