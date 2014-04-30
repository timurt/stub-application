package kz.beesoft.servlet;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import kz.beesoft.client.IProcessor;

@WebServlet("/ws/*")
public class WSServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Inject
	private IProcessor processor;

	public WSServlet() {
		super();
	}
	private static String absPath = System.getProperty("jboss.server.temp.dir")
			+ File.separator + "soap" + File.separator + "ws";
	
	private String process(HttpServletRequest request,
			HttpServletResponse response) {
		System.out.println("WSServlet.java >> "+request.getRequestURI().toString());
		File folder = new File(absPath);
		folder.mkdirs();
		String xml = "";
		String config = "";

		String[] parts = request.getRequestURI().toString().split("/");
		if (parts.length < 4) {
			return "Wrong url";
		} else {
			
			String path = absPath + File.separator + parts[3] + File.separator + "config.xml";
			File configFile = new File(path);
			if (configFile.exists()) {
				try {
					BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(configFile),"UTF-8"));

					while (in.ready()) {
						boolean t1 = true;
						boolean t2 = true;
						String s = in.readLine();
						s = s.replaceAll("\n", "").replaceAll("\r", "")
								.replaceAll("\t", "");
						String res = "";
						for (int i = 0; i < s.length(); i++) {
							if (s.charAt(i) != ' ')
								t1 = true;
							if (t1) {
								res += s.charAt(i);
							}
							if (s.charAt(i) == '>')
								t1 = false;
						}
						s = res;
						res = "";
						for (int i = (s.length() - 1); i >= 0; i--) {
							if (s.charAt(i) != ' ')
								t2 = true;
							if (t2) {
								res = s.charAt(i) + res;
							}
							if (s.charAt(i) == '<')
								t2 = false;
						}
						config += res;
					}
					in.close();
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else {
				return "Service not found";
			}

		}

		if (request.getContentLength() > 0) {

			try {
				String result = "";
				BufferedReader in = new BufferedReader(request.getReader());
				String line = "";
				while ((line = in.readLine()) != null) {
					result += line;
				}
				in.close();
				if (request.getCharacterEncoding() != null) {
					String s = result;

					int i = 0;
					while (i < s.length()) {
						if (s.charAt(i) == '<') {
							String sub = s.substring(i);
							String sub2 = sub.substring(0, sub.indexOf('>'));
							xml += sub2;
							i += sub2.length();
						} else {
							if (s.charAt(i) == ' ') {
								i++;
								continue;
							} else {
								xml += s.charAt(i);
								i++;
							}
						}
					}
					xml = xml.replaceAll("\n", "").replaceAll("\r", "")
							.replaceAll("\t", "");
				} else {

					xml = result;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

		} else {
			return "No XML recieved";
		}
		System.out.println("WSServlet.java >> "+xml);
		String res = processor.process(config, xml);
		
		return res;

	}

	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		response.setContentType("text/xml; charset=UTF-8");
		response.setCharacterEncoding("UTF-8");
		request.setCharacterEncoding("UTF-8");
		PrintWriter out = response.getWriter();
		out.println(process(request, response));
		out.flush();
		out.close();
	}

	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

}
