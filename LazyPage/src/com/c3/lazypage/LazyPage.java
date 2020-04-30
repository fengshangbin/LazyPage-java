package com.c3.lazypage;
import java.io.File;
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

import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;
import javax.servlet.ServletContext;

//import org.apache.log4j.Logger;

import com.c3.lazypage.filter.LazyPageFilter;

public class LazyPage {
	//protected static final Logger LOG = Logger.getLogger(LazyPage.class);
	
	/*public static String encoding = "UTF-8";
	public static String host = null;
	public static HashSet<String> jsPaths = new HashSet<String>();*/
	//public static Map<String, String> map = new HashMap<String, String>();
	/*public static TreeMap<String, String> map = new TreeMap<String, String>(new Comparator<String>() {
		@Override
		public int compare(String o1, String o2) {
			if(o1.length()>o2.length())return 1;
			else if(o1.length()<o2.length())return -1;
			else return o1.compareTo(o2);
		}
	});*/
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
	
	/*public static void host(String realHost){
		LazyPage.host = realHost;
	}*/

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
	
	private void loadconfig(String rootPath) {
		File file = new File(rootPath, "config.json");
		if(file.exists()){
			
		}
	}
		
		  /*try {
		    var data = fs.readFileSync(root + '/config.json', 'utf-8');
		    try {
		      let config = JSON.parse(data);
		      if (config.mapping && config.mapping.length > 0) {
		        mapping = config.mapping;
		      }
		      if (config.import) {
		        template.defaults.imports.$import = {};
		        for (var i = 0; i < config.import.length; i++) {
		          let path = config.import[i];
		          try {
		            var code = fs.readFileSync(root + '/' + path, 'utf-8');
		            code = '(function(root) {' + code + '})(template.defaults.imports.$import);';
		            eval(code);
		          } catch (e) {
		            console.log('error js: ' + path, e);
		          }
		           let path = config.import[key];
		          try {
		            var code = fs.readFileSync(root + '/' + path, 'utf-8');
		            loadjs(eval(code), key);
		          } catch (e) {
		            console.log('error js: ' + path, e);
		          } 
		        }
		      }
		      if (config.config) {
		        var set = {};
		        Object.assign(set, config.config);
		        template.defaults.imports.$config = set;
		      }
		    } catch (e) {
		      console.error('config.json need json format!');
		    }
		  } catch (e) {}
		}*/
}
