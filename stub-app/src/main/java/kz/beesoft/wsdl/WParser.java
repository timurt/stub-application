package kz.beesoft.wsdl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.xml.sax.SAXException;

import com.predic8.wsdl.Definitions;
import com.predic8.wsdl.Operation;
import com.predic8.wsdl.PortType;
import com.predic8.wsdl.WSDLParser;

public class WParser {
	String url;
	public List<String> requests;
	WSDLParser parser;
	Definitions defs;

	// public static java.lang.String WSDL_SERVICE;

	public WParser(String url) {
		parser = new WSDLParser();
		this.url = url;
		defs = parser.parse(url);
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
}
