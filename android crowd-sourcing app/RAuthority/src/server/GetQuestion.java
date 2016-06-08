package server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;

import tools.CipherPack;
import tools.SpartanDatabase;

/**
 * Servlet implementation class GetQuestion
 */
@WebServlet("/GetQuestion")
public class GetQuestion extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public GetQuestion() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		this.doPost(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		// System.out.println("Participant request: "+request.getParameter("data"));
		// System.out.println(request.getRemoteAddr());
		PrintWriter writer = response.getWriter();
		JSONObject json=new JSONObject(request.getParameter("data"));
		SpartanDatabase spartan = new SpartanDatabase("cipher_key", "root", "1713321614");
		String sql = String.format("select * from service_key where start_time=\"%d\" and end_time=\"%d\" "
				+ "and tag=\"%s\";", json.getInt("start_time"),json.getInt("end_time"),json.getString("question"));
		// System.out.println(sql);
		spartan.getConnection();
		spartan.query(sql);
		boolean haha = false; // tag already recorded
		try {
			if (!spartan.set.next()) { // register successfully.
				spartan.insert(sql);
				haha = true;
			} else {
				System.out.println("Register error: username already in use.");
				writer.write("3");
				return;
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
		if (sendTag(json) == true) { // send new tag to server
			if (generateKeyPairs(json, spartan))
				writer.write("1");
			else 
				writer.write("2");
		} else {
			writer.write("2");
		}
		spartan.closeConnection();
	}
	
	public boolean generateKeyPairs(JSONObject json, SpartanDatabase spartan) {
		int start_time = json.getInt("start_time");
		int end_time = json.getInt("end_time");
		String tag = json.getString("question");
		CipherPack cipher = new CipherPack("NONE");
		//cipher.GenerateAESKey();
		cipher.generateRSAKeyPair();
		
		String rsa_public_key = cipher.KeyToString(cipher.publicKey);
		String rsa_private_key = cipher.KeyToString(cipher.privateKey);
		// use RSA private key as AES key to encrypt tag, set it to be a key (black one in PPT)
		// String aes_tmp_key = cipher.getMD5(rsa_private_key);
		// String aes_key = cipher.AESEncryption(tag, cipher.StringToSecretKey(aes_tmp_key));
		
		String sql = String.format("insert into service_key values(\"%d\",\"%d\",\"%s\","
				+ "\"%s\",\"%s\");", start_time,end_time,tag,rsa_public_key,rsa_private_key);
		return spartan.insert(sql);
	}
	
	protected boolean sendTag(JSONObject json){
		System.out.println("Sending service information to server");
		URL url;
		try {
			url = new URL("http://localhost:8081/WebServer/AddService");
			URLConnection conn = url.openConnection();
			conn.setDoOutput(true);
			conn.setDoInput(true);
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(conn.getOutputStream()));
			out.write("data="+json.toString());
			out.flush();
			out.close();

			// feedback from server
			String str;
			BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			StringBuilder sb = new StringBuilder();
			while ((str = in.readLine()) != null) {
				sb.append(str);
			}
			// System.out.println("Feedback from server: "+sb.toString());
			if (sb.toString().equals("success"))
				return true;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println(e.getMessage());
		}
		return false;
	}
}
