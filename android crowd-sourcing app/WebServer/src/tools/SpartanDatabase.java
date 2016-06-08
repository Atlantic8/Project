package tools;

import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import org.json.JSONArray;
import org.json.JSONObject;

import com.mysql.jdbc.Connection;
import com.mysql.jdbc.Statement;

/**
 * create/release connection.
 * @author 343.
 * insert/delete/update/query operation.
 * Sql sentence as input.
 */
public class SpartanDatabase {
	
	private Connection connection;
	private String database;
	private String username, password;
	private Statement statement;
	public ResultSet set; // feedback from query.
	public JSONArray json;
	
	// constructor
	public SpartanDatabase(String Database, String username, String password) {
		this.database = Database;
		this.username = username;
		this.password = password;
		this.json = null;
		this.set = null;
        try { // 加载驱动 
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void getConnection() { 
        //3.使用Connection来创建一个Statement对象  
        try {
        	connection=(Connection) DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/"
        				+this.database,this.username,this.password); 
			statement = (Statement) connection.createStatement();
			System.out.println("connection established");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
	
	public boolean query (String sql) {
		try {
			set = statement.executeQuery(sql);
			/*
			while (set.next()) {
			    System.out.println(set.getString("id"));
			    System.out.println(set.getString("name"));
			} */
			System.out.println("query success");
			return true;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			System.out.println(e.getMessage());
			return false;
		}
	}
	
	public boolean insert (String sql) {
		return this.update(sql);
	}
	
	public boolean update (String sql) {
		try {
			statement.executeUpdate(sql);
			System.out.println("update success");
			return true;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			System.out.println(e.getMessage());
			return false;
		}
	}
	
	public boolean delete (String sql) {
		try {
			statement.execute(sql);
			System.out.println("delete successed");
			return true;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			System.out.println(e.getMessage());
			return false;
		}
	}
	
	public void closeConnection() {
		try {
			if (json != null)
				for (int i=0; i<json.length(); i++)
					json.remove(i);
			if (set != null)
				this.set.close();
			statement.close();
			connection.close();
			System.out.println("connection released");
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
	}
	
	public void resultSetToJson() {
		json = new JSONArray();
		ResultSetMetaData metaData;
		String key, value;
		try {
			metaData = set.getMetaData();
			int column = metaData.getColumnCount();
			while (set.next()) {
				JSONObject jsonObject = new JSONObject();
				for (int i=0; i<column; i++) {
					key = metaData.getColumnLabel(i+1);
					value = set.getString(key);
					jsonObject.put(key, value);
					System.out.println(key+value);
				}
				json.put(jsonObject);
			}
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
	}
	
}
