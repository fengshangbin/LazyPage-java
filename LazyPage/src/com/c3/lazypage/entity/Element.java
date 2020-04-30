package com.c3.lazypage.entity;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.c3.lazypage.query.QueryHelp;

public class Element implements QueryInterface {
	private String attrRegStr = " *= *\"([^\"]*)\"";

	private FastDom dom;
	private int start;
	private int end;
	private String html;
	private int attrLen;
	private int closeLen;

	public Element(FastDom dom, int start, int end, String html, int attrLen, int closeLen) {
		super();
		this.dom = dom;
		this.start = start;
		this.end = end;
		this.html = html;
		this.attrLen = attrLen;
		this.closeLen = closeLen;
	}

	public int getStart() {
		return start;
	}

	public int getEnd() {
		return end;
	}

	public int getAttrLen() {
		return attrLen;
	}

	public FastDom getDom() {
		return dom;
	};

	public String getInnerHTML() {
		return html.substring(attrLen, html.length() - closeLen);
	}

	public void setInnerHTML(String innerhtml) {
		if (innerhtml == null)
			innerhtml = "";
		innerhtml = innerhtml.toString();
		html = html.substring(0, attrLen) + innerhtml + html.substring(end - closeLen - start);
		dom.notifyChangeIndex(start + attrLen, end - closeLen, innerhtml, this);
	}

	public String getOuterHTML() {
		return html;
	}

	public void setOuterHTML(String outerHtml) {
		dom.notifyChangeIndex(start, end, outerHtml, this);
		// Release Element
		destroy();
	};

	public String getAttrHTML() {
		return html.substring(0, attrLen);
	};

	public void setAttrHTML(String attrHTML) {
		html = attrHTML + html.substring(attrLen);
		int lenOff = attrHTML.length() - attrLen;
		dom.notifyChangeIndex(start, start + attrLen, attrHTML, this);
		attrLen += lenOff;
	};

	private void destroy() {
		if (dom != null)
			dom.releaseElement(this);
		dom = null;
	}

	private Matcher getAttributeObj(String key) {
		String regStr = key + attrRegStr;
		Pattern reg = Pattern.compile(regStr, Pattern.CASE_INSENSITIVE);
		String attrHTML = getAttrHTML();
		Matcher m = reg.matcher(attrHTML);
		if (m.find()) {
			return m;
		}
		return null;
	}

	public String getAttribute(String key) {
		Matcher obj = getAttributeObj(key);
		if (obj == null)
			return null;
		return obj.group(1);
	};

	public boolean hasAttribute(String key) {
		Matcher m = this.getAttributeObj(key);
		return m != null;
	};

	public void setAttribute(String key, String value) {
		Matcher obj = getAttributeObj(key);
		String newAttr = (obj == null ? " " : "") + key + "=\"" + value + "\"";
		if (value == null)
			newAttr = "";
		int newLen = newAttr.length();
		int oldStart = obj == null ? attrLen - 1 : obj.start();
		int oldLen = obj == null ? 0 : obj.group(0).length();

		attrLen += newLen - oldLen;
		html = html.substring(0, oldStart) + newAttr + html.substring(oldStart + oldLen);

		dom.notifyChangeIndex(start + oldStart, start + oldStart + oldLen, newAttr, this);

	};

	public void removeAttribute(String key) {
		this.setAttribute(key, null);
	};

	public void changeIndex(int _start, int offlen, int _end, Element source) {
		if (start >= _start && end <= _end) {
			destroy();
		} else if (end > _start) {
			// console.log("before","."+html+".", start, end);
			boolean hasChangeHTML = start < _end;
			if (start > _start) {
				start += offlen;
			}
			end += offlen;
			if (hasChangeHTML && this != source) {
				html = dom.getHTML().substring(start, end);
			}
			// console.log("after","."+html+".", start, end);
		}
	};

	public Element querySelector(String regStr) {
		return QueryHelp.querySelector(this, regStr);
	};

	public ArrayList<Element> querySelectorAll(String regStr) {
		return QueryHelp.querySelectorAll(this, regStr);
	}

	@Override
	public String toString() {
		return "Element [start=" + start + ", end=" + end + ", html=" + html + ", attrLen=" + attrLen + ", closeLen="
				+ closeLen + "]";
	}
}
