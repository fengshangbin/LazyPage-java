package com.fengshangbin.lazypage.core;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.Cookie;

import com.fengshangbin.lazypage.LazyPage;

public class LazyHttpProxy {
	public static String ajax(String method, String urlString, String parameters, Cookie[] cookies) throws Exception {
		if(method==null)method="GET";
		method = method.toUpperCase();
		
		HashMap<String, String> mapping = LazyPage.mapping;
		for (Entry<String, String> entry : mapping.entrySet()) {
			urlString = urlString.replace(entry.getKey(), entry.getValue());
		}
		
		//System.out.println(urlString+"-"+parameters);
		HttpURLConnection urlConnection = null;
		OutputStream out = null;
		BufferedReader reader = null;
		StringBuffer temp = null;
		if(parameters!=null){
			parameters = MyURIEncoder.encode(parameters, "utf-8");
		}
		if (method.equalsIgnoreCase("GET") && parameters != null) {
			urlString +=urlString.indexOf("?")>0?"&":"?";
			urlString += parameters;
		}
		//try{
			URL url = new URL(urlString);
			urlConnection = (HttpURLConnection) url.openConnection();
			urlConnection.setRequestMethod(method);
			urlConnection.setDoOutput(true);
			urlConnection.setDoInput(true);
			urlConnection.setUseCaches(false);
			urlConnection.setRequestProperty("Accept-Charset", "");
			urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
			//urlConnection.setRequestProperty("Charset", "UTF-8");
			if(cookies!=null){
				StringBuffer cookiesBuffer = new StringBuffer();
				for (Cookie cookie : cookies) {
					cookiesBuffer.append(cookie.getName());
					cookiesBuffer.append("=");
					cookiesBuffer.append(cookie.getValue());
					cookiesBuffer.append("; ");
				}
				String cookie = cookiesBuffer.toString();
				//System.out.println(urlString+": "+cookie.substring(0, cookie.length()-2));
				urlConnection.setRequestProperty("Cookie", cookie.substring(0, cookie.length()-2));
			}
			urlConnection.setConnectTimeout(15000);
			urlConnection.setReadTimeout(15000);
			urlConnection.connect();
			if (method.equalsIgnoreCase("POST") && parameters != null) {
				out = urlConnection.getOutputStream();
				out.write(parameters.getBytes());
				out.flush();
				out.close();
				out = null;
			}
			int resultCode = urlConnection.getResponseCode();
			if (HttpURLConnection.HTTP_OK == resultCode) {
				InputStream in = urlConnection.getInputStream();
				reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
				temp = new StringBuffer();
				String line = reader.readLine();
				while (line != null) {
					temp.append(line).append("\r\n");
					line = reader.readLine();
				}
				reader.close();
				reader = null;
			}else{
				throw new Exception("error on ajax "+urlString+": statusCode "+resultCode);
			}
		/*}catch (Exception e) {
			e.printStackTrace();
		}finally {
			if(urlConnection != null) urlConnection.disconnect();
			try{
				if(out != null) out.close();
			}catch (IOException e){
                e.printStackTrace();
            }
            try{
                if(reader != null) reader.close();
            }catch (IOException e){
                e.printStackTrace();
            }
		}*/
			urlConnection.disconnect();
			if(out != null)out.close();
		return temp.toString();
		/*if (temp != null) {
			return temp.toString();
		} else {
			return null;
		}*/
	}
}
