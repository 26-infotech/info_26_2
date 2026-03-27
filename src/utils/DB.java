package utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DB {
	public static Connection con;
	public static Statement stmt;
	public static String DB_NAME = "lecture";
	public static String DB_USER = "root";
	public static String DB_USER_PW = "1234";

	public static void init() throws SQLException {
		con = DriverManager.getConnection("jdbc:mysql://geonhee-gram/"+DB_NAME+"?serverTimezone=UTC&allowLoadLocalInfile=true", DB_USER, DB_USER_PW);
		stmt = con.createStatement();
	}

	public static ResultSet execute(String sql) throws SQLException {
		System.out.println("sql excute: " + sql);
		return stmt.executeQuery(sql);
	}
	
	public static int executeUpdate(String sql) throws SQLException {
        System.out.println("sql executeUpdate: " + sql);
        return stmt.executeUpdate(sql);
    }
}
