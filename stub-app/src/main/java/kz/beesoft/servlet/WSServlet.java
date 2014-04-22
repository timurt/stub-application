package kz.beesoft.servlet;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
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

	private String process(HttpServletRequest request,
			HttpServletResponse response) {
		String xml = "";
		String config = "";

		String[] parts = request.getRequestURI().toString().split("/");
		if (parts.length < 4) {
			return "Wrong url";
		} else {
			String path = System.getProperty("jboss.server.temp.dir")
					+ File.separator + "soap" + File.separator + parts[2]
					+ File.separator + parts[3] + File.separator + "config.xml";
			File configFile = new File(path);
			if (configFile.exists()) {
				try {
					BufferedReader in = new BufferedReader(new FileReader(
							configFile));
					while (in.ready()) {
						String s = in.readLine();
						for (int i = 0; i < s.length(); i++) {
							if (s.charAt(i) == ' ') {
								continue;
							} else {
								s = s.substring(i, s.length());
								break;
							}
						}
						config += s.replaceAll("\n", "").replaceAll("\r", "")
								.replaceAll("\t", "");
						config = removeSpaces(config);
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

		return processor.process(config, xml);

	}

	private String removeSpaces(String s) {
		String result = "";
		boolean t = true;
		for (int i = 0; i < s.length(); i++) {
			if (s.charAt(i) != ' ')
				t = true;
			if (t) {
				result += s.charAt(i);
			}
			if (s.charAt(i) == '>')
				t = false;
		}
		s = result;
		result = "";
		for (int i = (s.length() - 1); i >= 0; i--) {
			if (s.charAt(i) != ' ')
				t = true;
			if (t) {
				result = s.charAt(i) + result;
			}
			if (s.charAt(i) == '<')
				t = false;
		}
		return result;
	}

	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		response.setContentType("text/xml; charset=UTF-8");
		PrintWriter out = response.getWriter();

		out.println(process(request, response));
		out.close();

	}

	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

}
