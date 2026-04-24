package org.tbee.webstack.postgres;

import nl.softworks.calendarAggregator.CalendarAggregatorApplicationPostgres;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.MountableFile;

import java.io.File;
import java.nio.file.Path;
import java.util.function.Consumer;

public class PostgresTestContainer {

    private String containerName = "postgres:latest";
    private String database;
    private String username;
    private String password;
    private boolean preventDoubleStart = true;
    private boolean configureSpringDatasource = true;
    private Consumer<String> log = System.out::println;
    private File dumpfile;

    public record Info(String containerId, String url) {}

    public Info start() {
        try {
            String startMarker = CalendarAggregatorApplicationPostgres.class.getName() + "@" + database;
            if (preventDoubleStart && System.getProperty(startMarker) != null) {
                log.accept("Postgres container is already running");
                return null;
            }

            // Start postgres container
            PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:18")
                    .withDatabaseName(database)
                    .withUsername(username)
                    .withPassword(password)
                    .withLogConsumer(frame -> {
                        String line = frame.getUtf8String();
                        if (line != null && !line.isEmpty()) {
                            log.accept("{postgres " + database + "} " + line.replaceAll("\\r?\\n", ""));
                        }
                    })
                    .waitingFor(org.testcontainers.containers.wait.strategy.Wait.forListeningPort());
            postgreSQLContainer.start();
            log.accept("Postgres container started on " + postgreSQLContainer.getJdbcUrl());

            // Restore a database
            if (dumpfile != null) {
                log.accept("Importing " + dumpfile);
                String containerPath = "/tmp/dump.sql";
                postgreSQLContainer.copyFileToContainer(MountableFile.forHostPath(Path.of(dumpfile.getAbsolutePath())), containerPath);
                postgreSQLContainer.execInContainer("psql", "-U", username, "-d", database, "-f", containerPath);
            }

            // Setup spring
            if (configureSpringDatasource) {
                System.setProperty("spring.datasource.url", postgreSQLContainer.getJdbcUrl());
                System.setProperty("spring.datasource.username", postgreSQLContainer.getUsername());
                System.setProperty("spring.datasource.password", postgreSQLContainer.getPassword());
            }

            // Mark as started
            System.setProperty(startMarker, "started");

            return new Info(postgreSQLContainer.getContainerId(), postgreSQLContainer.getJdbcUrl());
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // =================================


    public String containerName() {
        return containerName;
    }

    public PostgresTestContainer containerName(String containerName) {
        this.containerName = containerName;
        return this;
    }

    public String database() {
        return database;
    }

    public PostgresTestContainer database(String database) {
        this.database = database;
        return this;
    }

    public String username() {
        return username;
    }

    public PostgresTestContainer username(String username) {
        this.username = username;
        return this;
    }

    public String password() {
        return password;
    }

    public PostgresTestContainer password(String password) {
        this.password = password;
        return this;
    }

    public boolean preventDoubleStart() {
        return preventDoubleStart;
    }

    public PostgresTestContainer preventDoubleStart(boolean preventDoubleStart) {
        this.preventDoubleStart = preventDoubleStart;
        return this;
    }

    public Consumer<String> log() {
        return log;
    }

    public PostgresTestContainer log(Consumer<String> log) {
        this.log = log;
        return this;
    }

    public File load() {
        return dumpfile;
    }

    public PostgresTestContainer load(File dumpfile) {
        if (!dumpfile.exists()) {
            throw new IllegalStateException("dumpfile does not exist: " + dumpfile.getAbsolutePath());
        }
        this.dumpfile = dumpfile;
        return this;
    }

    public boolean configureSpringDatasource() {
        return configureSpringDatasource;
    }

    public PostgresTestContainer configureSpringDatasource(boolean configureSpringDatasource) {
        this.configureSpringDatasource = configureSpringDatasource;
        return this;
    }
}
