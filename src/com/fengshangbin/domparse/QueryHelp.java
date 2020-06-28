package com.fengshangbin.domparse;

/*!
 *  QueryHelp.java 
 *  by fengshangbin 2019-06-28 
 *  正则匹配 HTML 嵌套元素
 */

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


class MoreRegs {
	int index;
	String regStr;
	boolean exclude;

	public MoreRegs(int index, String regStr) {
		this.index = index;
		this.regStr = regStr;
		this.exclude = false;
	}

	public MoreRegs(int index, String regStr, boolean exclude) {
		this.index = index;
		this.regStr = regStr;
		this.exclude = exclude;
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

class Regs {
	String regStr;
	String attrRegs;
	String classRegs;

	public Regs(String regStr, String attrRegs, String classRegs) {
		super();
		this.regStr = regStr;
		this.attrRegs = attrRegs;
		this.classRegs = classRegs;
	}
}

class CloseResult {
	int index;
	int len;
	
	public CloseResult(int index, int len) {
		super();
		this.index = index;
		this.len = len;
	}
}

public class QueryHelp {
	private static Object querySelectorElement(QueryInterface parent, String regStr, boolean multiElement) {
		regStr = encodEscapeWord(regStr.trim());
		String[] regArr = regStr.split(" ");
		Object source = new ArrayList<String>();
		((ArrayList<QueryInterface>) source).add(parent);
		int index = 0;
		while (index < regArr.length && source != null) {
			source = queryBlock((ArrayList<QueryInterface>) source, regArr[index], index == regArr.length - 1,
					multiElement);
			if (index < regArr.length - 1 || multiElement) {
				if (((ArrayList<String>) source).size() == 0)
					break;
			}
			index++;
		}
		if (!multiElement && source instanceof ArrayList) {
			return null;
		}
		return source;
	}

	private static Regs parseRegStr(String regStr) {
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
		return new Regs(regStr, attrRegs, classRegs);
	}

	private static Object queryBlock(ArrayList<QueryInterface> source, String regStr, boolean last,
			boolean multiElement) {
		Option option = new Option(!last || multiElement, new ArrayList<MoreRegs>());
		boolean needClassMark = false;

		String notReg = ":not\\((.*?)\\)";
		Matcher group = Pattern.compile(notReg).matcher(regStr);
		;
		while (group.find()) {
			Regs result = parseRegStr(group.group(1));
			if (result.attrRegs.length() > 0)
				option.moreRegs.add(new MoreRegs(2, buildAttrReg(result.attrRegs), true));
			if (result.classRegs.length() > 0) {
				option.moreRegs.add(new MoreRegs(3, buildClassReg(result.classRegs), true));
				needClassMark = true;
			}
		}
		regStr = regStr.replaceAll(notReg, "");

		Regs result = parseRegStr(regStr);
		if (result.attrRegs.length() > 0)
			option.moreRegs.add(new MoreRegs(2, buildAttrReg(result.attrRegs)));
		if (result.classRegs.length() > 0) {
			option.moreRegs.add(new MoreRegs(3, buildClassReg(result.classRegs)));
			needClassMark = true;
		}
		regStr = result.regStr;

		String tagReg = regStr.trim();
		if (tagReg.length() == 0)
			tagReg = "[^ >\n\r]*";
		String classRegsMark = needClassMark ? "[^>]*?\\bclass *= *\"([^\"]*)\"" : "";
		regStr = "< *(" + tagReg + ")(" + classRegsMark + "[^>]*)>";

		ArrayList<String> resultAll = new ArrayList<String>();
		for (int i = 0; i < source.size(); i++) {
			Object result2 = queryElement(regStr, source.get(i), option);
			if (!option.multiElement) {
				if (result2 != null || i == source.size() - 1)
					return result2;
			} else
				resultAll.addAll((ArrayList<String>) result2);
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

	private static Object getElementByAttr(QueryInterface html, String attrs, boolean multiElement) {
		ArrayList<MoreRegs> moreRegs = new ArrayList<MoreRegs>();
		moreRegs.add(new MoreRegs(2, buildAttrReg(attrs)));
		Option option = new Option(multiElement, moreRegs);
		String regStr = "< *([^ >\n\r]*)\\b([^>]*)>";
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
				attrReg += "(?=.*?\\b" + key + " *= *\"" + value + "\"" + "\\B)";
			}
		}
		return attrReg;
	}

	private static Object queryElement(String regStr, QueryInterface parent, Option option) {
		// System.out.println(option);
		int parentStart = 0;
		String html = null;
		FastDom dom = null;
		if (parent instanceof FastDom) {
			parentStart = 0;
			html = ((FastDom) parent).getHTML();
			dom = (FastDom) parent;
		} else if (parent instanceof Element) {
			parentStart = ((Element) parent).getStart() + ((Element) parent).getAttrLen();
			html = ((Element) parent).getInnerHTML();
			dom = ((Element) parent).getDom();
		} else {
			throw new Error("查询主体必须是FastDom或者Element");
		}

		Pattern match = Pattern.compile(regStr, Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
		ArrayList<Element> result = new ArrayList<Element>();
		Matcher group = match.matcher(html);
		while (group.find()) {
			if (option.moreRegs != null && option.moreRegs.size() > 0) {
				boolean moreState = true;
				for (int i = 0; i < option.moreRegs.size(); i++) {
					String moreContent = group.group(option.moreRegs.get(i).index);
					Pattern moreMatch = Pattern.compile(option.moreRegs.get(i).regStr,
							Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
					boolean matchResult = moreMatch.matcher(moreContent).find();
			        if (option.moreRegs.get(i).exclude) matchResult = !matchResult;
			        moreState = moreState && matchResult;
				}
				if (!moreState)
					continue;
			}
			Element el = dom.findElement(group.start());
		    if (el == null) {
				int searchStart = group.end();
				int closeIndex = 0;
				int closeLen = 0;
				if (Pattern.compile("\\/ *>").matcher(group.group(0)).find() == false) {
					CloseResult closeObj = queryCloseTag(group.group(1), html.substring(searchStart));
					closeIndex = closeObj.index;
			        closeLen = closeObj.len;
				}
				String targetHtml = html.substring(group.start(), searchStart + closeIndex);
				el = new Element(dom, group.start() + parentStart, searchStart + closeIndex + parentStart, targetHtml, group.group(0).length(), closeLen);
			      dom.addElement(el);
		    }
			if (!option.multiElement) {
				return el;
			} else {
				result.add(el);
			}
		}
		if (result.isEmpty() && !option.multiElement)
			return null;
		return result;
	}

	public static CloseResult queryCloseTag(String tag, String html) {
		String regStrAll = "< */? *" + tag + "[^>]*>";
		Pattern matchAll = Pattern.compile(regStrAll, Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);

		String regStrClose = "< */ *" + tag + " *>";
		Pattern matchClose = Pattern.compile(regStrClose, Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);

		int openCount = 1;
		int lastCloseIndex = 0;
		int closeLen = 0;
		Matcher groupAll = matchAll.matcher(html);
		while (openCount > 0) {
			boolean findAll = groupAll.find();
			if (findAll == false) {
				break;
			} else {
				if (matchClose.matcher(groupAll.group(0)).matches()) {
					openCount--;
					lastCloseIndex = groupAll.end();
					closeLen = groupAll.group(0).length();
				} else {
					openCount++;
					if (Pattern.compile("\\b" + tag + "\\b", Pattern.CASE_INSENSITIVE).matcher("input br image").find())
						return new CloseResult(0, 0);
				}
			}
		}
		return new CloseResult(lastCloseIndex, closeLen);
	}

	public static Element getElementById(QueryInterface html, String id) {
		return (Element) getElementByAttr(html, "[id=" + id + "]", false);
	};

	public static ArrayList<Element> getElementsByTag(QueryInterface html, String tag) {
		String regStr = "< *(" + tag + ")[^>]*>";
		return (ArrayList<Element>) queryElement(regStr, html, new Option(true, null));
	};

	public static ArrayList<Element> getElementsByClass(QueryInterface html, String classNames) {
		ArrayList<MoreRegs> moreRegs = new ArrayList<MoreRegs>();
		moreRegs.add(new MoreRegs(2, buildClassReg(classNames)));
		Option option = new Option(true, moreRegs);
		String regStr = "< *([^ >]*)[^>]*?\\bclass *= *\"([^\"]*)\"[^>]*>";
		return (ArrayList<Element>) queryElement(regStr, html, option);
	};

	public static Element querySelector(QueryInterface html, String regStr) {
		return (Element) querySelectorElement(html, regStr, false);
	};

	public static ArrayList<Element> querySelectorAll(QueryInterface html, String regStr) {
		return (ArrayList<Element>) querySelectorElement(html, regStr, true);
	};

	public static void main(String[] args) throws Exception {
		FastDom dom = new FastDom("123<input id=\"test\" class=\"test\">zhende<br/><br/><br></input>321"); 
		Element result = dom.getElementById("test");
		System.out.println(result);

		ArrayList<Element> result2 = dom.getElementsByTag("input");
		System.out.println(result2);

		ArrayList<Element> result3 = dom.getElementsByClass("test");
		System.out.println(result3);

		Element result4 = dom.querySelector("input#test br");
		System.out.println(result4);

		ArrayList<Element> result5 = dom.querySelectorAll("input#test br");
		System.out.println(result5);
	}
}