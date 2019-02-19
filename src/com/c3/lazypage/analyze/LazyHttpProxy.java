package com.c3.lazypage.analyze;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LazyHttpProxy {
	public static void main(String[] args) {
		String path = "Https://abc.com/haha/ni/o.html";
		String rootPath = getRootPath(path);
		String[] paths = path.replaceAll("("+rootPath+"/?)|([/?]$)", "").split("/");
		if(paths.length>0 && paths[paths.length-1].indexOf(".")>-1){
			paths = Arrays.copyOf(paths, paths.length-1);
		}
		System.out.println(getRealUrl(rootPath, paths, "a1.data"));
	}
	private static String getRealUrl(String rootPath, String[] paths, String url){
		if(checkUrl(url))return url;
		else{
			if(url.startsWith("/")){
				return rootPath+url;
			}else if(url.startsWith("../")){
				int count = 0;
				while(url.startsWith("../")){
					url = url.substring(3);
					count++;
				}
				StringBuffer pathBuffer = new StringBuffer("/");
				for(int i=0; i<paths.length-count; i++){
					pathBuffer.append(paths[i]);
				}
				if(pathBuffer.length()>1)pathBuffer.append("/");
				return rootPath+pathBuffer.toString()+url;
			}else{
				url = url.replaceAll("\\./", "");
				String pathStr = String.join("/", paths);
				if(pathStr.length()>0)pathStr+="/";
				return rootPath+"/"+pathStr+url;
			}
		}
	}
	private static boolean checkUrl(String url){
		String regex = "^((https|http|ftp|rtsp|mms)?://)(.*?)";
		Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
		Matcher isUrl = pattern.matcher(url);
		return isUrl.find();
		//return isUrl.matches();
	}
	private static String getRootPath(String path){
		String regex = "^((https|http|ftp|rtsp|mms)?://[^/]*)";
		Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
		Matcher m = pattern.matcher(path);
		if(m.find()){
			//System.out.println(m.group(0)+"-"+m.group(1)+"-"+m.group(2));
			return m.group(1);
		}
		return null;
	}
	public static String ajax(String rootPath, String[] paths, String method, String urlString, String parameters) {
		if(method==null)method="GET";
		method = method.toUpperCase();
		urlString = getRealUrl(rootPath, paths, urlString);
		//System.out.println(urlString+"-"+parameters);
		HttpURLConnection urlConnection = null;
		OutputStream out = null;
		BufferedReader reader = null;
		StringBuffer temp = null;
		if (method.equalsIgnoreCase("GET") && parameters != null) {
			urlString +=urlString.indexOf("?")>0?"&":"?";
			urlString += parameters;
		}
		try{
			URL url = new URL(urlString);
			urlConnection = (HttpURLConnection) url.openConnection();
			urlConnection.setRequestMethod(method);
			urlConnection.setDoOutput(true);
			urlConnection.setDoInput(true);
			urlConnection.setUseCaches(false);
			urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			urlConnection.setRequestProperty("Charset", "UTF-8");
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
				reader = new BufferedReader(new InputStreamReader(in, "utf-8"));
				temp = new StringBuffer();
				String line = reader.readLine();
				while (line != null) {
					temp.append(line).append("\r\n");
					line = reader.readLine();
				}
				reader.close();
				reader = null;
			}
		}catch (Exception e) {
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
		}
		
		if (temp != null) {
			return temp.toString();
		} else {
			return null;
		}
	}
}
