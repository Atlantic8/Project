package server;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONObject;

import tools.CipherPack;
import tools.SpartanDatabase;

/**
 * Servlet implementation class ServerEntry
 */
@WebServlet("/ServerEntry")
public class ServerEntry extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private CipherPack cipher;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public ServerEntry() {
		super();
		cipher = new CipherPack("NONE");
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		this.doPost(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		System.out.println("Getting client request for cipher keys.");
		JSONObject json = new JSONObject(request.getParameter("data")), retJson = new JSONObject();
		String userType = json.getString("user_type");
		SpartanDatabase spartan = new SpartanDatabase("cipher_key", "root", "1713321614");
		spartan.getConnection();
		int start_time = json.getInt("start_time");
		int end_time = json.getInt("end_time");
		String tag = json.getString("tag");
		String sql = String.format(
				"select * from service_key where start_time=\"%d\" and " + "end_time=\"%d\" and tag=\"%s\";",
				start_time, end_time, tag);
		//System.out.println("Execute sql transaction: "+sql);
		spartan.query(sql);
		spartan.resultSetToJson();
		// no record being found.
		if (spartan.json.length() == 0) {
			System.out.println("Exception: " + sql + " failed.");
			retJson.put("state", "failed");
		} else {
			// System.out.println("extracting data.");
			JSONObject tmp = spartan.json.getJSONObject(0);
			if (userType.equals("participant") == true) {
				CipherPack cipher = new CipherPack("NONE");
                String tmpAesKey = cipher.getMD5((String)tmp.get("rsa_private_key"));
				// String tmpAesKey = ((String)tmp.get("rsa_private_key")).substring(50, 74);
                // System.out.println(tmpAesKey);
                String tmpTag = String.format("%d %d %s", start_time, end_time, tag);
                // System.out.println(tmpTag);
				retJson.put("RSAPublicKey", tmp.get("rsa_public_key"));
				retJson.put("SecretKey", cipher.AESEncryption(tmpTag, cipher.OriginalStringToSecretKey(tmpAesKey)));
				retJson.put("state", "success");
			} else if (userType.equals("querier") == true) {
				retJson.put("RSAPublicKey", tmp.get("rsa_public_key"));
				retJson.put("RSAPrivateKey", tmp.get("rsa_private_key"));
				// retJson.put("AESKey", tmp.get("aes_key"));
				retJson.put("state", "success");
			} else { // invalid user type
				System.out.println("Exception: invalid user type.");
				retJson.put("state", "failed");
			}
		}
		PrintWriter writer = response.getWriter();
		writer.write(retJson.toString());
		writer.flush();
	}
}
