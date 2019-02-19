package com.c3.lazypage.servlet;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.c3.lazypage.LazyPage;
import com.c3.lazypage.analyze.AnalyzeHtml;

public class LazyPageServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    	Cookie[] cookies = request.getCookies();
    	String query = request.getQueryString();
    	String url = request.getRequestURL().toString();
    	String serverPath = request.getServletPath();
    	String filePath = this.getServletContext().getRealPath(serverPath);
    	String html = readToString(filePath);
    	boolean isAjax = false;
    	if(cookies!=null){
	    	for (Cookie cookie : cookies) {
	    		if(cookie.getName().equals("LazyPageAjax"))isAjax=true;
	    	}
    	}
    	response.setCharacterEncoding("utf-8");
    	response.setContentType("text/html");
        PrintWriter writer = response.getWriter();
        if(!isAjax){
        	String result = new AnalyzeHtml().parse(url, query, html);
            writer.print(result);
        }else{
        	writer.print(html);
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }
    
    public static String readToString(String fileName) {
        File file = new File(fileName);  
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
            return new String(filecontent, LazyPage.encoding);  
        } catch (UnsupportedEncodingException e) {
        	String errorMessage = "The OS does not support " + LazyPage.encoding;
            System.err.println(errorMessage);
            e.printStackTrace();  
            return errorMessage;  
        }  
    }
}
