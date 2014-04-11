package kz.beesoft.processor;

import javax.enterprise.inject.Alternative;

import kz.beesoft.client.IProcessor;

@Alternative
public class StubProcessor implements IProcessor {

	@Override
	public String process(String config, String request) {
		return "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\">   <soap:Body>      <ConversionRateResponse xmlns=\"http://www.webserviceX.NET/\">         <ConversionRateResult>35.5465</ConversionRateResult>      </ConversionRateResponse>   </soap:Body></soap:Envelope>";
	}

}
