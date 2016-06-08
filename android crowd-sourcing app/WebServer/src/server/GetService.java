package server;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import tools.SpartanDatabase;

/**
 * Servlet implementation class GetService
 */
@WebServlet("/GetService")
public class GetService extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public GetService() {
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
		System.out.println("Receive request from server for service list.");
		System.out.println(request.getParameter("operation"));
		SpartanDatabase spartan = new SpartanDatabase("project_spartan","root","1713321614");
		spartan.getConnection();
		spartan.query("select * from service_schedule;");
		spartan.resultSetToJson();
		PrintWriter writer = response.getWriter();
		writer.write(spartan.json.toString());
		System.out.println("已将json数据发送给客户端："+spartan.json.toString());
		spartan.closeConnection();
	}

}
