package kz.beesoft.servlet;

import java.io.BufferedInputStream;
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
						config += in.readLine();
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

			byte[] xmlData = new byte[request.getContentLength()];
			try {
				BufferedInputStream in = new BufferedInputStream(
						request.getInputStream());
				in.read(xmlData, 0, xmlData.length);
				if (request.getCharacterEncoding() != null) {
					xml = new String(xmlData, request.getCharacterEncoding());
				} else {
					xml = new String(xmlData);
				}
				in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

		} else {
			return "No XML recieved";
		}

		return processor.process(config, xml);

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
