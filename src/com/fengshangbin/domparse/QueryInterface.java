package com.fengshangbin.domparse;

import java.util.ArrayList;

public interface QueryInterface {
	public Element querySelector(String regStr);
	public ArrayList<Element> querySelectorAll(String regStr);
}
