import java.sql.*;

public class CheckUser {
    public static void main(String[] args) {
        String url = "jdbc:postgresql://localhost:5432/SmartInvoice";
        String user = "postgres";
        String password = "sanchita";

        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            String query = "SELECT id, email, name, user_type FROM users WHERE email LIKE 'sanmey%'";
            try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
                while (rs.next()) {
                    System.out.println("ID: " + rs.getLong("id"));
                    System.out.println("Email: " + rs.getString("email"));
                    System.out.println("Name: " + rs.getString("name"));
                    System.out.println("UserType: " + rs.getInt("user_type"));
                    System.out.println("-------------------------");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
