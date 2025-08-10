package nl.softworks.calendarAggregator;

import org.hsqldb.persist.HsqlProperties;

public class Application {
    public static void main(String[] args) {
        System.out.println("HsqlDbFunctions.encodeBase64: " + org.jumpmind.symmetric.db.hsqldb.HsqlDbFunctions.encodeBase64("test".getBytes()));

        HsqlProperties hsqlProperties = new HsqlProperties();
        hsqlProperties.setProperty("server.port", 9147);
        hsqlProperties.setProperty("hsqldb.tx", "mvcc"); // multi version concurrency control
        hsqlProperties.setProperty("server.database.0", "file:hsqldb/calendarAggregator;user=technical;password=technical;shutdown=true");
        hsqlProperties.setProperty("server.dbname.0", "calendarAggregator");

        org.hsqldb.Server server = new org.hsqldb.Server();
        try {
            server.setProperties(hsqlProperties);
        } catch (Exception e) {
            e.printStackTrace();
        }
        server.start();
    }

    public static String hello() {
        return "Hello from Application!";
    }
}
