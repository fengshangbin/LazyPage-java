package com.fengshangbin.lazypage.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

public class LazyScriptEngine {
	//private static Invocable invocable = null;
	private static StringBuffer sb = new StringBuffer();

	//private static Bindings bindings = new SimpleBindings();
	static{
		InputStream is=LazyScriptEngine.class.getResourceAsStream("/template-web.js");
		sb.append("var process = {'env': {'NODE_ENV': 'production'}};");
		loadJSCode(is);
		sb.append("template.defaults.imports.Date = Date;");
		sb.append("template.defaults.imports.Math = Math;");
		sb.append("template.defaults.imports.JSON = JSON;");
		sb.append("template.defaults.imports.decodeURI = decodeURI;");
		sb.append("template.defaults.imports.decodeURIComponent = decodeURIComponent;");
		sb.append("template.defaults.imports.encodeURI = encodeURI;");
		sb.append("template.defaults.imports.encodeURIComponent = encodeURIComponent;");
		sb.append("template.defaults.imports.escape = escape;");
		sb.append("template.defaults.imports.eval = eval;");
		sb.append("template.defaults.imports.isNaN = isNaN;");
		sb.append("template.defaults.imports.Number = Number;");
		sb.append("template.defaults.imports.parseFloat = parseFloat;");
		sb.append("template.defaults.imports.parseInt = parseInt;");
		sb.append("template.defaults.imports.String = String;");
		sb.append("template.defaults.imports.unescape = unescape;");
		
		sb.append("template.defaults.imports.$import = {};");
		sb.append("var lazyPagePathNames, lazyPageQuery;");
		sb.append("template.defaults.imports.$path = function(index) {return index < lazyPagePathNames.length ? lazyPagePathNames[index] : null;};");
		sb.append("template.defaults.imports.$query = function(key) {"
				+ "if (lazyPageQuery == null) return null;"
				+ "var regStr = '(^|&)' + key + '=([^&]*)(&|$)';"
				+ "var reg = new RegExp(regStr, 'i');"
				+ "var r = lazyPageQuery.match(reg);"
				+ "if (r != null) return r[2];"
				+ "return null;};");
		sb.append("function run(html, source, path, query, blocks) {"
				+ "if(typeof source == 'string') source = JSON.parse(source);"
				+ "if(typeof blocks == 'string') blocks = JSON.parse(blocks);"
				+ "lazyPagePathNames=path; lazyPageQuery=query;"
				+ "template.defaults.imports.$block = blocks;"
				+ "return template.render(html, source);};");
	}
	
	/*public static void compile(){
		try {
			ScriptEngineManager manager = new ScriptEngineManager();
			ScriptEngine engine = manager.getEngineByName("nashorn");
			CompiledScript compiled = ((Compilable)engine).compile(sb.toString());
			compiled.eval();
			invocable = (Invocable) compiled.getEngine();
		} catch (ScriptException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}*/
	
	private static ThreadLocal<Invocable> invocableHolder = new ThreadLocal<Invocable>(); //多线程隔离运行js
	
	private static Invocable getInvocable(){
		if (invocableHolder.get() == null) {
			ScriptEngineManager manager = new ScriptEngineManager();
			ScriptEngine engine = manager.getEngineByName("nashorn");
			Invocable invocable = null;
			try {
				CompiledScript compiled = ((Compilable)engine).compile(sb.toString());
				compiled.eval();
				invocable = (Invocable) compiled.getEngine();
	            invocableHolder.set(invocable);
			} catch (ScriptException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
            return invocable;
        }else{
            return invocableHolder.get();
        }
    }
	
	public static String run(String html, String source, String[] path, String query, JsonHashMap blocks) throws NoSuchMethodException, ScriptException{
		String result = null;
        //try {
        	result = (String)getInvocable().invokeFunction("run", html, source, path, query, blocks.toString());
        /*} catch (Exception e) {
			e.printStackTrace();
		}*/
        return result;
	}
	
	public static void loadConfig(String config){
		sb.append("template.defaults.imports.$config = "+config+";");
	}
	
	public static void loadJSCode(String fileName){
		sb.append("(function(root) {");
		try {
			loadJSCode(new FileInputStream(fileName));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		sb.append("})(template.defaults.imports.$import);");
	}
	/*private static void loadJSCode(File file){
		sb.append("(function(root) {");
		try {
			loadJSCode(new FileInputStream(file));
		}catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		sb.append("})(template.defaults.imports.$import);");
	}*/
	
	private static void loadJSCode(InputStream is){
		try {
			BufferedReader br=new BufferedReader(new InputStreamReader(is, "UTF-8"));
			String line = br.readLine();
			while (line != null) {
				sb.append(line).append("\r\n");
				line = br.readLine();
			}
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
