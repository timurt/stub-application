package kz.beesoft.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class WSServlet
 */
@WebServlet("/ws/*")
public class WSServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public WSServlet() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		PrintWriter out = response.getWriter();
		String uri = request.getRequestURI().toString();
		String serviceName = uri.substring(0);
		out.println("<h1>"+request.getRequestURI()+" "+request.getRequestURL()+"</h1>");
		Map<String,String[]> a = request.getParameterMap();
		Iterator<String> iterator = a.keySet().iterator();
		
		while (iterator.hasNext()) {
			String key = iterator.next();
			out.println("<h1>"+key+"</h1>");
			String [] value = a.get(key);
			for (int i=0;i<value.length;i++) {
				out.print("<h1>"+value[i]+"</h1> ");
			}
			out.println();
		}
		out.close();
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	}

}
