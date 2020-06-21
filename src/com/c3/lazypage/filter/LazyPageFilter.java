package com.c3.lazypage.filter;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.Date;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.c3.lazypage.LazyPage;
import com.c3.lazypage.analyze.AnalyzeHtml;
import com.c3.lazypage.analyze.JsonHashMap;
import com.c3.lazypage.entity.Element;
import com.c3.lazypage.entity.FastDom;
import com.c3.lazypage.query.QueryLazyPage;

public class LazyPageFilter implements Filter {
	// protected static final Logger LOG =
	// Logger.getLogger(LazyPageFilter.class);

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {

	}

	@Override
	public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain)
			throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) req;
		String serverPath = request.getServletPath().toLowerCase();
		String fileName = serverPath.substring(serverPath.lastIndexOf("/")+1);
		
		//request.getHeader(arg0);
		if(fileName.indexOf(".")>0){
			if(!fileName.endsWith(".html")){
				chain.doFilter(req, resp);
				return;
			}else if(fileName.startsWith("_")){
				chain.doFilter(req, resp);
				return;
			}
		}
		//System.out.println("lazypage_url:"+request.getRequestURL().toString());
		//LOG.info("serverPath:"+serverPath);
		if(serverPath.endsWith("/") && LazyPage.htmlPaths.contains(serverPath+"index.html")){
			String query = request.getQueryString();
	    	String url = request.getRequestURL().toString();
			request.setAttribute("lazypage_url", url);
			request.setAttribute("lazypage_query", query);
			request.getRequestDispatcher(serverPath+"index.html").forward(req, resp);
			return;
		}
		//System.out.println(serverPath +"|"+ fileName+"|"+LazyPage.htmlPaths.contains(serverPath));
		if(! LazyPage.htmlPaths.contains(serverPath)){
			for (Entry<String, String> e: LazyPage.map) {
				//System.out.println(e.getKey()+":"+e.getValue());
				String route = e.getKey();
				Pattern pattern = Pattern.compile(route, Pattern.CASE_INSENSITIVE);
				Matcher isUrl = pattern.matcher(serverPath);
				if(isUrl.matches() || (serverPath.endsWith("/") && pattern.matcher(serverPath+"index.html").matches())){
					/*int count = isUrl.groupCount();
					String[] group = null;
					if(count>0){
						group = new String[count];
						for(int i=0; i<count; i++){
							group[i]=isUrl.group(i+1);
						}
					}
					if(group != null){
						request.setAttribute("lazypage_group", group);
						//request.setAttribute("lazypage_route", route);
					}*/
					String realPath = e.getValue().replaceAll("\\+", "%2B");
					//String realPath = URLDecoder.decode(e.getValue(), "utf-8");
					String query = request.getQueryString();
			    	String url = request.getRequestURL().toString();
					request.setAttribute("lazypage_url", url);
					request.setAttribute("lazypage_query", query);
					request.getRequestDispatcher(realPath).forward(req, resp);
					return;
				}
			}
			chain.doFilter(req, resp);
			return;
		}

		HtmlResponseWrapper capturingResponseWrapper = new HtmlResponseWrapper((HttpServletResponse) resp);

		chain.doFilter(req, capturingResponseWrapper);
		if (resp.getContentType() != null && resp.getContentType().contains("text/html")) {
			resp.setCharacterEncoding("UTF-8");
			HttpServletResponse response = (HttpServletResponse) resp;
			response.setHeader("ETag", Math.round(Math.random() * 100000) + "-" + new Date().getTime());
			response.setDateHeader("Last-Modified", new Date().getTime());
			Cookie[] cookies = request.getCookies();
			String content = capturingResponseWrapper.getCaptureAsString();
			String replacedContent = "";
			Object lazypageQuery = request.getAttribute("lazypage_query");
			String query = lazypageQuery!=null?lazypageQuery.toString():request.getQueryString();
			if(query!=null)query=URLDecoder.decode(query, "utf-8");
			//System.out.println("query:"+query);
			Object lazypageUrl = request.getAttribute("lazypage_url");
	    	String url = lazypageUrl!=null?lazypageUrl.toString():request.getRequestURL().toString();
	    	//System.out.println(url +"|"+ query +"|"+ content);
			FastDom dom = new AnalyzeHtml().parse(url, query, content, cookies);
			if(!dom.isError()){
				String lazypageTargetSelector = getQueryString(query, "lazypageTargetSelector");
				if(lazypageTargetSelector.length()>0){
					Element block = QueryLazyPage.queryLazyPageSelector(dom, lazypageTargetSelector);
					
					JsonHashMap<String, Object> dataMap = new JsonHashMap<String, Object>();
					dataMap.put("block", block!=null ? block.getOuterHTML().replaceAll(" lazypagelevel\\d", "").replaceAll("(\r|\n)( *(\r|\n))+", "\r") : null);
					dataMap.put("hasTargetLazyPage", block != null);
		            if (block!=null) {
		        	    dataMap.put("title", dom.querySelector("title").getInnerHTML());
		            }
		            replacedContent = dataMap.toString();
		          //System.out.print(outString);
				}else{
					replacedContent = dom.getHTML().replaceAll("(\r|\n)( *(\r|\n))+", "\r");
				}
			}else{
				if(LazyPage.debug) replacedContent = dom.getHTML();
				else replacedContent = "server error";
			}
			byte[] outByte = replacedContent.getBytes("UTF-8");
			response.setCharacterEncoding("UTF-8");
	    	response.setContentType("text/html");
			response.setContentLength(outByte.length);
			ServletOutputStream out = response.getOutputStream();
            out.write(outByte);
            out.flush();
			//resp.getWriter().write(replacedContent);
		}
	}

	@Override
	public void destroy() {

	}

	public static String getQueryString(String query, String name) {
		if (query == null)
			return "";
		String reg = "(^|&)" + name + "=([^&]*)(&|$)";
		Pattern pattern = Pattern.compile(reg, Pattern.CASE_INSENSITIVE);
		Matcher m = pattern.matcher(query);
		if (m.find()) {
			return m.group(2);
		}
		return "";
	}
}
