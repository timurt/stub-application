package kz.beesoft.servlet;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import kz.beesoft.wsdl.Case;
import kz.beesoft.wsdl.CaseOutput;
import kz.beesoft.wsdl.Config;
import kz.beesoft.wsdl.Method;
import kz.beesoft.wsdl.Variable;
import kz.beesoft.wsdl.WParser;
import net.sf.json.JSON;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;
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
		File folder = new File(path);
		folder.mkdirs();
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
				} else {
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
					configFile.createNewFile();

					WParser wp = new WParser(path + File.separator + service
							+ File.separator, service);
					// String xsdPath = System
					// .getProperty("jboss.server.temp.dir")
					// + File.separator
					// + "soap"
					// + File.separator
					// + "configxsd.xsd";
					// String xmlPath = path + File.separator + service
					// + File.separator + "config.xml";
					wp.writeXML(configFile, service);
					wp.writeOp();

					// if (wp.validateXMLSchema(xsdPath, xmlPath)) {
					// System.out.println("Config validation complete");
					// } else {
					// System.out.println("Error");
					// }

					response.sendRedirect(request.getContextPath()
							+ "/edit.html?service=" + service);

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
					if (parts.length >= 7 && parts[5].equals("grequest")) {
						response.setContentType("application/xml; charset=UTF-8");
						BufferedReader in = new BufferedReader(new FileReader(
								path + File.separator + parts[4]
										+ File.separator + "templates"
										+ File.separator + parts[6]
										+ File.separator + "request.xml"));

						String xml = "";
						while (in.ready()) {
							xml += in.readLine();
						}
						result = xml;
						in.close();
					} else if (parts.length >= 7 && parts[5].equals("srequest")) {
						response.setContentType("application/xml; charset=UTF-8");
						PrintWriter out = new PrintWriter(path + File.separator
								+ parts[4] + File.separator + "templates"
								+ File.separator + parts[6] + File.separator
								+ "request.xml");

						String xml = "";
						byte[] xmlData = new byte[request.getContentLength()];
						try {
							BufferedInputStream in = new BufferedInputStream(
									request.getInputStream());
							in.read(xmlData, 0, xmlData.length);
							if (request.getCharacterEncoding() != null) {
								xml = new String(xmlData,
										request.getCharacterEncoding());
							} else {

								xml = new String(xmlData);
							}
							in.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
						out.println(xml);
						out.flush();
						out.close();
					} else if (parts.length >= 7
							&& parts[5].equals("gresponse")) {
						response.setContentType("application/xml; charset=UTF-8");
						BufferedReader in = new BufferedReader(new FileReader(
								path + File.separator + parts[4]
										+ File.separator + "templates"
										+ File.separator + parts[6]
										+ File.separator + "response.xml"));

						String xml = "";
						while (in.ready()) {
							xml += in.readLine();
						}
						result = xml;
						in.close();
					} else if (parts.length >= 7
							&& parts[5].equals("sresponse")) {
						response.setContentType("application/xml; charset=UTF-8");
						PrintWriter out = new PrintWriter(path + File.separator
								+ parts[4] + File.separator + "templates"
								+ File.separator + parts[6] + File.separator
								+ "response.xml");

						String xml = "";
						byte[] xmlData = new byte[request.getContentLength()];
						try {
							BufferedInputStream in = new BufferedInputStream(
									request.getInputStream());
							in.read(xmlData, 0, xmlData.length);
							if (request.getCharacterEncoding() != null) {
								xml = new String(xmlData,
										request.getCharacterEncoding());
							} else {

								xml = new String(xmlData);
							}
							in.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
						out.println(xml);
						out.flush();
						out.close();
					} else if (parts.length >= 8
							&& parts[5].equals("fresponse")) {
						response.setContentType("application/xml; charset=UTF-8");
						PrintWriter out = new PrintWriter(path + File.separator
								+ parts[4] + File.separator + "templates"
								+ File.separator + parts[6] + File.separator
								+ "responses" + File.separator + parts[7]);

						String xml = "";
						byte[] xmlData = new byte[request.getContentLength()];
						try {
							BufferedInputStream in = new BufferedInputStream(
									request.getInputStream());
							in.read(xmlData, 0, xmlData.length);
							if (request.getCharacterEncoding() != null) {
								xml = new String(xmlData,
										request.getCharacterEncoding());
							} else {

								xml = new String(xmlData);
							}
							in.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
						out.println(xml);
						out.flush();
						out.close();
					} else if (parts.length >= 6 && parts[5].equals("save")) {
						String out = path + File.separator + parts[4]
								+ File.separator + "config.xml";

						String xml = "";
						byte[] xmlData = new byte[request.getContentLength()];
						try {
							BufferedInputStream in = new BufferedInputStream(
									request.getInputStream());
							in.read(xmlData, 0, xmlData.length);
							if (request.getCharacterEncoding() != null) {
								xml = new String(xmlData,
										request.getCharacterEncoding());
							} else {

								xml = new String(xmlData);
							}
							in.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
						JSON json = JSONSerializer.toJSON(xml);
						JSONtoXML(json, out);

					} else {
						BufferedReader in = new BufferedReader(new FileReader(
								path + File.separator + parts[4]
										+ File.separator + "config.xml"));
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
		}
		PrintWriter out = response.getWriter();
		out.println(result);
		out.flush();
		out.close();
	}

	private void JSONtoXML(JSON js, String path) {

		PrintWriter out;
		try {
			out = new PrintWriter(path);

			Config config = new Config();

			JSONObject json = (JSONObject) JSONSerializer.toJSON(js);
			String configName = json.getString("name");

			// Configuration parameters
			config.setName(configName);

			ArrayList<Method> methodList = new ArrayList<Method>();
			if (!json.getJSONArray("methods").isEmpty()) {
				List<JSONObject> jsobj = (List) json.getJSONArray("methods");
				for (JSONObject method1 : jsobj) {
					Method m = new Method();
					m.setName(method1.getString("name"));
					ArrayList<Variable> variablelist = new ArrayList<Variable>();
					ArrayList<Case> caselist = new ArrayList<Case>();
					if (!method1.getString("variables").equals(" ")) {
						List<JSONObject> variables = (List) method1
								.getJSONArray("variables");
						for (JSONObject variable : variables) {
							Variable var = new Variable();
							var.setKey(variable.getString("key"));
							var.setPath(variable.getString("path"));
							variablelist.add(var);
						}
					}
					if (!method1.getString("cases").equals(" ")) {
						List<JSONObject> cases = (List) method1
								.getJSONArray("cases");
						for (JSONObject cas : cases) {
							Case c = new Case();
							c.setTest(cas.getString("test"));
							JSONObject file = cas.getJSONObject("file");
							c.setFilepath(file.getString("path"));
							ArrayList<CaseOutput> outputList = new ArrayList<CaseOutput>();
							List<JSONObject> caseout = cas
									.getJSONArray("outputs");
							for (JSONObject outinfo : caseout) {
								CaseOutput o = new CaseOutput();
								o.setPath(outinfo.getString("path"));
								o.setValue(outinfo.getString("value"));
								outputList.add(o);
							}
							c.setOutputs(outputList);
							caselist.add(c);
						}
					}
					m.setCases(caselist);
					m.setVariables(variablelist);
					methodList.add(m);
				}
			}
			config.setMethodlist(methodList);
			WParser wp = new WParser();
			wp.writeXML(config, out);

			// String xsdPath = System.getProperty("jboss.server.temp.dir")
			// + File.separator + "soap" + File.separator
			// + "configxsd.xsd";
			// String xmlPath = path;
			// if (wp.validateXMLSchema(xsdPath, xmlPath)) {
			// System.out.println("Config validation complete");
			// } else {
			// System.out.println("Error");
			// }
			out.flush();
			out.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
