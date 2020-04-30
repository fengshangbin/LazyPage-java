package com.c3.lazypage.entity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

import com.c3.lazypage.query.QueryHelp;

public class FastDom implements QueryInterface {

	private String html;
	
	public FastDom(String html) {
		super();
		this.html = html;
	}

	private HashSet<Element> elements = new HashSet();

	public String getHTML() {
		return html;
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

	public void notifyChangeIndex(int start, int end, String newHTML, Element source) {
		html = html.substring(0, start) + newHTML + this.html.substring(end);
		int offlen = newHTML.length() - end + start;
		Iterator<Element> it = elements.iterator();
		while (it.hasNext()) {
			Element el = it.next();
			el.changeIndex(start, offlen, end, source);
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
