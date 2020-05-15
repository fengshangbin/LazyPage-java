package com.c3.lazypage.entity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

import com.c3.lazypage.query.QueryHelp;

public class FastDom implements QueryInterface {

	private String html;
	private boolean error = false;
	public FastDom(String html) {
		super();
		this.html = html;
	}
	public FastDom(String html, boolean error) {
		super();
		this.html = html;
		this.error = error;
	}

	private HashSet<Element> elements = new HashSet();

	public String getHTML() {
		return html;
	}
	
	public boolean isError() {
		return error;
	}

	public void addElement(Element el) {
		elements.add(el);
	};

	public Element findElement(int start) {
		Iterator<Element> it = elements.iterator();
		while (it.hasNext()) {
			Element el = it.next();
			if (el.getStart() == start)
				return el;
		}
		return null;
	};

	public void releaseElement(Element el) {
		elements.remove(el);
	};

	public synchronized void notifyChangeIndex(int start, int end, String newHTML, Element source) {
		html = html.substring(0, start) + newHTML + this.html.substring(end);
		int offlen = newHTML.length() - end + start;
		Iterator<Element> it = elements.iterator();
		while (it.hasNext()) {
			Element el = it.next();
			boolean result = el.changeIndex(start, offlen, end, source);
			if(!result) it.remove();
		}
	};

	public Element querySelector(String regStr) {
		return QueryHelp.querySelector(this, regStr);
	};

	public ArrayList<Element> querySelectorAll(String regStr) {
		return QueryHelp.querySelectorAll(this, regStr);
	};

	public Element getElementById(String id) {
		return QueryHelp.getElementById(this, id);
	};

	public ArrayList<Element> getElementsByTag(String tag) {
		return QueryHelp.getElementsByTag(this, tag);
	};

	public ArrayList<Element> getElementsByClass(String classNames) {
		return QueryHelp.getElementsByClass(this, classNames);
	};

}
