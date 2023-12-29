package nl.softworks.calendarAggregator.testing.glue;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.Tracing;
import io.cucumber.java.After;
import io.cucumber.java.AfterAll;
import io.cucumber.java.Before;
import io.cucumber.java.BeforeAll;
import io.cucumber.java.Scenario;
import nl.softworks.calendarAggregator.testing.Configuration;
import nl.softworks.calendarAggregator.testing.Database;
import nl.softworks.calendarAggregator.testing.TestContext;
import org.apache.log4j.Logger;

import java.io.File;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class CucumberHooks {
    private static final Logger LOG = Logger.getLogger(CucumberHooks.class);

    private static Configuration configuration;
    private static Playwright playwright;
    private Browser browser;
    private BrowserContext browserContext;
    private Page page;
    private Database database;


    @BeforeAll
    static public void beforeAll() {
        loadConfiguration();
        createDatabaseDump();
        setupBrowser();
    }

    @Before
    public void before() {
        restoreDatabase();
        openDatabase();
        openBrowser();
        prepareTestContext();
        startTrace();
    }

    @After
    public void after(Scenario scenario) {
        String features = "/src/main/features/";
        String featureFilename = scenario.getUri().toString().substring(scenario.getUri().toString().indexOf(features) + features.length());
        stopTrace(!scenario.isFailed() ? null : new File("traces/" + featureFilename + "-" + scenario.getName().replace(" ", "_") + ".playwrightTrace.zip"));
        closeBrowser();
        closeDatabase();
    }

    @AfterAll
    static public void afterAll() {
        cleanupBrowser();
    }

    /**
     * Must be done after all setups and opens
     */
    public void prepareTestContext() {
        TestContext.prepareToRunTest(configuration, browserContext, page, database);
    }

    private static void loadConfiguration() {
        configuration = new Configuration();
    }

    public static void createDatabaseDump() {
    }

    public void restoreDatabase() {
    }

    public void openDatabase() {
        database = new Database();
    }

    public void closeDatabase() {
        if (database != null) {
            database.close();
            database = null;
        }
    }

    public static void setupBrowser() {
        playwright = Playwright.create();
    }

    public void openBrowser() {
        // Playwright
        Map<String, String> env = new HashMap();
        env.put("PWDEBUG", "1");
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                .setHeadless(configuration.runHeadless())
                .setEnv(env)
        );
        browserContext = browser.newContext();
        page = browserContext.newPage();
        page.onRequest(request -> {
            if ("xhr".equals(request.resourceType())) {
                TestContext.get().increaseActiveXhrRequestsCount();
            }
        });
        page.onRequestFinished(request -> {
            if ("xhr".equals(request.resourceType())) {
                TestContext.get().decreaseActiveXhrRequestsCount();
            }
        });
        page.onRequestFailed(request -> {
            if ("xhr".equals(request.resourceType())) {
                TestContext.get().decreaseActiveXhrRequestsCount();
            }
        });
    }

    public void closeBrowser() {
        if (page != null) {
            page.close();
            page = null;
        }

        if (browserContext != null) {
            browserContext.close();
            browserContext = null;
        }

        if (browser != null) {
            browser.close();
            browser = null;
        }
    }

    public static void cleanupBrowser() {
        if (playwright != null) {
            playwright.close();
            playwright = null;
        }
    }

    public void startTrace() {
        browserContext.tracing().start(new Tracing.StartOptions()
                .setScreenshots(true)
                .setSnapshots(true)
                .setSources(true));
    }

    public void stopTrace(File saveInFile) {
        Tracing.StopOptions stopOptions = new Tracing.StopOptions();
        if (saveInFile != null) {
            stopOptions.setPath(Paths.get(saveInFile.getAbsolutePath()));
            LOG.info("Saving Playwright trace to " + saveInFile.getAbsolutePath() + ", open with showTrace.sh or on trace.playwright.dev");
        }
        browserContext.tracing().stop(stopOptions);
    }
}
