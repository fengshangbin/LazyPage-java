package com.c3.lazypage.entity;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Block {
	//private String outHtml;
	private String innerHtml;
	private String attrHtml;
	private Document document;
	private int start;
	private int end;
	private String data;
	private String html;
	public String getHtml() {
		return html;
	}
	public void setHtml(String html) {
		this.html = html;
	}
	public String getData() {
		return data;
	}
	public void setData(String data) {
		this.data = data;
	}
	private int runStated;
	
	private String attrRegStr = " *= *\"([^\"]*)\"";
	public Block(String attrHtml, String innerHtml, Document document, int start, int end) {
		this.attrHtml = attrHtml;
		this.innerHtml = innerHtml;
		this.document = document;
		this.start = start;
		this.end = end;
		this.runStated = 0;
	}
	
	/*public String getOutHtml() {
		return outHtml;
	}*/
	public void setOutHtml(String outHtml) {
		//this.outHtml = outHtml;
		int oldLength = end-start;
		int newLength = outHtml.length();
		int offLength = newLength-oldLength;
		String oldHtml = document.getHtml();
		document.setHtml(oldHtml.substring(0, start)+outHtml+oldHtml.substring(end));
		document.notifyChangeIndex(start, offLength);
		runStated=2;
		//document.removeBlock(this);
	}
	public String getInnerHtml() {
		return innerHtml;
	}
	/*public void setInnerHtml(String innerHtml) {
		this.innerHtml = innerHtml;
	}*/
	public String getAttrHtml() {
		return attrHtml;
	}
	/*public void setAttrHtml(String attrHtml) {
		this.attrHtml = attrHtml;
	}*/
	/*public Document getDocument() {
		return document;
	}
	public void setDocument(Document document) {
		this.document = document;
	}*/
	public int getRunStated() {
		return runStated;
	}
	public void setRunStated(int runStated) {
		this.runStated = runStated;
	}
	
	public String getAttribute(String key){
		String regStr = key + attrRegStr;
		Pattern pattern = Pattern.compile(regStr, Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
		Matcher m = pattern.matcher(attrHtml);
		if(m.find()){
			//System.out.println("+++++"+m.group(1));
			return m.group(1);
		}
		return null;
	}
	public String[] getOutAttribute(String key){
		String regStr = key + attrRegStr;
		Pattern pattern = Pattern.compile(regStr, Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
		Matcher m = pattern.matcher(attrHtml);
		if(m.find()){
			//System.out.println("+++++"+m.group(0)+"--"+m.group(1));
			return new String[]{m.group(0), m.group(1)};
		}
		return null;
	}
	public boolean hasAttribute(String key){
		String attr = getAttribute(key);
		return attr!=null && attr.isEmpty()==false;
	}
	public void setAttribute(String key, String value){
		String[] oldAttr = getOutAttribute(key);
		if(oldAttr == null)return;
		int oldLength = oldAttr[1].length();
		int newLength = value.length();
		int offLength = newLength-oldLength;
		String newAttr = oldAttr[0].replace(oldAttr[1], value);
		//System.out.println(attrHtml+"---"+oldAttr[0]+"---"+newAttr);
		attrHtml = attrHtml.replace(oldAttr[0], newAttr);
		String oldHtml = document.getHtml();
		document.setHtml(oldHtml.substring(0, start) + attrHtml + oldHtml.substring(start+attrHtml.length()-offLength));
		document.notifyChangeIndex(start, offLength);
	}
	public void tagBlock(){
		setAttribute("type","x-tmpl-lazypage-tag");
	}
	public void changeIndex(int start, int offLength){
		if(this.start==start)this.end+=offLength;
		else if(this.start>start){
			this.start+=offLength;
			this.end+=offLength;
		}
	}
}
