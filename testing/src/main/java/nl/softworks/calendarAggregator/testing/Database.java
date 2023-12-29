package nl.softworks.calendarAggregator.testing;

import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;

import java.io.Closeable;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database implements Closeable {

    private Connection connection = null;
    private Handle handle;

    public Connection jdbcConnection() {
        if (connection == null) {
            try {
                Configuration configuration = TestContext.get().configuration();
                connection = DriverManager.getConnection(configuration.jdbcUrl(), configuration.jdbcUsername(), configuration.jdbcPassword());
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
        return connection;
    }
    public Handle jdbi() {
        if (handle == null) {
            handle = DBI.open(jdbcConnection()).begin();
        }
        return handle;
    }

    @Override
    public void close() {
        try {
            if (connection != null) {
                connection.close();
                connection = null;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void begin() {
        jdbi().begin();
    }
    public void rollback() {
        jdbi().rollback();
    }
    public void commit() {
        jdbi().commit();
    }
    public void inTransaction(Runnable runnable) {
        try {
            begin();
            runnable.run();
            commit();
        } catch (Exception e) {
            rollback();
        }
    }
}
