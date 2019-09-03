package com.c3.lazypage.query;

/*!
 *  QueryHelp.java 
 *  by fengshangbin 2019-06-28 
 *  ÕýÔòÆ¥Åä HTML Ç¶Ì×ÔªËØ
 */

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class MoreRegs {
	int index;
	String regStr;

	public MoreRegs(int index, String regStr) {
		this.index = index;
		this.regStr = regStr;
	}

	@Override
	public String toString() {
		return "MoreRegs [index=" + index + ", regStr=" + regStr + "]";
	}
}

class Option {
	boolean multiElement;
	ArrayList<MoreRegs> moreRegs;

	public Option(boolean multiElement, ArrayList<MoreRegs> moreRegs) {
		this.multiElement = multiElement;
		this.moreRegs = moreRegs;
	}

	@Override
	public String toString() {
		return "Option [multiElement=" + multiElement + ", moreRegs=" + moreRegs + "]";
	}
}

public class QueryHelp {
	private static Object querySelectorElement(String html, String regStr, boolean multiElement) {
		regStr = encodEscapeWord(regStr);
		String[] regArr = regStr.split(" ");
		Object source = new ArrayList<String>();
		((ArrayList<String>) source).add(html);
		int index = 0;
		while (index < regArr.length) {//source != null &&  ((ArrayList<String>) source).size() > 0 && 
			if (regArr[index].length() > 0) {
				source = queryBlock((ArrayList<String>) source, regArr[index], index == regArr.length - 1, multiElement);
			}
			if(index < regArr.length - 1 || multiElement){
				if(((ArrayList<String>)source).size()==0)break;
			}
			index++;
		}
		return source;
	}

	private static Object queryBlock(ArrayList<String> source, String regStr, boolean last, boolean multiElement) {
		String attrRegs = "";
		String attrReg = "\\[[^\\]]*\\]";
		Matcher group = Pattern.compile(attrReg).matcher(regStr);
		while (group.find()) {
			attrRegs += decodeEscapeWord(group.group(0));
		}
		regStr = regStr.replaceAll(attrReg, "");

		String idReg = "#[^#\\.\\[]*";
		Matcher group2 = Pattern.compile(idReg).matcher(regStr);
		while (group2.find()) {
			attrRegs += "[id=" + group2.group(0).substring(1) + "]";
		}
		regStr = regStr.replaceAll(idReg, "");

		String classRegs = "";
		String classReg = "\\.[^#\\.\\[]*";
		Matcher group3 = Pattern.compile(classReg).matcher(regStr);
		while (group3.find()) {
			classRegs += group3.group(0);
		}
		regStr = regStr.replaceAll(classReg, "");

		String tagReg = regStr.trim();
		if (tagReg.length() == 0)
			tagReg = "[^ >]*";
		String classRegsMark = classRegs.length() > 0 ? "[^>]*?\\bclass *= *\"([^\"]*)\"" : "";
		regStr = "< *(" + tagReg + ")(" + classRegsMark + "[^>]*)>";

		ArrayList<MoreRegs> moreRegs = new ArrayList<MoreRegs>();
		Option option = new Option(!last || multiElement, moreRegs);
		if (attrRegs.length() > 0)
			moreRegs.add(new MoreRegs(2, buildAttrReg(attrRegs)));
		if (classRegs.length() > 0)
			moreRegs.add(new MoreRegs(3, buildClassReg(classRegs)));
		ArrayList<String> resultAll = new ArrayList<String>();
		for (int i = 0; i < source.size(); i++) {
			Object result = queryElement(regStr, source.get(i), option);
			if (!option.multiElement) {
				if (result != null || i == source.size() - 1)
					return result;
			} else
				resultAll.addAll((ArrayList<String>) result);
		}
		return resultAll;
	}

	private static String encodEscapeWord(String regStr) {
		ArrayList<String> marks = new ArrayList<String>();
		Pattern marksReg = Pattern.compile("'[^']*");
		Pattern marksReg2 = Pattern.compile("\"[^\"]*\"");
		Matcher group = marksReg.matcher(regStr);
		while (group.find()) {
			marks.add(group.group(0));
		}
		Matcher group2 = marksReg2.matcher(regStr);
		while (group2.find()) {
			marks.add(group2.group(0));
		}
		for (int i = 0; i < marks.size(); i++) {
			regStr = regStr.replace(marks.get(i), marks.get(i).replaceAll(" ", "{-space-}")
					.replaceAll("\\[", "{-left-}").replaceAll("\\]", "{-right-}"));
		}
		return regStr;
	}

	private static String decodeEscapeWord(String regStr) {
		return regStr.replaceAll("\\{-space-\\}", " ").replaceAll("\\{-left-\\}", "[").replaceAll("\\{-right-\\}", "]");
	}

	private static String buildClassReg(String classNames) {
		String[] classArr = classNames.split("\\.");
		String classReg = "";
		for (int i = 0; i < classArr.length; i++) {
			String className = classArr[i];
			if (className.length() > 0) {
				classReg += "(?=.*?\\b" + className + "\\b)";
			}
		}
		return classReg;
	}

