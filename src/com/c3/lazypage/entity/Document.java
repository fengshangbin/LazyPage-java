package com.c3.lazypage.entity;

import java.util.Iterator;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.c3.lazypage.servlet.LazyPageServlet;

public class Document {
	private String html;
	private Vector<Block> blocks=new Vector<Block>();
	public Document(String html){
		this.html=html;
	}
	public String getHtml() {
		return html;
	}
	public void setHtml(String html) {
		this.html = html;
	}
	public static void main(String[] args) {
		String html = LazyPageServlet.readToString("D:\\js project tools\\lazypage\\examples\\index.html");
		//System.out.println(html);
		Document doc = new Document(html);
		doc.queryBlocks();
		System.out.println(doc.getHtml());
	}
	public Vector<Block> queryBlocks(){
		removeFinishBlocks();
		String regStr = "(<script[^>]*type *= *\"x-tmpl-lazypage\"[^>]*>)([\\S\\s]*?)</script\\s*>";
		Pattern pattern = Pattern.compile(regStr, Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
		Matcher m = pattern.matcher(html);
		while (m.find()) {
			blocks.add(new Block(m.group(1), m.group(2), this,m.start(), m.end()));
			//System.out.println("+++++"+m.group(0)+"--"+m.group(1)+"--"+m.group(2)+"--"+m.start()+"-"+m.end());
		}
		tagBlocks();
		return blocks;
	}
	public Vector<Block> getBlocks(){
		return blocks;
	}
	private void tagBlocks(){
		Iterator<Block> it=blocks.iterator();  
		while(it.hasNext()){
			it.next().tagBlock();
		}
	}
	
	public void removeFinishBlocks(){
		Iterator<Block> it=blocks.iterator();  
		while(it.hasNext()){
			if(it.next().getRunStated()==2)
				it.remove();
		}
		//blocks.remove(block);
	}
	
	public void notifyChangeIndex(int start, int offlen){
		Iterator<Block> it=blocks.iterator();  
		while(it.hasNext()){
			it.next().changeIndex(start, offlen);
		}
	}
}
