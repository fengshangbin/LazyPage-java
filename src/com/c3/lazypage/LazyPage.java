package com.c3.lazypage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;
import javax.servlet.ServletContext;

import com.c3.lazypage.analyze.LazyScriptEngine;

//import org.apache.log4j.Logger;

import com.c3.lazypage.filter.LazyPageFilter;

public class LazyPage {
	//protected static final Logger LOG = Logger.getLogger(LazyPage.class);
	
	private static Map<String,String> list = new HashMap<String,String>();
	public static List<Map.Entry<String, String>> map = new ArrayList<Map.Entry<String, String>>();
	
	public static HashSet<String> htmlPaths = new HashSet<String>();

	public static void main(String[] args) {
		String url = "index-haha-nihao-1";
		String regex = "index-([^/]*?)-nihao-([^/]*?)";
		Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
		Matcher isUrl = pattern.matcher(url);
		boolean matches = isUrl.matches();
		System.out.println(matches);
		if(matches){
			int count = isUrl.groupCount();
			System.out.println(count);
			for(int i=0; i<count+1; i++){
				System.out.println(isUrl.group(i));
			}
		}
	}

	public static void init(ServletContext context){
		init(context, "");
	}
	public static void init(String htmlPath){
		init(null, htmlPath, true);
	}
	public static void init(ServletContext context, String htmlPath){
		init(context, htmlPath, true);
	}
	public static void init(ServletContext context, boolean scanChildrenDirectory){
		init(context, "", scanChildrenDirectory);
	}

	/**
	 * LazyPage初始化方法，请在web初始化时调用如ServletContextListener实例的contextInitialized中
	 * @param  context  Servlet上下文Context 为null时htmlPath需要绝对路径并且需要手动添加过滤器LazyPageFilter
	 * @param  htmlPath  html文件所在目录，默认为web跟目录
	 * @param  scanChildrenDirectory  是否扫描子目录中的html文件，默认为false
	 */
	public static void init(ServletContext context, String htmlPath, boolean scanChildrenDirectory){
		File file = new File(context==null ? htmlPath : context.getRealPath(htmlPath));
		String rootPath = context==null ? htmlPath : context.getRealPath("");
		/*rootPath = rootPath.replaceFirst("^/([a-zA-Z]){1}:", "$1:")
				.replaceAll("%20"," ")
				.replaceAll("/", "\\\\");*/
		//System.out.println(rootPath);
		//LOG.info("rootPath:"+rootPath);
		
		
		loadconfig(rootPath);
		filterHtmlByDirectory(rootPath, file, scanChildrenDirectory);
		
		map = new ArrayList<Entry<String, String>>(list.entrySet());
		Collections.sort(map,new Comparator<Map.Entry<String,String>>() {
            public int compare(Entry<String, String> o1, Entry<String, String> o2) {
            	int o1Sort = getSort(o1.getValue());
            	int o2Sort = getSort(o2.getValue());
                if(o1Sort < o2Sort){
                	return 1;
                }else if(o1Sort > o2Sort){
                	return -1;
                }else{
                	return 0;
                }
            }
            public int getSort(String realPath){
            	int sort = 0;
            	int len=realPath.length();
                for (int i = 0; i < len; i++) {
                  char str = realPath.charAt(i);
                  sort += (str == '$' ? 1 : 2) * 10 * (len - i);
                }
                return sort;
            }
        });
		
		/*map.forEach((key, value) -> {
			//System.out.println(key+" - "+value);
			LOG.info(key+" - "+value);
		});*/
		/*for (Map.Entry<String, String> mapping : map) {
			LOG.info(mapping.getKey() + ":" + mapping.getValue());
        }
		htmlPaths.forEach(key -> {
			LOG.info(key);
		});*/
		if(context!=null){
			FilterRegistration.Dynamic dynamicFilter = context.addFilter("lazyPageFilter", LazyPageFilter.class);
			EnumSet<DispatcherType> dispatcherTypes = EnumSet.allOf(DispatcherType.class);
			dispatcherTypes.add(DispatcherType.REQUEST);
			dispatcherTypes.add(DispatcherType.FORWARD);
			dynamicFilter.addMappingForUrlPatterns(dispatcherTypes, false, "/*"); //isMatchAfter
		}
	}

	/**
	 * 注册全局脚本文件，请在web初始化时调用
	 * @param  jsPath  脚本文件路径
	 */
	/*public static void addJsFile(String jsPath){
		jsPaths.add(jsPath);
	}*/

