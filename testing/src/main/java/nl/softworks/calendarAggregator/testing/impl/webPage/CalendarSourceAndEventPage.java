package nl.softworks.calendarAggregator.testing.impl.webPage;


public class CalendarSourceAndEventPage extends Page {

//    static public CalendarSourceAndEventPage forSourceName(String name) {
//        return new CalendarSourceAndEventPage(name);
//    }
//
//    private final String name;
//
//    private CalendarSourceAndEventPage(String name) {
//        this.name = name;
//    }

    public void visit() {
        page().navigate(baseUrl() + "/");
        assertOnPage();
    }

    public void assertOnPage() {
//        page().waitForSelector(String.format("xpath=//a[@href='...']", ...));
    }

}
