package kz.beesoft.servlet;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.predic8.wsdl.*;

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

			DocumentBuilderFactory docFactory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

			// root elements
			Document doc = docBuilder.newDocument();

			Element config = doc.createElement("config");
			config.setAttribute("name", configuration);

			Element methods = doc.createElement("methods");
			doc.appendChild(config);
			config.appendChild(methods);

			for (String vm : methodsList) {

				Element method = doc.createElement("method");
				methods.appendChild(method);

				Attr attr = doc.createAttribute("name");
				method.setAttributeNode(attr);
				attr.setValue(vm); // set method name

				Element variables = doc.createElement("variables");
				method.appendChild(variables);
				variables.setTextContent(" ");
				Element cases = doc.createElement("cases");
				method.appendChild(cases);
				cases.setTextContent(" ");

			}

			TransformerFactory transformerFactory = TransformerFactory
					.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(doc);

			StreamResult result = new StreamResult(file);

			transformer.transform(source, result);
			System.out.println("File saved!");

		} catch (ParserConfigurationException pce) {
			pce.printStackTrace();
		} catch (TransformerException tfe) {
			tfe.printStackTrace();
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
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (IOException e) {
			System.out.println("Exception: " + e.getMessage());
			return false;
		}
		return true;
	}
}