	private static void filterHtmlByDirectory(String rootPath, File directory, boolean scanChildrenDirectory){
		if(directory.exists() && directory.isDirectory()){
			File[] files = directory.listFiles(file -> (scanChildrenDirectory && file.isDirectory())
					|| (file.getName().endsWith(".html") && !file.getName().startsWith("_")));
			for(int i=0; i<files.length; i++){
				if(files[i].isFile()){
					//System.out.println(files[i].getAbsolutePath());
					String realPath = "/"+files[i].getAbsolutePath().replace(rootPath, "").replaceAll("\\\\", "/").toLowerCase();
					String routePath = realPath.replace("-.html", "");
					routePath = routePath.replaceAll("\\+", "/");
					routePath = routePath.replaceAll("\\$", "([^/]*?)");
					htmlPaths.add(realPath);
					if(! routePath.equals(realPath)){
						list.put("^"+routePath+"$", realPath);
					}
				}else{
					filterHtmlByDirectory(rootPath, files[i], scanChildrenDirectory);
				}
			}
		}
	}
	
	public static HashMap<String, String> mapping = new HashMap<String, String>();
	public static String ignorePath = "";
	private static void loadconfig(String rootPath) {
		File file = new File(rootPath, "config.json");
		if(file.exists()){
			String config = null;
			Long filelength = file.length();  
	        byte[] filecontent = new byte[filelength.intValue()];  
	        try {  
	            FileInputStream in = new FileInputStream(file);  
	            in.read(filecontent);  
	            in.close();  
	        } catch (FileNotFoundException e) {  
	            e.printStackTrace();  
	        } catch (IOException e) {  
	            e.printStackTrace();  
	        }  
	        try {  
	        	config = new String(filecontent, "UTF-8");  
	        } catch (UnsupportedEncodingException e) {
	            e.printStackTrace();  
	            return;  
	        }  
			ScriptEngineManager manager = new ScriptEngineManager();
			ScriptEngine engine = manager.getEngineByName("nashorn");
			Invocable invokeEngine = null;
			StringBuffer sb = new StringBuffer();
			sb.append("var configJSON = {};");
			sb.append("function setConfigJSON(configString){ configJSON = JSON.parse(configString); }; ");
			sb.append("function getConfig(){ return JSON.stringify(configJSON.config); }; ");
			sb.append("function getImport(){ return configJSON.import ? configJSON.import.join() : [].join(); }; ");
			sb.append("function getMapping(){ "
					+ "var mapping = configJSON.mapping, result=[]; "
					+ "if(!mapping) return result.join(); "
					+ "for(var i=0; i<mapping.length; i++){ "
					+ "var map = mapping[i]; "
					+ "if(map.from && map.to){ "
					+ "result.push(map.from); result.push(map.to);}}; "
					+ "return result.join();}; "
					+ "function getIgnorePath(){ return configJSON.ignorePath ? configJSON.ignorePath : '' };");
	        try {
	        	engine.eval(sb.toString());
				invokeEngine = (Invocable)engine;
			} catch (ScriptException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
	        try {
	        	invokeEngine.invokeFunction("setConfigJSON", config);
	        } catch (Exception e) {
				e.printStackTrace();
				return;
			}
	        try {
	        	String result = (String)invokeEngine.invokeFunction("getConfig");
	        	LazyScriptEngine.loadConfig(result);
	        } catch (Exception e) {
				e.printStackTrace();
			}
	        try {
	        	String result = (String)invokeEngine.invokeFunction("getImport");
	        	if(result.length()>0){
		        	String[] imports = result.split(",");
		        	for(int i=0; i<imports.length; i++){
		        		String fileName = rootPath + "/" + imports[i];
		        		LazyScriptEngine.loadJSCode(fileName);
		        	}
	        	}
	        } catch (Exception e) {
				e.printStackTrace();
			}
	        try {
	        	String result = (String)invokeEngine.invokeFunction("getMapping");
	        	if(result.length()>0){
		        	String[] maps = result.split(",");
		        	for(int i=0; i<maps.length; i+=2){
		        		mapping.put(maps[i], maps[i+1]);
		        	}
	        	}
	        } catch (Exception e) {
				e.printStackTrace();
			}
	        try {
	        	ignorePath = (String)invokeEngine.invokeFunction("getIgnorePath");
	        	if(ignorePath.startsWith("/"))ignorePath=ignorePath.substring(1);
	        	if(ignorePath.endsWith("/"))ignorePath=ignorePath.substring(0, ignorePath.length()-1);
	        } catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
