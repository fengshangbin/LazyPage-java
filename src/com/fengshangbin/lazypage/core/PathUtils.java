package com.fengshangbin.lazypage.core;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PathUtils {
	public static void main(String[] args) {
		String url ="https://blog.csdn.net/u012109105/article/details/47003949?a=1&b=2#home";
		System.out.println(getDomain(url));
		System.out.println(getPath(url));
	}
	public static String getDomain(String url){
		String regex = "^((https|http|ftp|rtsp|mms|file)?://[^/]*)";
		Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
		Matcher m = pattern.matcher(url);
		if(m.find()){
			return m.group(1);
		}
		return null;
	}
	public static String getPath(String url){
		String domain = getDomain(url);
		String path = url.replaceFirst("(?i)"+domain, "").replace("#[^#]*?", "").replaceAll("\\/\\/", "/");
		return path;
	    //return url.replace(new RegExp(domain, 'i'), '').replace(/(\?.*)|(#.*)/, '').replace(/\/\//g, '/');
	}
}
