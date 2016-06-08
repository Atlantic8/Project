package server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.json.JSONArray;
import org.json.JSONObject;
import tools.CipherPack;
import tools.SpartanDatabase;

/**
 * Servlet implementation class ServerEntry
 */
@WebServlet("/ServerEntry")
public class ServerEntry extends HttpServlet {
	private static final long serialVersionUID = 1L;

	String publicKey, privateKey;
	CipherPack cipher;

	public SpartanDatabase spartan;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public ServerEntry() {
		super();
		// initialize RSA keys
		cipher = new CipherPack("RSA");
		this.publicKey = new sun.misc.BASE64Encoder().encodeBuffer(cipher.publicKey.getEncoded());
		this.privateKey = new sun.misc.BASE64Encoder().encodeBuffer(cipher.privateKey.getEncoded());
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		this.doPost(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// operation defines the services of server
		// login: login service
		// register: register service
		// participate: participant submit information pair<key, value>.
		// query: query service
		// other: reserved.
		System.out.println("Receive request.");
		String operation = request.getParameter("operation");
		// String feedback = "";
		System.out.println("For operation: "+operation);
		PrintWriter writer = response.getWriter();
		boolean executeState = false;
		String username, password;
		JSONObject json;
		switch (operation) {
		case "register":
			username = request.getParameter("username");
			password = request.getParameter("password");
			executeState = this.Register(username, password, (String) request.getParameter("usertype"));
			json = new JSONObject();
			if (executeState == false) { // login failed
				json.put("state", "failed");
			} else {
				json.put("state", "success");
				HttpSession session = request.getSession();
				session.setAttribute("username", username);
				session.setAttribute("password", password);
				//json.put("ServerPublicKey", publicKey);
			}
			writer.write(json.toString());
			break;
		case "login":
			username = request.getParameter("username");
			password = request.getParameter("password");
			executeState = this.Login(username, password, (String) request.getParameter("usertype"));
			json = new JSONObject();
			if (executeState == false) { // login failed
				json.put("state", "failed");
				System.out.println(username+" login falied.");
			} else {
				System.out.println(username+" login succeeded.");
				json.put("state", "success");
				HttpSession session = request.getSession();
				session.setAttribute("username", username);
				session.setAttribute("password", password);
			}
			writer.write(json.toString());
			break;
		case "query": // query
			json = new JSONObject(request.getParameter("data"));
			String tag = json.getString("tag");
			// feedback
			writer.write(this.query(tag));
			break;
		case "participate":
			// get pair<tag, data>. data is String temporarily
			System.out.println("Participant request: "+request.getParameter("data"));
			System.out.println();
			JSONObject jsonRecv=new JSONObject(request.getParameter("data"));
			json = new JSONObject();
			if (this.participate(jsonRecv.getString("username"), jsonRecv.getString("tag"), 
					jsonRecv.getString("content"),jsonRecv.getInt("start_time"),
					jsonRecv.getInt("end_time"),"1") == true) {
				json.put("state", "success");
			} else {
				json.put("state", "failed");
			}
			writer.write(json.toString());
			break;
		case "get_service": // implemented in Servlet GetService
			spartan = new SpartanDatabase("project_spartan", "root", "1713321614");
			spartan.getConnection();
			JSONArray jsonArray = this.getService();
			writer.write(jsonArray.toString());
			spartan.closeConnection();
			break;	
		case "get_public_key":
			json = new JSONObject();
			json.put("public_key", this.publicKey);
			writer.write(json.toString());
			break;
		default:
			System.out.println("Exception: no such operation.");
			break;
		}
	}

	private boolean Login(String username, String password, String userType) {
		if (userType.equals("participant")==false && userType.equals("querier")==false) {
			System.out.println("Logical exception: unexpected user type.");
			return false;
		}
		spartan = new SpartanDatabase("project_spartan", "root", "1713321614");
		spartan.getConnection();
		// query user_info table, type=1: participant; type=2: querier.
		String sql;
		if (userType.equals("participant"))
			sql = String.format(
					"select * from user_info where username=\"%s\" " + "and password=\"%s\" and user_type=\"1\";", username,
					password);
		else
			sql = String.format(
					"select * from user_info where username=\"%s\" " + "and password=\"%s\" and user_type=\"2\";", username,
					password);
		System.out.println(sql);
		spartan.query(sql);
		try {
			if (spartan.set.next()) { // login successfully.
				return true;
			}
			spartan.closeConnection();
			return false;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

	private boolean Register(String username, String password, String userType) {
		if (userType.equals("participant")==false && userType.equals("querier")==false) {
			System.out.println("Logical exception: unexpected user type.");
			return false;
		}
		spartan = new SpartanDatabase("project_spartan", "root", "1713321614");
		spartan.getConnection();
		// query user_info table, type=1: participant; type=2: querier.
		String sql, tmpSql;
		if (userType.equals("participant")) {
			sql = String.format("insert into user_info(username,password,user_type) values(\"%s\"," + "\"%s\",\"1\");",
					username, password);
			tmpSql = String.format("select * from user_info where username=\"%s\" and password=\"%s\" and user_type=\"1\";",username, password);
		} else {
			sql = String.format("insert into user_info(username,password,user_type) values(\"%s\"," + "\"%s\",\"2\");",
					username, password);
			tmpSql = String.format("select * from user_info where username=\"%s\" and password=\"%s\" and user_type=\"2\";",username, password);
		}	
		System.out.println(sql);
		spartan.query(tmpSql);
		try {
			if (!spartan.set.next()) { // login successfully.
				spartan.insert(sql);
				spartan.closeConnection();
				return true;
			}
			System.out.println("Register error: username already in use.");
			spartan.closeConnection();
			return false;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * insert operation
	 * @param username
	 * @param tag
	 * @param data
	 * @return state of this transaction
	 */
	public boolean participate(String username, String tag, String data, int start_time, int end_time, String type) {
		String curTime = Long.toString(System.currentTimeMillis() / 1000);
		spartan = new SpartanDatabase("project_spartan", "root", "1713321614");
		spartan.getConnection();
		String sql = String.format(
				"insert into content (time,username,start_time,end_time,tag,data,type)"
				+ " values(\"%s\",\"%s\",\"%d\",\"%d\",\"%s\",\"%s\",\"%s\");", curTime,
				username, start_time, end_time, tag, data, type);
		boolean res = spartan.insert(sql);
		spartan.closeConnection();
		System.out.println("Debug: participate " + Boolean.toString(res));
		return res;
	}

	/**
	 * 
	 * @param tag:
	 * tag for query
	 * @return results of the query.
	 *         etc
	 */
	private String query(String tag) {
		spartan = new SpartanDatabase("project_spartan", "root", "1713321614");
		spartan.getConnection();
		String sql = String.format("select * from content where tag=\"%s\";", tag);
		spartan.query(sql);
		spartan.resultSetToJson();
		String tmp = spartan.json.toString();
		spartan.closeConnection();
		return tmp;
	}

	/**
	 *  get service list available
	 * @return JSONArray of the service list.
	 */
	private JSONArray getService() {
		spartan.query("select * from service_schedule;");
		spartan.resultSetToJson();
		return spartan.json;
	}
	
	/**
	 * 
	 */
	private boolean contactTAuthority() {
		try {
			URL url = new URL("");
			URLConnection conn = url.openConnection();
			conn.setDoOutput(true);
			conn.setDoInput(true);
			// conn.setRequestProperty("user", "SWING");
			spartan = new SpartanDatabase("project_spartan", "root", "1713321614");
			spartan.getConnection();
			String sql = "select * from service_schedule;";
			spartan.query(sql);
			spartan.resultSetToJson();
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(conn.getOutputStream()));
			out.write(spartan.json.toString());
			out.flush();
			out.close();

			// String client = conn.getHeaderField("location"), str;// Í·×Ö¶ÎµÄÃû³Æ
			String str;
			BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			StringBuilder sb = new StringBuilder();
			while ((str = in.readLine()) != null) {
				sb.append(str);
			}
			System.out.println("Feedback from third authority: "+sb.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return true;
	}

}

