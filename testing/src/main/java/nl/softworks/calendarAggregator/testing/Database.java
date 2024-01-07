package nl.softworks.calendarAggregator.testing;

import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.jpa.JpaPlugin;

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
                Class.forName(configuration.jdbcDriver());
                connection = DriverManager.getConnection(configuration.jdbcUrl(), configuration.jdbcUsername(), configuration.jdbcPassword());
            } catch (SQLException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
        return connection;
    }
    public Handle jdbi() {
        if (handle == null) {
            Jdbi jdbi = Jdbi.create(jdbcConnection());
            jdbi.installPlugin(new JpaPlugin());
            handle = jdbi.open().begin();
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
