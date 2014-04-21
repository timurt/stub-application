package kz.beesoft.wsdl;

import java.util.ArrayList;

public class Method {
	String name;
	ArrayList<Variable> variables= new ArrayList<Variable>();
	ArrayList<Case> cases= new ArrayList<Case>();
	
	public Method(){
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
