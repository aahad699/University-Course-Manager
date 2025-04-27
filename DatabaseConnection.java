import java.sql.*;
import java.util.Properties;

public class DatabaseConnection {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/student_management";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "69Kill3d";
    
    public static Connection getConnection() throws SQLException {
        Properties props = new Properties();
        props.setProperty("user", DB_USER);
        props.setProperty("password", DB_PASSWORD);
        props.setProperty("useSSL", "false");
        props.setProperty("autoReconnect", "true");
        
        return DriverManager.getConnection(DB_URL, props);
    }
}