package com.c3.lazypage.entity;

import java.util.ArrayList;

public interface QueryInterface {
	public Element querySelector(String regStr);
	public ArrayList<Element> querySelectorAll(String regStr);
}
