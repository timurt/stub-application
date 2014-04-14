package kz.beesoft.processor;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.StringTokenizer;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import kz.beesoft.client.IProcessor;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class SoapProcessor implements IProcessor {

	static HashMap<String, String> ns, ns2;

	static HashMap<String, String> final_data = new HashMap<String, String>();

	public static String parseSoap(String mess, String path, XPath xpath)
			throws Exception {

		DocumentBuilderFactory domFactory = DocumentBuilderFactory
				.newInstance();
		domFactory.setNamespaceAware(true);
		DocumentBuilder builder = domFactory.newDocumentBuilder();
		Document doc = builder.parse(new InputSource(new StringReader(mess)));

		// Create XPathFactory object
		XPathExpression expr = xpath.compile(path);

		Node node = (Node) expr.evaluate(doc, XPathConstants.NODE);

		return node.getTextContent();

	}

	public static String parseConfig(String mess, String soap, String path,
			XPath xpath) {
		String s = "";

		try {

			Node n = (Node) xpath.compile(path).evaluate(
					loadXMLFromString(mess), XPathConstants.NODE);
			for (int i = 0; i < n.getChildNodes().getLength(); i++) {
				final_data.put(
						(n.getChildNodes().item(i).getAttributes().item(0)
								.getTextContent()),
						(parseSoap(soap, n.getChildNodes().item(i)
								.getAttributes().item(1).getTextContent(),
								xpath)));

			}
			return s;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static Document loadXMLFromString(String xml) throws Exception {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		factory.setNamespaceAware(false);

		InputSource is = new InputSource(new StringReader(xml));
		// factory.setIgnoringElementContentWhitespace(true);
		return builder.parse(is);
	}

	public static HashMap<String, String> divide(String s) {
		HashMap<String, String> hm = new HashMap<String, String>();
		StringTokenizer st = new StringTokenizer(s, "<>>< ");
		while (st.hasMoreTokens()) {
			String temp = st.nextToken();
			if (temp.startsWith("xmlns:")) {
				String[] nsDef = temp.substring("xmlns:".length()).split("=");
				if (nsDef.length == 2) {
					hm.put(nsDef[0],
							new StringTokenizer(nsDef[1], "\"").nextToken());
				}
			}
		}
		return hm;
	}

	private static String nodeToString(Node node) {
		StringWriter sw = new StringWriter();
		try {
			Transformer t = TransformerFactory.newInstance().newTransformer();
			t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			t.transform(new DOMSource(node), new StreamResult(sw));
		} catch (TransformerException te) {
			System.out.println("nodeToString Transformer Exception");
		}
		return sw.toString();
	}

	public static String getCondition(String mess, XPath xpath, String method)
			throws Exception {

		Node n = (Node) xpath.compile(
				"/config/methods/method[@name = '" + method + "']/cases")
				.evaluate(loadXMLFromString(mess), XPathConstants.NODE);

		for (int i = 0; i < n.getChildNodes().getLength(); i++) {
			if (n.getChildNodes().item(i).getNodeName().equals("case")) {
				if (compute(final_data, n.getChildNodes().item(i)
						.getAttributes().item(0).getTextContent())) {
					return createResponse(mess, xpath, n.getChildNodes()
							.item(i));
				}
			}
		}

		return "error_response";
	}

	public static boolean compute(HashMap<String, String> hm, String s)
			throws Exception {

		ScriptEngineManager mgr = new ScriptEngineManager();
		ScriptEngine engine = mgr.getEngineByName("JavaScript");
		for (String key : hm.keySet()) {
			if (s.contains(key)) {
				if (hm.get(key).matches(".*[a-zA-Z].*")) {
					s = s.replace(key, "'" + hm.get(key) + "'");
				} else {
					s = s.replace(key, hm.get(key));
				}
			}
		}
		// System.out.println(s);
		return Boolean.parseBoolean(engine.eval(s).toString());
	}

	@Override
	public String process(String config, String request) {
		// TODO Auto-generated method stub
		/*
		 * String soap =
		 * "<soap:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">"
		 * + "<soap:Body>" + "<getName>" + "<iin>123</iin>" +
		 * "<surname>Mazhikov</surname>" + "<name>Baur</name>" + "</getName>" +
		 * "</soap:Body>" + "</soap:Envelope>";
		 * 
		 * config = "<config> <methods> <method name='getName'>" +
		 * "<variables><variable key=':iin' path='/soap:Envelope/soap:Body/getName/iin'>"
		 * +
		 * "</variable><variable key=':name' path='/soap:Envelope/soap:Body/getName/name'></variable>"
		 * +
		 * "<variable key=':surname' path='/soap:Envelope/soap:Body/getName/surname'></variable>"
		 * + "</variables><cases>" +
		 * "<case test=':iin == 123' /><case test=\":name == 'Baur'\" /><response></response><outputs><output path='/GetNameResponse/name' value='Baur'/>"
		 * + "</outputs> </cases> </method></methods></config>";
		 */
		// System.out.println(config);
		// System.out.println(request);
		try {
			InputStream is = new ByteArrayInputStream(request.getBytes());
			SOAPMessage soapMessage = MessageFactory.newInstance()
					.createMessage(null, is);
			SOAPBody body = soapMessage.getSOAPBody();
			SOAPEnvelope soapEnv = soapMessage.getSOAPPart().getEnvelope();
			NodeList nl = soapEnv.getChildNodes();
			String method = body.getChildNodes().item(0).getNodeName();
			String s = "";
			for (int i = 0; i < nl.getLength(); i++) {
				s += nodeToString(nl.item(i));
			}

			ns = divide(s);

			String path = "/config/methods/method[@name = '" + method
					+ "']/variables";

			// System.out.println(ns.keySet());
			// System.out.println(ns.values());

			XPathFactory xpathFactory = XPathFactory.newInstance();

			// Create XPath object
			XPath xpath = xpathFactory.newXPath();
			xpath.setNamespaceContext(new NamespaceContext() {
				public String getNamespaceURI(String prefix) {
					for (int i = 0; i < ns.size(); i++) {
						if (ns.containsKey(prefix)) {
							return ns.get(prefix);
						}
					}
					return XMLConstants.NULL_NS_URI;
				}

				// This method isn't necessary for XPath processing.
				public String getPrefix(String uri) {
					throw new UnsupportedOperationException();
				}

				@Override
				public Iterator<?> getPrefixes(String namespaceURI) {
					// TODO Auto-generated method stub
					return null;
				}

			});

			path = parseConfig(config, request, path, xpath);

			return getCondition(config, xpath, method);
		} catch (Exception e) {
			e.printStackTrace();
			return "Error";
		}
	}

	public static String createResponse(String mess, XPath xpath, Node node)
			throws Exception {
		Node n = (Node) xpath.compile("/config").evaluate(
				loadXMLFromString(mess), XPathConstants.NODE);
		NodeList nl = node.getChildNodes();
		String path = "";
		for (int i = 0; i < nl.getLength(); i++) {
			if (nl.item(i).getNodeName().equals("file")) {
				path = System.getProperty("jboss.server.temp.dir")
						+ File.separator + "soap" + File.separator + "ws"
						+ File.separator
						+ n.getAttributes().item(0).getTextContent()
						+ File.separator
						+ nl.item(i).getAttributes().item(0).getTextContent();

				File configFile = new File(path);
				String s = "";

				if (configFile.exists()) {
					try {
						BufferedReader in = new BufferedReader(new FileReader(
								configFile));
						while (in.ready()) {
							String temp = in.readLine();
							for (int j = 0; j < s.length(); j++) {
								if (temp.charAt(j) == ' ') {
									continue;
								} else {
									temp = temp.substring(j, temp.length());
									break;
								}
							}
							s += temp.replaceAll("\n", "").replaceAll("\r", "")
									.replaceAll("\t", "");
						}
						in.close();
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
					if (node.getChildNodes().item(1).getNodeName().equals("")) {
						return s;
					}
					return writeData(s, node, xpath);
				} else {
					return "Service not found";
				}
			}
		}

		return "";
	}

	public static String writeData(String s, Node node, XPath xpath)
			throws Exception {

		ns = divide(s);

		xpath.setNamespaceContext(new NamespaceContext() {
			public String getNamespaceURI(String prefix) {
				System.out.println(prefix + " asdasd");
				for (int i = 0; i < ns.size(); i++) {
					if (ns.containsKey(prefix)) {
						return ns.get(prefix);
					}
				}
				return XMLConstants.NULL_NS_URI;
			}

			// This method isn't necessary for XPath processing.
			public String getPrefix(String uri) {
				throw new UnsupportedOperationException();
			}

			@Override
			public Iterator<?> getPrefixes(String namespaceURI) {
				// TODO Auto-generated method stub
				return null;
			}

		});

		DocumentBuilderFactory domFactory = DocumentBuilderFactory
				.newInstance();
		domFactory.setNamespaceAware(true);
		DocumentBuilder builder = domFactory.newDocumentBuilder();
		Document doc = builder.parse(new InputSource(new StringReader(s)));

		for (int i = 0; i < node.getChildNodes().item(1).getChildNodes()
				.getLength(); i++) {

			String path = node.getChildNodes().item(1).getChildNodes().item(i)
					.getAttributes().item(0).getTextContent();
			String value = node.getChildNodes().item(1).getChildNodes().item(i)
					.getAttributes().item(1).getTextContent();
			XPathExpression expr = xpath.compile(path);

			Node nd = (Node) expr.evaluate(doc, XPathConstants.NODE);
			nd.setTextContent(value);
		}
		return nodeToString(doc.getFirstChild());
	}
}
