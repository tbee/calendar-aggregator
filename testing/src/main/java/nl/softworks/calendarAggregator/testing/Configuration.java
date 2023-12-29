package nl.softworks.calendarAggregator.testing;

import java.util.Properties;

public class Configuration {

    final private Properties properties = new Properties();

    public Configuration() {
//        try (FileInputStream fileInputStream = new FileInputStream(nl.softworks.calendarAggregator.testing.glue.web.CucumberHooks.determineLocalSettingsFile())) {
//            properties.load(fileInputStream);
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//
//        try (FileInputStream fileInputStream = new FileInputStream(nl.softworks.calendarAggregator.testing.glue.web.CucumberHooks.determineLocalProjectFile())) {
//            properties.load(fileInputStream);
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
    }

    public String jdbcUsername() {
        return properties.getProperty("mysql.system.username");
    }
    public String jdbcPassword() {
        return properties.getProperty("mysql.system.password");
    }
    public String jdbcHostname() {
        return properties.getProperty("deploy.datasource.hostname");
    }
    public String jdbcDriver() {
        return "com.mysql.jdbc.Driver";
    }
    public String jdbcDatabasename() {
        return properties.getProperty("deploy.datasource.database");
    }
    public String jdbcUrl() {
        return "jdbc:mysql://" + jdbcHostname() + "/" + jdbcDatabasename() + "?useSSL=false";
    }

    public String webBaseUrl() {
        return "http://localhost:" + properties.getProperty("deploy.jbossweb.port", "9000") + "/";
    }

    public boolean runHeadless() {
        return Boolean.parseBoolean(properties.getProperty("cucumber.headless", "false"));
    }
}
