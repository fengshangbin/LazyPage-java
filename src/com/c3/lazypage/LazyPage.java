package com.c3.lazypage;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;

import javax.servlet.ServletContext;
import javax.servlet.ServletRegistration;

import com.c3.lazypage.servlet.LazyPageServlet;

public class LazyPage {
	public static String encoding = "UTF-8";
	public static HashSet<String> jsPaths = new HashSet<String>();
	
	public static void init(ServletContext context){
		init(context, "");
	}
	public static void init(ServletContext context, String htmlPath){
		init(context, htmlPath, false);
	}
	public static void init(ServletContext context, boolean scanChildrenDirectory){
		init(context, "", scanChildrenDirectory);
	}
	
	/**
	 * LazyPage初始化方法，请在web初始化时调用如ServletContextListener实例的contextInitialized中
	 * @param  context  Servlet上下文Context
	 * @param  htmlPath  html文件所在目录，默认为web跟目录
	 * @param  scanChildrenDirectory  是否扫描子目录中的html文件，默认为false
	 */
	public static void init(ServletContext context, String htmlPath, boolean scanChildrenDirectory){
		ServletRegistration.Dynamic dynamicServlet=context.addServlet("lazyPageServlet", LazyPageServlet.class);
		File file = new File(context.getRealPath(htmlPath));
		ArrayList<String> htmlPaths = filterHtmlByDirectory(file, scanChildrenDirectory);
		String rootPath = context.getRealPath("");
		htmlPaths.forEach(path -> {
			dynamicServlet.addMapping("/"+path.replace(rootPath, "").replaceAll("\\\\", "/"));
		});
        dynamicServlet.setAsyncSupported(true);
        dynamicServlet.setLoadOnStartup(1);
	}
	
	/**
	 * 注册全局脚本文件，请在web初始化时调用
	 * @param  jsPath  脚本文件路径
	 */
	public static void addJsFile(String jsPath){
		jsPaths.add(jsPath);
	}
	
	private static ArrayList<String> filterHtmlByDirectory(File directory, boolean scanChildrenDirectory){
		ArrayList<String> htmlPaths = new ArrayList<String>();
		if(directory.exists() && directory.isDirectory()){
			File[] files = directory.listFiles(file -> (scanChildrenDirectory && file.isDirectory())
					|| (file.getName().endsWith(".html") && !file.getName().startsWith("_")));
			for(int i=0; i<files.length; i++){
				if(files[i].isFile()){
					htmlPaths.add(files[i].getAbsolutePath());
                }else{
                	htmlPaths.addAll(filterHtmlByDirectory(files[i], scanChildrenDirectory));
                }
			}
		}
		return htmlPaths;
	}
}
