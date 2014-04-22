package kz.beesoft.wsdl;

import java.util.ArrayList;

public class Config {
	String name;
	ArrayList<Method> methodlist;

	public Config() {
		methodlist = new ArrayList<Method>();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public ArrayList<Method> getMethodlist() {
		return methodlist;
	}

	public void setMethodlist(ArrayList<Method> methodlist) {
		this.methodlist = methodlist;
	}
}
