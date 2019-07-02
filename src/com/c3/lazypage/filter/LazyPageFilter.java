package com.c3.lazypage.filter;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.Set;
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

public class LazyPageFilter implements Filter {
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
		//System.out.println(serverPath);
		if(! LazyPage.htmlPaths.contains(serverPath)){
			Set<String> keys = LazyPage.map.keySet();
			Iterator<String> it = keys.iterator();  
			while(it.hasNext()) {  
			  String route = it.next();  
			  Pattern pattern = Pattern.compile(route, Pattern.CASE_INSENSITIVE);
				Matcher isUrl = pattern.matcher(serverPath);
				if(isUrl.matches()){
					int count = isUrl.groupCount();
					String[] group = null;
					if(count>0){
						group = new String[count];
						for(int i=0; i<count; i++){
							group[i]=isUrl.group(i+1);
						}
					}
					if(group != null){
						request.setAttribute("lazypage_group", group);
						request.setAttribute("lazypage_route", route);
					}
					String realPath = LazyPage.map.get(route).replaceAll("\\+", "%2B");
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
		HttpServletResponse response = (HttpServletResponse) resp;
		ResponseWrapper respWrapper = new ResponseWrapper(response);
		chain.doFilter(req, respWrapper);
		byte[] content = respWrapper.getContent();
		if (content.length > 0) {
			String outString = new String(content, "UTF-8");
			boolean lazyPageSpider = false;
			Cookie[] cookies = request.getCookies();
			if(cookies!=null){
		    	for (Cookie cookie : cookies) {
		    		if(cookie.getName().equals("LazyPageSpider")){
		    			lazyPageSpider = true;
			 			break;
		    		}
		    	}
	    	}

			Object lazypageGroup = request.getAttribute("lazypage_group");
			String[] pathParams = null;
			if(lazypageGroup!=null){
				pathParams = (String[])lazypageGroup;
				//String lazypageRoute = (String)request.getAttribute("lazypage_route");
				if(pathParams!=null){
					String pathStr = "[\""+String.join("\",\"", pathParams)+"\"]";
					int bodyEnd = outString.lastIndexOf("</body>");
					if(bodyEnd>0){
						outString = outString.substring(0, bodyEnd)+"<script>LazyPage.pathParams="+pathStr+";</script>\n"+outString.substring(bodyEnd);
					}else{
						outString += "\n<script>LazyPage.pathParams="+pathStr+"</script>";
					}
					//LazyPage.pathReg='"+lazypageRoute+"';
				}
			}
			
			if(lazyPageSpider == false){
				Object lazypageQuery = request.getAttribute("lazypage_query");
				String query = lazypageQuery!=null?lazypageQuery.toString():request.getQueryString();
				Object lazypageUrl = request.getAttribute("lazypage_url");
		    	String url = lazypageUrl!=null?lazypageUrl.toString():request.getRequestURL().toString();
				outString = new AnalyzeHtml().parse(url, query, outString, pathParams, cookies);
			}
			
			byte[] outByte = outString.getBytes("UTF-8");
			response.setCharacterEncoding("UTF-8");
	    	response.setContentType("text/html");
			response.setContentLength(outByte.length);
			ServletOutputStream out = response.getOutputStream();
            out.write(outByte);
            out.flush();
		}
	}

	@Override
	public void destroy() {

	}
}
