package com.c3.lazypage.filter;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

public class LazyPageFilter implements Filter {
	//protected static final Logger LOG = Logger.getLogger(LazyPageFilter.class);
	
	@Override
	public void init(FilterConfig filterConfig) throws ServletException {

	}

	@Override
	public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain)
			throws IOException, ServletException {
		
		CharResponseWrapper wrapper = new CharResponseWrapper(
		         (HttpServletResponse)resp);
			
		   //chain.doFilter(req, wrapper);

		   /*CharArrayWriter writer = new CharArrayWriter();
		   String originalContent = wrapper.getResponseContent();
		   writer.write(originalContent);
		   writer.write("<h1>Added Title</h1>");
		   System.out.println("."+originalContent+"."+writer.toString().length());
		   resp.setContentLength(writer.toString().length());
		   PrintWriter out = resp.getWriter();
		   out.write(writer.toString()); 
		   out.close();*/
		   
		   /*String originalContent = wrapper.getResponseContent();
		   String outString = originalContent;
		   outString += "<h1>Added Title</h1>";
		   byte[] outByte = outString.getBytes("UTF-8");
		   resp.setCharacterEncoding("UTF-8");
		   resp.setContentType("text/html");
		   resp.setContentLength(outByte.length);
		   System.out.println("."+outString+"."+outByte.length);
		   ServletOutputStream out = resp.getOutputStream();
	       out.write(outByte);
	       out.flush();
		   return;*/
		   
		   
		   HtmlResponseWrapper capturingResponseWrapper = new HtmlResponseWrapper((HttpServletResponse) resp);

		   chain.doFilter(req, capturingResponseWrapper);
		   //System.out.println(resp.getContentType());
	        if (resp.getContentType() != null && resp.getContentType().contains("text/html")) {
	        	resp.setCharacterEncoding("UTF-8");
	        	((HttpServletResponse)resp).setDateHeader("Expires",-1);
	        	((HttpServletResponse)resp).setHeader("Cache-Control","no-cache");
	        	((HttpServletResponse)resp).setHeader("Pragma","no-cache");
	        	((HttpServletResponse)resp).setHeader("Last-Modified", "Thu, 30 Apr 2020 23:57:17 GMT");
	            String content = capturingResponseWrapper.getCaptureAsString();

	            // replace stuff here
	            String replacedContent = content.replaceAll(
	                    "<title>(.*?)</title>",
	                    "<title>$1 - HTML replaced</title>");

	            System.out.println(replacedContent);

	            resp.getWriter().write(replacedContent);

	        }
	}
	
	@Override
	public void destroy() {

	}
	
	public static String getQueryString(String query, String name){
		if(query==null)return "";
		String reg = "(^|&)"+ name +"=([^&]*)(&|$)";
		Pattern pattern = Pattern.compile(reg, Pattern.CASE_INSENSITIVE);
		Matcher m = pattern.matcher(query);
		if(m.find()){
			return m.group(2);
		}
		return "";
	}
}