	private static Object getElementByAttr(String html, String attrs, boolean multiElement) {
		ArrayList<MoreRegs> moreRegs = new ArrayList<MoreRegs>();
		moreRegs.add(new MoreRegs(2, buildAttrReg(attrs)));
		Option option = new Option(multiElement, moreRegs);
		String regStr = "< *([^ >]*)\\b([^>]*)>";
		return queryElement(regStr, html, option);
	}

	private static String buildAttrReg(String attrs) {
		String[] attrArr = attrs.substring(1, attrs.length() - 1).split("\\]\\[");
		String attrReg = "";
		for (int i = 0; i < attrArr.length; i++) {
			String[] attrGroup = attrArr[i].split("=");
			String key = attrGroup[0].trim();
			String value = null;
			if (attrGroup.length == 2) {
				value = attrGroup[1].trim().replaceAll("^'|\"", "").replaceAll("'|\"$", "");
			}
			if (value == null) {
				attrReg += "(?=.*?\\b" + key + "\\b)";
			} else {
				attrReg += "(?=.*?\\b" + key + " *= *\"?" + value + "\"?" + "\\b)";
			}
		}
		return attrReg;
	}

	private static Object queryElement(String regStr, String html, Option option) {
		// System.out.println(option);
		Pattern match = Pattern.compile(regStr, Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
		ArrayList<String> result = new ArrayList<String>();
		Matcher group = match.matcher(html);
		while (group.find()) {
			if (option.moreRegs != null && option.moreRegs.size() > 0) {
				boolean moreState = true;
				for (int i = 0; i < option.moreRegs.size(); i++) {
					String moreContent = group.group(option.moreRegs.get(i).index);
					Pattern moreMatch = Pattern.compile(option.moreRegs.get(i).regStr,
							Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
					moreState = moreState && moreMatch.matcher(moreContent).find();
				}
				if (!moreState)
					continue;
			}
			int searchStart = group.end();
			int closeIndex = 0;
			if (Pattern.compile("\\/ *>").matcher(group.group(0)).find() == false) {
				closeIndex = queryCloseTag(group.group(1), html.substring(searchStart));
			}
			String targetHtml = html.substring(group.start(), searchStart + closeIndex);
			if (!option.multiElement) {
				return targetHtml;
			} else {
				result.add(targetHtml);
			}
		}
		if (result.isEmpty() && !option.multiElement)
			return null;
		return result;
	}

	public static int queryCloseTag(String tag, String html) {
		String regStrAll = "< */? *" + tag + "[^>]*>";
		Pattern matchAll = Pattern.compile(regStrAll, Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);

		String regStrClose = "< */ *" + tag + " *>";
		Pattern matchClose = Pattern.compile(regStrClose, Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);

		int openCount = 1;
		int lastCloseIndex = 0;
		Matcher groupAll = matchAll.matcher(html);
		while (openCount > 0) {
			boolean findAll = groupAll.find();
			if (findAll == false) {
				break;
			} else {
				if (matchClose.matcher(groupAll.group(0)).matches()) {
					openCount--;
					lastCloseIndex = groupAll.end();
				} else {
					openCount++;
					if (Pattern.compile("\\b" + tag + "\\b", Pattern.CASE_INSENSITIVE).matcher("input br image").find())
						return 0;
				}
			}
		}
		return lastCloseIndex;
	}

	public static String getElementById(String html, String id) {
		return (String) getElementByAttr(html, "[id=" + id + "]", false);
	};

	public static ArrayList<String> getElementsByTag(String html, String tag) {
		String regStr = "< *(" + tag + ")[^>]*>";
		return (ArrayList<String>) queryElement(regStr, html, new Option(true, null));
	};

	public static ArrayList<String> getElementsByClass(String html, String classNames) {
		ArrayList<MoreRegs> moreRegs = new ArrayList<MoreRegs>();
		moreRegs.add(new MoreRegs(2, buildClassReg(classNames)));
		Option option = new Option(true, moreRegs);
		String regStr = "< *([^ >]*)[^>]*?\\bclass *= *\"([^\"]*)\"[^>]*>";
		return (ArrayList<String>) queryElement(regStr, html, option);
	};

	public static String querySelector(String html, String regStr) {
		return (String) querySelectorElement(html, regStr, false);
	};

	public static ArrayList<String> querySelectorAll(String html, String regStr) {
		return (ArrayList<String>) querySelectorElement(html, regStr, true);
	};

	public static void main(String[] args) throws Exception {

		String result = QueryHelp.getElementById("123<input id=\"test\">zhende<br/><br/><br></input>321", "test");
		System.out.println(result);

		ArrayList<String> result2 = QueryHelp
				.getElementsByTag("123<input class=\"haha test\">zhende<br/><br/><br></input>321", "input");
		System.out.println(result2);

		ArrayList<String> result3 = QueryHelp
				.getElementsByClass("123<input class=\"haha test\">zhende<br/><br/><br></input>321", "test");
		System.out.println(result3);
		
		String result4 = QueryHelp.querySelector("123<input id=\"test\">zhende<br/><br/><br></input>321", "input#test br");
		System.out.println(result4);
		
		ArrayList<String> result5 = QueryHelp.querySelectorAll("123<input id=\"test\">zhende<br/><br/><br></input>321", "input#test br");
		System.out.println(result5);
	}
}