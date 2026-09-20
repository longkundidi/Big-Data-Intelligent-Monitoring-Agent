import java.sql.*;

public class DatabaseQuery {
    public static void main(String[] args) throws Exception {
        String host = "192.168.65.59";
        String user = "root";
        String password = "root";
        String database = "new_algorithom_Repository";
        String url = "jdbc:mysql://" + host + ":3306/" + database + "?useSSL=false&serverTimezone=UTC";
        
        Class.forName("com.mysql.cj.jdbc.Driver");
        Connection conn = DriverManager.getConnection(url, user, password);
        
        System.out.println("=== 查询1: turbine_code = '367-奥克斯1-电梯#1' ===");
        executeQuery(conn, "SELECT node_code, var_name FROM config_bom_perceived_variable WHERE turbine_code = '367-奥克斯1-电梯#1' ORDER BY node_code, var_name;");
        
        System.out.println("\n=== 查询2: turbine_code = '368-奥克斯1-电梯#1' ===");
        executeQuery(conn, "SELECT node_code, var_name FROM config_bom_perceived_variable WHERE turbine_code = '368-奥克斯1-电梯#1' ORDER BY node_code, var_name;");
        
        conn.close();
    }
    
    static void executeQuery(Connection conn, String sql) throws SQLException {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(sql);
        ResultSetMetaData meta = rs.getMetaData();
        
        while (rs.next()) {
            System.out.println(rs.getString("node_code") + "\t" + rs.getString("var_name"));
        }
        rs.close();
        stmt.close();
    }
}
