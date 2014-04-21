package kz.beesoft.wsdl;

import java.util.ArrayList;

public class Case {
	String test;
	String filepath;
	ArrayList<CaseOutput> outputs = new ArrayList<CaseOutput>();
	
	public Case(){
	}
	public String getTest() {
		return test;
	}
	public void setTest(String test) {
		this.test = test;
	}
	public String getFilepath() {
		return filepath;
	}
	public void setFilepath(String filepath) {
		this.filepath = filepath;
	}
	public ArrayList<CaseOutput> getOutputs() {
		return outputs;
	}
	public void setOutputs(ArrayList<CaseOutput> outputs) {
		this.outputs = outputs;
	}
}
