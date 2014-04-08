package kz.beesoft.processor;

import javax.enterprise.inject.Alternative;

import kz.beesoft.client.IProcessor;

@Alternative
public class StubProcessor implements IProcessor {

	@Override
	public String process(String config, String request) {
		return "Empty";
	}

}
