package com.c3.lazypage.query;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class QueryLazyPage {
	public static String queryLazyPageSelector(String html, String selector) {
		String[] selectors = selector.split(" ");
		for (int i = 0; i < selectors.length; i++) {
			selectors[i] = ".lazypagelevel" + i + selectors[i];
		}
		selector = join(selectors, " ");

		html = addLevelMark(html);
		//System.out.println(selector);
		//System.out.println(html);
		return removeLevelMark(QueryHelp.querySelector(html, selector));
	}
	
	private static String join(String[] strs, String contact){
		String result="";
		for(int i=0;i<strs.length;i++){
			result=result+(i==0?"":contact)+strs[i];
		}
		return result;
	}

	private static String addLevelMark(String html) {
		List<LazyPageBlock> elements = queryLazyPageElement(html);
		int addCount = 0;
		for (int i = 0; i < elements.size(); i++) {
			LazyPageBlock item = elements.get(i);
			int end = item.getEnd();
			for (int j = 0; j < i; j++) {
				if (end < elements.get(j).getEnd())
					item.setLevel(item.getLevel()+1);
			}
			String mark = " lazypagelevel" + item.getLevel();
			html = html.substring(0, item.getLazypageIndex() + addCount) + mark
					+ html.substring(item.getLazypageIndex() + addCount);
			addCount += mark.length();
		}
		return html;
	}

	private static String removeLevelMark(String html) {
	  return html!=null ? html.replaceAll(" lazypagelevel\\d", "") : null;
	}

	private static List<LazyPageBlock> queryLazyPageElement(String html) {
	  String regStr = "< *([^ >]*)[^>]*?\\b(class *= *\"[^\"]*\")[^>]*>";
	  Pattern match = Pattern.compile(regStr, Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
	  List<LazyPageBlock> result = new ArrayList<LazyPageBlock>();
	  Matcher group = match.matcher(html);
	  while (group.find()) {
	    int classIndex;
	    Matcher pageClassMatcher=Pattern.compile("\\blazypage\\b").matcher(group.group(2));
	    if(pageClassMatcher.find()){
	    	classIndex=pageClassMatcher.start();
	    }else{
	    	continue;
	    }
	    int lazypageIndex = group.start(2) + classIndex + "lazypage".length();
	    int searchStart = group.end();
	    int closeIndex = 0;
	    if (Pattern.compile("\\/ *>").matcher(group.group(0)).find() == false) {
			closeIndex = QueryHelp.queryCloseTag(group.group(1), html.substring(searchStart));
		}
	    LazyPageBlock lazyPage = new LazyPageBlock(group.start(), searchStart + closeIndex, lazypageIndex, 0);
	    result.add(lazyPage);
	  }
	  return result;
	}
}

class LazyPageBlock {
	private int start;
	private int end;
	private int lazypageIndex;
	private int level;
	public int getStart() {
		return start;
	}
	public int getEnd() {
		return end;
	}
	public int getLazypageIndex() {
		return lazypageIndex;
	}
	public int getLevel() {
		return level;
	}
	public void setLevel(int level) {
		this.level = level;
	}
	public LazyPageBlock(int start, int end, int lazypageIndex, int level) {
		this.start = start;
		this.end = end;
		this.lazypageIndex = lazypageIndex;
		this.level = level;
	}
	@Override
	public String toString() {
		return "LazyPageBlock [start=" + start + ", end=" + end + ", lazypageIndex=" + lazypageIndex + ", level="
				+ level + "]";
	}
}
