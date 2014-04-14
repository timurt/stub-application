package kz.beesoft.servlet;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSON;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.xml.XMLSerializer;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

@WebServlet("/soap/*")
public class SoapControllerServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	private static final int MAX_MEMORY_SIZE = 1024 * 1024 * 2;
	private static final int MAX_REQUEST_SIZE = 1024 * 1024;

	private static final String path = System
			.getProperty("jboss.server.temp.dir")
			+ File.separator
			+ "soap"
			+ File.separator + "ws";


	public SoapControllerServlet() {
		super();
	}

	private void delete(File file) {
		for (File f : file.listFiles()) {
			if (f.isDirectory()) {
				delete(f);
			} else {
				f.delete();
			}
		}
		file.delete();
	}

	private void process(HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		// Angular JS response headers
		response.addHeader("Access-Control-Allow-Origin", "*");
		response.addHeader("Access-Control-Allow-Headers",
				"X-PINGOTHER, Origin, X-Requested-With, Content-Type, Accept");
		response.addHeader("Access-Control-Allow-Methods",
				"POST, GET, OPTIONS, PUT, DELETE, HEAD");

		response.addHeader("Access-Control-Max-Age", "1728000");
		response.setContentType("application/json; charset=UTF-8");
		response.setCharacterEncoding("UTF-8");

		// Requests /soap/create /soap/edit /soap/services /soap/delete
		String[] parts = request.getRequestURI().toString().split("/");

		String result = "";
		if (parts.length >= 4) {
			String action = parts[3];

			// Returns JSON of all services
			if ("services".equals(action)) {

				JSONArray services = new JSONArray();
				JSONObject service;

				File storageFolder = new File(path);
				if (storageFolder.exists()) {
					for (File f : storageFolder.listFiles()) {
						if (f.isDirectory()) {
							service = new JSONObject();
							service.put("text", f.getName());
							services.add(service);
						}
					}
				}
				if (services != null) {
					result = services.toString();
				}

				// Creates new service with specified name and wsdl file
			} else if ("create".equals(action)) {

				String service = "";
				FileItem uploadedFile = null;

				DiskFileItemFactory factory = new DiskFileItemFactory();
				factory.setSizeThreshold(MAX_MEMORY_SIZE);
				factory.setRepository(new File(System
						.getProperty("jboss.server.temp.dir")));

				ServletFileUpload upload = new ServletFileUpload(factory);
				upload.setSizeMax(MAX_REQUEST_SIZE);

				try {
					List<FileItem> fileItems = upload.parseRequest(request);
					Iterator<FileItem> i = fileItems.iterator();
					System.out.println(fileItems.size());
					while (i.hasNext()) {
						FileItem fi = (FileItem) i.next();
						if (!fi.isFormField()) {
							uploadedFile = fi;
						} else {
							service = fi.getString();
						}
					}

					File folderToCreate = new File(path + File.separator
							+ service);
					folderToCreate.mkdirs();
					File wsdlFile = new File(path + File.separator + service
							+ File.separator + service + ".wsdl");
					wsdlFile.createNewFile();
					uploadedFile.write(wsdlFile);

					
					File configFile = new File(path + File.separator + service
							+ File.separator + "config.xml");
//					configFile.createNewFile();
//					uploadedFile.write(configFile);
					WParser wp = new WParser(path + File.separator + service
							+ File.separator + service + ".wsdl");
					String xsdPath= "C:/Users/Kudaybergen/Documents/GitHub/stub-application/stub-app/src/main/webapp/WEB-INF/templates/configxsd.xsd";
					String xmlPath= path + File.separator + service
							+ File.separator + "config.xml";
					wp.writeXML(configFile,service);
					wp.validateXMLSchema(xsdPath, xmlPath);
					

				} catch (Exception e) {
					e.printStackTrace();
				}
			} else if ("delete".equals(action)) {
				if (parts.length >= 5) {
					File folderToDelete = new File(path + File.separator
							+ parts[4]);
					if (folderToDelete.exists()) {
						delete(folderToDelete);
						result = "Successfully deleted";
					} else {
						result = "No File Found";
					}

				}

			} else if ("service".equals(action)) {
				if (parts.length >= 5) {
					BufferedReader in = new BufferedReader(new FileReader(path
							+ File.separator + parts[4] + File.separator
							+ "config.xml"));
					String xml = "";
					while (in.ready()) {
						xml += in.readLine();
					}
					in.close();
					XMLSerializer xmlSerializer = new XMLSerializer();
					
					JSON json = xmlSerializer.read(xml);
					result = json.toString().replace("@", "");
				}
			}
		}
		PrintWriter out = response.getWriter();
		out.print(result);
		out.flush();
		out.close();
	}

	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		process(request, response);
	}

	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		process(request, response);
	}

}
