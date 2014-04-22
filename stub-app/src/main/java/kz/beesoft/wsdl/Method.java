package kz.beesoft.wsdl;

import java.util.ArrayList;

public class Method {
	String name;
	ArrayList<Variable> variables;
	ArrayList<Case> cases;

	public Method() {
		variables = new ArrayList<Variable>();
		cases = new ArrayList<Case>();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public ArrayList<Variable> getVariables() {
		return variables;
	}

	public void setVariables(ArrayList<Variable> variables) {
		this.variables = variables;
	}

	public ArrayList<Case> getCases() {
		return cases;
	}

	public void setCases(ArrayList<Case> cases) {
		this.cases = cases;
	}
}
