package nl.softworks.calendarAggregator.testing;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class Configuration {

    final private Properties properties = new Properties();

    public Configuration() {
        File file = new File("../app/src/main/resources/application.properties");
        System.out.println(file.getAbsolutePath());
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            properties.load(fileInputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String jdbcUsername() {
        return properties.getProperty("spring.datasource.username");
    }
    public String jdbcPassword() {
        return properties.getProperty("spring.datasource.password");
    }
    public String jdbcDriver() {
        return properties.getProperty("spring.datasource.driver-class-name");
    }
    public String jdbcUrl() {
        return properties.getProperty("spring.datasource.url");
    }

    public String webBaseUrl() {
        String port = properties.getProperty("server.http.port", "8080");
        if ("0".equals(port)) {
            port = "8080";
        }
        return "http://localhost:" + port;
    }

    public boolean runHeadless() {
        return Boolean.parseBoolean(properties.getProperty("cucumber.headless", "false"));
    }
}
