package com.c3.lazypage.query;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.c3.lazypage.entity.Element;
import com.c3.lazypage.entity.FastDom;

public class QueryLazyPage {
	public static Element queryLazyPageSelector(FastDom dom, String selector) {
		String[] selectors = selector.split(" ");
		for (int i = 0; i < selectors.length; i++) {
			selectors[i] = ".lazypagelevel" + i + selectors[i];
		}
		selector = join(selectors, " ");

		addLevelMark(dom);
		//System.out.println(selector);
		//System.out.println(html);
		return dom.querySelector(selector);
	}
	
	private static String join(String[] strs, String contact){
		String result="";
		for(int i=0;i<strs.length;i++){
			result=result+(i==0?"":contact)+strs[i];
		}
		return result;
	}

	private static void addLevelMark(FastDom dom) {
		List<Element> elements = dom.querySelectorAll(".lazypage");
		int addCount = 0;
		for (int i = 0; i < elements.size(); i++) {
			Element item = elements.get(i);
			int end = item.getEnd();
			int level = 0;
			for (int j = 0; j < i; j++) {
				if (end < elements.get(j).getEnd())
					level++;
			}
			String mark = " lazypagelevel" + level;
			String classes = item.getAttribute("class");
		    item.setAttribute("class", classes + mark);
		}
	}

	private static String removeLevelMark(String html) {
	  return html!=null ? html.replaceAll(" lazypagelevel\\d", "") : null;
	}
}
