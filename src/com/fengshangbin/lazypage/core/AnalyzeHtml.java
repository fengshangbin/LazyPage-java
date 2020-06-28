package com.fengshangbin.lazypage.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.Cookie;

import com.fengshangbin.domparse.Element;
import com.fengshangbin.domparse.FastDom;
import com.fengshangbin.lazypage.LazyPage;

public class AnalyzeHtml {
	private JsonHashMap<String, String> dataMap = new JsonHashMap<String, String>();
	private FastDom doc;
	private boolean continueCheck = false;
	private String rootPath;
	private String[] paths;
	private String query;
	private String[] pathnames;
	private Cookie[] cookies;

	private int blockMarkIndex = 0;

	public AnalyzeHtml() {
		// doc = new Document(html);
	}

	public static void main(String[] args) {
		/*
		 * String html = LazyPageServlet.
		 * readToString("D:\\js project tools\\lazypage\\examples\\index-.html"
		 * ); AnalyzeHtml analyzeHtml = new AnalyzeHtml(); String outHtml =
		 * analyzeHtml.parse("http://localhost:8080/J2EEWebTest/index.html",
		 * "id=20", html, null, null); System.out.println(outHtml);
		 */
		/*String name = "q";
		String reg = "(^|&)" + name + "=([^&]*)(&|$)";
		Pattern pattern = Pattern.compile(reg, Pattern.CASE_INSENSITIVE);
		Matcher m = pattern.matcher("q=city");
		if (m.find()) {
			System.out.println(m.group(2));
		}*/
		String[] a = "1/2/3".split("/");
		System.out.println(a.length);
	}

	public FastDom parse(String path, String query, String html, Cookie[] cookies) {
		doc = new FastDom(html);
		this.rootPath = getRootPath(path);
		path = path.replaceAll("(" + rootPath + "/?)", "");
		// if(LazyPage.host!=null)this.rootPath=LazyPage.host;
		if (path.endsWith("/"))
			path += "end";
		this.pathnames = path.replaceFirst("(?i)"+"/?"+LazyPage.ignorePath+"/?", "").split("/");
		if(pathnames[pathnames.length-1].equals("end")){
			pathnames[pathnames.length-1]="";
		}
		this.paths = path.split("/");
		if (paths.length > 0) {
			paths = Arrays.copyOf(paths, paths.length - 1);
		}
		this.query = query;
		this.cookies = cookies;

		try {
			parseBlock();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return new FastDom(StringUtils.encodeHtml(e.getMessage()).replaceAll("\r|\n", "<br>"), true);//
		}
		//String result = doc.getHTML();
		return doc;
		//else return new FastDom("server error");
	}

	private static String getRootPath(String path) {
		String regex = "^((https|http|ftp|rtsp|mms)?://[^/]*)";
		Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
		Matcher m = pattern.matcher(path);
		if (m.find()) {
			return m.group(1);
		}
		return null;
	}

	private void parseBlock() throws Exception {
		Element block = doc.querySelector("block:not([wait])");
		if (block != null) {
			String attrHTML = block.getAttrHTML();
			String regStr = ":([\\w-_]*?) *= *\"(.*?)\"";
			Pattern pattern = Pattern.compile(regStr, Pattern.MULTILINE);
			Matcher m = pattern.matcher(attrHTML);
			StringBuffer sb = new StringBuffer();
			while (m.find()) {
				String p1 = m.group(1);
				String p2 = m.group(2);
				String result = p1 + "=\"{{@" + p2 + "}}\"";
				m.appendReplacement(sb, Matcher.quoteReplacement(result));
			}
			m.appendTail(sb);
			attrHTML = sb.toString();

			attrHTML = LazyScriptEngine.run(attrHTML, "{}", pathnames, query, dataMap);
			//if (attrHTML == null) return false;
			block.setAttrHTML(attrHTML);

			String source = block.getAttribute("source");
			String matchJson = "(^\\{(.*?)\\}$)|(^\\[(.*?)\\]$)";
			pattern = Pattern.compile(matchJson);
			if (source != null && !source.equals("")) {
				m = pattern.matcher(source);
				if (m.find()) {
					source = source.replaceAll("\\\\'", "&#39;").replaceAll("'", "\"").replaceAll("&#39;", "'");
				} else {
					String ajaxType = block.getAttribute("rquest-type");
					String ajaxData = block.getAttribute("rquest-param");
					source = LazyHttpProxy.ajax(rootPath, paths, ajaxType, source, ajaxData, cookies);
					//if (source == null) return false;
				}
			} else {
				source = "{}";
			}

			String src = block.getAttribute("src");
			if (src != null && !src.equals("")) {
				String result = LazyHttpProxy.ajax(rootPath, paths, null, src, null, cookies);
				//if (result == null) return false;
				block.setInnerHTML(result);
			}
			renderDom(block, source);
		}
	}

