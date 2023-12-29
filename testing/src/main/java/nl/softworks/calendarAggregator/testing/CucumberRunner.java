package nl.softworks.calendarAggregator.testing;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.InitializationError;

import java.util.ArrayList;
import java.util.List;

/**
 * You can run this in the IDE:
 * - as a test
 * - as a java application
 * Or in a terminal:
 * - mvn compile exec:exec@run-cucumbers
 *   (you need a java executable in the PATH)
 */
@RunWith(Cucumber.class)
@CucumberOptions(plugin = { "pretty", "html:target/cucumber" },
        features={ "testing/src/main/features" },
        glue = {"nl.softworks.calendarAggregator.testing.glue"},
        tags = "")
public class CucumberRunner {

    public static void main(String[] args) {
        List<Failure> failedTests = new ArrayList<>();
        try {
            Cucumber cucumber = new Cucumber(CucumberRunner.class);

            RunNotifier notifier = new RunNotifier();
            notifier.addListener(new RunListener(){
                @Override
                public void testFailure(Failure failure) {
                    failedTests.add(failure);
                }
                @Override
                public void testAssumptionFailure(Failure failure) {
                    failedTests.add(failure);
                }
            });
            cucumber.run(notifier);

            // Needed to fail maven if run through exec:java
            if (failedTests.size() > 0) {
                System.out.println("\n\nFailed Cucumber tests:");
                failedTests.stream().forEach(ft -> {
                    Description description = ft.getDescription();
                    System.out.println("> " + description.getClassName() + " - " + description.getMethodName() + ": "  + ft.getException().getMessage());
                });
                System.out.println("\n");
                throw new RuntimeException(failedTests.size() + " test(s) failed");
            }
        } catch (InitializationError e) {
            throw new RuntimeException(e);
        }
    }
}
