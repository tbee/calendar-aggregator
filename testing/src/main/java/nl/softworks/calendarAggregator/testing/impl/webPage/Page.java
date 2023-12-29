package nl.softworks.calendarAggregator.testing.impl.webPage;

import nl.softworks.calendarAggregator.testing.TestContext;
import nl.softworks.calendarAggregator.testing.TestUtil;

public abstract class Page {

    protected com.microsoft.playwright.Page page() {
        return TestContext.get().page();
    }

    protected String baseUrl() {
        return TestContext.get().configuration().webBaseUrl();
    }

    protected void fill(String selector, String value) {
        page().focus(selector);
        page().fill(selector, value);
    }

    /**
     * This waits for all AJAX requests to complete.
     *
     * There may be situations where using this makes sense.
     * But usually it is possible, and much better, to wait for something to become available on the webpage.
     * So please try that first.
     *
     * Also, repeating (polling) ajax calls may cause this call to wait forever.
     */
    public void waitForNoActiveXhrRequests() {
        TestUtil.sleep(200); // We need to wait a bit and allow the browser to trigger XHR requests, there is no way to detect that intent.
        long startTime = System.currentTimeMillis();
        while (TestContext.get().activeXhrRequestsCount() > 0) {
            TestUtil.sleep(200);
            if (startTime + 30000 < System.currentTimeMillis()) {
                throw new IllegalStateException("Timeout on waiting for the completion of XHR requests");
            }
        }
    }
}
