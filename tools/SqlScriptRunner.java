import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

/** Executes a trusted workspace SQL migration through JDBC. */
public class SqlScriptRunner {
    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            throw new IllegalArgumentException("Usage: SqlScriptRunner <sql-file>");
        }
        String sql = Files.readString(Path.of(args[0]), StandardCharsets.UTF_8);
        try (Connection connection = DriverManager.getConnection(
                System.getenv("DB_URL"), System.getenv("DB_USER"), System.getenv("DB_PASSWORD"));
             Statement statement = connection.createStatement()) {
            statement.execute(sql);
            while (statement.getMoreResults() || statement.getUpdateCount() != -1) {
                // Consume every result produced by a multi-statement migration.
            }
        }
        System.out.println("SQL migration completed: " + args[0]);
    }
}
