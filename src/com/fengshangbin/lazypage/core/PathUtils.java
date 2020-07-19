package com.fengshangbin.lazypage.core;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PathUtils {
	public static void main(String[] args) {
		String url = "https://blog.csdn.net/u012109105/article/details/47003949?a=1&b=2#home";
		System.out.println(getDomain(url));
		System.out.println(getPath(url));
		String[] paths = getPaths(url);
		for (int i = 0; i < paths.length; i++) {
			System.out.println(paths[i]);
		}
		System.out.println("---------------------------");
		String[] paths2 = getPathsWithIgnorePath(url, "u012109105/article");
		for (int i = 0; i < paths2.length; i++) {
			System.out.println(paths2[i]);
		}
		System.out.println(getFinalURL(url, "http://www.baidu.com/a"));
		System.out.println(getFinalURL(url, "/a"));
		System.out.println(getFinalURL(url, "a"));
		System.out.println(getFinalURL(url, "../../a"));
	}

	public static String getDomain(String url) {
		String regex = "^((https|http|ftp|rtsp|mms|file)?://[^/]*)";
		Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
		Matcher m = pattern.matcher(url);
		if (m.find()) {
			return m.group(1);
		}
		return null;
	}

	public static String getPath(String url) {
		String domain = getDomain(url);
		String path = url.replaceFirst("(?i)" + domain, "").replaceFirst("(\\?.*)|(#.*)", "").replaceAll("//", "/");
		return path;
	}

	public static String[] getPaths(String url) {
		String[] paths = getPath(url).replaceFirst("^/", "").split("/");
		return paths;
	}

	public static String[] getPathsWithIgnorePath(String url, String ignorePath) {
		String[] paths = getPath(url).replaceFirst("^/", "").replaceFirst("(?i)" + "/?" + ignorePath + "/?", "")
				.split("/");
		return paths;
	}

	public static String getFinalURL(String currentURL, String targetURL) {
		if (checkUrl(targetURL))
			return targetURL;
		else {
			String domain = getDomain(currentURL);
			String[] paths = getPaths(currentURL);
			paths = Arrays.copyOf(paths, paths.length - 1);
			if (targetURL.startsWith("/")) {
				return domain + targetURL;
			} else if (targetURL.startsWith("../")) {
				while (targetURL.startsWith("../")) {
					targetURL = targetURL.substring(3);
					paths = Arrays.copyOf(paths, paths.length - 1);
				}
				return (domain + "/" + String.join("/", paths) + (paths.length > 0 ? "/" : "") + targetURL);
			} else {
				return (domain + "/" + String.join("/", paths) + (paths.length > 0 ? "/" : "") + targetURL);
			}
		}
	}

	private static boolean checkUrl(String url) {
		String regex = "^((https|http|ftp|rtsp|mms|file)?://)(.*?)";
		Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
		Matcher isUrl = pattern.matcher(url);
		return isUrl.find();
	}
}