	private void renderDom(Element block, String source) throws Exception {
		//System.out.println("."+block.getOuterHTML()+"."+block.getInnerHTML()+".");
		HashMap<String, String> blockChildren = new HashMap<String, String>();
		Element blockChild = block.querySelector("block:not([mark])");
		while (blockChild != null) {
			String innerHTML = blockChild.getInnerHTML();
			blockMarkIndex++;
			String key = "<!-- lzb" + blockMarkIndex + " -->";
			blockChildren.put(key, innerHTML);
			blockChild.setInnerHTML(key);
			blockChild.setAttribute("mark", "true");
			blockChild = block.querySelector("block:not([mark])");
		}
		String html = block.getInnerHTML();
		//System.out.println(html+"-----1");
		String regStr1 = "<[^>]* +:[\\w-_]*? *= *\".*?\" *[^>]*>";
		Pattern pattern1 = Pattern.compile(regStr1, Pattern.MULTILINE);
		Matcher m1 = pattern1.matcher(html);
		StringBuffer sb1 = new StringBuffer();
		while (m1.find()) {
			String attrHTML = m1.group(0);
			String regStr = ":([\\w-_]*?) *= *\"(.*?)\"";
			Pattern pattern = Pattern.compile(regStr, Pattern.MULTILINE);
			Matcher m = pattern.matcher(attrHTML);
			StringBuffer sb = new StringBuffer();
			while (m.find()) {
				String p1 = m.group(1);
				String p2 = m.group(2);
				String result = p1 + "=\"{{@" + p2 + "}}\"";
				m.appendReplacement(sb, Matcher.quoteReplacement(result));
			}
			m.appendTail(sb);
			attrHTML = Matcher.quoteReplacement(sb.toString());
			m1.appendReplacement(sb1, attrHTML);
		}
		m1.appendTail(sb1);
		html = sb1.toString().trim();
		//System.out.println(html+"-----2");
		String id = block.getAttribute("id");
		if (id != null) {
			dataMap.put(id, source);
			ArrayList<Element> waitBlocks = doc.querySelectorAll("block[wait=" + id + "]");
			waitBlocks.forEach(item -> {
				String waitAttr = item.getAttribute("wait");
				String regex = id + " ?";
				waitAttr = waitAttr.replaceAll(regex, "");
				waitAttr = waitAttr.trim();
				if (waitAttr.equals(""))
					waitAttr = null;
				item.setAttribute("wait", waitAttr);
			});
		}
		/*for(int i=0; i<pathnames.length; i++){
			System.out.println("pathnames: "+i+"=>"+pathnames[i]);
		}*/
		//System.out.println(html+"-----"+source);
		String result = LazyScriptEngine.run(html, source, pathnames, query, dataMap);
		for (Entry<String, String> entry : blockChildren.entrySet()) {
			result = result.replace(entry.getKey(), entry.getValue());
		}
		// console.log(result);
		block.setOuterHTML(result);
		parseBlock();
	}
}
