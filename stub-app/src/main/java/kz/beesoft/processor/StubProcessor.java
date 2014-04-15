package kz.beesoft.processor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import javax.enterprise.inject.Alternative;

import kz.beesoft.client.IProcessor;

@Alternative
public class StubProcessor implements IProcessor {

	@Override
	public String process(String config, String request) {
		String xml = "";
		BufferedReader in;
		try {

			in = new BufferedReader(new FileReader(
					System.getProperty("jboss.server.temp.dir")
							+ File.separator + "soap" + File.separator + "ws"
							+ File.separator + "terminal" + File.separator
							+ "response.xml"));
			while (in.ready()) {
				xml += in.readLine();
			}
			in.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return xml;
	}

}
