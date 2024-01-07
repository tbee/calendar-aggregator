package nl.softworks.calendarAggregator.testing;

import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;

import java.util.HashMap;
import java.util.Map;

public class TestContext {

    private static final ThreadLocal<TestContext> threadLocalTestContext = new ThreadLocal<TestContext>();
    public static final String ADMINISTRATOR = "administrator";

    private final Map<Class<?>, Object> instanceCache = new HashMap<>();
    private final Configuration configuration;
    private final BrowserContext browserContext;
    private final Page page;
    private final Database database;
    private int activeXhrRequestsCount = 0;

    private final Map<String, String> usernameToPassword = new HashMap<String, String>();
    private String lastVisitedCalendarSourceName = null;

    // ===============================================================
    // TestContext

    public static TestContext prepareToRunTest(Configuration configuration, BrowserContext browserContext, Page page, Database database) {
        TestContext testContext = new TestContext(configuration, browserContext, page, database);
        threadLocalTestContext.set(testContext);
        return testContext;
    }

    public static TestContext get() {
        return threadLocalTestContext.get();
    }

    // ===============================================================
    // Constructor

    private TestContext(Configuration configuration, BrowserContext context, Page page, Database database) {
        this.configuration = configuration;
        this.browserContext = context;
        this.page = page;
        this.database = database;

        usernameToPassword.put(ADMINISTRATOR, "123");
    }

    // ===============================================================
    // Singleton-per-test of implementation classes (allow state)

    /**
     * Intented for sharing single page, db and api instances within a TestContext
     * @param clazz
     * @return
     * @param <T>
     */
    public static <T> T get(Class<T> clazz) {
        try {
            Object instance = get().instanceCache.get(clazz);
            if (instance == null) {
                instance = clazz.newInstance();
                get().instanceCache.put(clazz, instance);
            }
            return (T)instance;
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    // ===============================================================
    // Environment

    public Configuration configuration() {
        return this.configuration;
    }

    public Page page() {
        return this.page;
    }
    public BrowserContext browserContext() {
        return this.browserContext;
    }

    public Database database() {
        return this.database;
    }


    // ===============================================================
    // Active requests

    public int activeXhrRequestsCount() {
        return this.activeXhrRequestsCount;
    }
    public void increaseActiveXhrRequestsCount() {
        this.activeXhrRequestsCount++;
    }
    public void decreaseActiveXhrRequestsCount() {
        this.activeXhrRequestsCount--;
    }

    // ===============================================================
    // User administration, so we can just do steps like "login as 'administrator'"

    public void setPasswordFor(String username, String password) {
        usernameToPassword.put(username, password);
    }
    public String getPasswordFor(String username) {
        return usernameToPassword.get(username);
    }


    // ===============================================================
    // Last visited calendar source

    public String lastVisitedCalendarSourceName() {
        return this.lastVisitedCalendarSourceName;
    }
    public void lastVisitedCalendarSourceName(String v) {
        this.lastVisitedCalendarSourceName = v;
    }
}
