package kz.beesoft.wsdl;

import groovy.xml.MarkupBuilder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.xml.sax.SAXException;

import com.predic8.wsdl.Binding;
import com.predic8.wsdl.Definitions;
import com.predic8.wsdl.Message;
import com.predic8.wsdl.Operation;
import com.predic8.wsdl.Output;
import com.predic8.wsdl.Part;
import com.predic8.wsdl.PortType;
import com.predic8.wsdl.WSDLParser;
import com.predic8.wstool.creator.RequestTemplateCreator;
import com.predic8.wstool.creator.SOARequestCreator;

public class WParser {
	String url;
	public List<String> requests;
	WSDLParser parser;
	Definitions defs;

	public WParser(String url, String service) {
		parser = new WSDLParser();
		this.url = url;
		String url2 = url + service + ".wsdl";
		defs = parser.parse(url2);
	}

	public ArrayList<String> getMethods() {
		ArrayList<String> methods = new ArrayList<String>();
		for (PortType pt : defs.getPortTypes()) {
			for (Operation op : pt.getOperations()) {
				String mm = op.getName();
				methods.add(mm);
			}
		}
		return methods;
	}

	public void writeXML(File file, String configuration) {
		ArrayList<String> methodsList = getMethods();
		try {
			PrintWriter out = new PrintWriter(file);
			out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
			out.println("<config name=\"" + configuration + "\">");
			out.println("	<methods>");
			for (String name : methodsList) {
				out.println("		<method name=\"" + name + "\">");
				out.println("			<variables> </variables>");
				out.println("			<cases> </cases>");
				out.println("		</method>");
			}
			out.println("	</methods>");
			out.println("</config>");
			out.flush();
			out.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public boolean validateXMLSchema(String xsdPath, String xmlPath) {

		try {
			SchemaFactory factory = SchemaFactory
					.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
			try {
				Schema schema = factory.newSchema(new File(xsdPath));
				Validator validator = schema.newValidator();
				validator.validate(new StreamSource(new File(xmlPath)));
			} catch (SAXException e) {
				e.printStackTrace();
				return false;
			}
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public void writeOp() {
		for (PortType pt : defs.getPortTypes()) {
			for (Operation op : pt.getOperations()) {
				getResponse(op);
				 for (Binding bin : defs.getBindings()) {
				 getRequests(op, pt, bin);
				 }
			}
		}
	}

	public void getResponse(Operation o) {
		String path = url + "templates" + File.separator + o.getName();
		File responseFile = new File(path);
		if (!responseFile.exists()) {
			responseFile.mkdirs();
		} else {
		}
		File respon = new File(path + File.separator + "response.xml");
		if (respon.exists()) {
			try {
				respon.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		PrintWriter out;
		try {
			out = new PrintWriter(respon);
			Output output = o.getOutput();
			Message m = defs.getMessage(output.getMessagePrefixedName().getLocalName());
			for (Part p : m.getParts()) {
				String s = p.getElement().getRequestTemplate().replace("?XXX?", "?");
				String str = s.replace("?", "");
				String firstpart = "\n <s11:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\"> \n <s11:Body>";
				String secondpart = "\n </s11:Body> \n </s11:Envelope>";
				String all = firstpart + " " + str + " " + secondpart;
				out.println(all);
			}
			out.flush();
			out.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void getRequests(Operation o, PortType pt, Binding binding) {
		String path = url + "templates" + File.separator + o.getName();
		File respon = new File(path + File.separator + "request.xml");
		try {
			respon.createNewFile();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		PrintWriter out;
		try {
			out = new PrintWriter(respon);
			StringWriter writer = new StringWriter();
			SOARequestCreator creator = new SOARequestCreator(defs,	new RequestTemplateCreator(), new MarkupBuilder(writer));
			creator.createRequest(pt.getName(), o.getName(), binding.getName());
			String s = writer.toString().replace("?XXX?", "?");
			String str = s.toString().replace("?", "");
			out.print(str);
			out.flush();
			out.close();
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
}
